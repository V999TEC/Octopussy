package uk.co.myzen.a_z.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Register {

	@JsonProperty("identifier")
	private String identifier;

	@JsonProperty("rate")
	private String rate;

	@JsonProperty("is_settlement_register")
	private Boolean isSettlementRegister;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public Boolean getIsSettlementRegister() {
		return isSettlementRegister;
	}

	public void setIsSettlementRegister(Boolean isSettlementRegister) {
		this.isSettlementRegister = isSettlementRegister;
	}

}
