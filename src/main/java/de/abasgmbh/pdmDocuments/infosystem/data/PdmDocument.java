package de.abasgmbh.pdmDocuments.infosystem.data;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import de.abasgmbh.pdmDocuments.infosystem.PdmDocumentsException;
import de.abasgmbh.pdmDocuments.infosystem.utils.DocumentsUtil;
import de.abasgmbh.pdmDocuments.infosystem.utils.Util;

public class PdmDocument {

	protected final static Logger log = Logger.getLogger(PdmDocument.class);

	private File file;
	private String filename;
	private String filetyp;
	private String documenttyp;
	private String pageformat;
	private String urlDocFile;
	private String error = "";

	HashMap<String, DocMetaData> metaDataList;

	public PdmDocument(File file, String documenttyp) throws PdmDocumentsException {
		super();
		this.file = file;
		this.filename = this.file.getName();
		this.filetyp = DocumentsUtil.getFileExtension(this.file);
		this.metaDataList = new HashMap<String, DocMetaData>();
		this.documenttyp = documenttyp;
		try {
			this.pageformat = DocumentsUtil.getPageFormat(this.file);
		} catch (IOException e) {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.formatcheck.error.io"));
		}
	}

	public PdmDocument(String filename, String documenttyp, String urlDocFile) {
		super();
		this.urlDocFile = urlDocFile;
		this.filename = filename;
		this.filetyp = DocumentsUtil.getFileExtensionFromName(filename);
		this.documenttyp = documenttyp;
		this.metaDataList = new HashMap<String, DocMetaData>();

	}

	public void addFile(File file) {

		this.file = file;
		this.filename = this.file.getName();
		this.filetyp = DocumentsUtil.getFileExtension(this.file);
		try {
			this.pageformat = DocumentsUtil.getPageFormat(this.file);
		} catch (IOException e) {
			this.error = this.error + " " + Util.getMessage("pdmDocument.formatcheck.error.io");
		}
	}

	public String getUrlDocFile() {
		return urlDocFile;
	}

	public String getDocumenttyp() {
		return documenttyp;
	}

	public void addDocMetaData(String valueName, String value) throws PdmDocumentsException {

		DocMetaData docMetaData = new DocMetaData(valueName, value);

		if (!metaDataList.containsKey(valueName)) {
			this.metaDataList.put(valueName, docMetaData);
		} else {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.metaDataList.doubleValue", valueName));
		}

	}

	public void addDocMetaData(String valueName, Integer value) throws PdmDocumentsException {

		DocMetaData docMetaData = new DocMetaData(valueName, value);

		if (!metaDataList.containsKey(valueName)) {
			this.metaDataList.put(valueName, docMetaData);
		} else {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.metaDataList.doubleValue", valueName));
		}

	}

	public void addDocMetaData(String valueName, Date value) throws PdmDocumentsException {

		DocMetaData docMetaData = new DocMetaData(valueName, value);

		if (!metaDataList.containsKey(valueName)) {
			this.metaDataList.put(valueName, docMetaData);
		} else {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.metaDataList.doubleValue", valueName));
		}

	}

	public void addDocMetaData(String valueName, BigDecimal value) throws PdmDocumentsException {

		DocMetaData docMetaData = new DocMetaData(valueName, value);

		if (!metaDataList.containsKey(valueName)) {
			this.metaDataList.put(valueName, docMetaData);
		} else {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.metaDataList.doubleValue", valueName));
		}

	}

	public DocMetaData getDocMetaDataByName(String valueName) {

		if (this.metaDataList.containsKey(valueName)) {
			return this.metaDataList.get(valueName);
		} else {
			return null;
		}
	}

	public void addDocMetaData(String valueName, Object value) throws PdmDocumentsException {
		DocMetaData docMetaData = new DocMetaData(valueName, value);

		if (!metaDataList.containsKey(valueName)) {
			this.metaDataList.put(valueName, docMetaData);
		} else {
			throw new PdmDocumentsException(Util.getMessage("pdmDocument.metaDataList.doubleValue", valueName));
		}
	}

	public Boolean hasFile() {
		if (this.file == null || this.file.equals(null)) {
			return false;
		}
		return true;
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

	public Collection<DocMetaData> getMetaDataList() {
		return this.metaDataList.values();
	}

	public String getError() {
		return error;
	}

	public void addError(String errortxt) {
		if (this.error.isEmpty()) {
			this.error = errortxt;
		} else {
			this.error = this.error + ";" + errortxt;
		}
	}

	public boolean hasError() {
		if (this.error.isEmpty()) {
			return false;
		}
		return true;
	}

	public Boolean checkFileListTyp(String[] fileListTyp) {

		String filetyp = this.filetyp;
		Boolean allempty = true;

		if (fileListTyp != null) {
			if (!fileListTyp.toString().isEmpty()) {
				for (String typ : fileListTyp) {
					if (!typ.isEmpty()) {
						if (typ.trim().toUpperCase().equals(filetyp.toUpperCase())) {
							log.trace(Util.getMessage("pdmDocument.checkdocument.includePdmDoc", this.getFilename(),
									Arrays.toString(fileListTyp)));
							return true;
						}
						allempty = false;
					}
				}
			}
		}
		if (allempty) {
			log.trace(Util.getMessage("pdmDocument.checkdocument.includePdmDoc.emptyTypliste", this.getFilename()));
			return true;
		} else {
			log.trace(Util.getMessage("pdmDocument.checkdocument.excludePdmDoc", this.getFilename(),
					Arrays.toString(fileListTyp)));
			return false;
		}

	}

	public Boolean checkFileNameList(ArrayList<String> fileNameList) {

		String pdmFileName = this.filename;
		Boolean allempty = true;

		if (fileNameList != null) {
			if (fileNameList.size() > 0) {
				for (String filename : fileNameList) {
					if (!filename.isEmpty()) {
						if (filename.trim().toUpperCase().equals(pdmFileName.toUpperCase())) {
							log.trace(Util.getMessage("pdmDocument.checkdocument.includeFilenameList",
									this.getFilename()));
							return true;
						}
						allempty = false;
					}
				}
			}
		}
		if (allempty) {
			log.trace(Util.getMessage("pdmDocument.checkdocument.includeFilenameList.emptyList", this.getFilename()));
			return true;
			// return false;
		} else {
			log.trace(Util.getMessage("pdmDocument.checkdocument.excludeFilenameList", this.getFilename()));
			return false;
		}

	}

	public Boolean checkDocTypList(ArrayList<String> doctypList) {

		String pdmDocumenttyp = this.documenttyp;
		Boolean allempty = true;

		if (doctypList != null) {
			if (doctypList.size() > 0) {
				for (String doctyp : doctypList) {
					if (!doctyp.isEmpty()) {
						if (doctyp.trim().toUpperCase().equals(pdmDocumenttyp.toUpperCase())) {
							log.trace(
									Util.getMessage("pdmDocument.checkdocument.includeDoctypList", this.getFilename()));
							return true;
						}
						allempty = false;
					}
				}
			}
		}
		if (allempty) {
			log.trace(Util.getMessage("pdmDocument.checkdocument.includeDoctypList.emptyList", this.getFilename()));
			return true;
			// return false;
		} else {
			log.trace(Util.getMessage("pdmDocument.checkdocument.excludeDoctypList", this.getFilename()));
			return false;
		}

	}

}
