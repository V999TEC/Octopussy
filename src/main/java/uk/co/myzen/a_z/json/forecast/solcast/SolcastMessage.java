package uk.co.myzen.a_z.json.forecast.solcast;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SolcastMessage {

	@JsonProperty("forecasts")
	private List<SolcastForecast> forecasts;

	public List<SolcastForecast> getForecasts() {
		return forecasts;
	}

	public void setForecasts(List<SolcastForecast> forecasts) {
		this.forecasts = forecasts;
	}

}
