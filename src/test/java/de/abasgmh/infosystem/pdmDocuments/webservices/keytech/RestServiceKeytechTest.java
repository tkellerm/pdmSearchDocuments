package de.abasgmh.infosystem.pdmDocuments.webservices.keytech;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import de.abas.erp.db.schema.userenums.UserEnumPdmSystems;
import de.abasgmbh.infosystem.pdmDocuments.DocumentSearchfactory;
import de.abasgmbh.infosystem.pdmDocuments.DocumentsInterface;
import de.abasgmbh.infosystem.pdmDocuments.PdmDocumentsException;
import de.abasgmbh.infosystem.pdmDocuments.config.Configuration;
import de.abasgmbh.infosystem.pdmDocuments.config.ConfigurationHandler;
import de.abasgmbh.infosystem.pdmDocuments.data.PdmDocument;

public class RestServiceKeytechTest {

//	@Test
//	public void testRestServiceKeytech() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testSearchPdmProductID() { 
		Configuration config = Configuration.getInstance();
		config.setPdmSystem(UserEnumPdmSystems.KEYTECH);
		
		DocumentSearchfactory factory = new DocumentSearchfactory();
		
		try {
			config.setRestServer("demo.keytech.de", "jgrant", "", "");
			DocumentsInterface restService = factory.create(config);
			String ergebnis = restService.searchPdmProductID("BCU 4010 GP");
			assertTrue(ergebnis.length() > 0);
		} catch (PdmDocumentsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.toString());
		}
	}

	@Test
	public void testGetAllDocuments() {
		DocumentSearchfactory factory = new DocumentSearchfactory();
		DocumentsInterface restService;
		try {
			restService = factory.create(Configuration.getInstance());
			ArrayList<PdmDocument> ergebnis = restService.getAllDocuments("ITM004730");
			assertTrue(ergebnis!=null);
		} catch (PdmDocumentsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail(e.toString());
		}
	}
//
//	@Test
//	public void testGetAllDocumentsUnderThisProduct() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testRequestRestservicePDMProductID() {
//		fail("Not yet implemented");
//	}

}
