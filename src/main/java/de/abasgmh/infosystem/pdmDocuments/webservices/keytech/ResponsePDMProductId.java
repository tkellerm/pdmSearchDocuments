package de.abasgmh.infosystem.pdmDocuments.webservices.keytech;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponsePDMProductId {

//	{
//		  "PageNumber": 1,
//		  "Totalrecords": 1,
//		  "PageSize": 50,
//		  "ElementList": [
//		    {
//		      "Key": "DEFAULT_MI:600182",
//		      "KeyValueList": [],
//		      "Name": "BCU 4010 GP",
//		      "DisplayName": "BCU 4010 GP - 00 - hydraulisches Schneidgerät - in Arbeit",
//		      "ClassDisplayName": "Alle Artikel",
//		      "Version": "00",
//		      "Status": "in Arbeit",
//		      "Description": "hydraulisches Schneidgerät",
//		      "CreatedAt": "/Date(1347141600000)/",
//		      "CreatedBy": "pmiller",
//		      "CreatedByLong": "Peter Miller",
//		      "ChangedAt": "/Date(1347141600000)/",
//		      "ChangedBy": "pmiller",
//		      "ChangedByLong": "Peter Miller",
//		      "ReleasedAt": "/Date(-3600000)/",
//		      "ReleasedBy": "",
//		      "ReleasedByLong": "",
//		      "ThumbnailHint": "DEFAULT_MI",
//		      "HasVersions": false
//		    }
//		  ]
//		}
	
	
	
	
	
	@JsonProperty("PageNumber")
	Integer pageNumber;
	
	@JsonProperty("Totalrecords")
	Integer totalRecords;
	
	@JsonProperty("PageSize")
	Integer pageSize;
	
	@JsonProperty("ElementList")
	ArrayList<Elements> elementsList;
	
	public Integer getPageNumber() {
		return pageNumber;
	}
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}
	public Integer getTotalRecords() {
		return totalRecords;
	}
	public void setTotalRecords(Integer totalRecords) {
		this.totalRecords = totalRecords;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public ArrayList<Elements> getElementsList() {
		return elementsList;
	}

	//	public void setElementsList(ArrayList<Elements> elementsList) {
//		this.elementsList = elementsList;
//	}
	
	
}
