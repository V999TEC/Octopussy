package uk.co.myzen.a_z.json.forecast.solar;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResultMessage {

	@JsonProperty("result")
	private SolarResult result;

	@JsonProperty("message")
	private SolarMessage message;

	public SolarMessage getMessage() {
		return message;
	}

	public void setMessage(SolarMessage message) {
		this.message = message;
	}

	public SolarResult getResult() {
		return result;
	}

	public void setResult(SolarResult result) {
		this.result = result;
	}

}
