package uk.co.myzen.a_z;

import java.time.OffsetDateTime;

public class ConsumptionHistory {

	private Float consumption;

	private OffsetDateTime from;

	private OffsetDateTime to;

	private Float priceImportedOrExported;

	private Float costImportedOrExported;

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

	public Float getPriceImportedOrExported() {
		return priceImportedOrExported;
	}

	public void setPriceImportedOrExported(Float price) {
		this.priceImportedOrExported = price;
	}

	public Float getCostImportedOrExported() {
		return costImportedOrExported;
	}

	public void setCostImportedOrExported(Float cost) {
		this.costImportedOrExported = cost;
	}

}
