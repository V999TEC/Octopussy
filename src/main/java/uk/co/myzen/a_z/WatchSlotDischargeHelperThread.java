package uk.co.myzen.a_z;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import uk.co.myzen.a_z.json.ChargeDischarge;

public class WatchSlotDischargeHelperThread extends Thread implements Runnable {

	private static final char longStrokeOverlay = '̶';

	private final int runTimeoutMinutes;
	private final int scheduleIndex;
	private final int socMinPercent;
	private final int power;

//	private final String expiryTime;

	private final String expiryTimeHHMM;

	private final int secOfDayTimeOut;

	private final IOctopus i;

	private final String slotN;

	protected static String XN(int zeroBasedIndex) {

		char[] regular = { 'X', String.valueOf(1 + zeroBasedIndex).charAt(0), ' ' };

		return String.valueOf(regular);
	}

	protected static String XN(int zeroBasedIndex, boolean cancelled) {

		return cancelled ? XN(zeroBasedIndex, longStrokeOverlay) : XN(zeroBasedIndex);
	}

	protected static String XN(int zeroBasedIndex, char overlay) {

		char[] strikeThrough = { overlay, 'X', overlay, XN(zeroBasedIndex).charAt(1), ' ' };

		return String.valueOf(strikeThrough);
	}

	protected WatchSlotDischargeHelperThread(IOctopus i, String expiryTime, int runTimeoutMinutes, int scheduleIndex,
			int socMinPercent, int power) {

		this.i = i;

		this.runTimeoutMinutes = runTimeoutMinutes;

		this.scheduleIndex = scheduleIndex;

		this.socMinPercent = socMinPercent;

		this.power = power;

//		this.expiryTime = expiryTime;

		this.expiryTimeHHMM = Octopussy.chargeSchedule[scheduleIndex];

		int hh = Integer.parseInt(expiryTimeHHMM.substring(0, 2));
		int mm = Integer.parseInt(expiryTimeHHMM.substring(3));

		secOfDayTimeOut = LocalDateTime.now().withHour(hh).withMinute(mm).toLocalTime().toSecondOfDay();

		// Implicit: the start of each 30-min slot will be roughly 29 minutes earlier
		// than schedule passed in, (which is always HH:59 or HH:29),
		// thus at HH:30 or HH:00 respectively.
		// we use runTimeOutMinutes to limit the maximum period within a 30 minute slot.
		// Thus if we start at HH:00:15 and runtimeOutMinutes is 29, we would expect to
		// terminate at HH:29:15 - thus 45 seconds before the next half-hour slot
		// starts.
		// Using runTimeInMinutes allows the start to be delayed.
		// So if ==2 then we might start at HH:02:15 but the cutoff would still be
		// HH:29:15 and if ==0 we would try to not have any delay, but in practice
		// it will be a few seconds.

		slotN = XN(scheduleIndex);
	}

	@Override
	public void run() {
		int secOfDayNow = LocalTime.now().toSecondOfDay();

//		final long millisTimeout = (runTimeoutMinutes * 60000) + System.currentTimeMillis();

		Thread currentThread = Thread.currentThread();

		String idHexString = Long.toHexString(currentThread.getId());

		currentThread.setName("Discharge-" + idHexString);

		i.logErrTime(slotN + "Monitoring starts min:" + socMinPercent + "% power: " + power + " watts");

		DateTimeFormatter formatter24HourClock = Octopussy.formatter24HourClock;

		int prevBatLev = 100;

		float prevTemperature = 0f;

		float prevChargeUnits = 0f;

		float prevDischargeUnits = 0f;

		int reason = 0;

		boolean chargeRestarted = false;

		do { // repeat loop every 45 seconds or so

			Integer batteryLevel = null;

			Float temperatureDegreesC = null;

			Float chargeUnits = null;

			Float dischargeUnits = null;

			try {

				Thread.sleep(15000L);

				temperatureDegreesC = i.execReadTemperature();

				Thread.sleep(15000L);

				batteryLevel = i.execReadBatteryPercent();

				Thread.sleep(15000L);

				ChargeDischarge chargeAndDischargeUnits = i.execReadChargeDischarge();

				if (null == chargeAndDischargeUnits || null == temperatureDegreesC || null == batteryLevel) {

					reason = -2;
					break;
				}

				chargeUnits = chargeAndDischargeUnits.getCharge();

				dischargeUnits = chargeAndDischargeUnits.getDischarge();

			} catch (InterruptedException e) {

				reason = 2;
				i.logErrTime(slotN + "InterruptedException");
				break; // get out of run() asap

			} catch (Exception e2) {

				e2.printStackTrace();

				continue; // loop
			}

			secOfDayNow = LocalTime.now().toSecondOfDay();

			if (secOfDayNow >= secOfDayTimeOut) {

				reason = 1;
				i.logErrTime(slotN + "Hit runTimeoutMinutes:" + runTimeoutMinutes);
				break;
			}

//			if (System.currentTimeMillis() >= millisTimeout) {
//
//				reason = 1;
//				i.logErrTime(slotN + "Hit runTimeoutMinutes:" + runTimeoutMinutes);
//				break;
//			}

			if (prevBatLev <= socMinPercent) {

				i.logErrTime(slotN + "WARNING: Battery <= " + socMinPercent + "% terminating discharge now");

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

						prevTemperature = temperatureDegreesC.floatValue();

						log = true;
					}

					if (chargeUnits.intValue() != Float.valueOf(prevChargeUnits).intValue()) {

						prevChargeUnits = chargeUnits.floatValue();

						log = true;
					}

					if (dischargeUnits.intValue() != Float.valueOf(prevDischargeUnits).intValue()) {

						prevDischargeUnits = dischargeUnits.floatValue();

						log = true;
					}

					if (log) {

						i.logErrTime(slotN + "Bat:" + prevBatLev + "% Tmp:" + prevTemperature + "°C Cha:" + chargeUnits
								+ " Dis:" + dischargeUnits);
					}
				}
			}

		} while (0 != expiryTimeHHMM.compareTo(LocalDateTime.now().format(formatter24HourClock)));

		if (reason < 2 || chargeRestarted) {

			i.resetDischargingSlot(scheduleIndex, expiryTimeHHMM, expiryTimeHHMM, 4);
		}

		i.logErrTime(slotN + "Monitoring finished. Reason:" + reason + " Restarted:" + chargeRestarted);
	}
}
