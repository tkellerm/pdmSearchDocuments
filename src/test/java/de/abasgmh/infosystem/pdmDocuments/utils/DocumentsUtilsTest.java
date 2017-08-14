package de.abasgmh.infosystem.pdmDocuments.utils;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.abasgmbh.infosystem.pdmDocuments.utils.DocumentsUtil;

public class DocumentsUtilsTest {

	@Test
	public void testGetPageFormat() {
		File testfilepdf = new File("test.pdf");
		File testfiletiff = new File("test.tif");
		String testpdf = null;
		String testtiff = null;
		try {
			testpdf = DocumentsUtil.getPageFormat(testfilepdf);
			testtiff = DocumentsUtil.getPageFormat(testfiletiff);
		} catch (IOException e) {
			fail(e.getMessage());
			
		}
		assertTrue(!testpdf.isEmpty() && !testtiff.isEmpty());
		
		
	}

}
