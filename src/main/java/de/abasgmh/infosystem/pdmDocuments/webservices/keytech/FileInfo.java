package de.abasgmh.infosystem.pdmDocuments.webservices.keytech;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileInfo {

	@JsonProperty("FileID")
	private String fileid;
	
	@JsonProperty("FileName")
	private String filename;
	
	@JsonProperty("FileSize")
	private Integer fileSize;
	
	@JsonProperty("FileStorageType")
	private String storetyp;
	
	
	public String getFileid() {
		return fileid;
	}
	public String getFilename() {
		return filename;
	}
	public Integer getFileSize() {
		return fileSize;
	}
	public String getStoretyp() {
		return storetyp;
	}
	
}
