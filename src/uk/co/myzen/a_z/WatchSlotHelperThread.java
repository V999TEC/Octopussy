package uk.co.myzen.a_z;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WatchSlotHelperThread extends Thread implements Runnable {

	private final int runTimeoutMinutes;
	private final int scheduleIndex;
	private final int limitPercent;

	private final String expiryTime;

	protected WatchSlotHelperThread(int runTimeoutMinutes, int scheduleIndex, int limitPercent) {

		this.runTimeoutMinutes = runTimeoutMinutes;

		this.scheduleIndex = scheduleIndex;

		this.limitPercent = limitPercent;

		this.expiryTime = Octopussy.schedule[scheduleIndex];
	}

	@Override
	public void run() {

		final long millisTimeout = (runTimeoutMinutes * 60000) + System.currentTimeMillis();

		Thread.currentThread().setName("WatchSlotHelperThread");

		Octopussy.logErrTime("Running for Slot" + (1 + scheduleIndex));

		DateTimeFormatter formatter24HourClock = Octopussy.formatter24HourClock;

		int prevBatLev = 0;

		float prevTemperature = 0;

		String now24HrClock = null;

		int flag = 0;

		do { // repeat every 20 seconds or so

			Integer batteryLevel = null;

			Float temperatureDegreesC = null;

			try {

				Thread.sleep(10000L);

				temperatureDegreesC = Octopussy.execReadTemperature();

				Thread.sleep(10000L);

				batteryLevel = Octopussy.execReadBattery();

			} catch (InterruptedException e) {

				flag = 1;
				Octopussy.logErrTime("InterruptException");
				break; // get out of run() asap
			}

			if (System.currentTimeMillis() >= millisTimeout) {

				flag = 2;
				Octopussy.logErrTime("Slot runTimeoutMinutes:" + runTimeoutMinutes);
				break;
			}

			if (null == temperatureDegreesC || null == batteryLevel) {

				flag = 3;
				Octopussy.logErrTime("Bat: or Temp: cannot be read");
				break;
			}

			if (batteryLevel.intValue() != prevBatLev || temperatureDegreesC.floatValue() != prevTemperature) {

				if (batteryLevel.intValue() != prevBatLev) {

					prevBatLev = batteryLevel.intValue();
				}

				if (temperatureDegreesC.floatValue() != prevTemperature) {

					prevTemperature = temperatureDegreesC.floatValue();
				}

				Octopussy.logErrTime("Bat:" + prevBatLev + "% Temp:" + prevTemperature + "°C");
			}

			now24HrClock = LocalDateTime.now().format(formatter24HourClock);

			if (prevBatLev >= limitPercent) {

				flag = -1;
			}

		} while (0 != expiryTime.compareTo(now24HrClock) && prevBatLev < limitPercent);

		if (flag < 1) {

			Octopussy.resetSlot(scheduleIndex, expiryTime);
		}

		Octopussy.logErrTime("Slot" + (1 + scheduleIndex) + " charging finished. Flag:" + flag);
	}
}
