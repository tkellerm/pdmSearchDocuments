package de.abasgmh.infosystem.pdmDocuments.webservices.keytech;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import de.abasgmh.infosystem.pdmDocuments.PdmDocumentsException;
import de.abasgmh.infosystem.pdmDocuments.data.PdmDocument;
import de.abasgmh.infosystem.pdmDocuments.utils.Util;
import de.abasgmh.infosystem.pdmDocuments.webservices.AbstractRestService;

public class RestServiceKeytech extends AbstractRestService {

	private static String SEARCHPRODUCT_URL = "https://%1s/Search?classtypes=DEFAULT_MI&fields=as_mi__name=%2s";
	private static String LINKEDDOCUMENT_URL = "https://%1s/elements/%2s/links";
	private static String FILES_AT_DOKUMENT_URL = "https://%1s/elements/%2s/files";
	private static String GETFILE_URL = "https://%1s/elements/%2s/files/%3s";
	
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
		ResponsePDMProductId response = requestRestservicePDMProductID(url);
		ArrayList<Elements> elementsList = response.getElementsList();
		if (elementsList.size() == 1 ) {
			return elementsList.get(0).getKey();
		}else {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.moreThanOneResult", abasIdNo));
		}
		
	}
	
	private PDMLinks searchProductLinks(String pdmProductID){
		
		String url = String.format(LINKEDDOCUMENT_URL, this.server, pdmProductID);
		RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<String>(getHeaders());
        ResponseEntity<PDMLinks> response = restTemplate.exchange(url, HttpMethod.GET, request, PDMLinks.class);
		PDMLinks pdmLinks = response.getBody();
		
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
				
				File file = getPdmFile(fileInfo , childLink); 
				PdmDocument pdmDocument = new PdmDocument(file, fileInfo.getStoretyp());
				pdmDocument.addDocMetaData("FileID" ,fileInfo.getFileid() );
				pdmDocument.addDocMetaData("FileName" ,fileInfo.getFilename() );
				pdmDocument.addDocMetaData("StoreTyp" ,fileInfo.getStoretyp() );
				pdmDocument.addDocMetaData("FileSize" ,fileInfo.getFileSize().toString() );
			pdmDocumentList.add(pdmDocument);	
			}
			
		}
		
			return pdmDocumentList;
		
	}

	private File getPdmFile(FileInfo fileInfo, ChildLink childLink) throws PdmDocumentsException {
		
		
		String url =  String.format(GETFILE_URL, this.server,childLink.getChildLinkTo(), fileInfo.getFileid());
		String zielverz = "rmtmp/pdmgetDocuments/" + TIMESTAMP + "/"; 
		String zieldatei =  fileInfo.getFilename(); 
		File zielver = new File(zielverz);
		if (!zielver.exists()) {
			zielver.mkdirs();
		}
		File targetFile = new File(zielverz + zieldatei);
		RestTemplate restTemplate = new RestTemplate();
	    restTemplate.getMessageConverters().add(
	            new ByteArrayHttpMessageConverter());

	    HttpHeaders headers = getHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));

	    HttpEntity<String> entity = new HttpEntity<String>(headers);

	    ResponseEntity<byte[]> response = restTemplate.exchange(
	            url,
	            HttpMethod.GET, entity, byte[].class, "1");

	    if (response.getStatusCode() == HttpStatus.OK) {
	        try {
				 Path path = Files.write(Paths.get(zielverz + zieldatei), response.getBody());
				 File file = new File(path.toString());
				 return file;
			} catch (IOException e) {

				throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.keytech.error.getfilefromService"), e);
			}
	    }else {
	    	throw new PdmDocumentsException(String.format(Util.getMessage("pdmDocument.restservice.keytech.error.getfilehttprequest"), response.getStatusCode().toString()));
		}
		
	}

	private ArrayList<FileInfo> getFileInfoList(ChildLink childLink) {
		
		String url = String.format(FILES_AT_DOKUMENT_URL, this.server, childLink.getChildLinkTo());
		RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<String>(getHeaders());
        ResponseEntity<FileInfoList> response = restTemplate.exchange(url, HttpMethod.GET, request, FileInfoList.class);
        FileInfoList fileInfoList = response.getBody();
        return fileInfoList.getFileInfoList();
       
	}

	@Override
	public ArrayList<PdmDocument> getAllDocumentsUnderThisProduct(String abasIdNo) throws PdmDocumentsException  {
		// TODO Auto-generated method stub
		return null;
	}

	 protected ResponsePDMProductId requestRestservicePDMProductID(String url) throws PdmDocumentsException {
			
	    	
	        RestTemplate restTemplate = new RestTemplate();
	        HttpEntity<String> request = new HttpEntity<String>(getHeaders());
	        ResponseEntity<ResponsePDMProductId> response;
			try {
//				response = restTemplate.exchange(url,HttpMethod.GET,request,  String.class);
			      response = restTemplate.exchange(url, HttpMethod.GET, request, ResponsePDMProductId.class);
			} catch (RestClientException e) {
				// TODO Auto-generated catch block
				
					throw new PdmDocumentsException(String.format(Util.getMessage("pdmDocument.restservice.keytech.error.resttemplate")) , e);
				
			}
  
	        ResponsePDMProductId responseDat = response.getBody();
//	        String responseDat = response.getBody();
	        return responseDat;
	        
	       
	    }
	
	
	
}
