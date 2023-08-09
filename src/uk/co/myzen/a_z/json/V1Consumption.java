package uk.co.myzen.a_z.json;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;

public class V1Consumption {

	@JsonProperty("count")
	private Integer count;

	@JsonProperty("next")
	private String next;

	@JsonProperty("previous")
	private String previous;

	@JsonProperty("results")
	protected ArrayList<Object> results;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getNext() {
		return next;
	}

	public void setNext(String next) {
		this.next = next;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	protected ArrayList<Object> getResults() {
		return results;
	}

	protected void setResults(ArrayList<Object> results) {
		this.results = results;
	}
}
