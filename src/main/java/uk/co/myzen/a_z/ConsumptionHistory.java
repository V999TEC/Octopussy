package uk.co.myzen.a_z;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class ConsumptionHistory {

	private Float consumption;

	private OffsetDateTime from;

	private OffsetDateTime to;

	private Float priceImportedOrExported;

	private Float costImportedOrExported;

	private final DateTimeFormatter defaultDateTimeFormatter;

	public ConsumptionHistory(DateTimeFormatter defaultDateTimeFormatter) {

		this.defaultDateTimeFormatter = defaultDateTimeFormatter;
	}

	public ConsumptionHistory() {

		defaultDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	}

	public String toString() {

		String entry = String.format("%6.3f", getConsumption()) + ", " + getFrom().format(defaultDateTimeFormatter)
				+ ", " + getTo().format(defaultDateTimeFormatter) +

				(null == getPriceImportedOrExported() ? "" :

						", " + String.format("%5.2f", getPriceImportedOrExported()) + ", "
								+ String.format("%6.3f", getCostImportedOrExported()));
		return entry;
	}

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
