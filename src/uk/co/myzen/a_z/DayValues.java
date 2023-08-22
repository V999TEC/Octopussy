package uk.co.myzen.a_z;

public class DayValues {

	private Float lowestPrice;

	private String dayOfWeek;

	private int slotCount;

	private float dailyConsumption;

	private float dailyPrice;

	public float getDailyConsumption() {
		return dailyConsumption;
	}

	public void setDailyConsumption(float dailyConsumption) {
		this.dailyConsumption = dailyConsumption;
	}

	public float getDailyPrice() {
		return dailyPrice;
	}

	public void setDailyPrice(float dailyPrice) {
		this.dailyPrice = dailyPrice;
	}

	public int getSlotCount() {
		return slotCount;
	}

	public void setSlotCount(int slotCount) {
		this.slotCount = slotCount;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public Float getLowestPrice() {
		return lowestPrice;
	}

	public void setLowestPrice(Float lowestPrice) {
		this.lowestPrice = lowestPrice;
	}
}
