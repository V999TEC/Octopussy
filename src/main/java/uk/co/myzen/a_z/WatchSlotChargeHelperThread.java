package uk.co.myzen.a_z;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import uk.co.myzen.a_z.json.ChargeDischarge;

public class WatchSlotChargeHelperThread extends Thread implements Runnable {

	// See https://symbl.cc/en/unicode/blocks/combining-diacritical-marks/

	private static final char longStrokeOverlay = '̶';

//	private static char longSolidusOverlay = '̸';
//
//	private static char xAbove = '̽';
	private final int delayStartMinutes;
	private final int runTimeOutMinutes;
	private final int scheduleIndex;
	private final int socMaxPercent;
	private final int socMinPercent;

	private final String expiryTimeHHMM;

	private final int power;

	final int secOfDayTimeOut;

	// the start of the slot will be 29 minutes earlier (i.e 1740 seconds earlier)

	final int secOfDayTimeIn;

	private final IOctopus i;

	private final String slotN;

	protected static String SN(int zeroBasedIndex) {

		char[] regular = { 'S', String.valueOf(1 + zeroBasedIndex).charAt(0), ' ' };

		return String.valueOf(regular);
	}

	protected static String SN(int zeroBasedIndex, boolean cancelled) {

		return cancelled ? SN(zeroBasedIndex, longStrokeOverlay) : SN(zeroBasedIndex);
	}

	protected static String SN(int zeroBasedIndex, char overlay) {

		char[] strikeThrough = { overlay, 'S', overlay, SN(zeroBasedIndex).charAt(1), ' ' };

		return String.valueOf(strikeThrough);
	}

	protected WatchSlotChargeHelperThread(IOctopus i, int runTimeOutMinutes, int scheduleIndex, int socMinPercent,
			int socMaxPercent, int power, int delayStartMinutes) {

		this.i = i;
		this.delayStartMinutes = delayStartMinutes;

		this.runTimeOutMinutes = runTimeOutMinutes;

		this.scheduleIndex = scheduleIndex;

		this.socMaxPercent = socMaxPercent;
		this.socMinPercent = socMinPercent;

		this.power = power;

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

		secOfDayTimeIn = secOfDayTimeOut - 1800 + (60 * delayStartMinutes) + 15;

		// there is no guarantee that start & finish is on a SS == 0 boundary
		// but we are confident the period between is as desired

		slotN = SN(scheduleIndex);
	}

	@Override
	public void run() {

		int secOfDayNow = LocalTime.now().toSecondOfDay();

		Thread currentThread = Thread.currentThread();

		String idHexString = Long.toHexString(currentThread.getId());

		currentThread.setName("Charge-" + idHexString);

		i.logErrTime(slotN + "Monitoring starts aft:" + delayStartMinutes + " min:" + socMinPercent + "% max:"
				+ socMaxPercent + "% power:" + power + " watts");

		i.batteryChargePower(power);

		boolean chargeLevelNotDefault = false;

		DateTimeFormatter formatter24HourClock = Octopussy.formatter24HourClock;

		int prevBatLev = 0;

		float prevTemperature = 0f;

		float prevChargeUnits = 0f;

		float prevDischargeUnits = 0f;

		int reason = 0;

		boolean chargeExpedited = false;

		do { // repeat loop every 15 or 45 seconds or so

			Integer batteryLevel = null;

			Float temperatureDegreesC = null;

			Float chargeUnits = null;

			Float dischargeUnits = null;

			try {

				Thread.sleep(15000L);

				secOfDayNow = LocalTime.now().toSecondOfDay();

				if (secOfDayNow < secOfDayTimeIn) {

					continue; // tick every 15 seconds until we've started charging
				}

				temperatureDegreesC = i.execReadTemperature();

				Thread.sleep(15000L);

				batteryLevel = i.execReadBatteryPercent();

				Thread.sleep(15000L);

				ChargeDischarge chargeAndDischargeUnits = i.execReadChargeDischarge();

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
				i.logErrTime(slotN + "Hit runTimeoutMinutes:" + runTimeOutMinutes);
				break;
			}

			if (prevBatLev >= socMaxPercent) {

				i.logErrTime(slotN + "WARNING: Battery >= " + socMaxPercent + "% terminating charge now");

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

			// for scheduling, play safe and ensure a future time
			String soon = LocalDateTime.now().plusMinutes(1l).format(formatter24HourClock);

			if (batteryLevel < socMinPercent) {

				if (!chargeExpedited) {

					i.logErrTime(slotN + "WARNING: Battery < " + socMinPercent + "% expedite charging now at "
							+ Octopussy.maxRate + " watts");

					i.batteryChargePower(Integer.parseInt(Octopussy.maxRate));

					chargeLevelNotDefault = true;

					i.resetChargingSlot(scheduleIndex, soon, expiryTimeHHMM, socMaxPercent);

					chargeExpedited = true;
				}

			} else {

				if (chargeExpedited && chargeLevelNotDefault) {

					if (power >= -1) {

						i.logErrTime(slotN + "Battery reached " + socMinPercent + "% returning charging level to "
								+ power + " watts");

						i.batteryChargePower(power);

					} else {

						i.logErrTime(slotN + "Battery reached " + socMinPercent + "%");
					}

					chargeLevelNotDefault = false;
				}
			}

		} while (0 != expiryTimeHHMM.compareTo(LocalDateTime.now().format(formatter24HourClock)));

		if (reason < 2 || chargeExpedited) {

			i.resetChargingSlot(scheduleIndex, expiryTimeHHMM, expiryTimeHHMM, 100);
		}

		i.logErrTime(slotN + "Monitoring finished. Reason:" + reason + " Expedited:" + chargeExpedited);
	}
}
