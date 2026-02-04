package uk.co.myzen.hsp.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ElectricityMeterPoint extends MeterPoint {

	@JsonProperty("mpan")
	private String mpan;

	@JsonProperty("profile_class")
	private Integer profileClass;

	public String getMpan() {
		return mpan;
	}

	public void setMpan(String mpan) {
		this.mpan = mpan;
	}

	public Integer getProfileClass() {
		return profileClass;
	}

	public void setProfileClass(Integer profileClass) {
		this.profileClass = profileClass;
	}

}
