package uk.co.myzen.a_z;

public class SlotCost {

	private String simpleTimeStamp;

	private Long epochSecond;

	private String slotStartTime24hr;

	private String slotEndTime24hr;

	private Boolean isMinimumImportPrice;

	private Boolean isMaximumExportPrice;

	private Float importPrice;

	private Float exportPrice;

	public String getSimpleTimeStamp() {
		return simpleTimeStamp;
	}

	public void setSimpleTimeStamp(String simpleTimeStamp) {
		this.simpleTimeStamp = simpleTimeStamp;
	}

	public Float getImportPrice() {
		return importPrice;
	}

	public void setImportPrice(Float importPrice) {
		this.importPrice = importPrice;
	}

	public Float getExportPrice() {
		return exportPrice;
	}

	public void setExportPrice(Float exportPrice) {
		this.exportPrice = exportPrice;
	}

	public Long getEpochSecond() {
		return epochSecond;
	}

	public void setEpochSecond(Long epochSecond) {
		this.epochSecond = epochSecond;
	}

	public Boolean getIsMinimumImportPrice() {
		return isMinimumImportPrice;
	}

	public void setIsMinimumImportPrice(Boolean isMinimumImportPrice) {
		this.isMinimumImportPrice = isMinimumImportPrice;
	}

	public Boolean getIsMaximumExportPrice() {
		return isMaximumExportPrice;
	}

	public void setIsMaximumExportPrice(Boolean isMaximumExportPrice) {
		this.isMaximumExportPrice = isMaximumExportPrice;
	}

	public String getSlotEndTime24hr() {
		return slotEndTime24hr;
	}

	public void setSlotEndTime24hr(String slotEndTime24hr) {
		this.slotEndTime24hr = slotEndTime24hr;
	}

	public String getSlotStartTime24hr() {
		return slotStartTime24hr;
	}

	public void setSlotStartTime24hr(String slotStartTime24hr) {
		this.slotStartTime24hr = slotStartTime24hr;
	}

}
