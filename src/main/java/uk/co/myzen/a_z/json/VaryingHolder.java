package uk.co.myzen.a_z.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VaryingHolder {

	@JsonProperty("varying")
	private Varying varying;

	@JsonProperty("direct_debit_monthly")
	private Varying directDebitMonthly;

	public Varying getVarying() {
		return varying;
	}

	public void setVarying(Varying varying) {
		this.varying = varying;
	}

	public Varying getDirectDebitMonthly() {
		return directDebitMonthly;
	}

	public void setDirectDebitMonthly(Varying directDebitMonthly) {
		this.directDebitMonthly = directDebitMonthly;
	}

}
