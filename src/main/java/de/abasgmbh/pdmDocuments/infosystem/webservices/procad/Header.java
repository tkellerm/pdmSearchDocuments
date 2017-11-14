
package de.abasgmbh.pdmDocuments.infosystem.webservices.procad;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "Links", "HasFile" })
public class Header {

	@JsonProperty("Links")
	private List<Link> links = null;
	@JsonProperty("HasFile")
	private Boolean hasFile;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("Links")
	public List<Link> getLinks() {
		return links;
	}

	@JsonProperty("Links")
	public void setLinks(List<Link> links) {
		this.links = links;
	}

	@JsonProperty("HasFile")
	public Boolean getHasFile() {
		return hasFile;
	}

	@JsonProperty("HasFile")
	public void setHasFile(Boolean hasFile) {
		this.hasFile = hasFile;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
