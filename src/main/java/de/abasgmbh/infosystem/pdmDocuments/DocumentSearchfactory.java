package de.abasgmbh.infosystem.pdmDocuments;

import org.jboss.logging.Logger;

import de.abasgmbh.infosystem.pdmDocuments.config.Configuration;
import de.abasgmbh.infosystem.pdmDocuments.webservices.keytech.RestServiceKeytech;
import de.abasgmbh.infosystem.pdmDocuments.webservices.procad.RestServiceProcad;

public class DocumentSearchfactory {

	protected final static Logger log = Logger.getLogger(DocumentSearchfactory.class);
	
	
	public DocumentsInterface create(Configuration config) throws PdmDocumentsException {

//		testserver(server);
		
		
			switch (config.getPdmSystem()) {
			case PROFILE:{
					log.info("Procad Service");
					DocumentsInterface restService = new RestServiceProcad(config);
					if (restService.testConnection()) {
						return restService;
					}
				}

			case KEYTECH: {
					log.info("Keytech Service");
					DocumentsInterface restService = new RestServiceKeytech(config);
					if (restService.testConnection()) {
						return restService;
					} 
				}
			default:
				
				break;
			}
		
		return null;
	
	
	}


	



	
	
}
