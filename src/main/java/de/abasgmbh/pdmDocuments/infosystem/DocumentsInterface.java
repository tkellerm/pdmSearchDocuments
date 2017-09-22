package de.abasgmbh.pdmDocuments.infosystem;

import java.util.ArrayList;

import de.abasgmbh.pdmDocuments.infosystem.data.PdmDocument;

public interface DocumentsInterface {
	
	public void setServer(String serveradresse);
	
	public void setProduct(String pdmProductID);
	
	public void setDocumentTyp(String pdmDocumentTyp);
	
	public void setPasword(String password);
	
	public void setUser(String user);
	
	public Boolean testConnection() throws PdmDocumentsException;
	
	public String searchPdmProductID(String abasIdNo) throws PdmDocumentsException;
	
	public  ArrayList<PdmDocument> getAllDocuments(String abasIdNo) throws PdmDocumentsException;
	
	
	
	
}
