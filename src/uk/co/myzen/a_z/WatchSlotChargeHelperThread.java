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

		Thread.currentThread().setName("WatchSlotChargeHelperThread");

		i.logErrTime(slotN + "monitoring starts");

		DateTimeFormatter formatter24HourClock = Octopussy.formatter24HourClock;

		int prevBatLev = 0;

		float prevTemperature = 0;

		String now24HrClock = null;

		int reason = 0;

		boolean chargeRestarted = false;

		do { // repeat loop every 40 seconds or so

			Integer batteryLevel = null;

			Float temperatureDegreesC = null;

			try {

				Thread.sleep(20000L);

				temperatureDegreesC = i.execReadTemperature();

				Thread.sleep(20000L);

				batteryLevel = i.execReadBattery();

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

				reason = -1;
				break;
			}

			if (null == temperatureDegreesC || null == batteryLevel) {

				i.logErrTime(slotN + "Bat: or Temp: cannot be read");

			} else {

				if (batteryLevel.intValue() != prevBatLev || temperatureDegreesC.floatValue() != prevTemperature) {

					boolean log = false;

					if (batteryLevel.intValue() != prevBatLev) {

						prevBatLev = batteryLevel.intValue();

						log = true;
					}

					if (temperatureDegreesC.floatValue() != prevTemperature) {

						// only log diagnostic if units change - ignore decimals

						if (Float.valueOf(prevTemperature).intValue() != temperatureDegreesC.intValue()) {

							log = true;
						}

						prevTemperature = temperatureDegreesC.floatValue();
					}

					if (log) {

						i.logErrTime(slotN + "Bat:" + prevBatLev + "% Temp:" + prevTemperature + "°C");
					}
				}
			}

			now24HrClock = LocalDateTime.now().format(formatter24HourClock);

			if (batteryLevel < minPercent) {

				if (!chargeRestarted) {

					i.logErrTime(slotN + "Battery < " + minPercent + "% restart charging at " + defaultChargeRate
							+ " watts");

					i.resetChargingPower(defaultChargeRate);

					i.resetSlot(scheduleIndex, now24HrClock, expiryTime, maxPercent);

					chargeRestarted = true;
				}
			}

		} while (0 != expiryTime.compareTo(now24HrClock));

		if (reason < 2 || chargeRestarted) {

			i.resetSlot(scheduleIndex, expiryTime, expiryTime, 100);
		}

		i.logErrTime(slotN + "monitoring finished. Reason:" + reason + " Restarted:" + chargeRestarted);
	}
}
