package uk.co.myzen.a_z;

public class WatchSlotDischargeHelperThread extends WatchSlotBaseThread {

	protected WatchSlotDischargeHelperThread(IOctopus i, String[] schedule, int scheduleIndex, int socMinPercent,
			int socMaxPercent, int power, int delayStartMinutes) {

		super(false, i, schedule, scheduleIndex, socMinPercent, socMaxPercent, power, delayStartMinutes);
	}

	@Override
	protected String getSlotN(int zeroBasedIndex, boolean cancelled) {

		return XN(zeroBasedIndex, cancelled);
	}

	static String XN(int zeroBasedIndex) {

		char[] regular = { 'X', String.valueOf(1 + zeroBasedIndex).charAt(0), ' ' };

		return String.valueOf(regular);
	}

	static String XN(int zeroBasedIndex, boolean cancelled) {

		return cancelled ? XN(zeroBasedIndex, longStrokeOverlay) : XN(zeroBasedIndex);
	}

	static String XN(int zeroBasedIndex, char overlay) {

		char[] strikeThrough = { overlay, 'X', overlay, XN(zeroBasedIndex).charAt(1), ' ' };

		return String.valueOf(strikeThrough);
	}
}
