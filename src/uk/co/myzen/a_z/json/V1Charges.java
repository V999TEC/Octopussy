package uk.co.myzen.a_z.json;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class V1Charges extends V1Results {

	@JsonProperty("results")
	private List<Prices> priceResults;

	public List<Prices> getPriceResults() {
		return priceResults;
	}

	public void setPriceResults(List<Prices> priceResults) {
		this.priceResults = priceResults;
	}

}
