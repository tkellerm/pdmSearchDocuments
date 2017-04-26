package de.abasgmh.infosystem.pdmDocuments.webservices.keytech;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.abasgmh.infosystem.pdmDocuments.PdmDocumentsException;
import de.abasgmh.infosystem.pdmDocuments.data.PdmDocument;
import de.abasgmh.infosystem.pdmDocuments.utils.Util;
import de.abasgmh.infosystem.pdmDocuments.webservices.AbstractRestService;

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
		String targetPath = "rmtmp/pdmgetDocuments/" + TIMESTAMP + "/"; 
		String targetFileName=  fileInfo.getFilename().replaceAll(" ", "_"); 
		File targetPathFile = new File(targetPath);
		if (!targetPathFile.exists()) {
			targetPathFile.mkdirs();
		}
		
		try {
			List<File> fileList = downloadFileFromRestservice(url, targetFileName , targetPath);
			log.info("Ok - Download : "  + targetPath + targetFileName);
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

	

	
	
	
	
}
