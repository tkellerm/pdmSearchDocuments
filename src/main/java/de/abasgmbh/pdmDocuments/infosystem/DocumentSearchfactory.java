package de.abasgmbh.pdmDocuments.infosystem;

import org.jboss.logging.Logger;

import de.abasgmbh.pdmDocuments.infosystem.config.Configuration;
import de.abasgmbh.pdmDocuments.infosystem.webservices.keytech.RestServiceKeytech;
import de.abasgmbh.pdmDocuments.infosystem.webservices.procad.RestServiceProcad;

public class DocumentSearchfactory {

	protected final static Logger log = Logger.getLogger(DocumentSearchfactory.class);

	public DocumentsInterface create(Configuration config) throws PdmDocumentsException {

		// testserver(server);

		switch (config.getPdmSystem()) {
		case PROFILE: {
			log.info("Procad Service");
			DocumentsInterface restService = new RestServiceProcad(config);
			if (restService.testConnection()) {
				return restService;
			}

		}
			break;
		case KEYTECH: {
			log.info("Keytech Service");
			DocumentsInterface restService = new RestServiceKeytech(config);
			if (restService.testConnection()) {
				return restService;
			}

		}
			break;
		default:

			break;
		}

		return null;

	}

}
