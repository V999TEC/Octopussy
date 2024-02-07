package uk.co.myzen.a_z;

public interface IOctopus {

	public String logErrTime(String text);

	public void resetSlot(int scheduleIndex, String expiryTime);

	public Float execReadTemperature();

	public Integer execReadBattery();
}
