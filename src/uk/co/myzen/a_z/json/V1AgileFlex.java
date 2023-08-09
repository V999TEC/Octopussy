package uk.co.myzen.a_z.json;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class V1AgileFlex extends V1Consumption {

	@JsonProperty("results")
	protected ArrayList<Agile> agileResults;

	public ArrayList<Agile> getAgileResults() {
		return agileResults;
	}

	public void setAgileResults(ArrayList<Agile> agileResults) {
		this.agileResults = agileResults;
	}

}
