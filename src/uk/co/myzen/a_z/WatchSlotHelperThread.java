package uk.co.myzen.a_z;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WatchSlotHelperThread extends Thread implements Runnable {

	private final int runTimeoutMinutes;
	private final int scheduleIndex;
	private final int limitPercent;

	private final String expiryTime;

	private final IOctopus i;

	protected WatchSlotHelperThread(IOctopus i, int runTimeoutMinutes, int scheduleIndex, int limitPercent) {

		this.i = i;

		this.runTimeoutMinutes = runTimeoutMinutes;

		this.scheduleIndex = scheduleIndex;

		this.limitPercent = limitPercent;

		this.expiryTime = Octopussy.schedule[scheduleIndex];
	}

	@Override
	public void run() {

		final long millisTimeout = (runTimeoutMinutes * 60000) + System.currentTimeMillis();

		Thread.currentThread().setName("WatchSlotHelperThread");

		i.logErrTime("Running for Slot" + (1 + scheduleIndex));

		DateTimeFormatter formatter24HourClock = Octopussy.formatter24HourClock;

		int prevBatLev = 0;

		float prevTemperature = 0;

		String now24HrClock = null;

		int reason = 0;

		do { // repeat every 40 seconds or so

			Integer batteryLevel = null;

			Float temperatureDegreesC = null;

			try {

				Thread.sleep(20000L);

				temperatureDegreesC = i.execReadTemperature();

				Thread.sleep(20000L);

				batteryLevel = i.execReadBattery();

			} catch (InterruptedException e) {

				reason = 1;
				i.logErrTime("InterruptException");
				break; // get out of run() asap
			}

			if (System.currentTimeMillis() >= millisTimeout) {

				reason = 2;
				i.logErrTime("Slot runTimeoutMinutes:" + runTimeoutMinutes);
				break;
			}

			if (null == temperatureDegreesC || null == batteryLevel) {

				reason = 3;
				i.logErrTime("Bat: or Temp: cannot be read");
				break;
			}

			if (batteryLevel.intValue() != prevBatLev || temperatureDegreesC.floatValue() != prevTemperature) {

				if (batteryLevel.intValue() != prevBatLev) {

					prevBatLev = batteryLevel.intValue();
				}

				if (temperatureDegreesC.floatValue() != prevTemperature) {

					prevTemperature = temperatureDegreesC.floatValue();
				}

				i.logErrTime("Bat:" + prevBatLev + "% Temp:" + prevTemperature + "°C");
			}

			now24HrClock = LocalDateTime.now().format(formatter24HourClock);

			if (prevBatLev >= limitPercent) {

				reason = -1;
			}

		} while (0 != expiryTime.compareTo(now24HrClock) && prevBatLev < limitPercent);

		if (reason < 1) {

			i.resetSlot(scheduleIndex, expiryTime);
		}

		i.logErrTime("Slot" + (1 + scheduleIndex) + " charging finished. Reason:" + reason);
	}
}
