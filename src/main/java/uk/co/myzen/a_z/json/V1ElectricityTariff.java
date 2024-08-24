package uk.co.myzen.a_z.json;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class V1ElectricityTariff extends V1Results {

	@JsonProperty("results")
	protected ArrayList<Tariff> tariffResults;

	public ArrayList<Tariff> getTariffResults() {
		return tariffResults;
	}

	public void setTariffResults(ArrayList<Tariff> tariffResults) {
		this.tariffResults = tariffResults;
	}

}
