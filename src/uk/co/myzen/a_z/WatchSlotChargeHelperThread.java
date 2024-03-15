package uk.co.myzen.a_z;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WatchSlotChargeHelperThread extends Thread implements Runnable {

	private final int runTimeoutMinutes;
	private final int scheduleIndex;
	private final int maxPercent;
	private final int minPercent;
	private final int defaultChargeRate;

	private final String expiryTime;

	private final IOctopus i;

	private final String slotN;

	protected WatchSlotChargeHelperThread(IOctopus i, int runTimeoutMinutes, int scheduleIndex, int maxPercent,
			int minPercent, int defaultChargeRate) {

		this.i = i;

		this.runTimeoutMinutes = runTimeoutMinutes;

		this.scheduleIndex = scheduleIndex;

		this.maxPercent = maxPercent;
		this.minPercent = minPercent;

		this.defaultChargeRate = defaultChargeRate;

		this.expiryTime = Octopussy.schedule[scheduleIndex];

		slotN = "Slot" + String.valueOf(1 + scheduleIndex) + " ";
	}

	@Override
	public void run() {

		final long millisTimeout = (runTimeoutMinutes * 60000) + System.currentTimeMillis();

		Thread.currentThread().setName("Watch");

		i.logErrTime(slotN + "Monitoring starts");

		DateTimeFormatter formatter24HourClock = Octopussy.formatter24HourClock;

		int prevBatLev = 0;

		float prevTemperature = 0;

		float prevChargeUnits = 0;

		String now24HrClock = null;

		int reason = 0;

		boolean chargeRestarted = false;

		do { // repeat loop every 40 seconds or so

			Integer batteryLevel = null;

			Float temperatureDegreesC = null;

			Float chargeUnits = null;

			try {

				Thread.sleep(10000L);

				temperatureDegreesC = i.execReadTemperature();

				Thread.sleep(10000L);

				batteryLevel = i.execReadBattery();

				Thread.sleep(10000L);

				chargeUnits = i.execReadCharge();

			} catch (InterruptedException e) {

				reason = 2;
				i.logErrTime(slotN + "InterruptedException");
				break; // get out of run() asap
			}

			if (System.currentTimeMillis() >= millisTimeout) {

				reason = 1;
				i.logErrTime(slotN + "Hit runTimeoutMinutes:" + runTimeoutMinutes);
				break;
			}

			if (prevBatLev >= maxPercent) {

				i.logErrTime(slotN + "WARNING: Battery >= " + maxPercent + "% terminating charge now");

				reason = -1;
				break;
			}

			if (null == temperatureDegreesC || null == batteryLevel || null == chargeUnits) {

				i.logErrTime(slotN + "Battery, Temperature or Charge cannot be read");

			} else {

				if (batteryLevel.intValue() != prevBatLev || temperatureDegreesC.floatValue() != prevTemperature
						|| chargeUnits != prevChargeUnits) {

					boolean log = false;

					if (batteryLevel.intValue() != prevBatLev) {

						prevBatLev = batteryLevel.intValue();

						log = true;
					}

					if (temperatureDegreesC.intValue() != Float.valueOf(prevTemperature).intValue()) {

						log = true;

						prevTemperature = temperatureDegreesC.floatValue();
					}

					if (chargeUnits.intValue() != Float.valueOf(prevChargeUnits).intValue()) {

						prevChargeUnits = chargeUnits.floatValue();

						log = true;
					}

					if (log) {

						i.logErrTime(slotN + "Battery:" + prevBatLev + "% Temperature:" + prevTemperature + "°C Charge:"
								+ chargeUnits + "kWhr");
					}
				}
			}

			now24HrClock = LocalDateTime.now().format(formatter24HourClock);

			if (batteryLevel < minPercent) {

				if (!chargeRestarted) {

					i.logErrTime(slotN + "WARNING: Battery < " + minPercent + "% expedite charging now at "
							+ defaultChargeRate + " watts");

					i.resetChargingPower(defaultChargeRate);

					i.resetSlot(scheduleIndex, now24HrClock, expiryTime, maxPercent);

					chargeRestarted = true;
				}
			}

		} while (0 != expiryTime.compareTo(now24HrClock));

		if (reason < 2 || chargeRestarted)

		{

			i.resetSlot(scheduleIndex, expiryTime, expiryTime, 100);
		}

		i.logErrTime(slotN + "Monitoring finished. Reason:" + reason + " Restarted:" + chargeRestarted);
	}
}
