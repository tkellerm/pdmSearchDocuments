package de.abasgmh.infosystem.pdmDocuments.data;

import java.math.BigDecimal;
import java.util.Date;

public class DocMetaData {

	private String 	name;
	private String 	value;
	private MetaDataTyp typ;
	
	
	public DocMetaData(String name, Integer value) {
		
		super();
		this.name = name;
		this.value = value.toString();
		this.typ = MetaDataTyp.INTEGER;
		
	}
	
	public DocMetaData(String name, Date value) {
		
		super();
		this.name = name;
		this.value = value.toString();
		this.typ = MetaDataTyp.DATE;
		
	}
	
	
	public DocMetaData(String name, BigDecimal value) {
		
		super();
		this.name = name;
		this.value = value.toString();
		this.typ = MetaDataTyp.BIGDEZIMAL;
		
	}
	
	
	
	public DocMetaData(String name, String value) {
		super();
		this.name = name;
		this.value = value;
		this.typ = MetaDataTyp.STRING;
	}

	public String getName() {
		return name;
	}
	
	public String getValue(){
		return value;
	}

	public MetaDataTyp getType() {
		return typ;
	}
	
	
	
	
	
	 
	
	
}
