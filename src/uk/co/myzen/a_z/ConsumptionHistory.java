package uk.co.myzen.a_z;

import java.time.OffsetDateTime;

public class ConsumptionHistory {

	private Float consumption;

	private OffsetDateTime from;

	private OffsetDateTime to;

	private Float price; // typically the slot price

	private Float cost; // typically the cost of the energy consumed

	public Float getConsumption() {
		return consumption;
	}

	public void setConsumption(Float consumption) {
		this.consumption = consumption;
	}

	public OffsetDateTime getFrom() {
		return from;
	}

	public void setFrom(OffsetDateTime from) {
		this.from = from;
	}

	public OffsetDateTime getTo() {
		return to;
	}

	public void setTo(OffsetDateTime to) {
		this.to = to;
	}

	public Float getPrice() {
		return price;
	}

	public void setPrice(Float price) {
		this.price = price;
	}

	public Float getCost() {
		return cost;
	}

	public void setCost(Float cost) {
		this.cost = cost;
	}
}
