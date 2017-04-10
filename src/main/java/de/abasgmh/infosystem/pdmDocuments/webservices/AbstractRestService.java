package de.abasgmh.infosystem.pdmDocuments.webservices;

import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import de.abasgmh.infosystem.pdmDocuments.DocumentsInterface;

public abstract class AbstractRestService implements DocumentsInterface {
	
	
	protected final static Logger log = Logger.getLogger(AbstractRestService.class);
	
	protected String server;
	protected String pdmProductID;
	protected String pdmDocumentTyp;
	protected String user;
	protected String password;
	

	@Override
	public void setPasword(String password) {
		this.password = password;
		
	}

	@Override
	public void setUser(String user) {
		this.user = user;
		
	}

	@Override
	public void setServer(String serveradresse) {
		this.server = serveradresse;

	}

	@Override
	public void setProduct(String pdmProductID) {
		this.pdmProductID =  pdmProductID;

	}

	@Override
	public void setDocumentTyp(String pdmDocumentTyp) {
		this.pdmDocumentTyp = pdmDocumentTyp;

	}


   

    
    protected  HttpHeaders getHeaders(){
        String plainCredentials=this.user + ":" + this.password;
        String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
         
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Credentials);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }
	
	
	

	

}
