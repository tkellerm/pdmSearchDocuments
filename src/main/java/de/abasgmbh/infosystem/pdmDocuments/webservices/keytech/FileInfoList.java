package de.abasgmbh.infosystem.pdmDocuments.webservices.keytech;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FileInfoList {

	
	@JsonProperty("FileInfos")
	private ArrayList<FileInfo> fileInfoList;

	public ArrayList<FileInfo> getFileInfoList() {
		return fileInfoList;
	}
	
	
	
}
