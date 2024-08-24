package uk.co.myzen.a_z.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GasMeterPoint extends MeterPoint {

	@JsonProperty("mprn")
	private String mprn;

	public String getMprn() {
		return mprn;
	}

	public void setMprn(String mprn) {
		this.mprn = mprn;
	}

}
