package de.abasgmbh.infosystem.pdmDocuments.data;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import de.abasgmbh.infosystem.pdmDocuments.PdmDocumentsException;
import de.abasgmbh.infosystem.pdmDocuments.utils.DocumentsUtil;
import de.abasgmbh.infosystem.pdmDocuments.utils.Util;

public class PdmDocument {
	
	private File file;
	private String filename;
	private String filetyp;
	private String documenttyp;
	private String pageformat;
	
	HashMap<String , DocMetaData> metaDataList;

	public PdmDocument(File file, String documenttyp) throws PdmDocumentsException {
		super();
		this.file = file;
		this.filename = this.file.getName();
		this.filetyp = DocumentsUtil.getFileExtension(this.file);
		this.metaDataList = new HashMap<String , DocMetaData>();
		this.documenttyp = documenttyp;
		try {
			this.pageformat = DocumentsUtil.getPageFormat(this.file);
		} catch (IOException e) {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.formatcheck.error.io"));
		}
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
	
public void addDocMetaData(String valueName , Integer value) throws PdmDocumentsException{
		
		DocMetaData docMetaData = new DocMetaData(valueName, value);
		
		if (!metaDataList.containsKey(valueName)) {
			this.metaDataList.put(valueName , docMetaData);	
		}else {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.metaDataList.doubleValue", valueName));
		}
		
	}
	
public void addDocMetaData(String valueName , Date value) throws PdmDocumentsException{
	
	DocMetaData docMetaData = new DocMetaData(valueName, value);
	
	if (!metaDataList.containsKey(valueName)) {
		this.metaDataList.put(valueName , docMetaData);	
	}else {
		throw new PdmDocumentsException(Util.getMessage("pdmDocument.metaDataList.doubleValue", valueName));
	}
	
}

public void addDocMetaData(String valueName , BigDecimal value) throws PdmDocumentsException{
	
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
	
	public void addDocMetaData(String valueName, Object value) throws PdmDocumentsException {
		DocMetaData docMetaData = new DocMetaData(valueName, value);
		
		if (!metaDataList.containsKey(valueName)) {
			this.metaDataList.put(valueName , docMetaData);	
		}else {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.metaDataList.doubleValue", valueName));
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

	
	

}
