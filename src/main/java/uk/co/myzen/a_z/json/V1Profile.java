package uk.co.myzen.a_z.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class V1Profile {

	@JsonProperty("profile_class")
	private String profileClass;

	@JsonProperty("mpan")
	private String mpan;

	@JsonProperty("gsp")
	private String gsp;

	public String getGsp() {
		return gsp;
	}

	public void setGsp(String gsp) {
		this.gsp = gsp;
	}

	public String getMpan() {
		return mpan;
	}

	public void setMpan(String mpan) {
		this.mpan = mpan;
	}

	public String getProfileClass() {
		return profileClass;
	}

	public void setProfileClass(String profileClass) {
		this.profileClass = profileClass;
	}

}
