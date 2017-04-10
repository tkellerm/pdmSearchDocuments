package de.abasgmh.infosystem.pdmDocuments.testkeytech;

import java.util.Arrays;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.abasgmh.infosystem.pdmDocuments.webservices.keytech.ResponsePDMProductId;

import org.apache.commons.codec.binary.Base64;

public class TestSpringKeytech {
	
	 private static String REST_SERVICE_SEARCHPRODUCT_URL = "https://{1}/Search?classtypes=DEFAULT_MI&fields=as_mi__name=";
	 private static final String PASS = "pass";
	 private static final String FAIL = "fail";
	 private static String server = "demo.keytech.de"; 
	 private static final Logger log = Logger.getLogger(TestSpringKeytech.class);

	    public static void main(String args[]) {
	    	
	    	String productnumber = "BCU 4010 GP";
	    	String url1 = REST_SERVICE_SEARCHPRODUCT_URL + productnumber;
			 String url2 = String.format(url1 , TestSpringKeytech.server);
	        RestTemplate restTemplate = new RestTemplate();
	        
	        HttpEntity<String> request = new HttpEntity<String>(getHeaders());
	        ResponseEntity<ResponsePDMProductId> response = restTemplate.exchange(url2, HttpMethod.GET, request, ResponsePDMProductId.class);
	        ResponsePDMProductId responseDat = response.getBody(); 
	         
	        log.info(responseDat.toString());
	    }

	    
	    private static HttpHeaders getHeaders(){
	        String plainCredentials="jgrant:";
	        String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
	         
	        HttpHeaders headers = new HttpHeaders();
	        headers.add("Authorization", "Basic " + base64Credentials);
	        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
	        return headers;
	    }
}
