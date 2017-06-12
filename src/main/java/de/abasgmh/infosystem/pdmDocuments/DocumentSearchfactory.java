package de.abasgmh.infosystem.pdmDocuments;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.URLConnection;

import org.jboss.logging.Logger;

import de.abasgmh.infosystem.pdmDocuments.webservices.RestServiceProcad;
import de.abasgmh.infosystem.pdmDocuments.webservices.keytech.RestServiceKeytech;

public class DocumentSearchfactory {

	protected final static Logger log = Logger.getLogger(DocumentSearchfactory.class);
	

	public DocumentsInterface create(String pdmsystem, String server, String user, String password) {
		
		testserver(server);
		
		if (testserver(server)) {
			switch (pdmsystem) {
			case "procad":
				log.info("Procad Service");
				return new RestServiceProcad(server, user, password);

			case "keytech":
				log.info("Keytech Service");
				return new RestServiceKeytech(server, user, password);

			default:
				
				break;
			}
		}
		return null;
	}

	private boolean testserver(String urlString) {
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
//	    	  Server ist erreichbar, aber es ist keine richtige Seite verfuegbar
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
