package uk.co.myzen.a_z;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestOctopussyTwo {

	private final static String zone = "Europe/London";

	private final static String battery = "java -jar icarus.jar ./SwindonIcarus.properties inverter meter today battery";

	private final static String setting = "java -jar icarus.jar ./SwindonIcarus.properties inverter setting %1 %2 %3";

	private final static String macro = "java -jar icarus.jar ./SwindonIcarus.properties inverter macro %1 %2 %3 %4";

	private final static String temperature = "java -jar icarus.jar ./SwindonIcarus.properties inverter system inverter temperature";

	private final static String percent = "java -jar icarus.jar ./SwindonIcarus.properties inverter system battery percent";

	private static Octopussy instance = null;

	@BeforeClass
	public static void init() {

		instance = Octopussy.getInstance();

		Octopussy.ourZoneId = ZoneId.of(zone);

		Octopussy.battery = battery;

		Octopussy.macro = macro;

		Octopussy.setting = setting;

		Octopussy.temperature = temperature;

		Octopussy.percent = percent;

		Octopussy.execute = true;
	}

	@Ignore("disabled")
	@Test
	public void test01() {

		ZonedDateTime ourTimeNow = Octopussy.now.atZone(Octopussy.ourZoneId);

		String timestamp = ourTimeNow.toString().substring(0, 19);

		String hh_mm = timestamp.substring(11, 16);

		String currentSlotEndTime = null;

		for (int index = 0; index < Octopussy.slotStartTimes.length; index++) {

			String nextSlot = Octopussy.slotStartTimes[index];

			if (hh_mm.compareTo(nextSlot) < 0) {

				currentSlotEndTime = Octopussy.convertHHmmMinus1Minute(nextSlot);

				break; // slot after current time
			}
		}

		int minuteNow = ourTimeNow.getMinute();

		int runTimeoutMinutes = minuteNow % 30;

		int scheduleIndex = 0;
		int minPercent = 53;

		String[] schedule = { currentSlotEndTime };

		WatchSlotDischargeHelperThread dischargeMonitorThread = new WatchSlotDischargeHelperThread(instance, schedule,
				scheduleIndex, minPercent, 100, 1234, 0);

		dischargeMonitorThread.start();

		instance.acChargeEnable(false);

		instance.enableDcDischarge(true);

		long millis = 1000 * ((60 * runTimeoutMinutes) - 10l); // knock off 10 seconds

		try {

			dischargeMonitorThread.join(millis);

		} catch (InterruptedException e) {

			e.printStackTrace();

		}

		instance.acChargeEnable(true);
	}

	@Test
	public void test02() {

		String test1 = WatchSlotChargeHelperThread.SN(1, false);
		String test2 = WatchSlotChargeHelperThread.SN(2, true);

		char longSolidusOverlay = '̸';

		String test3 = WatchSlotChargeHelperThread.SN(3, longSolidusOverlay);

		System.out.println("def" + test1 + "abc");
		System.out.println("abc" + test2 + "def");
		System.out.println("xyz" + test3 + "abc");

	}
}
