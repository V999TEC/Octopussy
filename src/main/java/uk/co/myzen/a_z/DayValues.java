package uk.co.myzen.a_z;

public class DayValues {

	private Integer weekOfYear;

	private String dayOfWeek;

	private int slotCount;

	private Float highestExportPrice;

	private float dailyExport;

	private Float dailyExportPrice;

	private Float lowestImportPrice;

	private float dailyImport;

	private Float dailyImportPrice;

	public float getDailyImport() {

		return dailyImport;
	}

	public void setDailyImport(float dailyImport) {

		this.dailyImport = dailyImport;
	}

	public Float getDailyImportPrice() {
		return dailyImportPrice;
	}

	public void setDailyImportPrice(Float dailyPrice) {
		this.dailyImportPrice = dailyPrice;
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

	public Float getLowestImportPrice() {
		return lowestImportPrice;
	}

	public void setLowestImportPrice(Float lowestPrice) {
		this.lowestImportPrice = lowestPrice;
	}

	public Integer getWeekOfYear() {
		return weekOfYear;
	}

	public void setWeekOfYear(Integer weekOfYear) {
		this.weekOfYear = weekOfYear;
	}

	public Float getHighestExportPrice() {
		return highestExportPrice;
	}

	public void setHighestExportPrice(Float highestExportPrice) {
		this.highestExportPrice = highestExportPrice;
	}

	public float getDailyExport() {
		return dailyExport;
	}

	public void setDailyExport(float dailyExport) {
		this.dailyExport = dailyExport;
	}

	public Float getDailyExportPrice() {
		return dailyExportPrice;
	}

	public void setDailyExportPrice(Float dailyExportPrice) {
		this.dailyExportPrice = dailyExportPrice;
	}
}
