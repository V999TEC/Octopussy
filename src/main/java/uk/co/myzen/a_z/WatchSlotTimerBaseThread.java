package uk.co.myzen.a_z;

import java.time.LocalTime;

public class WatchSlotTimerBaseThread extends Thread {

	protected final IOctopus i;

	protected final int sPeriod;

	protected final int sFinishAt;

	boolean interrupt = false;

	private int count = 0;

	// The purpose of the class is to run for <up to> 29 minutes
	// or until the clock time is HH:29:00 or HH:59:00
	// sPeriod should be a number of seconds around 15
	// each sPeriod a tick event occurs
	// the boolean tick(int) method can terminate the thread early by returning
	// false
	//
	// The timing is specifically designed to allow a tick task to take time without
	// upsetting the scheduling of subsequent ticks, which will end at HH:29:0n or
	// HH:59:0n
	// Depending on when the thread is started, the first tick will be less than
	// sPeriod as it attempts to synchronise so there are a certain number of ticks
	//
	// The ticks will count down to zero, thus indicating to a task which implements
	// its own tick, that the last tick has occurred.

	public WatchSlotTimerBaseThread(IOctopus i, int sPeriod) {

		this(i, sPeriod, null, 0);
	}

	public WatchSlotTimerBaseThread(IOctopus i, int sPeriod, String expiryTimeHHMM) {

		this(i, sPeriod, expiryTimeHHMM, 0);
	}

	public WatchSlotTimerBaseThread(IOctopus i, int sPeriod, String expiryTimeHHMM, int seconds) {

		this.i = i;

		this.sPeriod = sPeriod;

		int hh = -1;
		int mm = -1;

		if (null != expiryTimeHHMM) {

			hh = Integer.parseInt(expiryTimeHHMM.substring(0, 2));
			mm = Integer.parseInt(expiryTimeHHMM.substring(3));
		}

		int ss = seconds;

		LocalTime lt = LocalTime.now();

		if (ss <= 0) {

			if (mm < 0) {

				sFinishAt = lt.getMinute() < 29 ? lt.withMinute(29).withSecond(0).toSecondOfDay()
						: lt.withMinute(59).withSecond(0).withNano(0).toSecondOfDay();
			} else {

				sFinishAt = lt.withHour(hh).withMinute(mm).withSecond(0).withNano(0).toSecondOfDay();
			}

		} else {

			sFinishAt = lt.withHour(hh).withMinute(mm).withSecond(ss).withNano(0).toSecondOfDay();
		}
	}

	protected void monitoringEvent(String reason) {

		count++;

		i.logErrTime("PB " + "(" + count + ")" + reason);
	}

	// Return false to stop further tick events & terminate the thread early
	// The class that extends WatchSlotTimerBaseThread is expected to provide its
	// own tick implementation, but it must last under the sPeriod time to execute

	protected boolean tick(int ticksRemaining) {

		LocalTime lt = LocalTime.now();

		long randomNum = (int) (Math.random() * 500 * sPeriod);

		monitoringEvent(ticksRemaining + "\t" + lt.toSecondOfDay() + "\t" + lt.toString() + "\t" + lt.getNano() + "\t"
				+ lt.getNano() / 1000000 + "\trand:" + randomNum);

		try {
			Thread.sleep(randomNum);

		} catch (InterruptedException e) {

			e.printStackTrace();

			return false;
		}

		return true;
	}

	@Override
	public void run() {

		LocalTime lt = LocalTime.now();

		int secOfDayNow = lt.toSecondOfDay();

		// how many seconds to termination?

		int secondsToRun = sFinishAt - secOfDayNow;

		int numberOfTicks = secondsToRun / sPeriod;

		int sRemainder = secondsToRun % sPeriod;

		// first delay will use up the remainder

		long millis = 1000 * sRemainder - (lt.getNano() / 1000000);

		int secs;

		int nextTickTime = secOfDayNow + sRemainder;

		try {

			do {
				// next event should be at

				nextTickTime += sPeriod;

				if (millis < 0) {

					monitoringEvent("Warning ms:" + millis + " in WatchSlotTimerBaseThread.run()");

				} else {

					Thread.sleep(millis);
				}

				if (!tick(numberOfTicks--)) { // assume tick takes less time than period

					break;
				}

				// what is the difference in mS between now and the next tick?

				lt = LocalTime.now();

				secOfDayNow = lt.toSecondOfDay();

				// how many seconds between now and next tick time?

				secs = nextTickTime - secOfDayNow;

				long mS = lt.getNano() / 1000000;

				millis = (secs * 1000) - mS;

			} while (secOfDayNow < sFinishAt);

		} catch (InterruptedException e) {

			interrupt = true;
		}

		termination();

		// now thread will evaporate
	}

	protected void termination() {

		int sRemaining = sFinishAt - LocalTime.now().toSecondOfDay();

		monitoringEvent(sRemaining <= 0 ? "Normal Timeout"
				: (interrupt ? "Interrupted " : "Assume break ") + sRemaining + " secs remained");
	}
}
