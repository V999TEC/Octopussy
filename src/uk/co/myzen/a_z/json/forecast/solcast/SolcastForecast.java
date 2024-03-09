package uk.co.myzen.a_z.json.forecast.solcast;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SolcastForecast {

	@JsonProperty("pv_estimate")
	private Float pvEstimate;

	@JsonProperty("pv_estimate10")
	private Float pvEstimate10;

	@JsonProperty("pv_estimate90")
	private Float pvEstimate90;

	@JsonProperty("period_end")
	private String periodEnd;

	@JsonProperty("period")
	private String period;

	public Float getPvEstimate() {
		return pvEstimate;
	}

	public void setPvEstimate(Float pvEstimate) {
		this.pvEstimate = pvEstimate;
	}

	public Float getPvEstimate10() {
		return pvEstimate10;
	}

	public void setPvEstimate10(Float pvEstimate10) {
		this.pvEstimate10 = pvEstimate10;
	}

	public Float getPvEstimate90() {
		return pvEstimate90;
	}

	public void setPvEstimate90(Float pvEstimate90) {
		this.pvEstimate90 = pvEstimate90;
	}

	public String getPeriodEnd() {
		return periodEnd;
	}

	public void setPeriodEnd(String periodEnd) {
		this.periodEnd = periodEnd;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

}
