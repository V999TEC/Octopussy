package uk.co.myzen.a_z;

import uk.co.myzen.a_z.json.ChargeDischarge;

public interface IOctopus {

	public String logErrTime(String text);

	public Float execReadTemperature();

	public Integer execReadBatteryPercent();

	public ChargeDischarge execReadChargeDischarge();

	public void resetChargingSlot(int scheduleIndex, String startTime, String expiryTime, int socMaxPercent);

	public void batteryChargePower(int power);

	public void resetDischargingSlot(int scheduleIndex, String startTime, String expiryTime, int socMinPercent);

	public void batteryDischargePower(int power);
}
