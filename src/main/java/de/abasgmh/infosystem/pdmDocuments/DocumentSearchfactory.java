package de.abasgmh.infosystem.pdmDocuments;

import de.abasgmh.infosystem.pdmDocuments.webservices.RestServiceProcad;
import de.abasgmh.infosystem.pdmDocuments.webservices.keytech.RestServiceKeytech;

public class DocumentSearchfactory {

	
	

	public DocumentsInterface create(String pdmsystem, String server, String user, String password) {
		
		
		
		switch (pdmsystem) {
		case "procad":
			return new RestServiceProcad(server, user , password );
			

		case "keytech":
			
			return new RestServiceKeytech(server, user, password);
			

		default:
			break;
		}
		
		return null;
	}
	
}
