package uk.co.myzen.a_z.json;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class V1GridSupplyPoints extends V1Results {

	@JsonProperty("results")
	protected ArrayList<V1GSP> pointResults;

	public ArrayList<V1GSP> getPointResults() {
		return pointResults;
	}

	public void setPointResults(ArrayList<V1GSP> pointResults) {
		this.pointResults = pointResults;
	}
}
