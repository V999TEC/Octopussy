package uk.co.myzen.a_z.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChargeDischarge {

	@JsonProperty("charge")
	private Float charge;

	@JsonProperty("discharge")
	private Float discharge;

	public Float getCharge() {
		return charge;
	}

	public void setCharge(Float charge) {
		this.charge = charge;
	}

	public Float getDischarge() {
		return discharge;
	}

	public void setDischarge(Float discharge) {
		this.discharge = discharge;
	}

}
