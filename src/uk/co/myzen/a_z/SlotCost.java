package uk.co.myzen.a_z;

public class SlotCost {

	private String simpleTimeStamp;

	private Long epochSecond;

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

}