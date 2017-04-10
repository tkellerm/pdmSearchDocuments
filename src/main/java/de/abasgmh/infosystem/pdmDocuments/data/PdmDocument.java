package de.abasgmh.infosystem.pdmDocuments.data;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import de.abasgmh.infosystem.pdmDocuments.PdmDocumentsException;
import de.abasgmh.infosystem.pdmDocuments.utils.Util;

public class PdmDocument {
	
	private File file;
	private String filename;
	private String filetyp;
	private String documenttyp;
	
	HashMap<String , DocMetaData> metaDataList;

	public PdmDocument(File file, String documenttyp) {
		super();
		this.file = file;
		this.filename = this.file.getName();
		this.filetyp = getFileExtension(this.file);
		this.metaDataList = new HashMap<String , DocMetaData>();
		this.documenttyp = documenttyp;
	}
	
	
	public String getDocumenttyp() {
		return documenttyp;
	}


	public void addDocMetaData(String valueName , String value) throws PdmDocumentsException{
		
		DocMetaData docMetaData = new DocMetaData(valueName, value);
		
		if (!metaDataList.containsKey(valueName)) {
			this.metaDataList.put(valueName , docMetaData);	
		}else {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.metaDataList.doubleValue", valueName));
		}
		
	}
	
	public DocMetaData getDocMetaDataByName(String valueName){
		
		if (this.metaDataList.containsKey(valueName)) {
			return this.metaDataList.get(valueName);	
		}else {
			return null;
		}
	}
	

	public File getFile() {
		return file;
	}


	public String getFilename() {
		return filename;
	}

	public String getFiletyp() {
		return filetyp;
	}

	public  Collection<DocMetaData> getMetaDataList() {
		return this.metaDataList.values();
	}

	private String getFileExtension(File file2) {
		
		String filename = file.getName();
		int pointIndex = filename.lastIndexOf(".");
		 if (pointIndex != -1) {
			String fileExtension = filename.substring(pointIndex);
			return fileExtension;
		}else 
			return "";
	}
	
	

}
