package uk.co.myzen.a_z;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import uk.co.myzen.a_z.json.ChargeDischarge;

public abstract class WatchSlotBaseThread extends WatchSlotTimerBaseThread {

	public final static DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	protected static char longStrokeOverlay = '̶';

	private final boolean charging;

	private final IOctopus i;

	private final int scheduleIndex;
	private final int socMinPercent;
	private final int socMaxPercent;
	private final int power;
	private final int delayStartMinutes;

	private final String expiryTimeHHMM;
	private final String slotN;

	private int secOfDayNow;
	private int secOfDayTimeIn; // the second when monitoring of bat/tmp/cha/dis begins

	private int count = 0;

	private int ticksRemaining = -1;

	boolean batteryLimitSOC = false;
	boolean pausedBattery = false;
	boolean expedited = false;

	int prevBatLev = 0;

	float prevTemperature = 0f;
	float prevChargeUnits = 0f;
	float prevDischargeUnits = 0f;

	int prevPowerLevel = 0;

	Integer batteryLevel = null;

	Integer powerLevel = null;

	Float temperatureDegreesC = null;
	Float chargeUnits = null;
	Float dischargeUnits = null;

	protected abstract String getSlotN(int zeroBasedIndex, boolean cancelled);

	// returns second of day related to the timestamp

	public static synchronized int logErrTime(String text) {

		ZonedDateTime zdt = ZonedDateTime.now();

		int secondOfDay = zdt.toLocalTime().toSecondOfDay();

		String sod = String.format("%05d", secondOfDay);

		String threadName = Thread.currentThread().getName();

		String timestamp = zdt.format(defaultDateTimeFormatter).substring(0, 19);

		System.err.println(timestamp + " " + threadName + ": " + sod + " " + text);

		return secondOfDay;
	}

	protected void monitoringEvent(String reason) {

		count++;

		logErrTime(slotN + "(" + count + ")" + ticksRemaining + ":" + reason);
	}

	/*
	 * N.B. When power < 0 passed, assume grid import price is negative. In this
	 * situation, strategy is to expeditiously charge without delay until bat is
	 * 100%. If battery is full and still time left in the negative price slot,
	 * pause the battery. In this situation the house load will use the negative
	 * price grid energy rather than drain battery during the remainder of the slot.
	 * 
	 * The value in delayStartMinutes will be overridden when battery % is <
	 * socMinPercent or when power -1 specified
	 * 
	 */
	protected WatchSlotBaseThread(boolean charging, IOctopus i, String[] schedule, int scheduleIndex, int socMinPercent,
			int socMaxPercent, int power, int delayStartMinutes) {

		super(i, 15, schedule[scheduleIndex], 30); // get tick events every 15 seconds until hh:29:30 or hh:59:30 as
													// appropriate

		slotN = getSlotN(scheduleIndex, false);

		this.expiryTimeHHMM = schedule[scheduleIndex];

		this.charging = charging;
		this.i = i;
		this.scheduleIndex = scheduleIndex;
		this.socMinPercent = socMinPercent;
		this.socMaxPercent = socMaxPercent;
		this.power = power;
		this.delayStartMinutes = delayStartMinutes;

		secOfDayTimeIn = sFinishAt - 1800 + (60 * delayStartMinutes);
	}

	@Override
	public void run() {

		Thread currentThread = Thread.currentThread();

		currentThread.setName((charging ? "Charge" : "Discharge") + "-" + Octopussy.slotNumber);

		monitoringEvent("Monitor aft:" + delayStartMinutes + " min:" + socMinPercent + "% max:" + socMaxPercent
				+ "% power:" + power + " watts end:" + expiryTimeHHMM);

		if (charging) {

			if (power < 0) {

				monitoringEvent("Setting charging power to maximum (" + Octopussy.maxRate + " watts)");

				i.batteryChargePower(Integer.parseInt(Octopussy.maxRate));

			} else {

				monitoringEvent("Setting charging power to " + power + " watts");

				i.batteryChargePower(power);
			}
		} else {

			if (power < 0) {

				monitoringEvent("Setting discharging power to maximum (" + Octopussy.maxRate + " watts)");

				i.batteryDischargePower(Integer.parseInt(Octopussy.maxRate));

			} else {

				monitoringEvent("Setting discharging power to " + power + " watts");

				i.batteryDischargePower(power);
			}
		}

		super.run(); // tick(n) will be called after each period has elapsed until tick(0)

		// thread about to evaporate
	}

	protected boolean tick(int ticksRemaining) {

		this.ticksRemaining = ticksRemaining;

		boolean logProbeData = false;

		secOfDayNow = LocalTime.now().toSecondOfDay();

		if (secOfDayNow < secOfDayTimeIn && !expedited) {

			probeBattery();

		} else {

			logProbeData = true;

			int task = ticksRemaining % 3; // i.e., 0 to 2

			switch (task) {

			case 2:

				probeChargeDischarge();

				break;

			case 1:

				probeTemperature();

				break;

			default:

				probeBattery();

				break;
			}
		}

		processBatteryLevel();

		if (batteryLimitSOC || 0 == ticksRemaining) {

			return false; // no more ticks please - we're done! Expect termination()
		}

		if (logProbeData) {

			logProbeData();
		}

		return true; // yes we expect more ticks
	}

	private void processBatteryLevel() {

		if (null != batteryLevel) {

			if (batteryLevel.intValue() < socMinPercent && !pausedBattery) {

				if (charging) {

					// expedite temporarily (if not already)

					if (!expedited) { // do this once only

						expedited = true;

						monitoringEvent("Expediting charge at max rate");

						i.batteryChargePower(Integer.parseInt(Octopussy.maxRate));

						LocalDateTime ldt = null;

						if (socMinPercent == socMaxPercent) { // a manual charging slot probably

							ldt = LocalDateTime.now().withMinute(delayStartMinutes);

						} else { // override the specified aft:

							ldt = LocalDateTime.now();
						}

						String startTime = ldt.format(Octopussy.formatter24HourClock);

						monitoringEvent(i.resetChargingSlot(scheduleIndex, startTime, expiryTimeHHMM, 100, false));
					}

				} else { // assume discharging

					monitoringEvent("WARNING: Battery <= " + socMinPercent + "% terminating now");

					batteryLimitSOC = true;
				}

			} else {

				if (expedited) {

					if (power >= -1) {

						monitoringEvent("Battery reached " + socMinPercent + "% returning charging level to " + power
								+ " watts");

						i.batteryChargePower(power);

					} else { // assume power = -1 which implicitly means max rate

						monitoringEvent("Battery reached " + socMinPercent + "%");
					}

					expedited = false;
				}
			}

			if (batteryLevel.intValue() >= socMaxPercent) {

				if (charging) {

					batteryLimitSOC = true;

					if (power > -1) {

						monitoringEvent("Battery reached " + socMaxPercent + "% terminating now");

					} else {

						if (!pausedBattery) {

							// assume negative pricing and battery reached socMaxPercent
							// pause the battery at least until the end of the slot

							i.batteryPauseChargeAndDischarge();

							pausedBattery = true;

							monitoringEvent(
									"The battery has been paused at " + socMaxPercent + "% as unit price is negative");
						}
					}
				}
			}
		}
	}

	private void logProbeData() {

		if (null != batteryLevel && null != temperatureDegreesC && null != chargeUnits && null != dischargeUnits
				&& null != powerLevel) {

			if (batteryLevel.intValue() != prevBatLev || temperatureDegreesC.floatValue() != prevTemperature
					|| chargeUnits != prevChargeUnits || dischargeUnits != prevDischargeUnits
					|| powerLevel.intValue() != prevPowerLevel) {

				boolean log = false;

				if (powerLevel.floatValue() != prevPowerLevel) {

					prevPowerLevel = powerLevel.intValue();

					log = true;
				}

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

					monitoringEvent("Bat:" + prevBatLev + "% Tmp:" + prevTemperature + "°C Cha:" + chargeUnits + " Dis:"
							+ dischargeUnits + " Wat:" + prevPowerLevel);
				}
			}
		}
	}

	private void probeChargeDischarge() {

		ChargeDischarge chargeAndDischargeUnits = i.execReadChargeDischarge();

		chargeUnits = chargeAndDischargeUnits.getCharge();

		dischargeUnits = chargeAndDischargeUnits.getDischarge();
	}

	private void probeTemperature() {

		temperatureDegreesC = i.execReadTemperature();
	}

	private void probeBattery() {

		String[] parameters = i.execReadBatteryPercentAndPower();

		batteryLevel = Integer.valueOf(parameters[0]);

		powerLevel = Integer.valueOf(parameters[1]);
	}

	@Override
	protected void termination() {

		if (pausedBattery) {

			i.batteryNotPaused();
		}

		if (charging) {

			monitoringEvent(i.resetChargingSlot(scheduleIndex, expiryTimeHHMM, expiryTimeHHMM, 100, false));

		} else {

			monitoringEvent(i.resetDischargingSlot(scheduleIndex, expiryTimeHHMM, expiryTimeHHMM, 4, false));
		}

		monitoringEvent("SoC reached:" + batteryLimitSOC);
	}
}
