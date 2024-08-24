package uk.co.myzen.a_z.json.forecast.solar;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SolarMessage {

	@JsonProperty("code")
	private Integer code;

	@JsonProperty("ratelimit")
	private RateLimit rateLimit;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public RateLimit getRateLimit() {
		return rateLimit;
	}

	public void setRateLimit(RateLimit rateLimit) {
		this.rateLimit = rateLimit;
	}

}
