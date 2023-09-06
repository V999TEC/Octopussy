package uk.co.myzen.a_z.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class V1Account {

	@JsonProperty("number")
	private String number;

	@JsonProperty("properties")
	private List<Detail> properties;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public List<Detail> getProperties() {
		return properties;
	}

	public void setProperties(List<Detail> properties) {
		this.properties = properties;
	}

}
