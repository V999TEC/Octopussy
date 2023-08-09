package uk.co.myzen.a_z.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Agile {

	@JsonProperty("value_exc_vat")
	private Float valueExcVat;

	@JsonProperty("value_inc_vat")
	private Float valueIncVat;

	@JsonProperty("valid_from")
	private String validFrom;

	@JsonProperty("valid_to")
	private String validTo;

	@JsonProperty("payment_method")
	private String paymentMethod;

	public Float getValueExcVat() {
		return valueExcVat;
	}

	public void setValueExcVat(Float valueExcVat) {
		this.valueExcVat = valueExcVat;
	}

	public Float getValueIncVat() {
		return valueIncVat;
	}

	public void setValueIncVat(Float valueIncVat) {
		this.valueIncVat = valueIncVat;
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
