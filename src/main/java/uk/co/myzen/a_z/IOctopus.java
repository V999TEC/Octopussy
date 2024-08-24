package uk.co.myzen.a_z;

import uk.co.myzen.a_z.json.ChargeDischarge;

public interface IOctopus {

	public String logErrTime(String text);

	public Float execReadTemperature();

	public Integer execReadBatteryPercent();

	public ChargeDischarge execReadChargeDischarge();

	public void resetSlot(int scheduleIndex, String startTime, String expiryTime, Integer maxPercent);

	public void resetChargingPower(int power);
}
