package uk.co.myzen.a_z.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Prices {

	@JsonProperty("value_exc_vat")
	private Float valueExcVAT;

	@JsonProperty("value_inc_vat")
	private Float valueIncVAT;

	@JsonProperty("valid_from")
	private String validFrom;

	@JsonProperty("valid_to")
	private String validTo;

	@JsonProperty("payment_method")
	private String paymentMethod;

	public Float getValueExcVAT() {
		return valueExcVAT;
	}

	public void setValueExcVAT(Float valueExcVAT) {
		this.valueExcVAT = valueExcVAT;
	}

	public Float getValueIncVAT() {
		return valueIncVAT;
	}

	public void setValueIncVAT(Float valueIncVAT) {
		this.valueIncVAT = valueIncVAT;
	}

	public String getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(String validFrom) {
		this.validFrom = validFrom;
	}

	public String getValidTo() {
		return validTo;
	}

	public void setValidTo(String validTo) {
		this.validTo = validTo;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
}
