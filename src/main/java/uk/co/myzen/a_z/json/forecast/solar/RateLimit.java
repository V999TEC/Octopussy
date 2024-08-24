package uk.co.myzen.a_z.json.forecast.solar;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RateLimit {

	@JsonProperty("period")
	private Integer period;

	@JsonProperty("limit")
	private Integer limit;

	@JsonProperty("remaining")
	private Integer remaining;

	public Integer getPeriod() {
		return period;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public Integer getRemaining() {
		return remaining;
	}

	public void setRemaining(Integer remaining) {
		this.remaining = remaining;
	}
}
