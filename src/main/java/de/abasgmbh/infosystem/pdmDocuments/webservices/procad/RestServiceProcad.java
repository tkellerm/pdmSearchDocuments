package de.abasgmbh.infosystem.pdmDocuments.webservices.procad;

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

import de.abasgmbh.infosystem.pdmDocuments.PdmDocumentsException;
import de.abasgmbh.infosystem.pdmDocuments.config.Configuration;
import de.abasgmbh.infosystem.pdmDocuments.data.PdmDocument;
import de.abasgmbh.infosystem.pdmDocuments.utils.Util;
import de.abasgmbh.infosystem.pdmDocuments.webservices.AbstractRestService;

import de.abasgmbh.infosystem.pdmDocuments.webservices.procad.ResponsePDMProductId;

public class RestServiceProcad extends AbstractRestService {
	
	private static String TESTSERVICE_URL = "http://%1s/procad/profile/api/version";
//	https://{server}:{port}/procad/profile/api/{tenant}/
	private static String BASE_URL = "http://%1s/procad/profile/api/%2s/";
	private static String SEARCHPRODUCT_URL = "objects/Part?query='%3s'='%4s'";
	private static String GETDOCUMENT_INFO = "";
	private static String GETDOCUMENT_FILE ="";
	private String tenant;
	private String sqlServer;
	private Integer sqlPort;
	private String sqlDatabase;
	private String sqlUser;
	private String sqlPassword;
	private Configuration config;
	
	public RestServiceProcad(String server, String user, String password, String tenant, String sqlserver, Integer sqlport, String sqldatabase, String sqluser, String sqlpassword) {

		super();
		this.setServer(server);
		this.setUser(user);
		this.setPasword(password);
		this.tenant = tenant;
		this.sqlServer = sqlserver;
		this.sqlPort = sqlport;
		this.sqlDatabase = sqldatabase;
		this.sqlUser = sqluser;
		this.sqlPassword = sqlpassword;
		
	}

	public RestServiceProcad(Configuration config) {
		super();
		this.config= config;
		this.setServer(config.getRestServer());
		this.setUser(config.getRestUser());
		this.setPasword(config.getRestPassword());
		this.tenant = config.getRestTenant();
		this.sqlServer = config.getSqlServer();
		this.sqlPort = config.getSqlPort();
		this.sqlDatabase = config.getSqldatabase();
		this.sqlUser = config.getSqlUser();
		this.sqlPassword = config.getSqlPassword();
		
		String testRestServiceUrl = String.format(TESTSERVICE_URL, this.server); 
		testRestService(testRestServiceUrl);
	}

	@Override
	public String searchPdmProductID(String abasIdNo) throws PdmDocumentsException {

		String url = String.format(BASE_URL + SEARCHPRODUCT_URL, this.server, this.tenant , this.config.getPartFieldName(),  abasIdNo);
		
		String jsonString = callRestservice(url);
		
		 ObjectMapper mapper = new ObjectMapper();
	        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	        ResponsePDMProductId response;
	        try {
				response = mapper.readValue(jsonString, ResponsePDMProductId.class);
			} catch (JsonParseException e) {
				throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.procad.error.jsonToObject", "ResponsePDMProductId"), e);
			} catch (JsonMappingException e) {
				throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.procad.error.jsonToObject", "ResponsePDMProductId"), e);
			} catch (IOException e) {
				throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.procad.error.jsonToObject", "ResponsePDMProductId"), e);
			}
	        
	        
		
	        List<PartObject> elementsList = response.getObjectsList();
		if (elementsList.size() == 1 ) {
//			return elementsList.get(0).getKey();
			Values values = elementsList.get(0).getValues();
			Map<String, Object> prop = values.getAdditionalProperties();
			
			if (prop.containsKey(config.getPartFieldName())) {
				Object objektID = prop.get(config.getPartProFileIDFieldName());
				if (objektID instanceof String) {
					String objStringID = (String)objektID;
					return objStringID;
				}
				 
			}
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.procad.ResultFalseField", abasIdNo));
		}else if (elementsList.size() == 0) {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.procad.noResult", abasIdNo));
		}else {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.restservice.procad.moreThanOneResult", abasIdNo));
		}
		
	}

	@Override
	public ArrayList<PdmDocument> getAllDocuments(String abasIdNo) throws PdmDocumentsException {
		String pdmProductID = searchPdmProductID(abasIdNo);
		
		return null;
	}

//	@Override
//	public ArrayList<PdmDocument> getAllDocumentsUnderThisProduct(String abasIdNo) throws PdmDocumentsException {
//		// http://localhost/procad/profile/api/profile86/objects/document
//	http://pro-biz01/procad/profile/api/profile86/objects/part/10068/
////		Durchwahl 693 Spindler
//		return null;
//	}

	public boolean testRestService(String urlString){
		
			 InputStream is = null;
		      try {
		         URL url = new URL(urlString);
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

@Override
public Boolean testConnection() throws PdmDocumentsException {
	if (testRestServer() && testSQLServer()) {
		return true;
	}
	
	return false;
	
}

private boolean testSQLServer() throws PdmDocumentsException {

	SQLConnectionHandler sqlConn = new SQLConnectionHandler(config);
	if (sqlConn != null) {
		return true;
	}else {
		return false;	
	}
	
	
	
}

private Boolean testRestServer() {
	String testRestServiceUrl = String.format(TESTSERVICE_URL, this.server); 
	 InputStream is = null;
     try {
        URL url = new URL(testRestServiceUrl);
        URLConnection con = url.openConnection();

        is = con.getInputStream();
        log.info("Server erreichbar");
        return true;
     } catch (NoRouteToHostException e) {
   	  log.error(e);
        return false;
     }catch (FileNotFoundException e) {
   	  log.info("Server erreichbar" , e);
//   	  Server ist erreichbar, aber es ist keine richtige Seite verfuegbar
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
