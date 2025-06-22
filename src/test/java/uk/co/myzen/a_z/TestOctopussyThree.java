package uk.co.myzen.a_z;

import java.time.LocalTime;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestOctopussyThree {

	private static Octopussy instance = null;

	@BeforeClass
	public static void init() {

		instance = Octopussy.getInstance();
	}

	@Ignore
	@Test
	public void test01() {

		WatchSlotPauseHelperThread helperThread = new WatchSlotPauseHelperThread(instance, 45); // stop at minute
																								// 29 or 59 tick
																								// every 15 secs

		helperThread.start();

		long millis = 300000l;

		try {
			helperThread.join(millis); // Waits at most millis milliseconds for this thread to die.

			if (helperThread.isAlive()) {

				instance.logErrTime("Forcing monitoring interrupt " + millis + "ms before next slot starts");

				helperThread.interrupt();
			}

		} catch (InterruptedException e) {

			e.printStackTrace();
		}

	}

	@Ignore
	@Test
	public void test02() {

		WatchSlotTimerBaseThread helperThread = new WatchSlotTimerBaseThread(instance, 15); // stop at minute
																							// 29 or 59 tick
																							// every 15 secs

		helperThread.start();

		long millis = 300000l;

		try {
			helperThread.join(millis); // Waits at most millis milliseconds for this thread to die.

			if (helperThread.isAlive()) {

				instance.logErrTime("Forcing monitoring interrupt " + millis + "ms before next slot starts");

				helperThread.interrupt();
			}

		} catch (InterruptedException e) {

			e.printStackTrace();
		}

	}

	@Ignore
	@Test
	public void test03() {

		LocalTime lt = LocalTime.now().plusMinutes(1).plusSeconds(13);

		String expiry = lt.toString();

		String expiryHHMM = expiry.substring(0, 5);

		WatchSlotTimerBaseThread helperThread = new WatchSlotTimerBaseThread(instance, 3, expiryHHMM, 29);

		long millis = (helperThread.sFinishAt - LocalTime.now().toSecondOfDay()) * 1000 + 60000;

		helperThread.start();

		try {
			helperThread.join(millis); // Waits at most millis milliseconds for this thread to die.

			if (helperThread.isAlive()) {

				instance.logErrTime("Forcing monitoring interrupt " + millis + "ms before next slot starts");

				helperThread.interrupt();
			}

		} catch (InterruptedException e) {

			e.printStackTrace();
		}

	}
}
