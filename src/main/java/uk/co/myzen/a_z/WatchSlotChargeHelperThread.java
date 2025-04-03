package uk.co.myzen.a_z;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import uk.co.myzen.a_z.json.ChargeDischarge;

public class WatchSlotChargeHelperThread extends Thread implements Runnable {

	// See https://symbl.cc/en/unicode/blocks/combining-diacritical-marks/

	private static final char longStrokeOverlay = '̶';

//	private static char longSolidusOverlay = '̸';
//
//	private static char xAbove = '̽';

	private final int runTimeoutMinutes;
	private final int scheduleIndex;
	private final int socMaxPercent;
	private final int socMinPercent;

	private final String expiryTime;

	private final int power;

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

	protected WatchSlotChargeHelperThread(IOctopus i, int runTimeoutMinutes, int scheduleIndex, int socMinPercent,
			int socMaxPercent, int power) {

		this.i = i;

		this.runTimeoutMinutes = runTimeoutMinutes;

		this.scheduleIndex = scheduleIndex;

		this.socMaxPercent = socMaxPercent;
		this.socMinPercent = socMinPercent;

		this.expiryTime = Octopussy.chargeSchedule[scheduleIndex];

		this.power = power;

		slotN = SN(scheduleIndex);
	}

	@Override
	public void run() {

		final long millisTimeout = (runTimeoutMinutes * 60000) + System.currentTimeMillis();

		Thread currentThread = Thread.currentThread();

		String idHexString = Long.toHexString(currentThread.getId());

		currentThread.setName("Charge-" + idHexString);

		i.logErrTime(slotN + "Monitoring starts min:" + socMinPercent + "% max:" + socMaxPercent + "% power:" + power
				+ " watts");

		i.batteryChargePower(power);

		boolean chargeLevelNotDefault = false;

		DateTimeFormatter formatter24HourClock = Octopussy.formatter24HourClock;

		int prevBatLev = 0;

		float prevTemperature = 0f;

		float prevChargeUnits = 0f;

		float prevDischargeUnits = 0f;

		int reason = 0;

		boolean chargeExpedited = false;

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

			if (System.currentTimeMillis() >= millisTimeout) {

				reason = 1;
				i.logErrTime(slotN + "Hit runTimeoutMinutes:" + runTimeoutMinutes);
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

					i.resetChargingSlot(scheduleIndex, soon, expiryTime, socMaxPercent);

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

		} while (0 != expiryTime.compareTo(LocalDateTime.now().format(formatter24HourClock)));

		if (reason < 2 || chargeExpedited) {

			i.resetChargingSlot(scheduleIndex, expiryTime, expiryTime, 100);
		}

		i.logErrTime(slotN + "Monitoring finished. Reason:" + reason + " Expedited:" + chargeExpedited);
	}
}
