package uk.co.myzen.a_z.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MeterPoint {

	@JsonProperty("consumption_standard")
	private Integer consumptionStandard;

	@JsonProperty("meters")
	private List<Meter> meters;

	@JsonProperty("agreements")
	private List<Agreement> agreements;

	public Integer getConsumptionStandard() {
		return consumptionStandard;
	}

	public void setConsumptionStandard(Integer consumptionStandard) {
		this.consumptionStandard = consumptionStandard;
	}

	public List<Meter> getMeters() {
		return meters;
	}

	public void setMeters(List<Meter> meters) {
		this.meters = meters;
	}

	public List<Agreement> getAgreements() {
		return agreements;
	}

	public void setAgreements(List<Agreement> agreements) {
		this.agreements = agreements;
	}

}
