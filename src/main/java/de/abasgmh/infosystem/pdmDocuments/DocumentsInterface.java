package de.abasgmh.infosystem.pdmDocuments;

import java.util.ArrayList;

import de.abasgmh.infosystem.pdmDocuments.data.PdmDocument;

public interface DocumentsInterface {
	
	public void setServer(String serveradresse);
	
	public void setProduct(String pdmProductID);
	
	public void setDocumentTyp(String pdmDocumentTyp);
	
	public void setPasword(String password);
	
	public void setUser(String user);
	
	public String searchPdmProductID(String abasIdNo) throws PdmDocumentsException;
	
	public  ArrayList<PdmDocument> getAllDocuments(String abasIdNo) throws PdmDocumentsException;
	
	public  ArrayList<PdmDocument> getAllDocumentsUnderThisProduct(String abasIdNo) throws PdmDocumentsException;
	
	
}
