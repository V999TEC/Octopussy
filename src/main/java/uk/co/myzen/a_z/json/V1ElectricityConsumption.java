package uk.co.myzen.a_z.json;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class V1ElectricityConsumption extends V1Results {

	@JsonProperty("results")
	protected ArrayList<V1PeriodConsumption> periodResults;

	public ArrayList<V1PeriodConsumption> getPeriodResults() {
		return periodResults;
	}

	public void setPeriodResults(ArrayList<V1PeriodConsumption> periodResults) {
		this.periodResults = periodResults;
	}
}
