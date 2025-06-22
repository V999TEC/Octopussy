package uk.co.myzen.a_z;

import uk.co.myzen.a_z.json.ChargeDischarge;

public class WatchSlotPauseHelperThread extends WatchSlotTimerBaseThread {

	public WatchSlotPauseHelperThread(IOctopus i, int sPeriod) {

		super(i, sPeriod);
	}

	public void run() {

		Thread currentThread = Thread.currentThread();

		currentThread.setName("PauseBat-" + Octopussy.slotNumber);

		super.run();
	}

	protected boolean tick(int ticksRemaining) {

		int task = ticksRemaining % 3; // i.e., number 0 to 2

		String info = "";

		switch (task) {

		case 2:

			ChargeDischarge chargeAndDischargeUnits = i.execReadChargeDischarge();

			Float chargeUnits = chargeAndDischargeUnits.getCharge();

			Float dischargeUnits = chargeAndDischargeUnits.getDischarge();

			StringBuffer sb = new StringBuffer();

			if (null != chargeUnits) {

				sb.append("Cha:");
				sb.append(chargeUnits);
				sb.append(' ');
			}

			if (null != dischargeUnits) {
				sb.append("Dis:");
				sb.append(dischargeUnits);
				sb.append(' ');
			}

			info = sb.toString();
			break;

		case 1:

			Float temperatureDegreesC = i.execReadTemperature();

			if (null != temperatureDegreesC) {

				info = "Tmp:" + temperatureDegreesC.toString() + "%";
			}

			break;

		default:

			Integer batteryLevel = i.execReadBatteryPercent();

			if (null != batteryLevel) {

				info = "Bat:" + batteryLevel.toString() + "%";
			}
			break;
		}

		monitoringEvent("tick " + ticksRemaining + "\t" + info);

		return ticksRemaining > 0;
	}

}
