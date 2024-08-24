package uk.co.myzen.a_z.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class V1PeriodConsumption {

	private Float consumption;

	@JsonProperty("interval_start")
	private String intervalStart;

	@JsonProperty("interval_end")
	private String intervalEnd;

	public Float getConsumption() {
		return consumption;
	}

	public void setConsumption(Float consumption) {
		this.consumption = consumption;
	}

	public String getIntervalStart() {
		return intervalStart;
	}

	public void setIntervalStart(String intervalStart) {
		this.intervalStart = intervalStart;
	}

	public String getIntervalEnd() {
		return intervalEnd;
	}

	public void setIntervalEnd(String intervalEnd) {
		this.intervalEnd = intervalEnd;
	}
}
