package uk.co.myzen.a_z;

public class DayValues {

	private float dailyConsumption;

	private long startEpochMilli;

	private long endEpochMilli;

	private float dailyPrice;

	public float getDailyConsumption() {
		return dailyConsumption;
	}

	public void setDailyConsumption(float dailyConsumption) {
		this.dailyConsumption = dailyConsumption;
	}

	public long getStartEpochMilli() {
		return startEpochMilli;
	}

	public void setStartEpochMilli(long startEpochMilli) {
		this.startEpochMilli = startEpochMilli;
	}

	public long getEndEpochMilli() {
		return endEpochMilli;
	}

	public void setEndEpochMilli(long endEpochMilli) {
		this.endEpochMilli = endEpochMilli;
	}

	public float getDailyPrice() {
		return dailyPrice;
	}

	public void setDailyPrice(float dailyPrice) {
		this.dailyPrice = dailyPrice;
	}
}
