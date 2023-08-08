package uk.co.myzen.a_z.json;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class V1GasConsumption {

	@JsonProperty("count")
	private Integer count;

	@JsonProperty("next")
	private Integer next;

	@JsonProperty("previous")
	private Integer previous;

//	@JsonProperty("results")
	private ArrayList<Object> results;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getNext() {
		return next;
	}

	public void setNext(Integer next) {
		this.next = next;
	}

	public Integer getPrevious() {
		return previous;
	}

	public void setPrevious(Integer previous) {
		this.previous = previous;
	}

	public ArrayList<Object> getResults() {
		return results;
	}

	public void setResults(ArrayList<Object> results) {
		this.results = results;
	}

}
