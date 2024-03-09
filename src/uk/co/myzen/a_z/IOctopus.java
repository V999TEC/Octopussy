package uk.co.myzen.a_z;

public interface IOctopus {

	public String logErrTime(String text);

	public Float execReadTemperature();

	public Integer execReadBattery();

	public void resetSlot(int scheduleIndex, String startTime, String expiryTime, Integer maxPercent);

	public void resetChargingPower(int power);
}
