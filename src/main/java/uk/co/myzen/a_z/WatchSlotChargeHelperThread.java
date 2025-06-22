package uk.co.myzen.a_z;

public class WatchSlotChargeHelperThread extends WatchSlotBaseThread {

	protected WatchSlotChargeHelperThread(IOctopus i, String[] schedule, int scheduleIndex, int socMinPercent,
			int socMaxPercent, int power, int delayStartMinutes) {

		super(true, i, schedule, scheduleIndex, socMinPercent, socMaxPercent, power, delayStartMinutes);
	}

	@Override
	protected String getSlotN(int zeroBasedIndex, boolean cancelled) {

		return SN(zeroBasedIndex, cancelled);
	}

	static String SN(int zeroBasedIndex) {

		char[] regular = { 'S', String.valueOf(1 + zeroBasedIndex).charAt(0), ' ' };

		return String.valueOf(regular);
	}

	static String SN(int zeroBasedIndex, boolean cancelled) {

		return cancelled ? SN(zeroBasedIndex, longStrokeOverlay) : SN(zeroBasedIndex);
	}

	static String SN(int zeroBasedIndex, char overlay) {

		char[] strikeThrough = { overlay, 'S', overlay, SN(zeroBasedIndex).charAt(1), ' ' };

		return String.valueOf(strikeThrough);
	}
}
