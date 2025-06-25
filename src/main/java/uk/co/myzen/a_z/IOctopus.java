package uk.co.myzen.a_z;

import uk.co.myzen.a_z.json.ChargeDischarge;

public interface IOctopus {

	public int logErrTime(String text);

	public Float execReadTemperature();

	public Integer execReadBatteryPercent();

	public String[] execReadBatteryPercentAndPower();

	public ChargeDischarge execReadChargeDischarge();

	public String resetChargingSlot(int scheduleIndex, String startTime, String expiryTime, int socMaxPercent,
			boolean log);

	public void resetChargingSlot(int scheduleIndex, String startTime, String expiryTime, int socMaxPercent);

	public String resetDischargingSlot(int scheduleIndex, String startTime, String expiryTime, int socMinPercent,
			boolean log);

	public void resetDischargingSlot(int scheduleIndex, String startTime, String expiryTime, int socMinPercent);

	public void batteryChargePower(int power);

	public void batteryDischargePower(int power);

	public void batteryNotPaused();

	public void batteryPauseCharge();

	public void batteryPauseDischarge();

	public void batteryPauseChargeAndDischarge();

}
