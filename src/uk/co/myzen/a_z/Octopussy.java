/**
 * 
 */
package uk.co.myzen.a_z;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.co.myzen.a_z.json.Agile;
import uk.co.myzen.a_z.json.V1AgileFlex;
import uk.co.myzen.a_z.json.V1ElectricityConsumption;
import uk.co.myzen.a_z.json.V1GSP;
import uk.co.myzen.a_z.json.V1GasConsumption;
import uk.co.myzen.a_z.json.V1GridSupplyPoints;
import uk.co.myzen.a_z.json.V1PeriodConsumption;

/**
 * @author howard
 *
 */

public class Octopussy {

	public static final String ANSI_RESET = "\u001B[0m";

	public static final String coloursANSI[] = { "BLACK", "RED", "GREEN", "YELLOW", "BLUE", "PURPLE", "CYAN", "WHITE" };

	// foreground ANSI colours

	public static final String foregroundANSI[] = { "\u001B[30m", "\u001B[31m", "\u001B[32m", "\u001B[33m",
			"\u001B[34m", "\u001B[35m", "\u001B[36m", "\u001B[37m" };

	public static Map<String, String> colourMapForeground = new HashMap<String, String>();

	// background ANSI colours

	public static final String backgroundANSI[] = { "\u001B[40m", "\u001B[41m", "\u001B[42m", "\u001B[43m",
			"\u001B[44m", "\u001B[45m", "\u001B[46m", "\u001B[47m" };

	public static Map<String, String> colourMapBackground = new HashMap<String, String>();

	private final static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36";
	private final static String contentType = "application/json";

	private final static String[] defaultPropertyKeys = { "apiKey", "electricity.mprn", "electricity.sn", "#",
			"gas.mprn", "gas.sn", "flexible.gas.unit", "flexible.gas.standing", "#", "flexible.electricity.unit",
			"flexible.electricity.standing", "agile.electricity.standing", "#", "zone.id", "history", "postcode",
			"region", "base.url", "import.product.code", "tariff.code", "tariff.url", "#", "export.product.code",
			"export.tariff.code", "export.tariff.url", "export", "#", "days", "plunge", "target", "width", "ansi",
			"colour", "color", "#", "extra", "referral" };

	private final static DateTimeFormatter simpleTime = DateTimeFormatter.ofPattern("E MMM dd pph:mm a");

	private final static DateTimeFormatter formatter24HourClock = DateTimeFormatter.ofPattern("HH:mm");

	private final static DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	private static ZoneId ourZoneId;

	private static ObjectMapper mapper;

	private static int width;

	private static boolean ansi;

	private static boolean extra = false; // overridden by extra=true|false in properties

	private static boolean usingExternalPropertyFile = false;

	private static Properties properties;

	private static Octopussy instance = null;

	private static String ANSI_COLOUR_FOREGROUND;
	private static String ANSI_COLOUR_ABOVE_TARGET;

	private static synchronized Octopussy getInstance() {

		if (null == instance) {

			instance = new Octopussy();
		}

		return instance;
	}

	static {
		mapper = new ObjectMapper();

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		for (int n = 0; n < coloursANSI.length; n++) {

			String colour = coloursANSI[n];

			colourMapForeground.put(colour, foregroundANSI[n]);
			colourMapBackground.put(colour, backgroundANSI[n]);
		}
	}

	private Octopussy() {
	}

	/**
	 * @param args
	 * @throws Exception
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws Exception {

		File importData = null;

		Scanner myReader = null;

		FileWriter fw = null;

		BufferedWriter bw = null;

		int extended = 0;

		try {

			String propertyFileName = "./octopussy.properties"; // the default

			if (args.length > 0) {

				extended = Integer.parseInt(args[0].trim());

				if (extended > 19) {

					extended = 19;

				} else if (extended < 1) {

					extended = 1;
				}

				if (args.length > 1) {

					// assume the optional second parameter is the name of a property file
					// which will be used to override the built-in resource octopussy.properties

					propertyFileName = args[1].trim();
				}

			}

			String keyValue = null;

			try {

				// Check for existence of octopussy.properties in the current directory
				// if it exists, use it in preference to the built-in resource compiled into the
				// jar

				File externalProperties = new File(propertyFileName);

				usingExternalPropertyFile = loadProperties(externalProperties);

				keyValue = properties.getProperty("apiKey").trim();

				if (null == keyValue) {

					throw new Exception("apiKey");
				}

				width = Integer.valueOf(properties.getProperty("width", "62").trim());

				ansi = Boolean.valueOf(properties.getProperty("ansi", "false").trim());

				ourZoneId = ZoneId.of(properties.getProperty("zone.id", "Europe/London").trim());

				if (null == ourZoneId) {

					throw new Exception("zone.id");
				}

				ANSI_COLOUR_FOREGROUND = colourMapForeground.get(properties.getProperty("colour", "GREEN").trim());
				ANSI_COLOUR_ABOVE_TARGET = colourMapForeground.get(properties.getProperty("color", "RED").trim());

				importData = new File(properties.getProperty("history", "octopus.import.csv").trim());

			} catch (Exception e) {

				e.printStackTrace();

				System.exit(-1);
			}

			//
			// read historical consumption data from octopus.import.csv
			//

			Map<Long, ConsumptionHistory> history = new TreeMap<Long, ConsumptionHistory>();

			long epochFrom = 0;

			if (importData.createNewFile()) {

				// we normally expect to see the history file.
				// Lets assume we are running for the first time or need to reset

				// display the content of the built-in property file (which can be used as a
				// template)

				if (!usingExternalPropertyFile) {

					for (String propertyKey : defaultPropertyKeys) {

						if ("postcode".equals(propertyKey)) {

							System.out.println("#");
							System.out.println(
									"# n.b. Southern England is region H - see https://mysmartenergy.uk/Electricity-Region");
							System.out.println("#");
							System.out.println(
									"# if postcode is uncommented it will override region=H based on Octopus API");
							System.out.println("#");
							System.out.println("#postcode=SN5");
							System.out.println("#");

						} else if ("ansi".equals(propertyKey)) {

							System.out.println("#");
							System.out.println(
									"# in Windows console to show ANSI update Registry set REG_DWORD VirtualTerminalLevel=1 for Computer\\HKEY_CURRENT_USER\\Console");
							System.out.println("#");
							System.out.println("ansi=true");

						} else {

							System.out.println(propertyKey
									+ ("#".equals(propertyKey) ? "" : "=" + properties.getProperty(propertyKey, "?")));
						}
					}

					System.exit(0);
				}

			} else {

				myReader = new Scanner(importData);

				while (myReader.hasNextLine()) {

					String data = myReader.nextLine();

					if (0 == data.trim().length()) {

						continue;
					}

					if (data.startsWith("Consumption")) {

						continue;
					}

					String[] fields = data.split(",");

					if (fields.length < 3) {

						continue;
					}

					ConsumptionHistory ch = new ConsumptionHistory();

					String c = fields[0].trim();

					if ("null".equals(c)) {

						ch.setConsumption(null);

					} else {

						ch.setConsumption(Float.valueOf(c));

					}

					OffsetDateTime from = OffsetDateTime.parse(fields[1].trim(), defaultDateTimeFormatter);
					OffsetDateTime to = OffsetDateTime.parse(fields[2].trim(), defaultDateTimeFormatter);

					if (4 == fields.length) { // assume price in data

						ch.setPrice(Float.valueOf(fields[3].trim()));

					} else {

						ch.setPrice(null);
					}

					ch.setFrom(from);
					ch.setTo(to);

					epochFrom = from.toEpochSecond();

					history.put(Long.valueOf(epochFrom), ch);
				}
			}

			//
			// epochFrom holds the latest timestamp from the data read from file
			// zero indicates no file/data
			//

			if (null != myReader) {

				myReader.close();
			}

			myReader = null;

			// here the octopus.import.csv or similar will exist even if empty

			instance = getInstance();

			properties.setProperty("basic", "Basic " + Base64.getEncoder().encodeToString(keyValue.getBytes()));

			boolean export = Boolean.valueOf(properties.getProperty("export", "false").trim());

			// calculate date for for 00:00 'yesterday'

			LocalDateTime now = LocalDateTime.now();

			int dayOfYear = now.getDayOfYear();

			int howManyDaysHistory = Integer.valueOf(properties.getProperty("days", "2").trim());

			LocalDateTime startOfPreviousDays = now.withDayOfYear(dayOfYear - howManyDaysHistory).withHour(0)
					.withMinute(0).withSecond(0).withNano(0);

			V1AgileFlex v1AgileFlex = instance.getV1AgileFlexImport(48 * howManyDaysHistory,
					startOfPreviousDays.toString(), null);

			ArrayList<Agile> agileResultsImport = v1AgileFlex.getAgileResults();

			if (extra) {

				System.out.println("\nPeriods of unit price import data obtained: " + v1AgileFlex.getCount() + "\t"
						+ agileResultsImport.get(agileResultsImport.size() - 1).getValidFrom() + "\t"
						+ agileResultsImport.get(0).getValidTo());
			}

			ArrayList<Agile> agileResultsExport = null; // only populated if export=true in octopussy.properties

			if (export) {

				v1AgileFlex = instance.getV1AgileFlexExport(48 * howManyDaysHistory, startOfPreviousDays.toString(),
						null);

				agileResultsExport = v1AgileFlex.getAgileResults();

				if (extra) {
					System.out.println("\nPeriods of unit price export data obtained: " + v1AgileFlex.getCount() + "\t"
							+ agileResultsExport.get(agileResultsExport.size() - 1).getValidFrom() + "\t"
							+ agileResultsExport.get(0).getValidTo());
				}
			}

			Map<LocalDateTime, ImportExportData> vatIncPriceMap = new HashMap<LocalDateTime, ImportExportData>();

			long epochNow = now.atZone(ourZoneId).toEpochSecond();

			long halfHourAgo = epochNow - 1800;

			String today = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochNow), ourZoneId).toString().substring(0,
					10);

			int plunge = Integer.valueOf(properties.getProperty("plunge", "0").trim()).intValue();
			int target = Integer.valueOf(properties.getProperty("target", "30").trim()).intValue();

			for (Agile agile : agileResultsImport) {

				String validFrom = agile.getValidFrom();

				LocalDateTime ldt = LocalDateTime.parse(validFrom, DateTimeFormatter.ISO_ZONED_DATE_TIME);

				// assume time obtained is zulu/UTC - need to convert to our local time (such as
				// BST)

				ZonedDateTime instant = ZonedDateTime.of(ldt, ZoneId.of("UTC"));
				LocalDateTime actual = instant.withZoneSameInstant(ourZoneId).toLocalDateTime();

				OffsetDateTime offsetDateTime = actual.atOffset(ZoneOffset.of("+01:00"));

				long epochActual = offsetDateTime.toEpochSecond();

				float valueIncVat = agile.getValueIncVat();

				ConsumptionHistory consumptionLatest = new ConsumptionHistory();

				consumptionLatest.setFrom(offsetDateTime);

				LocalDateTime actualTo = instant.withZoneSameInstant(ourZoneId).toLocalDateTime();

				OffsetDateTime offsetDateTimeTo = actualTo.atOffset(ZoneOffset.of("+01:00"));

				consumptionLatest.setTo(offsetDateTimeTo);

				consumptionLatest.setPrice(Float.valueOf(valueIncVat));

				history.put(epochActual, consumptionLatest);

				ImportExportData importExportPricesIncVat = new ImportExportData();

				importExportPricesIncVat.setImportPrice(Float.valueOf(valueIncVat));

				vatIncPriceMap.put(actual, importExportPricesIncVat);

			}

			if (export) {

				for (Agile agile : agileResultsExport) {

					String validFrom = agile.getValidFrom().substring(0, 19);

					LocalDateTime ldt = LocalDateTime.parse(validFrom, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

					// assume time obtained is zulu/UTC - need to convert to our local time (such as
					// BST)

					ZonedDateTime instant = ZonedDateTime.of(ldt, ZoneId.of("UTC"));
					LocalDateTime actual = instant.withZoneSameInstant(ourZoneId).toLocalDateTime();

					float valueIncVat = agile.getValueIncVat();

					ImportExportData importExportPricesIncVat = vatIncPriceMap.get(actual);

					importExportPricesIncVat.setExportPrice(Float.valueOf(valueIncVat));

					vatIncPriceMap.put(actual, importExportPricesIncVat);
				}
			}

			String someDaysAgo = startOfPreviousDays.toString();

			V1ElectricityConsumption v1ElectricityConsumption = instance.getV1ElectricityConsumption(null,
					48 * howManyDaysHistory, someDaysAgo, null);

			ArrayList<V1PeriodConsumption> periodResults = v1ElectricityConsumption.getPeriodResults();

			if (null == periodResults) {

				throw new Exception(
						"\r\nDouble check the apiKey, electricity.mprn & electricity.sn values in the octopussy.properties\r\n"
								+ "An option is to create a new octopussy.properties file in the current directory with the correct values\r\n"
								+ "A template octopussy.properties file can be redisplayed by deleting the octopus.import.csv and running again\r\n"
								+ "Alternatively replace the resource octopussy.properties inside the jar file using 7-Zip or similar\r\n");
			}

			// Add history data for any non-null consumptions in

			int tallyHistory = 0;

			for (Long key : history.keySet()) {

				ConsumptionHistory value = history.get(key);

				String timestamp = value.getFrom().toString();

				if (timestamp.equals(someDaysAgo)) {

					break;
				}

				if (null == value.getConsumption()) {

					continue;
				}

				Float halfHourPrice = value.getPrice();

				if (null == halfHourPrice) {

					continue;
				}

				tallyHistory++;

				// assume history up to someDaysAgo has consumption & price available

				V1PeriodConsumption entry = new V1PeriodConsumption();

				Float consumption = value.getConsumption();

				entry.setConsumption(consumption);
				entry.setIntervalStart(timestamp);
				entry.setIntervalEnd(value.getTo().toString());

				periodResults.add(entry);

				if (extra) {

					Float halfHourCharge = consumption * halfHourPrice;

					String intervalStart = timestamp;

					System.out.println("\t" + intervalStart + "\t" + halfHourPrice + "\t* " + consumption + "\t="
							+ String.format("%10.6f", halfHourCharge) + " p");
				}

			}

			if (extra) {

				System.out.println("\nLatest available consumption data obtained: "
						+ v1ElectricityConsumption.getCount() + " plus added from history: " + tallyHistory

						+ "\tRequired: " + 48 * howManyDaysHistory + " ( = " + howManyDaysHistory + " day(s) )");
			}

			Map<String, DayValues> elecMapDaily = new HashMap<String, DayValues>();

			for (V1PeriodConsumption v1PeriodConsumption : periodResults) {

				Float consumption = v1PeriodConsumption.getConsumption();

				OffsetDateTime from = OffsetDateTime.parse(v1PeriodConsumption.getIntervalStart(),
						defaultDateTimeFormatter);

				String intervalStart = v1PeriodConsumption.getIntervalStart().substring(0, 16);

				String key = intervalStart.substring(0, 10);

				if (elecMapDaily.containsKey(key)) {

					// it's possible that this day has already been marked incomplete
					// if so then iterate rather than processing the consumption declared for the
					// time slot

					if (null == elecMapDaily.get(key)) {

						continue;
					}
				}

				LocalDateTime ldt = LocalDateTime.parse(intervalStart);

				ImportExportData importExportData = vatIncPriceMap.get(ldt);

				if (null == importExportData) {

					elecMapDaily.put(key, null); // flag this day as incomplete - ignore all time slots on this day

					continue;
				}

				Float halfHourPrice = importExportData.getImportPrice();

				Long epochKey = from.toEpochSecond();

				ConsumptionHistory latestConsumption = history.get(epochKey);

				if (null == latestConsumption) {

					System.err.println("error at " + v1PeriodConsumption.getIntervalStart());

				} else {

					latestConsumption.setConsumption(consumption);
					latestConsumption.setPrice(halfHourPrice);

					history.put(epochKey, latestConsumption);
				}

				Float halfHourCharge = consumption * halfHourPrice;

				if (extra) {

					System.out.println("\t" + intervalStart + "\t" + halfHourPrice + "\t* " + consumption + "\t="
							+ String.format("%10.6f", halfHourCharge) + " p");
				}

				DayValues dayValues = null;

				if (elecMapDaily.containsKey(key)) {

					dayValues = elecMapDaily.get(key);

					if (halfHourPrice < dayValues.getLowestPrice()) {

						dayValues.setLowestPrice(Float.valueOf(halfHourPrice));
					}

					dayValues.setSlotCount(1 + dayValues.getSlotCount());

					dayValues.setDailyConsumption(consumption + dayValues.getDailyConsumption());

					dayValues.setDailyPrice(halfHourCharge.floatValue() + dayValues.getDailyPrice());

				} else {

					dayValues = new DayValues();

					dayValues.setLowestPrice(Float.valueOf(100));

					dayValues.setDayOfWeek(ldt.format(DateTimeFormatter.ofPattern("E")));

					dayValues.setSlotCount(1);

					dayValues.setDailyConsumption(consumption);

					dayValues.setDailyPrice(Float.valueOf(halfHourCharge.floatValue()));
				}

				elecMapDaily.put(key, dayValues);
			}

			//
			// now append recent data to history file (which conceivably could be empty)
			//

			fw = new FileWriter(importData, true);

			bw = new BufferedWriter(fw);

			for (Long key : history.keySet()) { // implicitly in ascending key based on epochSecond

				if (key > epochFrom) {

					// data to append to history

					if (0 == epochFrom) {

						bw.write("Consumption (kWh), Start, End, Price");
						epochFrom = key;
					}

					ConsumptionHistory ch = history.get(key);

					String entry = ch.getConsumption() + ", " + ch.getFrom().toString() + ", " + ch.getTo() + ", "
							+ ch.getPrice();

					bw.newLine();
					bw.write(entry);
				}
			}

			bw.close();

			bw = null;

			fw.close();

			fw = null;

			//
			//
			//

			System.out.println("\nDaily results:");

			int countDays = 0;

			float accumulateDifference = 0;
			float accumulatePower = 0;
			float accumulateCost = 0;

			float flatRate = Float.valueOf(properties.getProperty("flexible.electricity.unit"));

			SortedSet<String> setOfDays = new TreeSet<String>();

			setOfDays.addAll(elecMapDaily.keySet());

			for (String key : setOfDays) {

				// ignore today because consumption data will not yet be fully complete

				if (0 == today.compareTo(key)) {

					continue;
				}

				DayValues dayValues = elecMapDaily.get(key);

				// ignore other incomplete days

				if (null == dayValues) {

					continue;
				}

				if (48 != dayValues.getSlotCount()) {

					continue;
				}

				countDays++;

				float consumption = dayValues.getDailyConsumption();

				float agilePrice = dayValues.getDailyPrice();

				float agileCharge = Float.valueOf(properties.getProperty("agile.electricity.standing"));

				float standardPrice = consumption * flatRate;

				float standardCharge = Float.valueOf(properties.getProperty("flexible.electricity.standing"));

				float difference = (standardPrice + standardCharge) - (agilePrice + agileCharge);

				float lowestPrice = dayValues.getLowestPrice();

				System.out.println(dayValues.getDayOfWeek() + (lowestPrice < plunge ? " * " : "   ") + key
						+ (lowestPrice < plunge ? String.format("%6.2f", lowestPrice) + "p " : "        ")
						+ String.format("%7.2f", consumption) + " kWhr  Agile: " + String.format("%8.4f", agilePrice)
						+ "p +" + agileCharge + "p (X: " + String.format("%8.4f", standardPrice) + "p +"
						+ standardCharge + "p)  saving: £" + String.format("%5.2f", (difference / 100)));

				accumulateDifference += difference;

				accumulatePower += consumption;

				accumulateCost += agilePrice;
			}

			String pounds2DP = String.format("%.2f", accumulateDifference / 100);

			String averagePounds2DP = String.format("%.2f", accumulateDifference / 100 / countDays);

			Float unitCostAverage = accumulateCost / accumulatePower;

			String averageCostPerUnit = String.format("%.2f", unitCostAverage);

			int averageUnitCost = unitCostAverage.intValue();

			String averagePower = String.format("%.2f", accumulatePower / countDays);

			System.out.println("\nOver " + countDays + " days, using " + accumulatePower + " kWhr, Agile has saved £"
					+ pounds2DP + " compared to the " + flatRate + "p (X) flat rate tariff");
			System.out.println("Average daily saving: £" + averagePounds2DP + " Average cost per unit (A): "
					+ averageCostPerUnit + "p  Average daily power usage: " + averagePower + " kWhr");

			SortedSet<LocalDateTime> setOfLocalDateTime = new TreeSet<LocalDateTime>();

			setOfLocalDateTime.addAll(vatIncPriceMap.keySet());

			List<SlotCost> pricesPerSlot = new ArrayList<SlotCost>();

			for (LocalDateTime slot : setOfLocalDateTime) {

				long epochSecond = slot.atZone(ourZoneId).toEpochSecond();

				if (epochSecond >= halfHourAgo) {

					// this is recent: we are interested

					ImportExportData importExportData = vatIncPriceMap.get(slot);

					float importValueIncVat = importExportData.getImportPrice();

					Float exportValueIncVat = importExportData.getExportPrice(); // can be null if export=false

					SlotCost price = new SlotCost();

					price.setSimpleTimeStamp(slot.format(simpleTime));

					price.setEpochSecond(slot.atZone(ourZoneId).toEpochSecond());

					price.setImportPrice(importValueIncVat);

					price.setExportPrice(exportValueIncVat);

					pricesPerSlot.add(price);
				}
			}

			//
			//
			//

			if (export) {

				System.out.println("\nUpcoming best export price periods:");

				for (int slots = 1; slots < 11; slots++) { // each slot represents 30 minutes

					float highestAcc = -1;

					int indexOfHighest = 0;

					int limit = pricesPerSlot.size() - slots;

					for (int index = 0; index < limit; index++) {

						float accumulate = 0;

						for (int i = index; i < index + slots; i++) {

							accumulate += pricesPerSlot.get(i).getExportPrice();
						}

						if (-1 == highestAcc || accumulate > highestAcc) {

							highestAcc = accumulate;
							indexOfHighest = index;
						}
					}

					SlotCost price = pricesPerSlot.get(indexOfHighest);

					String simpleTimeStamp = price.getSimpleTimeStamp();

					Long epochSecond = price.getEpochSecond(); // the start of the period

					long epochSecondAtEndOfPeriod = epochSecond + 1800 * slots;

					float average = highestAcc / slots;

					int secondsInSlot = (int) (epochSecondAtEndOfPeriod - epochSecond);

					int hours = (int) (secondsInSlot / 3600);

					int minutes = (secondsInSlot % 3600) / 60;

					System.out.println((0 == hours ? "      " : String.format("%2d", hours) + " hr ")
							+ (0 == minutes ? "      " : String.format("%2d", minutes) + " min") + " period from "
							+ simpleTimeStamp + "  has average price: " + String.format("%5.2f", average) + "p");
				}
			}

			//
			//
			//

			ArrayList<Long> bestStartTime = new ArrayList<Long>();

			System.out.println("\nUpcoming best import price periods:");

			SlotCost cheapestSlot = null;

			// how many slots < epoch of noPriceDataFrom?

			int widestPeriod = extended;

			for (int period = 0; period < widestPeriod; period++) { // each period represents multiples of 30 minutes

				// for bestStartTime[] [0] will be 30 minutes, [1] 1hr [2] 1.5 hr ... [9] 5hr

				float lowestAcc = -1;

				int indexOfLowest = 0;

				int limit = pricesPerSlot.size() - 1 - period;

//				System.out.println("\t\tFor period " + period + " last time range considered will be from "
//						+ pricesPerSlot.get(limit - period).getSimpleTimeStamp() + " [" + (limit - period) + "] to "
//						+ pricesPerSlot.get(limit).getSimpleTimeStamp() + "   [" + limit + "]");

				for (int index = 0; index < limit - period + 1; index++) {

					float accumulate = 0;

					for (int i = index; i < index + period + 1; i++) {

						Float importPrice = pricesPerSlot.get(i).getImportPrice();

//						System.out.println("For period " + period + " at [" + i + "] "
//								+ pricesPerSlot.get(i).getSimpleTimeStamp() + " adding " + importPrice);

						accumulate += importPrice;
					}

					if (-1 == lowestAcc || accumulate < lowestAcc) {

						lowestAcc = accumulate;
						indexOfLowest = index;

//						System.out.println("\tFor period " + period + " the lowest acc so far is at index " + index
//								+ " " + pricesPerSlot.get(indexOfLowest).getSimpleTimeStamp());
					}
				}

				if (-1 == lowestAcc) {

					// restrict extended so that we only show definitive data

					extended = period;

					break; // this is because cannot prove we have found the lowest time
					// typically because there is no data after 22:30
				}

				SlotCost price = pricesPerSlot.get(indexOfLowest);

				String simpleTimeStamp = price.getSimpleTimeStamp();

				Long epochSecond = price.getEpochSecond();

				bestStartTime.add(epochSecond);

				Instant instant = Instant.ofEpochSecond(-60 + epochSecond + (period + 1) * 1800);

				LocalDateTime ldt = LocalDateTime.ofInstant(instant, ourZoneId);

				String periodEndTime = ldt.format(formatter24HourClock);

				if (0 == period) {

					cheapestSlot = price;
				}

				float average = lowestAcc / (period + 1); // the number of 30 minute periods in the slot

				int secondsInSlot = 1800 * (period + 1);

				int hours = (int) (secondsInSlot / 3600);

				int minutes = (secondsInSlot % 3600) / 60;

				System.out.println((0 == hours ? "      " : String.format("%2d", hours) + " hr ")
						+ (0 == minutes ? "      " : String.format("%2d", minutes) + " min") + " period from "
						+ simpleTimeStamp + " to " + periodEndTime + "  has average price: "
						+ String.format("%5.2f", average) + "p");
			}

			//
			//
			//

			int maxWidth = 0;

			for (SlotCost slotCost : pricesPerSlot) {
				// find highest price which determines width of asterisks

				Float importValueIncVat = slotCost.getImportPrice();

				if (importValueIncVat > maxWidth) {

					maxWidth = importValueIncVat.intValue();
				}
			}

			if (maxWidth > width) {

				maxWidth = width;
			}

			{
				StringBuffer sb = new StringBuffer();

				sb.append("\nCurrent & future import unit prices:");

				for (int n = (export ? -13 : -5); n < maxWidth; n++) {

					sb.append(' ');
				}

				sb.append('|');

				if (extended > 1) {

					String heads[] = { " 1hr |", " 1.5 |", " 2hr |", " 2.5 |", " 3hr |", " 3.5 |", " 4hr |", " 4.5 |",
							" 5hr |", " 5.5 |", " 6hr |", " 6.5 |", " 7hr |", " 7.5 |", " 8hr |", " 8.5 |", " 9hr |",
							" 9.5 |" };

					for (int e = 0; e < extended - 1; e++) {

						sb.append(heads[e]);
					}
				}

				if (extended > 0) {

					sb.append("HH:MM");
				}

				System.out.println(sb.toString());
			}

			StringBuffer sb1 = new StringBuffer();

			for (int index = 0; index < pricesPerSlot.size(); index++) {

				SlotCost slotCost = pricesPerSlot.get(index);

				Float importValueIncVat = slotCost.getImportPrice();

				Float exportValueIncVat = slotCost.getExportPrice(); // can be null;

				boolean cheapest = slotCost.equals(cheapestSlot);
				boolean lessThanAverage = importValueIncVat < averageUnitCost;

				sb1 = new StringBuffer();

				int n = 0;

				if (importValueIncVat <= plunge) {

					sb1.append(" <--- PLUNGE BELOW " + plunge + "p !!!");
					n = sb1.length();

				} else {

					boolean aboveTarget = false;

					for (; n < importValueIncVat && n < maxWidth; n++) {

						if (target == n) {

							aboveTarget = true;
							sb1.append(ANSI_COLOUR_ABOVE_TARGET);
							sb1.append('X');

						} else if (averageUnitCost == n) {

							sb1.append('A');

						} else {

							sb1.append('*');

						}
					}

					if (importValueIncVat == maxWidth) {

						sb1.append('>');
					}

					if (aboveTarget) {

						sb1.append(ANSI_RESET);
					}

				}

				String asterisks = sb1.toString();

				StringBuffer sb2 = new StringBuffer();

				for (; n < maxWidth + 1; n++) {

					sb2.append(' ');
				}

				sb2.append('|');

				String padding = sb2.toString();

				StringBuffer sb3 = new StringBuffer();

				if (extended > 1) {

					// calculate the average for 1hr/1.5hr/2hr... etc

					for (int i = 1; i < extended; i++) {

						float acc = 0;

						int count = 0;

						if (index + i < pricesPerSlot.size()) {

							for (int j = index; j < index + i + 1; j++) {

								acc += pricesPerSlot.get(j).getImportPrice();
								count++;
							}
						}

						if (count < (1 + i)) {

							sb3.append("     ");

						} else {

							// determine if this average price needs to be green

							if (ansi) {

								// n.b. i==1 represents 1 hr (i=0 unused: represents 30 mins)

								long bestPeriodStartsAt = bestStartTime.get(i); // inclusive
								long bestPeriodEndBefore = (i + 1) * 1800 + bestPeriodStartsAt; // exclusive

								long slotEpoch = slotCost.getEpochSecond();

								if (slotEpoch >= bestPeriodStartsAt && slotEpoch < bestPeriodEndBefore) {

									sb3.append(ANSI_COLOUR_FOREGROUND);
								}
							}

							sb3.append(String.format("%5.2f", acc / count));

							if (ansi) {

								sb3.append(ANSI_RESET);
							}

						}

						sb3.append('|');
					}
				}

				String prices = sb3.toString();

				String clockHHMM = "";

				if (extended > 0) {

					Long epochSecond = slotCost.getEpochSecond();

					Instant instant = Instant.ofEpochSecond(epochSecond);

					LocalDateTime ldt = LocalDateTime.ofInstant(instant, ourZoneId);

					clockHHMM = ldt.format(formatter24HourClock);
				}

				System.out.println((export ? String.format("%6.2f", exportValueIncVat) + "   " : "\t")
						+ slotCost.getSimpleTimeStamp() + "  " + (cheapest ? "!" : " ") + (lessThanAverage ? "!" : " ")
						+ "\t" + String.format("%5.2f", importValueIncVat) + "p  "
						+ (ansi & cheapest ? ANSI_COLOUR_FOREGROUND : "") + asterisks
						+ (ansi & cheapest ? ANSI_RESET : "") + padding + prices + clockHHMM);
			}

			System.exit(0);

		} catch (Exception e) {

			System.out.flush();

			e.printStackTrace();

			System.exit(-1);

		} finally {

			try {

				if (null != myReader) {

					myReader.close();
				}

				if (null != bw) {

					bw.close();
					bw = null;
				}

				if (null != fw) {

					fw.close();
					fw = null;
				}

			} catch (Exception e) {

//				e.printStackTrace();
				System.exit(-2);
			}
		}

	}

	private static boolean loadProperties(File externalPropertyFile) throws IOException {

		boolean external = false;

		properties = new Properties();

		InputStream is;

		if (null == externalPropertyFile || !externalPropertyFile.exists()) {

			ClassLoader cl = Thread.currentThread().getContextClassLoader();

			cl = ClassLoader.getSystemClassLoader();

			is = cl.getResourceAsStream("octopussy.properties");

		} else {

			is = new FileInputStream(externalPropertyFile);

			external = true;

		}

		properties.load(is);

		// if postcode=value specified, such as postcode=SN5
		// the region=value will be overriden by the associated code (such as H)

		String postcode = properties.getProperty("postcode", null);

		if (null != postcode) {

			V1GridSupplyPoints points = getV1GridSupplyPoints(postcode);

			ArrayList<V1GSP> pointList = points.getPointResults();

			V1GSP point = pointList.get(0);

			String groupId = point.getGroupId();

			properties.setProperty("region", groupId.substring(1));
		}

		extra = Boolean.valueOf(properties.getProperty("extra", "false").trim());

		// expand properties substituting $key$ values

		for (String key : properties.stringPropertyNames()) {

			String value = properties.getProperty(key);

			while (value.contains("$")) {

				int p = value.indexOf('$');

				int q = value.indexOf('$', 1 + p);

				String propertyKey = value.substring(1 + p, q);

				value = value.substring(0, p) + properties.getProperty(propertyKey) + value.substring(1 + q);

				if (extra) {

					System.err.println(key + "\t" + propertyKey + "\t" + value);
				}

				properties.setProperty(key, value);
			}
		}

		return external;
	}

	private V1ElectricityConsumption getV1ElectricityConsumption(Integer page, Integer pageSize, String periodFrom,
			String periodTo) throws MalformedURLException, IOException {

		String mprn = properties.getProperty("electricity.mprn").trim();

		String sn = properties.getProperty("electricity.sn").trim();

		String json = instance.getRequest(new URL(
				"https://api.octopus.energy/v1/electricity-meter-points/" + mprn + "/meters/" + sn + "/consumption/" +

						"?order_by=period" + (null == page ? "" : "&page=" + page)
						+ (null == pageSize ? "" : "&page_size=" + pageSize)
						+ (null == periodFrom ? "" : "&period_from=" + periodFrom)
						+ (null == periodTo ? "" : "&period_to=" + periodTo) + "&order_by=period"));

		V1ElectricityConsumption result = null;

		if (null == json || 0 == json.trim().length()) {

			System.err.println("Error obtaining consumption data. Check the apiKey!");

			result = new V1ElectricityConsumption(); // empty object

		} else {

			result = mapper.readValue(json, V1ElectricityConsumption.class);
		}

		return result;
	}

	private V1AgileFlex getV1AgileFlexImport(Integer pageSize, String periodFrom, String periodTo)
			throws MalformedURLException, IOException {

		String spec = properties.getProperty("tariff.url").trim() + "/standard-unit-rates/" + "?page_size=" + pageSize
				+ (null == periodFrom ? "" : "&period_from=" + periodFrom)
				+ (null == periodTo ? "" : "&period_to=" + periodTo);

		String json = getRequest(new URL(spec), false);

		V1AgileFlex result = mapper.readValue(json, V1AgileFlex.class);

		return result;
	}

	private V1AgileFlex getV1AgileFlexExport(Integer pageSize, String periodFrom, String periodTo)
			throws MalformedURLException, IOException {

		String spec = properties.getProperty("export.tariff.url").trim() + "/standard-unit-rates/" + "?page_size="
				+ pageSize + (null == periodFrom ? "" : "&period_from=" + periodFrom)
				+ (null == periodTo ? "" : "&period_to=" + periodTo);

		String json = getRequest(new URL(spec), false);

		V1AgileFlex result = mapper.readValue(json, V1AgileFlex.class);

		return result;
	}

	private static V1GridSupplyPoints getV1GridSupplyPoints(String postcode) throws MalformedURLException, IOException {

		String spec = properties.getProperty("base.url").trim() + "/v1/industry/grid-supply-points/" + "?postcode="
				+ postcode;

		String json = getRequest(new URL(spec), false);

		V1GridSupplyPoints result = mapper.readValue(json, V1GridSupplyPoints.class);

		return result;
	}

	private String getRequest(URL url) throws IOException {

		return getRequest(url, true);
	}

	private static String getRequest(URL url, boolean authorisationRequired) throws IOException {

		int status;

		HttpURLConnection con = null;

		con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		con.setRequestProperty("Content-Type", contentType);
		con.setRequestProperty("user-agent", userAgent);

		if (authorisationRequired) {

			con.setRequestProperty("Authorization", properties.getProperty("basic"));
		}

		con.connect();

		status = con.getResponseCode();

		String json = "";

		if (200 == status) {

			BufferedReader in = null;

			try {

				in = new BufferedReader(new InputStreamReader(con.getInputStream()));

				String inputLine;
				StringBuffer content = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {

					content.append(inputLine);
				}

				json = content.toString();

			} catch (IOException e) {

				e.printStackTrace();
			}

			if (null != in) {

				in.close();
			}
		}

		if (null != con) {

			con.disconnect();
		}

		return json;
	}

	private V1GasConsumption getV1GasConsumption(String periodFrom, String periodTo)
			throws MalformedURLException, IOException {

		String mprn = properties.getProperty("gas.mprn").trim();

		String sn = properties.getProperty("gas.sn").trim();

		String json = instance.getRequest(
				new URL("https://api.octopus.energy/v1/gas-meter-points/" + mprn + "/meters/" + sn + "/consumption/" +

						"?page_size=100&period_from=" + periodFrom + "&period_to=" + periodTo + "&order_by=period"));

		V1GasConsumption result = mapper.readValue(json, V1GasConsumption.class);

		return result;
	}

}
