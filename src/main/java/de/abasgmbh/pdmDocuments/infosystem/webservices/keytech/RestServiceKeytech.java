package de.abasgmbh.pdmDocuments.infosystem.webservices.keytech;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.abasgmbh.pdmDocuments.infosystem.PdmDocumentsException;
import de.abasgmbh.pdmDocuments.infosystem.config.Configuration;
import de.abasgmbh.pdmDocuments.infosystem.data.PdmDocument;
import de.abasgmbh.pdmDocuments.infosystem.utils.Util;
import de.abasgmbh.pdmDocuments.infosystem.webservices.AbstractRestService;

public class RestServiceKeytech extends AbstractRestService {

	private static String SEARCHPRODUCT_URL = "%1s/Search?classtypes=DEFAULT_MI&fields=as_mi__name=%2s";
	private static String LINKEDDOCUMENT_URL = "%1s/elements/%2s/links";
	private static String FILES_AT_DOKUMENT_URL = "%1s/elements/%2s/files";
	private static String GETFILE_URL = "%1s/elements/%2s/files/%3s";
	
	private static String DOCTYP_2D = "2DMISC_SLDDRW";
	private static String DOCTYP_3D = "3DMISC_SLDDRW";
	private static String  TIMESTAMP = Util.getTimestamp();
	
	

	public RestServiceKeytech(String server, String user, String password) {
		super();
		this.setServer(server);
		this.setUser(user);
		this.setPasword(password);
	}

	public RestServiceKeytech(Configuration config) {
		super();
		this.setServer(config.getRestServer());
		this.setUser(config.getRestUser());
		this.setPasword(config.getRestPassword());
	}

	@Override
	public String searchPdmProductID(String abasIdNo) throws PdmDocumentsException {
		
		String url = String.format(SEARCHPRODUCT_URL, this.server, abasIdNo);
		
		String jsonString = callRestservice(url);
		
		 ObjectMapper mapper = new ObjectMapper();
	        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        ResponsePDMProductId response;
	        try {
				response = mapper.readValue(jsonString, ResponsePDMProductId.class);
			} catch (JsonParseException e) {
				throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.jsonToObject", "ResponsePDMProductId"), e);
			} catch (JsonMappingException e) {
				throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.jsonToObject", "ResponsePDMProductId"), e);
			} catch (IOException e) {
				throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.jsonToObject", "ResponsePDMProductId"), e);
			}
	        
	        
		
		ArrayList<Elements> elementsList = response.getElementsList();
		if (elementsList.size() == 1 ) {
			return elementsList.get(0).getKey();
		}else if (elementsList.size() == 0) {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.noResult", abasIdNo));
		}else {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.moreThanOneResult", abasIdNo));
		}
		
	}
	
	private PDMLinks searchProductLinks(String pdmProductID) throws PdmDocumentsException{
		
		String url = String.format(LINKEDDOCUMENT_URL, this.server, pdmProductID);
		String jsonString = callRestservice(url);
		
		 ObjectMapper mapper = new ObjectMapper();
	        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        
		
		PDMLinks pdmLinks;
		try {
			pdmLinks = mapper.readValue(jsonString, PDMLinks.class);
		} catch (JsonParseException e) {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.jsonToObject", "PDMLink"), e);
		} catch (JsonMappingException e) {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.jsonToObject", "PDMLink"), e);
		} catch (IOException e) {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.jsonToObject", "PDMLink"), e);
		}
		
		return pdmLinks;
		
	}

	@Override
	public ArrayList<PdmDocument> getAllDocuments(String abasIdNo) throws PdmDocumentsException {
		String pdmProductID = searchPdmProductID(abasIdNo);
		PDMLinks pdmLinks = searchProductLinks(pdmProductID);
		ArrayList<PdmDocument> pdmDocumentList = new ArrayList<PdmDocument>();
				
		ArrayList<ChildLink> elementChildLinks = pdmLinks.getElementChildLinks();
		
		for (ChildLink childLink : elementChildLinks) {
			
			ArrayList<FileInfo> fileInfoList =  getFileInfoList(childLink);
			
			for (FileInfo fileInfo : fileInfoList) {
				
				 List<File> fileList = getPdmFile(fileInfo , childLink); 
				 
				 for (File file : fileList) {
					 PdmDocument pdmDocument = new PdmDocument(file, fileInfo.getDocumenttyp());
					 pdmDocument.addDocMetaData("FileID" ,fileInfo.getFileid() );
					 pdmDocument.addDocMetaData("FileName" ,fileInfo.getFilename() );
					 pdmDocument.addDocMetaData("StoreTyp" ,fileInfo.getStoretyp() );
					 pdmDocument.addDocMetaData("FileSize" ,fileInfo.getFileSize() );
					 Map<String, Object> properties = fileInfo.getProperties();
					 for (String key : properties.keySet()) {
						Object value = properties.get(key);
						
							pdmDocument.addDocMetaData(key ,value );
						
					}
	 
					 pdmDocumentList.add(pdmDocument);	
				}
					
			}
			
		}
		
			return pdmDocumentList;
		
	}

	private List<File> getPdmFile(FileInfo fileInfo, ChildLink childLink) throws PdmDocumentsException {
		
		
		String url =  String.format(GETFILE_URL, this.server,childLink.getChildLinkTo(), fileInfo.getFileid());
	
		String targetFileName=  Util.replaceUmlaute(fileInfo.getFilename().replaceAll(" ", "_"));
		
		
		try {
			List<File> fileList = downloadFileFromRestservice(url, targetFileName , getTargetPath());
			log.info("Ok - Download : "  + getTargetPath() + targetFileName);
			return fileList;
			
		} catch (FileNotFoundException e) {
			log.error(e);
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.getfilefromService"), e);
		} catch (IOException e) {
			log.error(e);
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.getfilefromService"), e);
		}
		
	}

	private ArrayList<FileInfo> getFileInfoList(ChildLink childLink) throws PdmDocumentsException {
		
		String url = String.format(FILES_AT_DOKUMENT_URL, this.server, childLink.getChildLinkTo());
		
		String jsonString = callRestservice(url);
		
		 ObjectMapper mapper = new ObjectMapper();
	        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        FileInfoList response;
	        try {
				response = mapper.readValue(jsonString, FileInfoList.class);
			} catch (JsonParseException e) {
				log.error(e);
				throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.jsonToObject", "FileInfoList"), e);
			} catch (JsonMappingException e) {
				log.error(e);
				throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.jsonToObject", "FileInfoList"), e);
			} catch (IOException e) {
				log.error(e);
				throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.jsonToObject", "FileInfoList"), e);
			}
		
        return response.getFileInfoList();
       
	}

	@Override
	public Boolean testConnection() {
			 InputStream is = null;
		      try {
		         URL url = new URL(this.server);
		         URLConnection con = url.openConnection();

		         is = con.getInputStream();
		         log.info("Server erreichbar");
		         return true;
		      } catch (NoRouteToHostException e) {
		    	  log.error(e);
		         return false;
		      }catch (FileNotFoundException e) {
		    	  log.info("Server erreichbar" , e);
//		    	  Server ist erreichbar, aber es ist keine richtige Seite verfuegbar
				return true;
			} catch (MalformedURLException e) {
				log.error(e);
				return false;
			} catch (IOException e) {
				log.error(e);
				return false;
			}
		      finally{
		         if(is!= null)
		            try {is.close();} catch (IOException e){ }
		      }
			
	}
	
}
