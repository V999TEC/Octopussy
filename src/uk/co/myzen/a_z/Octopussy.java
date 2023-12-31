/**
 * 
 */
package uk.co.myzen.a_z;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.co.myzen.a_z.json.Agile;
import uk.co.myzen.a_z.json.Agreement;
import uk.co.myzen.a_z.json.Detail;
import uk.co.myzen.a_z.json.ElectricityMeterPoint;
import uk.co.myzen.a_z.json.GasMeterPoint;
import uk.co.myzen.a_z.json.Meter;
import uk.co.myzen.a_z.json.Prices;
import uk.co.myzen.a_z.json.V1Account;
import uk.co.myzen.a_z.json.V1AgileFlex;
import uk.co.myzen.a_z.json.V1Charges;
import uk.co.myzen.a_z.json.V1ElectricityConsumption;
import uk.co.myzen.a_z.json.V1GSP;
import uk.co.myzen.a_z.json.V1GridSupplyPoints;
import uk.co.myzen.a_z.json.V1PeriodConsumption;

/**
 * @author howard
 *
 */

public class Octopussy {

	public static LocalDateTime now; // Initialised when singleton instance created

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

	private final static String DEFAULT_PROPERTY_FILENAME = "./octopussy.properties";

	private final static String DEFAULT_APIKEY_PROPERTY = "blah_BLAH2pMoreBlahPIXOIO72aIO1blah:";
	private final static String DEFAULT_BASE_URL_PROPERTY = "https://api.octopus.energy";
	private final static String DEFAULT_HISTORY_PROPERTY = "octopus.import.csv";
	private final static String DEFAULT_TARIFF_URL_PROPERTY = "";
	private final static String DEFAULT_DAYS_PROPERTY = "10";
	private final static String DEFAULT_YEARLY_PROPERTY = "false";
	private final static String DEFAULT_MONTHLY_PROPERTY = "false";
	private final static String DEFAULT_WEEKLY_PROPERTY = "false";
	private final static String DEFAULT_DAILY_PROPERTY = "false";
	private final static String DEFAULT_FLEXIBLE_ELECTRICITY_VIA_DIRECT_DEBIT_PROPERTY = "true";
	private final static String DEFAULT_FLEXIBLE_ELECTRICITY_PRODUCT_CODE_PROPERTY = "VAR-22-11-01";
	private final static String DEFAULT_FLEXIBLE_ELECTRICITY_UNIT_PROPERTY = "30.295124";
	private final static String DEFAULT_FLEXIBLE_ELECTRICITY_STANDING_PROPERTY = "47.9535";
	private final static String DEFAULT_AGILE_ELECTRICITY_STANDING_PROPERTY = "42.7665";
	private final static String DEFAULT_POSTCODE_PROPERTY = "?";
	private final static String DEFAULT_REGION_PROPERTY = "H";
	private final static String DEFAULT_CHECK_PROPERTY = "false";
	private final static String DEFAULT_EXTRA_PROPERTY = "false";
	private final static String DEFAULT_EXTRA2_PROPERTY = "false";
	private final static String DEFAULT_EXTRA3_PROPERTY = "false";
	private final static String DEFAULT_EXTRA4_PROPERTY = "false";
	private final static String DEFAULT_EXTRA5_PROPERTY = "false";

	private final static String DEFAULT_ELECTRICTY_MPRN_PROPERTY = "200001010163";
	private final static String DEFAULT_ELECTRICTY_SN_PROPERTY = "21L010101";

	private final static String DEFAULT_EXPORT_PROPERTY = "false";
	private final static String DEFAULT_EXPORT_PRODUCT_CODE_PROPERTY = "AGILE-OUTGOING-19-05-13";
	private final static String DEFAULT_EXPORT_TARIFF_CODE_PROPERTY = "E-1R-" + DEFAULT_EXPORT_PRODUCT_CODE_PROPERTY
			+ "-" + DEFAULT_REGION_PROPERTY;
	private final static String DEFAULT_EXPORT_TARIFF_URL_PROPERTY = DEFAULT_BASE_URL_PROPERTY + "/v1/products/"
			+ DEFAULT_EXPORT_PRODUCT_CODE_PROPERTY + "/electricity-tariffs/" + DEFAULT_EXPORT_TARIFF_CODE_PROPERTY;

	private final static String DEFAULT_WIDTH_PROPERTY = "46";
	private final static String DEFAULT_ANSI_PROPERTY = "false";
	private final static String DEFAULT_COLOUR_PROPERTY = "GREEN";
	private final static String DEFAULT_COLOR_PROPERTY = "RED";

	private final static String DEFAULT_PLUNGE_PROPERTY = "3";
	private final static String DEFAULT_TARGET_PROPERTY = "30";
	private final static String DEFAULT_ZONE_ID_PROPERTY = "Europe/London";

	private final static String DEFAULT_DAY_FROM_PROPERTY = "";
	private final static String DEFAULT_DAY_TO_PROPERTY = "";

	private final static String KEY_APIKEY = "apiKey";
	private final static String KEY_BASE_URL = "base.url";
	private final static String KEY_ELECTRICITY_MPRN = "electricity.mprn";
	private final static String KEY_ELECTRICITY_SN = "electricity.sn";
	private final static String KEY_GAS_MPRN = "gas.mprn";
	private final static String KEY_GAS_SN = "gas.sn";
	private final static String KEY_FLEXIBLE_GAS_PRODUCT_CODE = "flexible.gas.product.code";
	private final static String KEY_FLEXIBLE_GAS_UNIT = "flexible.gas.unit";
	private final static String KEY_FLEXIBLE_GAS_STANDING = "flexible.gas.standing";
	private final static String KEY_FLEXIBLE_ELECTRICITY_VIA_DIRECT_DEBIT = "flexible.electricity.via.direct.debit";
	private final static String KEY_FLEXIBLE_ELECTRICITY_PRODUCT_CODE = "flexible.electricity.product.code";
	private final static String KEY_FLEXIBLE_ELECTRICITY_UNIT = "flexible.electricity.unit";
	private final static String KEY_FLEXIBLE_ELECTRICITY_STANDING = "flexible.electricity.standing";
	private final static String KEY_AGILE_ELECTRICITY_STANDING = "agile.electricity.standing";
	private final static String KEY_IMPORT_PRODUCT_CODE = "import.product.code";
	private final static String KEY_TARIFF_CODE = "tariff.code";
	private final static String KEY_TARIFF_URL = "tariff.url";
	private final static String KEY_REGION = "region";
	private final static String KEY_POSTCODE = "postcode";

	private final static String KEY_ZONE_ID = "zone.id";
	private final static String KEY_HISTORY = "history";

	private final static String KEY_EXPORT_PRODUCT_CODE = "export.product.code";
	private final static String KEY_EXPORT_TARIFF_CODE = "export.tariff.code";
	private final static String KEY_EXPORT_TARIFF_URL = "export.tariff.url";
	private final static String KEY_EXPORT = "export";

	private final static String KEY_DAYS = "days";
	private final static String KEY_PLUNGE = "plunge";
	private final static String KEY_TARGET = "target";
	private final static String KEY_WIDTH = "width";

	private final static String KEY_ANSI = "ansi";
	private final static String KEY_COLOUR = "colour";
	private final static String KEY_COLOR = "color";

	private final static String KEY_YEARLY = "yearly";
	private final static String KEY_MONTHLY = "monthly";
	private final static String KEY_WEEKLY = "weekly";
	private final static String KEY_DAILY = "daily";

	private final static String KEY_DAY_FROM = "day.from";
	private final static String KEY_DAY_TO = "day.to";

	private final static String KEY_CHECK = "check";
	private final static String KEY_EXTRA = "extra";
	private final static String KEY_EXTRA2 = "extra2";
	private final static String KEY_EXTRA3 = "extra3";
	private final static String KEY_EXTRA4 = "extra4";
	private final static String KEY_EXTRA5 = "extra5";

	private final static String KEY_REFERRAL = "referral";

	private final static String[] defaultPropertyKeys = { KEY_APIKEY, "#", KEY_BASE_URL, "#", KEY_ELECTRICITY_MPRN,
			KEY_ELECTRICITY_SN, KEY_GAS_MPRN, KEY_GAS_SN, KEY_FLEXIBLE_GAS_PRODUCT_CODE, KEY_FLEXIBLE_GAS_UNIT,
			KEY_FLEXIBLE_GAS_STANDING, KEY_FLEXIBLE_ELECTRICITY_VIA_DIRECT_DEBIT, KEY_FLEXIBLE_ELECTRICITY_PRODUCT_CODE,
			KEY_FLEXIBLE_ELECTRICITY_UNIT, KEY_FLEXIBLE_ELECTRICITY_STANDING, KEY_AGILE_ELECTRICITY_STANDING,
			KEY_IMPORT_PRODUCT_CODE, KEY_TARIFF_CODE, KEY_TARIFF_URL, KEY_REGION, KEY_POSTCODE, KEY_ZONE_ID,
			KEY_HISTORY, "#", KEY_EXPORT_PRODUCT_CODE, KEY_EXPORT_TARIFF_CODE, KEY_EXPORT_TARIFF_URL, KEY_EXPORT, "#",
			KEY_DAYS, KEY_PLUNGE, KEY_TARGET, KEY_WIDTH, KEY_ANSI, KEY_COLOUR, KEY_COLOR, "#", KEY_YEARLY, KEY_MONTHLY,
			KEY_WEEKLY, KEY_DAILY, KEY_DAY_FROM, KEY_DAY_TO, "#", KEY_CHECK, KEY_EXTRA, KEY_EXTRA2, KEY_EXTRA3,
			KEY_EXTRA4, KEY_EXTRA5, "#", KEY_REFERRAL };

	private final static DateTimeFormatter simpleTime = DateTimeFormatter.ofPattern("E MMM dd pph:mm a");

	private final static DateTimeFormatter formatter24HourClock = DateTimeFormatter.ofPattern("HH:mm");

	private final static DateTimeFormatter formatter12HourClock = DateTimeFormatter.ofPattern("h:mm a");

	private final static DateTimeFormatter formatterDayHourMinute = DateTimeFormatter.ofPattern("E HH:mm");

	private final static DateTimeFormatter formatterDayHourMinuteSecond = DateTimeFormatter.ofPattern("E HH:mm:ss");

	private final static DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	private final static DateTimeFormatter formatterLocalDate = DateTimeFormatter.ISO_LOCAL_DATE;

	private static ZoneId ourZoneId;

	private static ObjectMapper mapper;

	private static int width;

	private static int plunge;

	private static int target;

	private static boolean ansi;

	private static boolean export;

	private static String check = DEFAULT_CHECK_PROPERTY; // overridden by check=value in properties

	private static String[] extra = { DEFAULT_EXTRA_PROPERTY, DEFAULT_EXTRA2_PROPERTY, DEFAULT_EXTRA3_PROPERTY,
			DEFAULT_EXTRA4_PROPERTY, DEFAULT_EXTRA5_PROPERTY };

	private static boolean usingExternalPropertyFile = false;

	private static int extended = 0; // overridden by args[]

	private static Properties properties;

	private static Octopussy instance = null;

	private static String ANSI_COLOUR_LO; // typically GREEN
	private static String ANSI_COLOR_HI; // typically RED

	private static long epochFrom = 0;

	private static Map<Long, ConsumptionHistory> history = null;

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

		now = LocalDateTime.now();
	}

	/**
	 * @param args
	 * @throws Exception
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws Exception {

		instance = getInstance();

		File importData = null;

		String propertyFileName = DEFAULT_PROPERTY_FILENAME;

		String propertyAccountId = null; // the default

		try {
			if (args.length > 0) {

				extended = Integer.parseInt(args[0].trim());

				if (extended > 19) {

					extended = 19;

				} else if (extended < 0) {

					extended = 0;
				}

				if (args.length > 1) {

					// assume the optional second parameter is the name of a property file
					// which will be used to override the built-in resource octopussy.properties

					propertyFileName = args[1].trim();

					if (args.length > 2) {

						// assume the optional third parameter is the account identifier

						propertyAccountId = args[2].trim();
					}
				}
			}

			instance.dealWithProperties(propertyFileName);

			if (null != propertyAccountId) { // optional verification of Account verses dot.properties

				V1Account details = instance.getV1AccountData(1, propertyAccountId);

				System.out.println("# Verifying these values in the properties file " + propertyFileName + "\n");

				Map<String, String> verify = instance.displayAccountDetails(details);

				for (String key : defaultPropertyKeys) {

					if (verify.containsKey(key)) {

						String value = verify.get(key);

						System.out.println((KEY_POSTCODE.equals(key) ? "#" : "") + key + "=" + value);
					}
				}

				int errors = instance.validateProperties(verify);

				if (0 != errors) {

					System.err.println(
							errors + " discrepencies in " + propertyFileName + " - Please edit and validate again.");
					System.exit(-1);
				}

				System.out.println("\n# " + propertyFileName + " file verified.");
			}

			importData = new File(properties.getProperty(KEY_HISTORY, DEFAULT_HISTORY_PROPERTY).trim());

			history = instance.readHistory(importData);

			// here the octopus.import.csv or similar will exist even if empty

			//
			//
			//

			ZonedDateTime ourTimeNow = now.atZone(ourZoneId);

			long epochNow = ourTimeNow.toEpochSecond();

			// get today as YYYY-MM-DD
			String today = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochNow), ourZoneId).toString().substring(0,
					10);

			int dayOfYearToday = ourTimeNow.getDayOfYear();

			// We define a recent history starting at the configured number of days ago
			// and include the remainder of today in the time span

			// N.B. we are unlikely to get data beyond 22:30 local if this is run before
			// 16:00
			// at which time when the schedule of prices appear for the next day

			// since we will get a variable amount of data returned we will not pin the
			// periodEnd
			// and instead just grab whatever is available at the time the method is called

			int howManyDaysHistory = Integer.valueOf(properties.getProperty(KEY_DAYS, DEFAULT_DAYS_PROPERTY).trim());

			ZonedDateTime timeRecent;

			if (dayOfYearToday < howManyDaysHistory) {

				timeRecent = ourTimeNow.minusDays(howManyDaysHistory).withHour(00).withMinute(00).withSecond(00)
						.withNano(0);
			} else {

				timeRecent = ourTimeNow.withDayOfYear(dayOfYearToday - howManyDaysHistory).withHour(00).withMinute(00)
						.withSecond(00).withNano(0);
			}

			// UTC is one hour behind BST in summer time

			ZonedDateTime zuluBegin = timeRecent.withZoneSameInstant(ZoneId.of("UTC"));

			Long upToEpochSecond = zuluBegin.toEpochSecond();

			// In summer time expecting something like 2023-08-27T23:00Z

			String beginRecentPeriod = zuluBegin.toString().substring(0, 17);

			Integer pageSize = 48 * (1 + howManyDaysHistory);

			// we hope to get this in a single page

			Integer page = 1;

			V1AgileFlex v1AgileFlex = instance.getV1AgileFlexImport(page, pageSize, beginRecentPeriod, null);

			ArrayList<Agile> agileResultsImport = v1AgileFlex.getAgileResults();

			while (null != v1AgileFlex.getNext()) {

				// we don't yet have all the results - pause and then get the next page

				Thread.sleep(5000); // this is just so we don't bombard the API provider

				page++;

				v1AgileFlex = instance.getV1AgileFlexImport(page, pageSize, beginRecentPeriod, null);

				ArrayList<Agile> pageAgileResults = v1AgileFlex.getAgileResults();

				agileResultsImport.addAll(pageAgileResults);
			}

			ArrayList<Agile> agileResultsExport = null; // only populated if export=true in octopussy.properties

			if (export) {

				v1AgileFlex = instance.getV1AgileFlexExport(48 * howManyDaysHistory, beginRecentPeriod, null);

				agileResultsExport = v1AgileFlex.getAgileResults();
			}

			Map<LocalDateTime, ImportExportData> importExportPriceMap = instance.createPriceMap(agileResultsImport,
					agileResultsExport);

			SortedSet<LocalDateTime> ascendingKeysForPriceMap = new TreeSet<LocalDateTime>();

			ascendingKeysForPriceMap.addAll(importExportPriceMap.keySet());

			//
			//
			//

			page = 1;

			// we hope to get this in a single page

			V1ElectricityConsumption v1ElectricityConsumption = instance.getV1ElectricityConsumption(page,
					48 * howManyDaysHistory, beginRecentPeriod, null);

			ArrayList<V1PeriodConsumption> periodResults = v1ElectricityConsumption.getPeriodResults();

			// the above call requires authentication, so remind the user about apiKey etc.,

			if (null == periodResults) {

				throw new Exception(
						"\r\nDouble check the apiKey, electricity.mprn & electricity.sn values in the octopussy.properties\r\n"
								+ "An option is to create a new octopussy.properties file in the current directory with the correct values\r\n"
								+ "A template octopussy.properties file can be redisplayed by deleting the octopus.import.csv and running again\r\n"
								+ "Alternatively replace the resource octopussy.properties inside the jar file using 7-Zip or similar\r\n");
			}

			while (null != v1ElectricityConsumption.getNext()) {

				// we don't yet have all the results - pause and then get the next page

				Thread.sleep(5000); // this is just so we don't bombard the API provider

				page++;

				v1ElectricityConsumption = instance.getV1ElectricityConsumption(page, 48 * howManyDaysHistory,
						beginRecentPeriod, null);

				ArrayList<V1PeriodConsumption> pagePeriodResults = v1ElectricityConsumption.getPeriodResults();

				periodResults.addAll(pagePeriodResults);
			}

			periodResults = instance.updateHistory(beginRecentPeriod, periodResults, howManyDaysHistory,
					v1ElectricityConsumption.getCount());

			//
			//
			//

			Map<String, DayValues> elecMapDaily = instance.buildElecMapDaily(periodResults, importExportPriceMap);

			//
			//
			//

			instance.appendToHistoryFile(importData, history);

			//
			//
			//

			Integer fromEpochDayIncl = null;
			Integer toEpochDayIncl = null;

			LocalDate fromIncl = null; // default

			String filterFrom = properties.getProperty(KEY_DAY_FROM, DEFAULT_DAY_FROM_PROPERTY).trim();

			if (!"".equals(filterFrom)) {

				fromIncl = LocalDate.parse(filterFrom, formatterLocalDate);

				fromEpochDayIncl = Long.valueOf(fromIncl.getLong(ChronoField.EPOCH_DAY)).intValue();
			}

			// beginRecentPeriod

			long requiredEpochSecond = -1; // default

			String filterTo = properties.getProperty(KEY_DAY_TO, DEFAULT_DAY_TO_PROPERTY).trim();

			if ("".equals(filterTo)) {

				if (!"".equals(filterFrom)) {

					requiredEpochSecond = epochNow;
				}

			} else {

				LocalDate toIncl = LocalDate.parse(filterTo, formatterLocalDate);

				toEpochDayIncl = Long.valueOf(toIncl.getLong(ChronoField.EPOCH_DAY)).intValue();

				requiredEpochSecond = 86400L * (1 + toIncl.getLong(ChronoField.EPOCH_DAY));
			}

			//
			//
			//

			if (Boolean.TRUE.equals(Boolean.valueOf(properties.getProperty(KEY_YEARLY, DEFAULT_YEARLY_PROPERTY)))) {

				SortedMap<Integer, PeriodicValues> yearly = accumulateCostsByField(ChronoField.YEAR, upToEpochSecond);

				System.out.println("\nHistorical yearly results:");

				displayPeriodSummary("Year", yearly, null, null);
			}

			//
			//
			//

			if (Boolean.TRUE.equals(Boolean.valueOf(properties.getProperty(KEY_MONTHLY, DEFAULT_MONTHLY_PROPERTY)))) {

				SortedMap<Integer, PeriodicValues> monthly = accumulateCostsByField(ChronoField.MONTH_OF_YEAR,
						upToEpochSecond);

				System.out.println("\nHistorical monthly results:");

				displayPeriodSummary("Month", monthly, null, null);
			}

			//
			//
			//

			if (Boolean.TRUE.equals(Boolean.valueOf(properties.getProperty(KEY_WEEKLY, DEFAULT_WEEKLY_PROPERTY)))) {

				SortedMap<Integer, PeriodicValues> weekly = accumulateCostsByField(ChronoField.ALIGNED_WEEK_OF_YEAR,
						upToEpochSecond);

				System.out.println("\nHistorical weekly results:");

				displayPeriodSummary("Week", weekly, null, null);
			}
			//
			//
			//

			if (Boolean.TRUE.equals(Boolean.valueOf(properties.getProperty(KEY_DAILY, DEFAULT_DAILY_PROPERTY)))) {

				// get epochSecond for start of next day of range

				SortedMap<Integer, PeriodicValues> daily = accumulateCostsByField(ChronoField.EPOCH_DAY,
						requiredEpochSecond < 0 ? upToEpochSecond : requiredEpochSecond);

				System.out.println("\nHistorical daily results: " + ("".equals(filterFrom) ? "" : " from " + filterFrom)
						+ ("".equals(filterTo) ? "" : " up to " + filterTo));

				displayPeriodSummary("Daily", daily, fromEpochDayIncl, toEpochDayIncl);
			}

			//
			//
			//

			int averageUnitCost = instance.dailyResults(today, elecMapDaily);

			//
			//
			//

			// filter by half an hour ago

			List<SlotCost> pricesPerSlot = instance.buildListSlotCosts(epochNow - 1800, ascendingKeysForPriceMap,
					importExportPriceMap);

			//
			//
			//

			ArrayList<Long> bestExportTime = null;

			if (export) {

				bestExportTime = instance.upcomingExport(pricesPerSlot);
			}

			//
			//
			//

			String[] schedule = instance.readChargingSchedule();

			//
			//
			//

			schedule = instance.scheduleBatteryCharging(pricesPerSlot, schedule);

			//
			//
			//

			ArrayList<Long> bestImportTime = instance.upcomingImport(pricesPerSlot);

			//
			//
			//

			instance.showAnalysis(pricesPerSlot, averageUnitCost, bestImportTime, bestExportTime, schedule);

			//
			//
			//

			// ADVANCED: check for a groupN=a,b,c etc and build a List of Set<Integer>

			List<Set<Integer>> sampleGroups = instance.loadDeviceGroups();

			// ADVANCED: check for a device profile sampleN=name

			instance.matchDevices(pricesPerSlot, sampleGroups);

			System.exit(0);

		} catch (Exception e) {

			System.out.flush();

			e.printStackTrace();

			System.exit(-1);
		}
	}

	private String[] readChargingSchedule() {

		String[] result = new String[5];

		String[] readEndTimesParameter = { "65", "103", "106", "109", "112" };

		// avoid setting the from/to times at the inverter if no change to previous
		// values for AC Charge 1 End Time

		for (int s = 0; s < 5; s++) {

			if ("false".equalsIgnoreCase(check)) {

				result[s] = null;

			} else {

				// what is the current 'to' time in the inverter for Slot 1/2/3/4/5?

				result[s] = execRead(check, readEndTimesParameter[s]);

				// System.err.println("Charging Slot " + String.valueOf(1 + s) + " ends at " +
				// result[s]);
			}
		}

		return result;
	}

	private List<Set<Integer>> loadDeviceGroups() {

		List<Set<Integer>> results = new ArrayList<Set<Integer>>();

		int groupNumber = 0;

		while (properties.containsKey("group" + String.valueOf(groupNumber))) {

			String csvDevices = properties.getProperty("group" + String.valueOf(groupNumber));

			String[] csv = csvDevices.split(",");

			Set<Integer> group = new HashSet<Integer>(csv.length);

			for (String value : csv) {

				Integer number = Integer.valueOf(value);

				group.add(number);
			}

			results.add(group);

			groupNumber++;
		}

		return results;
	}

	private void matchDevices(List<SlotCost> pricesPerSlot, List<Set<Integer>> sampleGroups)
			throws FileNotFoundException {

		List<List<PowerDuration>> allSamples = new ArrayList<List<PowerDuration>>();

		int lastSlot = pricesPerSlot.size() - 1;

		String startTime = pricesPerSlot.get(0).getSimpleTimeStamp();
		String stopTime = pricesPerSlot.get(lastSlot).getSimpleTimeStamp();

		System.out.println("\nPrice data available for half-hour slots in the range " + startTime + " and " + stopTime);

		int sampleNumber = 0;

		while (properties.containsKey("sample" + String.valueOf(sampleNumber))) {

			int accSeconds = 0;

			List<PowerDuration> offsetPowerList = new ArrayList<PowerDuration>();

			File profileName = new File(properties.getProperty("sample" + String.valueOf(sampleNumber)));

			Scanner myReader = new Scanner(profileName);

			while (myReader.hasNextLine()) {

				String line = myReader.nextLine();

				String[] parts = line.split("\t");

				if (20 == parts[0].length() || ('T' == parts[0].charAt(10) && parts[0].endsWith("Z"))) {

					String value = parts[1].trim();

					float power = 0;

					power = "0.0".equals(value) ? 0 : Float.parseFloat(value);

					Integer duration = Integer.parseInt(parts[3].trim());

					// add an array of OffsetPower for matching against timeslots in the next 24
					// hours or so

					PowerDuration offsetPower = new PowerDuration();

					offsetPower.setPower(power);

					offsetPower.setSecsDuration(duration);

					offsetPowerList.add(offsetPower);

					accSeconds += duration;

				} else {

					int pos = line.indexOf("kWhr consumed ");

					if (line.contains("day(s) from:") && -1 != pos) {

						// assume last significant line for this device

						Float.parseFloat(line.substring(pos - 10, pos).trim());

						break;
					}
				}
			}

			myReader.close();

			int lastIndex = offsetPowerList.size() - 1;

			// if zero power on last element, remove it from list

			while (0 == offsetPowerList.get(lastIndex).getPower()) {

				offsetPowerList.remove(lastIndex);
				lastIndex--;
			}

			// if zero power on first element, remove it from list

			while (0 == offsetPowerList.get(0).getPower()) {

				offsetPowerList.remove(0);
			}

			allSamples.add(sampleNumber, offsetPowerList);

			int group = -1;

			for (int index = 0; index < sampleGroups.size(); index++) {

				for (Integer value : sampleGroups.get(index)) {

					if (value.equals(Integer.valueOf(sampleNumber))) {

						group = index;

						break;
					}
				}

				if (-1 != group) {

					break;
				}
			}

			if (-1 == group) {

				// now deduce the best time to start this device based on the sampled energy
				// profile

				processSample(pricesPerSlot, sampleNumber, accSeconds, profileName.getName(), offsetPowerList);
			}

			sampleNumber++;
		}

		// now deal with the deferred samples that need to be combined

		for (int groupIndex = 0; groupIndex < sampleGroups.size(); groupIndex++) {

			List<PowerDuration> averageOffsetPowerList = new ArrayList<PowerDuration>();

			// create 1-second granularity list to merge multiple samples and deduce an
			// average profile

			for (Integer sampleIndex : sampleGroups.get(groupIndex)) {

				List<PowerDuration> offsetPowerList = allSamples.get(sampleIndex);

				averageOffsetPowerList = expandMergePowerList(averageOffsetPowerList, offsetPowerList, 0L);

				String name = properties.getProperty("sample" + String.valueOf(sampleIndex));

				System.out.println("group" + groupIndex + " : " + name);
			}

			int sampleSize = sampleGroups.get(groupIndex).size();

			// now find average power for each second by dividing by sample size

			int secondsInList = averageOffsetPowerList.size();

			for (int s = 0; s < secondsInList; s++) {

				PowerDuration pdMerge = averageOffsetPowerList.get(s);

				float power = averageOffsetPowerList.get(s).getPower();

				float avWatts = power / sampleSize;

				pdMerge.setPower(avWatts);

				averageOffsetPowerList.set(s, pdMerge);
			}

			processSample(pricesPerSlot, -1, secondsInList, "group" + groupIndex, averageOffsetPowerList);

			try {

				String fileName = "Samples-" + properties.getProperty("group" + groupIndex) + ".log";

				compressPowerList(fileName, averageOffsetPowerList);

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	private List<PowerDuration> expandMergePowerList(List<PowerDuration> averageOffsetPowerList,
			List<PowerDuration> offsetPowerList, long startAtEpochSecond) {

		int offsetIntoAverageList = 0;

		for (int i = 0; i < offsetPowerList.size(); i++) {

			PowerDuration pd = offsetPowerList.get(i);

			Integer duration = pd.getSecsDuration();

			Float power = pd.getPower();

			for (int d = 0; d < duration.intValue(); d++) {

				PowerDuration pdMerge = null;

				int last = averageOffsetPowerList.size() - 1;

				if (offsetIntoAverageList > last) {

					// need to extend the list

					pdMerge = new PowerDuration();

					pdMerge.setEpochSecond(startAtEpochSecond);
					pdMerge.setSecsDuration(1);

					pdMerge.setPower(power);

					averageOffsetPowerList.add(pdMerge);

					startAtEpochSecond++;

				} else {

					// need to update existing entry the list

					pdMerge = averageOffsetPowerList.get(offsetIntoAverageList);

					pdMerge.setPower(power + pdMerge.getPower());
				}

				offsetIntoAverageList++;
			}
		}

		return averageOffsetPowerList;
	}

	private void dumpPowerList(String fileName, List<PowerDuration> offsetPowerList) throws IOException {

		File file = new File(fileName);

		if (file.createNewFile()) {

		} else {

			// file already exists
		}

		OffsetDateTime odt = OffsetDateTime.ofInstant(Instant.now(), ourZoneId).withNano(0).withSecond(0);

		long fromDay = odt.get(ChronoField.DAY_OF_YEAR);

		String fromHHMM = odt.format(formatter24HourClock);

		FileWriter fw = new FileWriter(file, false);

		BufferedWriter bw = new BufferedWriter(fw);

		String upTo = odt.plusSeconds(offsetPowerList.size()).format(defaultDateTimeFormatter);

		bw.write(fileName + " \"SmartPlug\" " + odt.format(defaultDateTimeFormatter) + " " + upTo);

		bw.newLine();

		int secsUsingPower = 0;

		float accWattSeconds = 0f;

		float wattSeconds;

		final int lastLine = offsetPowerList.size() - 1;

		for (int line = 0; line < lastLine + 1; line++) {

			PowerDuration pd = offsetPowerList.get(line);

			Float power = pd.getPower();

			Integer secsDuration = pd.getSecsDuration();

			if (power > 0) {

				secsUsingPower += secsDuration;
			}

			wattSeconds = power * secsDuration;

			accWattSeconds += wattSeconds;

			bw.write(odt.format(defaultDateTimeFormatter) + "\t" + String.format("%7.1f", power) + "\twatts for\t"
					+ String.format("%8d", secsDuration) + "\tseconds\t" + String.format("%10.2f", wattSeconds)
					+ " watt-seconds ( " + String.format("%12.2f", accWattSeconds) + " accumulated)");
			bw.newLine();

			odt = odt.plusSeconds(secsDuration);
		}

		float kWhr = accWattSeconds / 3600 / 1000;

		long toDay = odt.get(ChronoField.DAY_OF_YEAR);

		String toHHMM = odt.format(formatter24HourClock);

		bw.write((toDay - fromDay + 1) + " day(s) from: " + fromHHMM + " on day " + fromDay + " to " + toHHMM
				+ " on day " + toDay + " " + String.format("%8.3f", kWhr) + " kWhr consumed via SmartPlug ( "
				+ secsUsingPower + " secs using power)");

		bw.newLine();

		bw.close();

		fw.close();
	}

	private void compressPowerList(String fileName, List<PowerDuration> averageOffsetPowerList) throws IOException {

		File file = new File(fileName);

		if (file.createNewFile()) {

		} else {

			// file already exists
		}

		OffsetDateTime odt = OffsetDateTime.ofInstant(Instant.now(), ourZoneId).withNano(0).withSecond(0);

		long fromDay = odt.get(ChronoField.DAY_OF_YEAR);

		String fromHHMM = odt.format(formatter24HourClock);

		FileWriter fw = new FileWriter(file, false);

		BufferedWriter bw = new BufferedWriter(fw);

		String upTo = odt.plusSeconds(averageOffsetPowerList.size()).format(defaultDateTimeFormatter);

		bw.write(fileName + " \"SmartPlug\" " + odt.format(defaultDateTimeFormatter) + " " + upTo);

		bw.newLine();

		int secsUsingPower = 0;

		float accWattSeconds = 0f;

		float wattSeconds;

		int deferredSeconds = 0;

		final int lastLine = averageOffsetPowerList.size() - 1;

		for (int line = 0; line < lastLine + 1; line++) {

			PowerDuration pd = averageOffsetPowerList.get(line);

			Float power = +pd.getPower();

			Integer secsDuration = pd.getSecsDuration();

			if (power > 0) {

				secsUsingPower += secsDuration;
			}

			Float nextPower = line < lastLine ? averageOffsetPowerList.get(1 + line).getPower() : -1f;

			if (power.equals(nextPower)) {

				deferredSeconds++;

			} else {

				Integer duration = secsDuration + deferredSeconds;

				wattSeconds = power * duration;

				accWattSeconds += wattSeconds;

				bw.write(odt.format(defaultDateTimeFormatter) + "\t" + String.format("%7.1f", power) + "\twatts for\t"
						+ String.format("%8d", duration) + "\tseconds\t" + String.format("%10.2f", wattSeconds)
						+ " watt-seconds ( " + String.format("%12.2f", accWattSeconds) + " accumulated)");
				bw.newLine();

				deferredSeconds = 0;

				odt = odt.plusSeconds(duration);
			}
		}

		float kWhr = accWattSeconds / 3600 / 1000;

		long toDay = odt.get(ChronoField.DAY_OF_YEAR);

		String toHHMM = odt.format(formatter24HourClock);

		bw.write((toDay - fromDay + 1) + " day(s) from: " + fromHHMM + " on day " + fromDay + " to " + toHHMM
				+ " on day " + toDay + " " + String.format("%8.3f", kWhr) + " kWhr consumed via SmartPlug ( "
				+ secsUsingPower + " secs using power)");

		bw.newLine();

		bw.close();

		fw.close();
	}

	private void processSample(List<SlotCost> pricesPerSlot, Integer sampleNumber, int accSeconds, String name,
			List<PowerDuration> offsetPowerList) {

		int lastSlot = pricesPerSlot.size() - 1;

		int hours = accSeconds / 3600;
		int mins = (accSeconds % 3600) / 60;
		int secs = (accSeconds % 3600) % 60;

		long startEpochSecond = pricesPerSlot.get(0).getEpochSecond();

		LocalDateTime ldt = LocalDateTime.ofInstant(Instant.now(), ourZoneId);

		int minute = ldt.getMinute();

		// how many minutes past the half-hour?

		if (minute > 29) {

			minute -= 30;
		}

		startEpochSecond += (60 * minute);

		long stopEpochSecond = pricesPerSlot.get(lastSlot).getEpochSecond() + 1800 - accSeconds;

		if (stopEpochSecond < startEpochSecond) {

			System.out.println("\n" + String.format("%30s", name) + " : " + hours + " hours " + mins + " mins " + secs
					+ " secs - too long a period to schedule now");

		} else {

			Float highestCost = null;
			Float lowestCost = null;

			LocalDateTime timeOfHighestCost = null;
			LocalDateTime timeOfLowestCost = null;

			for (long epochSecond = startEpochSecond; epochSecond < stopEpochSecond; epochSecond += 60) {

				Instant instant = Instant.ofEpochSecond(epochSecond);

				Float cost = analyseCost(epochSecond, pricesPerSlot, offsetPowerList);

				if (null == cost) {

					System.err.println("Error: sample" + sampleNumber + " unable to determine cost for "
							+ LocalDateTime.ofInstant(instant, ourZoneId).format(formatterDayHourMinuteSecond));

					// skip half-hour

					epochSecond += (1800 - 60);

					continue;
				}

				if (null == lowestCost || cost < lowestCost) {

					timeOfLowestCost = LocalDateTime.ofInstant(instant, ourZoneId);

					lowestCost = cost;
				}

				if (null == highestCost || cost > highestCost) {

					timeOfHighestCost = LocalDateTime.ofInstant(instant, ourZoneId);

					highestCost = cost;
				}

			}

			long epochTime = timeOfLowestCost.toEpochSecond(ZoneOffset.UTC);

			List<PowerDuration> expandedOffsetPowerList;

			if (offsetPowerList.size() == accSeconds) {

				// we know for sure the supplied powerList is already 1 second per entry
				// so we do not need to expand it

				expandedOffsetPowerList = offsetPowerList;

			} else {

				expandedOffsetPowerList = expandMergePowerList(new ArrayList<PowerDuration>(), offsetPowerList,
						epochTime);
			}

			// expandedOffsetPowerList will have 1 entry per second based at the start time
			// thus the number of entries in list will infer time elapsed

			int elapsedSecs = expandedOffsetPowerList.size();

			List<Float> accumulatedWattsSeconds = new ArrayList<Float>(elapsedSecs);

			Float aws = 0f;

			for (int i = 0; i < offsetPowerList.size(); i++) {

				PowerDuration pd = offsetPowerList.get(i);

				Float power = pd.getPower();

				Integer secsDuration = pd.getSecsDuration();

				for (int n = 0; n < secsDuration; n++) {

					aws += power;

					accumulatedWattsSeconds.add(aws);
				}
			}

			float wattSeconds = accumulatedWattsSeconds.get(elapsedSecs - 1);

			float equivalentWatts = wattSeconds / elapsedSecs;

			float kWhr = wattSeconds / 3600 / 1000;

			System.out.println("\n" + String.format("%30s", name) + " : " + hours + " hours " + mins + " mins " + secs
					+ " secs & " + String.format("%6.3f", kWhr) + " kWhr consumed equivalent to "
					+ String.format("%4.1f", equivalentWatts) + " watts averaged over " + elapsedSecs + " secs");

			System.out.println("\t" + timeOfHighestCost.format(formatterDayHourMinute) + "\t"
					+ String.format("%5.2f", highestCost) + " p");

			long epochTimeLimit = epochTime + elapsedSecs;

			StringBuffer sbSlotPrices = new StringBuffer();
			StringBuffer sbSlotSeconds = new StringBuffer();
			StringBuffer sbSlotWatts = new StringBuffer();

			int startIndex = 0;

			int stopIndex = 0;

			do {
				SlotData matchingSlot = getImportPriceAt(epochTime, pricesPerSlot);

				Float importPrice = matchingSlot.getImportPrice();

				sbSlotPrices.append(String.format("%6s", importPrice));

				Integer secsRemainingInSlot = matchingSlot.getSecsRemaingInSlot();

				if (elapsedSecs <= secsRemainingInSlot) {

					// final slot

					stopIndex += elapsedSecs;

					float wattsPerSlot = accumulatedWattsSeconds.get(stopIndex - 1)
							- accumulatedWattsSeconds.get(startIndex);

					String text = String.format("%6.1f", wattsPerSlot / elapsedSecs);

					sbSlotWatts.append(text);
					sbSlotWatts.append('w');

					sbSlotSeconds.append(String.format("%6d", elapsedSecs));
					sbSlotSeconds.append('s');

					sbSlotPrices.append('p');

				} else {
					// will loop again

					stopIndex += secsRemainingInSlot;

					float wattsPerSlot = accumulatedWattsSeconds.get(stopIndex - 1)
							- accumulatedWattsSeconds.get(startIndex);

					String text = String.format("%6.1f", wattsPerSlot / secsRemainingInSlot);

					sbSlotWatts.append(text);
					sbSlotWatts.append("w /");

					sbSlotSeconds.append(String.format("%6d", secsRemainingInSlot));
					sbSlotSeconds.append("s /");

					sbSlotPrices.append("p /");
				}

				startIndex = stopIndex;

				epochTime += secsRemainingInSlot;

				elapsedSecs -= secsRemainingInSlot; // could go negative

			} while (epochTime < epochTimeLimit);

			System.out.println("\t" + timeOfLowestCost.format(formatterDayHourMinute) + "\t"
					+ String.format("%5.2f", lowestCost) + " p   --> \t" + sbSlotPrices.toString());

			System.out.println("\t\t\t\t\t" + sbSlotSeconds.toString());
			System.out.println("\t\t\t\t\t" + sbSlotWatts.toString());
		}
	}

	private SlotData getImportPriceAt(long epochSecond, List<SlotCost> pricesPerSlot) {

		SlotData result = null;

		for (int index = pricesPerSlot.size() - 1; index > -1; index--) {

			long slotStartTime = pricesPerSlot.get(index).getEpochSecond();

			if (epochSecond >= slotStartTime) {

				Integer secsRemaingInSlot = (int) (slotStartTime + 1800 - epochSecond);

				result = new SlotData();

				result.setImportPrice(pricesPerSlot.get(index).getImportPrice());
				result.setSecsRemaingInSlot(secsRemaingInSlot);

				break;
			}
		}

		return result;
	}

	private Float analyseCost(long startingAtEpochSecond, List<SlotCost> pricesPerSlot,
			List<PowerDuration> offsetPowerList) {

		Float accumulatedCost = 0F;

		Float accumulatedWattSeconds = 0F;

		// rebase the times in the device profile to match our new starting time

		for (int index = 0; index < offsetPowerList.size(); index++) {

			offsetPowerList.get(index).setEpochSecond(startingAtEpochSecond);

			PowerDuration powerDuration = offsetPowerList.get(index);

			Integer secs = powerDuration.getSecsDuration();

			startingAtEpochSecond += secs;
		}

		for (int index = 0; index < offsetPowerList.size(); index++) {

			PowerDuration powerDuration = offsetPowerList.get(index);

			float watts = powerDuration.getPower();

			Integer secs = powerDuration.getSecsDuration();

			long epochSecond = powerDuration.getEpochSecond();

			do {

				SlotData matchingSlot = getImportPriceAt(epochSecond, pricesPerSlot);

				if (null == matchingSlot) {

					System.err.println("error getting price for index=" + index + "\t"
							+ offsetPowerList.get(index).getEpochSecond());
					System.err.println(
							"last slot epoch is " + pricesPerSlot.get(pricesPerSlot.size() - 1).getSimpleTimeStamp()
									+ "\t" + pricesPerSlot.get(pricesPerSlot.size() - 1).getEpochSecond());

					return null;
				}

				Float priceAt = matchingSlot.getImportPrice();

				// we have to see if the matching slot can accommodate the secs logged
				// or if the secs need to span more than one slot

				Integer secsRemainingInSlot = matchingSlot.getSecsRemaingInSlot();

				if (secsRemainingInSlot >= secs) {

					// we're ok

					float wattSeconds = watts * secs;

					accumulatedWattSeconds += wattSeconds;

					float kWhr = wattSeconds / 3600 / 1000;

					float cost = kWhr * priceAt;

					accumulatedCost += cost;

					break;
				}

				// split the seconds across this and next slot and iterate until secs <
				// secsRemainingInSlot

				secs -= secsRemainingInSlot;

				epochSecond += secsRemainingInSlot;

				float wattSeconds = watts * secsRemainingInSlot;

				accumulatedWattSeconds += wattSeconds;

				float kWhr = wattSeconds / 3600 / 1000;

				float cost = kWhr * priceAt;

				accumulatedCost += cost;

			} while (true);
		}

		return accumulatedCost;
	}

	private int validateProperties(Map<String, String> verify) {

		// compare what the account has told us verses the current property file
		// settings

		int tally = 0; // number of observed discrepancies

		for (String key : verify.keySet()) {

			if (!key.startsWith("#")) {

				String value = verify.get(key);

				String configuredValue = properties.getProperty(key);

				if (null != configuredValue) {

					if (!value.equals(configuredValue)) {

						System.err.println("Discrepency: expected " + key + "=" + value + "\tbut found " + key + "="
								+ configuredValue);

						tally++;
					}
				}
			}
		}

		return tally;
	}

	private Map<String, String> displayAccountDetails(V1Account account) throws Exception {

		Map<String, String> result = new HashMap<String, String>();

		result.put(KEY_BASE_URL, DEFAULT_BASE_URL_PROPERTY);

		List<Detail> details = account.getProperties();

		Detail detail = details.get(0);

		List<ElectricityMeterPoint> electricityMeterPoints = detail.getElectrictyMeterPoints();

		ElectricityMeterPoint electricityMeterPoint = electricityMeterPoints.get(0);

		result.put(KEY_ELECTRICITY_MPRN, electricityMeterPoint.getMpan());

		List<Meter> meters = electricityMeterPoint.getMeters();

		Meter firstElectricityMeter = meters.get(0);

		result.put(KEY_ELECTRICITY_SN, firstElectricityMeter.getSerialNumber());

		// find active electricity agreement

		Agreement activeAgreement = null;

		for (Agreement agreement : electricityMeterPoint.getAgreements()) {

			if (null == agreement.getValidTo()) {

				activeAgreement = agreement;
				break;
			}
		}

		if (null == activeAgreement) {

			throw new Exception("cannot discover active agreement for electricty_meter_point ");
		}

		String tariffCode = activeAgreement.getTariffCode();

		result.put(KEY_TARIFF_CODE, tariffCode);

		String postcode = detail.getPostcode().substring(0, 4).trim();

		result.put(KEY_POSTCODE, postcode);

		String region = tariffCode.substring(tariffCode.length() - 1);

		result.put(KEY_REGION, region);

		String prefix = tariffCode.substring(0, 5);

		String importProductCode = tariffCode.substring(5, tariffCode.length() - 2);

		result.put(KEY_IMPORT_PRODUCT_CODE, importProductCode);

		String periodFrom = activeAgreement.getValidFrom().substring(0, 10);

		V1Charges agileElectricityStandingCharges = getV1ElectricityStandingCharges(importProductCode, tariffCode, 100,
				periodFrom, null);

		result.put(KEY_AGILE_ELECTRICITY_STANDING,
				String.valueOf(agileElectricityStandingCharges.getPriceResults().get(0).getValueIncVAT()));

		boolean directDebitPrices = Boolean.parseBoolean(properties.getProperty(
				KEY_FLEXIBLE_ELECTRICITY_VIA_DIRECT_DEBIT, DEFAULT_FLEXIBLE_ELECTRICITY_VIA_DIRECT_DEBIT_PROPERTY));

		result.put(KEY_FLEXIBLE_ELECTRICITY_VIA_DIRECT_DEBIT, (directDebitPrices ? "true" : "false"));

		String flexibleElectricityProductCode = properties.getProperty(KEY_FLEXIBLE_ELECTRICITY_PRODUCT_CODE,
				DEFAULT_FLEXIBLE_ELECTRICITY_PRODUCT_CODE_PROPERTY);

		result.put(KEY_FLEXIBLE_ELECTRICITY_PRODUCT_CODE, flexibleElectricityProductCode);

		String flexibleElectricityTariffCode = prefix + flexibleElectricityProductCode + "-" + region;

		V1Charges flexibleElectricityStandingCharges = getV1ElectricityStandingCharges(flexibleElectricityProductCode,
				flexibleElectricityTariffCode, 100, periodFrom, null);

		result.put(KEY_FLEXIBLE_ELECTRICITY_STANDING,
				String.valueOf(flexibleElectricityStandingCharges.getPriceResults().get(0).getValueIncVAT()));

		V1Charges flexibleStandardUnitRates = getV1ElectricityStandardUnitRates(flexibleElectricityProductCode,
				flexibleElectricityTariffCode, 100, periodFrom, null);

		Prices activePrices = null;

		for (Prices prices : flexibleStandardUnitRates.getPriceResults()) {

			if ("DIRECT_DEBIT".equals(prices.getPaymentMethod()) && directDebitPrices) {

				activePrices = prices;
				break;
			}

			if ("NON_DIRECT_DEBIT".equals(prices.getPaymentMethod()) && !directDebitPrices) {

				activePrices = prices;
				break;
			}
		}

		if (null == activePrices) {

			throw new Exception(" Cannot get activePrices");
		}

		result.put(KEY_FLEXIBLE_ELECTRICITY_UNIT, String.valueOf(activePrices.getValueIncVAT()));

		result.put(KEY_TARIFF_URL, result.get(KEY_BASE_URL) + "/v1/products/" + result.get(KEY_IMPORT_PRODUCT_CODE)
				+ "/electricity-tariffs/" + result.get(KEY_TARIFF_CODE));

		//
		// following not really needed for now
		//

		List<GasMeterPoint> gasMeterPoints = detail.getGasMeterPoints();

		GasMeterPoint firstGasMeterPoint = gasMeterPoints.get(0);

		result.put(KEY_GAS_MPRN, firstGasMeterPoint.getMprn());

		// find active gas agreement

		activeAgreement = null;

		for (Agreement agreement : firstGasMeterPoint.getAgreements()) {

			if (null == agreement.getValidTo()) {

				activeAgreement = agreement;
				break;
			}
		}

		if (null == activeAgreement) {

			throw new Exception("cannot discover active agreement for gas.mprn=" + firstGasMeterPoint.getMprn());
		}

		List<Meter> gasMeters = firstGasMeterPoint.getMeters();

		Meter firstGasMeter = gasMeters.get(0);

		result.put(KEY_GAS_SN, firstGasMeter.getSerialNumber());

		return result;
	}

	private static void displayPeriodSummary(String id, SortedMap<Integer, PeriodicValues> periodic, Integer fromIncl,
			Integer toIncl) {

		String datestamp = null;

		Float tallyEnergy = Float.valueOf(0);

		Float tallyCost = Float.valueOf(0);

		int tallyHalfHours = 0;

		int count = 0;

		for (Integer number : periodic.keySet()) {

			if (null != fromIncl) {

				int test = number.compareTo(fromIncl);

				if (test < 0) {

					continue; // not yet reached the first date in range
				}
			}

			if (null != toIncl) {

				int test = number.compareTo(toIncl);

				if (test > 0) {

					break; // give up because we know the keySet is ordered chronologically
				}
			}

			PeriodicValues periodData = periodic.get(number);

			Integer countHalfHours = periodData.getCountHalfHours();

			tallyHalfHours += countHalfHours;

			if (id.startsWith("D")) {

				// get the day as YYYY-MM-DD

				Long epochSecond = (long) number * 86400;

				datestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ourZoneId).toString()
						.substring(0, 10);
			}

			count++;

			Float accCost = periodData.getAccCost();

			Float accConsumption = periodData.getAccConsumption();

			Float equivalentDays = countHalfHours / (float) 48;

			Float equivalentDailyAverageCost = accCost / equivalentDays / 100;

			Float equivalentDailyEnergy = accConsumption / equivalentDays;

			Float averagePricePerUnit = equivalentDailyAverageCost / equivalentDailyEnergy * 100;

			tallyEnergy += accConsumption;

			tallyCost += accCost;

			String tag = null == datestamp ? String.format("%5s", id) + ":" + String.format("%4d", number) : datestamp;

			System.out.println(tag + "  " + String.format("%8.3f", accConsumption) + " kWhr  "
					+ String.format("%8.2f", accCost) + "p  " + String.format("%5d", countHalfHours) + " half-hours ~ "
					+ String.format("%5.2f", equivalentDays) + " days @ £"
					+ String.format("%7.2f", equivalentDailyAverageCost) + "   equivalent to "
					+ String.format("%6.3f", equivalentDailyEnergy) + " kWhr @ "
					+ String.format("%4.2f", averagePricePerUnit) + "p per unit");
		}

		if (count > 1) {

			float equivalentDays = tallyHalfHours / (float) 48;

			float averageDailyEnergy = tallyEnergy / equivalentDays;

			System.out.println("Totals:     " + String.format("%8.3f", tallyEnergy) + " kWhr\t\t\t\t "
					+ String.format("%5.2f", equivalentDays) + " days   £" + String.format("%7.2f", (tallyCost / 100))
					+ "\t      Average: " + String.format("%6.3f", averageDailyEnergy) + " kWhr @ "
					+ String.format("%4.2f", tallyCost / tallyEnergy) + "p");
		}
	}

	private static SortedMap<Integer, PeriodicValues> accumulateCostsByField(ChronoField field, Long upToEpochSecond) {

		SortedMap<Integer, PeriodicValues> result = new TreeMap<Integer, PeriodicValues>();

		for (Long key : history.keySet()) {

			if (null != upToEpochSecond) {

				if (key >= upToEpochSecond) {

					break;
				}
			}

			ConsumptionHistory data = history.get(key);

			Float price = data.getPrice();

			if (null == price) {

				continue;
			}

			Float consumption = data.getConsumption();

			if (null == consumption) {

				continue;
			}

			LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(key), ourZoneId);

			Integer calendarElement = Long.valueOf(ldt.getLong(field)).intValue();

			Float cost = consumption * price;

			PeriodicValues values = null;

			Integer countHalfHours = 0;

			if (result.containsKey(calendarElement)) {

				values = result.get(calendarElement);

				consumption += values.getAccConsumption();

				cost += values.getAccCost();

				countHalfHours = values.getCountHalfHours();

			} else {

				values = new PeriodicValues();

				result.put(calendarElement, values);
			}

			values.setCountHalfHours(1 + countHalfHours);
			values.setAccConsumption(consumption);
			values.setAccCost(cost);
		}

		return result;
	}

	private static boolean loadProperties(File externalPropertyFile) throws Exception {

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
		// the region=value will be verified for consistency

		String postcode = properties.getProperty(KEY_POSTCODE, DEFAULT_POSTCODE_PROPERTY);

		if (null != postcode && 0 != postcode.trim().length() && !DEFAULT_POSTCODE_PROPERTY.equals(postcode)) {

			V1GridSupplyPoints points = getV1GridSupplyPoints(postcode);

			ArrayList<V1GSP> pointList = points.getPointResults();

			V1GSP point = pointList.get(0);

			String groupId = point.getGroupId();

			String region = groupId.substring(1);

			if (!properties.getProperty(KEY_REGION, DEFAULT_REGION_PROPERTY).equals(region)) {

				throw new Exception("Region and postcode discrepency");

			}
		}

		check = properties.getProperty(KEY_CHECK, DEFAULT_CHECK_PROPERTY).trim();

		extra[0] = properties.getProperty(KEY_EXTRA, DEFAULT_EXTRA_PROPERTY).trim();
		extra[1] = properties.getProperty(KEY_EXTRA2, DEFAULT_EXTRA2_PROPERTY).trim();
		extra[2] = properties.getProperty(KEY_EXTRA3, DEFAULT_EXTRA2_PROPERTY).trim();
		extra[3] = properties.getProperty(KEY_EXTRA4, DEFAULT_EXTRA2_PROPERTY).trim();
		extra[4] = properties.getProperty(KEY_EXTRA5, DEFAULT_EXTRA2_PROPERTY).trim();

		// expand properties substituting $key$ values

		for (String key : properties.stringPropertyNames()) {

			String value = properties.getProperty(key);

			while (value.contains("$")) {

				int p = value.indexOf('$');

				int q = value.indexOf('$', 1 + p);

				String propertyKey = value.substring(1 + p, q);

				value = value.substring(0, p) + properties.getProperty(propertyKey) + value.substring(1 + q);

				properties.setProperty(key, value);
			}
		}

		return external;
	}

	private V1ElectricityConsumption getV1ElectricityConsumption(Integer page, Integer pageSize, String periodFrom,
			String periodTo) throws MalformedURLException, IOException {

		String mprn = properties.getProperty(KEY_ELECTRICITY_MPRN, DEFAULT_ELECTRICTY_MPRN_PROPERTY).trim();

		String sn = properties.getProperty(KEY_ELECTRICITY_SN, DEFAULT_ELECTRICTY_SN_PROPERTY).trim();

		String baseUrl = properties.getProperty(KEY_BASE_URL, DEFAULT_BASE_URL_PROPERTY).trim();

		String json = instance.getRequest(
				new URL(baseUrl + "/v1/electricity-meter-points/" + mprn + "/meters/" + sn + "/consumption/" +

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

	private V1Account getV1AccountData(Integer pageSize, String account) throws MalformedURLException, IOException {

		String spec = properties.getProperty(KEY_BASE_URL, DEFAULT_BASE_URL_PROPERTY).trim() + "/v1/accounts/"
				+ account;

		String json = getRequest(new URL(spec), true);

		V1Account result = mapper.readValue(json, V1Account.class);

		return result;
	}

	private V1AgileFlex getV1AgileFlexImport(Integer page, Integer pageSize, String periodFrom, String periodTo)
			throws MalformedURLException, IOException {

		String spec = properties.getProperty(KEY_TARIFF_URL, DEFAULT_TARIFF_URL_PROPERTY).trim()
				+ "/standard-unit-rates/" + "?page_size=" + pageSize + (null == page ? "" : "&page=" + page)
				+ (null == periodFrom ? "" : "&period_from=" + periodFrom)
				+ (null == periodTo ? "" : "&period_to=" + periodTo);

		String json = getRequest(new URL(spec), false);

		V1AgileFlex result = mapper.readValue(json, V1AgileFlex.class);

		return result;
	}

	private V1Charges getV1ElectricityStandingCharges(String product, String tariff, Integer pageSize,
			String periodFrom, String periodTo) throws MalformedURLException, IOException {

		String spec = properties.getProperty(KEY_BASE_URL, DEFAULT_BASE_URL_PROPERTY).trim() + "/v1/products/" + product
				+ "/electricity-tariffs/" + tariff + "/standing-charges/?page_size=" + pageSize
				+ (null == periodFrom ? "" : "&period_from=" + periodFrom)
				+ (null == periodTo ? "" : "&period_to=" + periodTo);

		String json = getRequest(new URL(spec), false);

		V1Charges result = mapper.readValue(json, V1Charges.class);

		return result;
	}

	// standard-unit-rates

	private V1Charges getV1ElectricityStandardUnitRates(String product, String tariff, Integer pageSize,
			String periodFrom, String periodTo) throws MalformedURLException, IOException {

		String spec = properties.getProperty(KEY_BASE_URL, DEFAULT_BASE_URL_PROPERTY).trim() + "/v1/products/" + product
				+ "/electricity-tariffs/" + tariff + "/standard-unit-rates/?page_size=" + pageSize
				+ (null == periodFrom ? "" : "&period_from=" + periodFrom)
				+ (null == periodTo ? "" : "&period_to=" + periodTo);

		String json = getRequest(new URL(spec), false);

		V1Charges result = mapper.readValue(json, V1Charges.class);

		return result;
	}

	private V1AgileFlex getV1AgileFlexExport(Integer pageSize, String periodFrom, String periodTo)
			throws MalformedURLException, IOException {

		String spec = properties.getProperty(KEY_EXPORT_TARIFF_URL, DEFAULT_EXPORT_TARIFF_URL_PROPERTY).trim()
				+ "/standard-unit-rates/" + "?page_size=" + pageSize
				+ (null == periodFrom ? "" : "&period_from=" + periodFrom)
				+ (null == periodTo ? "" : "&period_to=" + periodTo);

		String json = getRequest(new URL(spec), false);

		V1AgileFlex result = mapper.readValue(json, V1AgileFlex.class);

		return result;
	}

	private static V1GridSupplyPoints getV1GridSupplyPoints(String postcode) throws MalformedURLException, IOException {

		String spec = properties.getProperty(KEY_BASE_URL, DEFAULT_BASE_URL_PROPERTY).trim()
				+ "/v1/industry/grid-supply-points/" + "?postcode=" + postcode;

		String json = getRequest(new URL(spec), false);

		V1GridSupplyPoints result = mapper.readValue(json, V1GridSupplyPoints.class);

		return result;
	}

	private String getRequest(URL url) throws IOException {

		return getRequest(url, true);
	}

	private static String getRequest(URL url, boolean authorisationRequired) throws IOException {

		int status = 0;

		HttpURLConnection con = null;

		con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		con.setRequestProperty("Content-Type", contentType);
		con.setRequestProperty("user-agent", userAgent);

		if (authorisationRequired) {

			con.setRequestProperty("Authorization", properties.getProperty("basic"));
		}

		try {
			con.connect();

			status = con.getResponseCode();

		} catch (java.net.SocketException e) {

			System.err.println("API not available temporarily.  Please try again.");
			System.exit(-1);
		}

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

//	private V1GasConsumption getV1GasConsumption(String periodFrom, String periodTo)
//			throws MalformedURLException, IOException {
//
//		String mprn = properties.getProperty("gas.mprn").trim();
//
//		String sn = properties.getProperty("gas.sn").trim();
//
//		String json = instance.getRequest(
//				new URL("https://api.octopus.energy/v1/gas-meter-points/" + mprn + "/meters/" + sn + "/consumption/" +
//
//						"?page_size=100&period_from=" + periodFrom + "&period_to=" + periodTo + "&order_by=period"));
//
//		V1GasConsumption result = mapper.readValue(json, V1GasConsumption.class);
//
//		return result;
//	}

	private Map<Long, ConsumptionHistory> readHistory(File importData)
			throws NumberFormatException, FileNotFoundException, IOException {

		//
		// read historical consumption data from octopus.import.csv
		//

		Map<Long, ConsumptionHistory> history = new TreeMap<Long, ConsumptionHistory>();

		Scanner myReader = null;

		if (importData.createNewFile()) {

			// we normally expect to see the history file.
			// Lets assume we are running for the first time or need to reset

			// display the content of the built-in property file (which can be used as a
			// template)

			if (!usingExternalPropertyFile) {

				for (String propertyKey : defaultPropertyKeys) {

					if (KEY_POSTCODE.equals(propertyKey)) {

						System.out
								.println("#postcode=" + properties.getProperty(propertyKey, DEFAULT_POSTCODE_PROPERTY));
						System.out.println("#");
						System.out.println("# n.b. Southern England is region H");
						System.out.println("#");
						System.out.println("# if postcode is uncommented it will verify region=? based on Octopus API");
						System.out.println("#");

					} else if (KEY_ANSI.equals(propertyKey)) {

						System.out.println("#");
						System.out.println(
								"# in Windows console to show ANSI update Registry set REG_DWORD VirtualTerminalLevel=1 for Computer\\HKEY_CURRENT_USER\\Console");
						System.out.println("#");
						System.out.println("ansi=true");

					} else if (KEY_DAY_FROM.equals(propertyKey)) {

						System.out
								.println("#day.from=" + properties.getProperty(propertyKey, DEFAULT_DAY_FROM_PROPERTY));

					} else if (KEY_DAY_TO.equals(propertyKey)) {

						System.out.println("#day.to=" + properties.getProperty(propertyKey, DEFAULT_DAY_TO_PROPERTY));

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

				Float consumption = null;

				if (!"null".equals(c) && 0 != c.length()) {

					consumption = Float.valueOf(c);
				}

				ch.setConsumption(consumption);

				OffsetDateTime from = OffsetDateTime.parse(fields[1].trim(), defaultDateTimeFormatter);

				OffsetDateTime to = null;

				if ("".equals(fields[2]) || "null".equals(fields[2])) {

					// generate the 'to' by assuming a 30-min slot

					to = from.plusMinutes(30);

				} else {

					to = OffsetDateTime.parse(fields[2].trim(), defaultDateTimeFormatter);
				}

				Float price = null;
				Float cost = null;

				if (fields.length > 3) { // assume price in data

					price = Float.valueOf(fields[3].trim());

					if (null != consumption) {

						cost = Float.valueOf(consumption * price);
					}
				}

				ch.setPrice(price);
				ch.setCost(cost);

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

		return history;
	}

	private void appendToHistoryFile(File importData, Map<Long, ConsumptionHistory> history) throws IOException {

		FileWriter fw = new FileWriter(importData, true);

		BufferedWriter bw = new BufferedWriter(fw);

		for (Long key : history.keySet()) { // implicitly in ascending key based on epochSecond

			if (key > epochFrom) {

				// data to append to history

				if (0 == epochFrom) {

					bw.write("Consumption(kWh), Start, End, Price(p), Cost(p)");
					// n.b Cost is derived from consumption * price

					epochFrom = key;
				}

				ConsumptionHistory ch = history.get(key);

				if (null != ch.getConsumption()) {

					String entry = ch.getConsumption() + ", " + ch.getFrom().toString() + ", " + ch.getTo() + ", "
							+ ch.getPrice() + ", " + ch.getCost();

					bw.newLine();
					bw.write(entry);
				}
			}
		}

		bw.close();

		bw = null;

		fw.close();

		fw = null;
	}

	private void dealWithProperties(String propertyFileName) {

		String keyValue = null;

		try {

			// Check for existence of octopussy.properties in the current directory
			// if it exists, use it in preference to the built-in resource compiled into the
			// jar

			File externalProperties = new File(propertyFileName);

			usingExternalPropertyFile = loadProperties(externalProperties);

			keyValue = properties.getProperty(KEY_APIKEY, DEFAULT_APIKEY_PROPERTY).trim();

			if (null == keyValue) {

				throw new Exception(KEY_APIKEY);
			}

			properties.setProperty("basic", "Basic " + Base64.getEncoder().encodeToString(keyValue.getBytes()));

			export = Boolean.valueOf(properties.getProperty(KEY_EXPORT, DEFAULT_EXPORT_PROPERTY).trim());

			width = Integer.valueOf(properties.getProperty(KEY_WIDTH, DEFAULT_WIDTH_PROPERTY).trim());

			ansi = Boolean.valueOf(properties.getProperty(KEY_ANSI, DEFAULT_ANSI_PROPERTY).trim());

			plunge = Integer.valueOf(properties.getProperty(KEY_PLUNGE, DEFAULT_PLUNGE_PROPERTY).trim()).intValue();

			target = Integer.valueOf(properties.getProperty(KEY_TARGET, DEFAULT_TARGET_PROPERTY).trim()).intValue();

			ourZoneId = ZoneId.of(properties.getProperty(KEY_ZONE_ID, DEFAULT_ZONE_ID_PROPERTY).trim());

			if (null == ourZoneId) {

				throw new Exception(KEY_ZONE_ID);
			}

			ANSI_COLOUR_LO = colourMapForeground
					.get(properties.getProperty(KEY_COLOUR, DEFAULT_COLOUR_PROPERTY).trim());
			ANSI_COLOR_HI = colourMapForeground.get(properties.getProperty(KEY_COLOR, DEFAULT_COLOR_PROPERTY).trim());

		} catch (Exception e) {

			e.printStackTrace();

			System.exit(-1);
		}

	}

	private ArrayList<Long> upcomingExport(List<SlotCost> pricesPerSlot) {

		System.out.println(
				"\nUpcoming best " + (ansi ? ANSI_COLOR_HI + "export" + ANSI_RESET : "export") + " price periods:");

		ArrayList<Long> bestStartTime = new ArrayList<Long>();

		int widestPeriod = 1 + extended;

		for (int period = 0; period < widestPeriod; period++) { // each period represents multiples of 30 minutes

			// for bestStartTime[] [0] will be 30 minutes, [1] 1hr [2] 1.5 hr ... [9] 5hr

			float optimumAcc = -1;

			int indexOfOptimum = 0;

			int limit = pricesPerSlot.size() - period;

			for (int index = 0; index < limit; index++) {

				float accumulate = 0;

				for (int i = index; i < index + period + 1; i++) {

					Float exportPrice = pricesPerSlot.get(i).getExportPrice();

					if (null == exportPrice) {

						break;
					}

					accumulate += exportPrice;
				}

				if (-1 == optimumAcc || accumulate > optimumAcc) {

					optimumAcc = accumulate;
					indexOfOptimum = index;
				}
			}

			if (-1 == optimumAcc) {

				// restrict extended so that we only show definitive data

				extended = period;

				break; // this is because cannot prove we have found the lowest time
				// typically because there is no data after 22:30
			}

			SlotCost slotCost = pricesPerSlot.get(indexOfOptimum);

			if (0 == period) {

				slotCost.setIsMaximumExportPrice(Boolean.TRUE);
			}

			String simpleTimeStamp = slotCost.getSimpleTimeStamp();

			Long epochSecond = slotCost.getEpochSecond();

			bestStartTime.add(epochSecond);

			Instant instant = Instant.ofEpochSecond(-60 + epochSecond + (period + 1) * 1800);

			LocalDateTime ldt = LocalDateTime.ofInstant(instant, ourZoneId);

			String periodEndTime = ldt.format(formatter24HourClock);

			float average = optimumAcc / (period + 1); // the number of 30 minute periods in the slot

			int secondsInSlot = 1800 * (period + 1);

			int hours = (int) (secondsInSlot / 3600);

			int minutes = (secondsInSlot % 3600) / 60;

			System.out.println((0 == hours ? "      " : String.format("%2d", hours) + " hr ")
					+ (0 == minutes ? "      " : String.format("%2d", minutes) + " min") + " period from "
					+ simpleTimeStamp + " to " + periodEndTime + "  has average price: "
					+ String.format("%5.2f", average) + "p");
		}

		return bestStartTime;
	}

	private int[] findOptimalCostSlotToday(final int howMany, List<SlotCost> pricesPerSlot, String untilBefore12hr) {

		int[] result = new int[howMany > -1 ? howMany : 0];

		if (howMany > 0) {

			String simpleTimeStamp = pricesPerSlot.get(0).getSimpleTimeStamp();

			String[] parts = simpleTimeStamp.split(" ");

			String today = parts[0] + " " + parts[1] + " " + parts[2];

			String time12hr = parts[parts.length - 2] + " " + parts[parts.length - 1];

			List<Float> prices = new ArrayList<Float>();

			int index = 0;

			do {

				simpleTimeStamp = pricesPerSlot.get(index).getSimpleTimeStamp();

				parts = simpleTimeStamp.split(" ");

				String testToday = parts[0] + " " + parts[1] + " " + parts[2];

				time12hr = parts[parts.length - 2] + " " + parts[parts.length - 1];

				if (0 != testToday.compareTo(today)) {

					break;
				}

				if (0 == untilBefore12hr.compareTo(time12hr)) {
					break;
				}

				prices.add(pricesPerSlot.get(index).getImportPrice());

				index++;

			} while (true);

			Collections.sort(prices);

			final int limit = prices.size();

			List<Integer> timeIndex = new ArrayList<Integer>(limit);

			for (index = 0; index < limit; index++) {

				Float f = prices.get(index); // this is the next lowest price

				// which time has this price (which we have not already stored in timeList)?

				for (int i = 0; i < limit; i++) {

					Float importPrice = pricesPerSlot.get(i).getImportPrice();

					if (importPrice == f) {

						if (timeIndex.contains(i)) {

							continue; // iterate
						}

						timeIndex.add(Integer.valueOf(i));
					}
				}
			}

			for (int i = 0; i < limit; i++) {

				Integer nextIndex = timeIndex.get(i);

				if (i < howMany) {

					result[i] = nextIndex.intValue();
				}

				System.out.println(pricesPerSlot.get(nextIndex).getSimpleTimeStamp() + "\t"
						+ pricesPerSlot.get(nextIndex).getImportPrice() + "p" + (i < howMany ? "\t[" + i + "]" : ""));
			}
		}

		return result;
	}

	private static String convertHHmmMinus1Minute(String hhmm) {

		LocalDateTime ldtAdjusted = convertHHmm(hhmm).minusMinutes(1L);

		return ldtAdjusted.format(formatter24HourClock);
	}

	private static String convertHHmmTo12Hr(String hhmm) {

		LocalDateTime ldtAdjusted = convertHHmm(hhmm);

		return ldtAdjusted.format(formatter12HourClock);
	}

	private static LocalDateTime convertHHmm(String hhmm) {

		String hh = hhmm.substring(0, 2);

		String mm = hhmm.substring(3);

		Instant now = Instant.now();

		LocalDateTime ldt = LocalDateTime.ofInstant(now, ourZoneId);

		LocalDateTime ldtAdjusted = ldt.withHour(Integer.parseInt(hh)).withMinute(Integer.parseInt(mm)).withSecond(0)
				.withNano(0);

		return ldtAdjusted;
	}

	private String[] scheduleBatteryCharging(List<SlotCost> pricesPerSlot, String[] currentTo) {

		Instant instantRangeStart = Instant.ofEpochSecond(pricesPerSlot.get(0).getEpochSecond());

		LocalDateTime ldtRangeStart = LocalDateTime.ofInstant(instantRangeStart, ourZoneId);

		String rangeStartTime = ldtRangeStart.format(formatter24HourClock);

		// BY convention we divide the day up into 4 parts - each is 24hr time HH:mm
		// EG 00:00, 08:00, 12:00, 18:00
		// However we will allow 0 to N parts with start times as HH:mm stored in
		// parts[N]
		// the times must be ascending and unique.

		int partNumber = 1;

		String key = "part1";

		LocalDateTime ldtPrevious = LocalDateTime.of(2024, 1, 1, 0, 0); // 2024-01-01T00:00

		List<String> parts = new ArrayList<String>();

		while (properties.containsKey(key)) {

			String value = properties.getProperty(key);

			if (parts.contains(value)) {

				System.err.println("Invalid part value\t" + key + ":" + value);
				parts.clear();
				break;
			}

			LocalDateTime ldt = convertHHmm(value);

			if (ldt.compareTo(ldtPrevious) <= 0) {

				System.err.println("Invalid part value\t" + key + ":" + value + " needs to be > previous values");
				parts.clear();
				break;
			}

			parts.add(value);

//			String powerKey = "power"+String.valueOf(partNumber);
//			
//			powers

			key = "part" + String.valueOf(++partNumber);
		}

		final int numberOfParts = parts.size();

		final String dayPartsEndAt24hr[] = new String[numberOfParts];

		final String dayPartsEndBefore12hr[] = new String[numberOfParts];

		final int slotsPerDayPart[] = new int[numberOfParts];

		final int powers[] = new int[numberOfParts];

		float units = 0;

		System.out.println("Configured charging schedule as follows:");

		for (int n = 0; n < numberOfParts; n++) {

			String partsKey = (n == numberOfParts - 1) ? parts.get(0) : parts.get(1 + n);

			dayPartsEndBefore12hr[n] = convertHHmmTo12Hr(partsKey);

			dayPartsEndAt24hr[n] = convertHHmmMinus1Minute(partsKey);

			slotsPerDayPart[n] = Integer.valueOf(properties.getProperty("slots" + String.valueOf(1 + n)));

			String powerKey = "power" + String.valueOf(1 + n);

			powers[n] = Integer.valueOf(properties.getProperty(powerKey));

			System.out.println(parts.get(n) + " to " + dayPartsEndAt24hr[n] + "\t" + slotsPerDayPart[n]
					+ " half-hour slot(s) @ " + powers[n] + " watts");

			units += (+powers[n] * slotsPerDayPart[n]);
		}

		units = units / 2000;

		System.out.println("Probable import of around " + units + " kWhr");

		int[] slots;

		String from;

		int p = 0;

		// what part of the day are we in?

		for (; p < numberOfParts - 1; p++) {

			if (rangeStartTime.compareTo(parts.get(1 + p)) < 0) {

				break;
			}
		}

		from = parts.get(p);

		String power = null;

		for (int n = 0; n < numberOfParts; n++) {

			if (0 == rangeStartTime.compareTo(parts.get(n))) {

				power = properties.getProperty("power" + String.valueOf(1 + n));
				break;
			}
		}

		if (null == power && slotsPerDayPart[p] > 0) {

			slots = new int[0];

			// Examine currentTo to check there is at least one time in the current part of
			// the day. EG: if we are in range "09:30" to "09:59" then a schedule
			// should have fired at 08:00 to set a time between 08:00 and 11:59

			final String until = dayPartsEndAt24hr[p];

			// was there a schedule within the part of day we are in?

			boolean foundMatch = false;

			for (int s = 0; s < currentTo.length; s++) {

				if (currentTo[s].compareTo(from) > 0) {

					if (currentTo[s].compareTo(until) <= 0) {

						// we have a scheduled charge time in the current range

						foundMatch = true;
						break;
					}
				}
			}

			if (!foundMatch) {

				// assume the previous scheduling update failed (typically due to inverter
				// timeout) so try again

				power = "6000";
			}
		}

		if (null != power) { // assume a change to AC Charge s schedule

			slots = findOptimalCostSlotToday(slotsPerDayPart[p], pricesPerSlot, dayPartsEndBefore12hr[p]);

			boolean changeToSchedule = false;

			for (int s = 0; s < slots.length; s++) {

				if (null != extra[s] && 0 != "false".compareTo(extra[s])) {

					String updatedTo = execMacro(extra[s], pricesPerSlot.get(slots[s]), null, currentTo[s]); // null
																												// implies
																												// 100%
																												// SoC
					if (null != updatedTo) {

						currentTo[s] = updatedTo;

						changeToSchedule = true;
					}
				}
			}

			if (changeToSchedule) {

				execWrite(check, "72", power);
			}
		}

		return currentTo;
	}

	private String execRead(String template, String p1) {

		String[] cmdarray = check.split(" ");

		for (int n = 0; n < cmdarray.length; n++) {

			if ("%1".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = "read";
			}

			else if ("%2".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = p1;
			}

			else if ("%3".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = "";
			}
		}

		String value = exec(cmdarray);

		int beginIndex = value.indexOf("\"value\" : \"") + 11;
		int endIndex = value.indexOf("\"", beginIndex);

		String result = value.substring(beginIndex, endIndex);

		if (result.length() < 5) { // assume error

			result = "00:00";
		}

		return result;
	}

	private String execWrite(String template, String p1, String p2) {

		String[] cmdarray = check.split(" ");

		for (int n = 0; n < cmdarray.length; n++) {

			if ("%1".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = "write";
			}

			else if ("%2".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = p1;
			}

			else if ("%3".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = p2;
			}
		}

		String value = exec(cmdarray);

		int beginIndex = value.indexOf("\"value\" : \"") + 11;
		int endIndex = value.indexOf("\"", beginIndex);

		String result = value.substring(beginIndex, endIndex);

		if (result.length() < 5) { // assume error

			result = "00:00";
		}

		return result;
	}

	private String[] startAndFinishTimeOfSlotCost(SlotCost slotCost) {

		Instant instantStart = Instant.ofEpochSecond(slotCost.getEpochSecond());

		LocalDateTime ldtStart = LocalDateTime.ofInstant(instantStart, ourZoneId);

		LocalDateTime ldtFinish = ldtStart.plusMinutes(29);

		String[] result = new String[] { ldtStart.format(formatter24HourClock),
				ldtFinish.format(formatter24HourClock) };

		return result;
	}

	private String execMacro(String template, SlotCost slotCost, String soc, String currentTo) {

		String[] period = startAndFinishTimeOfSlotCost(slotCost);

		String from = period[0];
		String to = period[1];

		if (null == currentTo || 0 != to.compareTo(currentTo)) {

			System.out.println(from + "\t" + to + "\t" + slotCost.getImportPrice() + "p");

			// assume extra contains a cmdarray to execute in a separate process
			// java -jar plugs.jar ./SwindonIcarus.properties inverter setting A %1 %2 %3

			// substitute from, to and power for %1,%2 and %3 respectively

			String[] cmdarray = template.split(" ");

			for (int n = 0; n < cmdarray.length; n++) {

				if ("%1".equalsIgnoreCase(cmdarray[n])) {

					cmdarray[n] = from;

				} else if ("%2".equalsIgnoreCase(cmdarray[n])) {

					cmdarray[n] = to;

				} else if ("%3".equalsIgnoreCase(cmdarray[n])) {

					cmdarray[n] = null == soc ? "" : soc;
				}

				System.out.println("param[" + n + "]=" + cmdarray[n]);
			}

			exec(cmdarray);

		} else {

			to = null; // imply no action taken
		}

		return to;
	}

	private ArrayList<Long> upcomingImport(List<SlotCost> pricesPerSlot) {

		System.out.println(
				"\nUpcoming best " + (ansi ? ANSI_COLOUR_LO + "import" + ANSI_RESET : "import") + " price periods:");

		ArrayList<Long> bestStartTime = new ArrayList<Long>();

		int widestPeriod = 1 + extended;

		for (int period = 0; period < widestPeriod; period++) { // each period represents multiples of 30 minutes

			// for bestStartTime[] [0] will be 30 minutes, [1] 1hr [2] 1.5 hr ... [9] 5hr

			float optimumAcc = -1;

			int indexOfOptimum = 0;

			int limit = pricesPerSlot.size() - period;

			for (int index = 0; index < limit; index++) {

				float accumulate = 0;

				for (int i = index; i < index + period + 1; i++) {

					Float importPrice = pricesPerSlot.get(i).getImportPrice();

					if (null == importPrice) {

						break;
					}

					accumulate += importPrice;
				}

				if (-1 == optimumAcc || accumulate < optimumAcc) {

					optimumAcc = accumulate;
					indexOfOptimum = index;
				}
			}

			if (-1 == optimumAcc) {

				// restrict extended so that we only show definitive data

				extended = period;

				break; // this is because cannot prove we have found the lowest time
				// typically because there is no data after 22:30
			}

			SlotCost slotCost = pricesPerSlot.get(indexOfOptimum);

			if (0 == period) {

				slotCost.setIsMinimumImportPrice(Boolean.TRUE);
			}

			String simpleTimeStamp = slotCost.getSimpleTimeStamp();

			Long epochSecond = slotCost.getEpochSecond();

			bestStartTime.add(epochSecond);

			Instant instant = Instant.ofEpochSecond(-60 + epochSecond + (period + 1) * 1800);

			LocalDateTime ldtEndTime = LocalDateTime.ofInstant(instant, ourZoneId);

			String periodEndTime = ldtEndTime.format(formatter24HourClock);

			float average = optimumAcc / (period + 1); // the number of 30 minute periods in the slot

			int secondsInSlot = 1800 * (period + 1);

			int hours = (int) (secondsInSlot / 3600);

			int minutes = (secondsInSlot % 3600) / 60;

			System.out.println((0 == hours ? "      " : String.format("%2d", hours) + " hr ")
					+ (0 == minutes ? "      " : String.format("%2d", minutes) + " min") + " period from "
					+ simpleTimeStamp + " to " + periodEndTime + "  has average price: "
					+ String.format("%5.2f", average) + "p");

		}

		return bestStartTime;
	}

	private String exec(String[] cmdarray) {

		Process process;

		String result = null;

		try {

			process = Runtime.getRuntime().exec(cmdarray);

			InputStream inputStream = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(inputStream);

			int n1;
			char[] c1 = new char[1024];
			StringBuffer sbOutput = new StringBuffer();

			while ((n1 = isr.read(c1)) > 0) {

				sbOutput.append(c1, 0, n1);
			}

			result = sbOutput.toString();

			InputStream errorStream = process.getErrorStream();
			InputStreamReader esr = new InputStreamReader(errorStream);

			int n2;
			char[] c2 = new char[1024];
			StringBuffer sbError = new StringBuffer();

			while ((n2 = esr.read(c2)) > 0) {

				sbError.append(c2, 0, n2);
			}

			System.err.println(sbError.toString());

		} catch (IOException e) {

			e.printStackTrace();
		}

		return result;
	}

	private Map<LocalDateTime, ImportExportData> createPriceMap(ArrayList<Agile> agileResultsImport,
			ArrayList<Agile> agileResultsExport) {

		Map<LocalDateTime, ImportExportData> priceMap = new HashMap<LocalDateTime, ImportExportData>();

		for (Agile agile : agileResultsImport) {

			String validFrom = agile.getValidFrom();

			LocalDateTime ldtFrom = LocalDateTime.parse(validFrom, DateTimeFormatter.ISO_ZONED_DATE_TIME);

			// assume time obtained is zulu/UTC - need to convert to our local time (such as
			// BST)

			ZonedDateTime instantFrom = ZonedDateTime.of(ldtFrom, ZoneId.of("UTC"));
			LocalDateTime actualFrom = instantFrom.withZoneSameInstant(ourZoneId).toLocalDateTime();

			OffsetDateTime offsetDateTime = actualFrom.atOffset(ZoneOffset.of("+01:00"));

			long epochActualFrom = offsetDateTime.toEpochSecond();

			float valueExcVat = agile.getValueExcVat();

			ConsumptionHistory consumptionLatest = new ConsumptionHistory();

			consumptionLatest.setFrom(offsetDateTime);

			String validTo = agile.getValidTo();

			LocalDateTime ldtTo = LocalDateTime.parse(validTo, DateTimeFormatter.ISO_ZONED_DATE_TIME);

			// assume time obtained is zulu/UTC - need to convert to our local time (such as
			// BST)

			ZonedDateTime instantTo = ZonedDateTime.of(ldtTo, ZoneId.of("UTC"));

			LocalDateTime actualTo = instantTo.withZoneSameInstant(ourZoneId).toLocalDateTime();

			OffsetDateTime offsetDateTimeTo = actualTo.atOffset(ZoneOffset.of("+01:00"));

			consumptionLatest.setTo(offsetDateTimeTo);

			consumptionLatest.setPrice(Float.valueOf(valueExcVat));

			Float cost = null;

			if (null != consumptionLatest.getConsumption()) {

				cost = valueExcVat * consumptionLatest.getConsumption();
			}

			consumptionLatest.setCost(cost);

			history.put(epochActualFrom, consumptionLatest);

			ImportExportData importExportPrices = new ImportExportData();

			importExportPrices.setImportPrice(Float.valueOf(valueExcVat));

			priceMap.put(actualFrom, importExportPrices);

		}

		if (export) {

			for (Agile agile : agileResultsExport) {

				String validFrom = agile.getValidFrom().substring(0, 19);

				LocalDateTime ldt = LocalDateTime.parse(validFrom, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

				// assume time obtained is zulu/UTC - need to convert to our local time (such as
				// BST)

				ZonedDateTime instant = ZonedDateTime.of(ldt, ZoneId.of("UTC"));
				LocalDateTime actual = instant.withZoneSameInstant(ourZoneId).toLocalDateTime();

				float valueExcVat = agile.getValueExcVat();

				ImportExportData importExportPrices = priceMap.get(actual);

				if (null == importExportPrices) {

					importExportPrices = new ImportExportData();
				}

				importExportPrices.setExportPrice(Float.valueOf(valueExcVat));

				priceMap.put(actual, importExportPrices);
			}
		}

		return priceMap;
	}

	private int dailyResults(String today, Map<String, DayValues> elecMapDaily) {

		// find the range of week numbers contained in the data

		Set<Integer> weekNumbers = new TreeSet<Integer>();

		for (String key : elecMapDaily.keySet()) {

			DayValues value = elecMapDaily.get(key); // can be null

			if (null != value) {

				weekNumbers.add(value.getWeekOfYear());
			}
		}

		StringBuffer sb = new StringBuffer();

		sb.append("\nRecent daily results (in week ");

		Iterator<Integer> iterator = weekNumbers.iterator();

		// there will be at least one week in set

		sb.append(iterator.next());

		while (iterator.hasNext()) {

			sb.append(',');

			sb.append(iterator.next());
		}

		sb.append("):");

		System.out.println(sb.toString());

		int countDays = 0;

		float accumulateDifference = 0;
		float accumulatePower = 0;
		float accumulateCost = 0;

		float flatRate = Float.valueOf(
				properties.getProperty(KEY_FLEXIBLE_ELECTRICITY_UNIT, DEFAULT_FLEXIBLE_ELECTRICITY_UNIT_PROPERTY));

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

			float agileCharge = Float.valueOf(properties.getProperty(KEY_AGILE_ELECTRICITY_STANDING,
					DEFAULT_AGILE_ELECTRICITY_STANDING_PROPERTY));

			float standardPrice = consumption * flatRate;

			float standardCharge = Float.valueOf(properties.getProperty(KEY_FLEXIBLE_ELECTRICITY_STANDING,
					DEFAULT_FLEXIBLE_ELECTRICITY_STANDING_PROPERTY));

			float agileCost = agilePrice + agileCharge;

			float difference = (standardPrice + standardCharge) - agileCost;

			float lowestPrice = dayValues.getLowestPrice();

			float dailyAverageUnitPrice = agilePrice / consumption;

			System.out.println(dayValues.getDayOfWeek() + (lowestPrice < plunge ? " * " : "   ") + key + "  £"
					+ String.format("%5.2f", agileCost / 100) + " " + String.format("%7.3f", consumption)
					+ " kWhr  Agile: " + String.format("%8.4f", agilePrice) + "p +" + agileCharge + "p (X: "
					+ String.format("%8.4f", standardPrice) + "p +" + standardCharge + "p)  saving: £"
					+ String.format("%5.2f", (difference / 100)) + "  @ " + String.format("%.2f", dailyAverageUnitPrice)
					+ "p per unit");

			accumulateDifference += difference;

			accumulatePower += consumption;

			accumulateCost += agilePrice;
		}

		String pounds2DP = String.format("%.2f", accumulateDifference / 100);

		String averagePounds2DP = String.format("%.2f", accumulateDifference / 100 / countDays);

		Float unitCostAverage = accumulateCost / accumulatePower;

		String averageCostPerUnit = String.format("%.2f", unitCostAverage);

		String averagePower = String.format("%.3f", accumulatePower / countDays);

		System.out.println("\nOver " + countDays + " days, importing " + String.format("%.3f", accumulatePower)
				+ " kWhr, Agile has saved £" + pounds2DP + " compared to the " + flatRate + "p (X) flat rate tariff");
		System.out.println("Average daily saving: £" + averagePounds2DP + " Recent average cost per unit (A): "
				+ averageCostPerUnit + "p and daily grid import: " + averagePower + " kWhr");

		return unitCostAverage.intValue();
	}

	private void showAnalysis(List<SlotCost> pricesPerSlot, int averageUnitCost, ArrayList<Long> bestImportTime,
			ArrayList<Long> bestExportTime, String[] schedule) {

		// schedule gives the end times of up to 5 slots

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

			sb.append('\n');

			if (export) {

				sb.append("Export & ");
			}

			sb.append("Import prices current & future:");

			for (int n = (export ? -3 : -11); n < maxWidth; n++) {

				sb.append(' ');
			}

			sb.append('|');

			if (extended > 0) {

				String heads[] = { " 1hr |", " 1.5 |", " 2hr |", " 2.5 |", " 3hr |", " 3.5 |", " 4hr |", " 4.5 |",
						" 5hr |", " 5.5 |", " 6hr |", " 6.5 |", " 7hr |", " 7.5 |", " 8hr |", " 8.5 |", " 9hr |",
						" 9.5 |" };

				for (int e = 0; e < extended; e++) {

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

			String[] period = startAndFinishTimeOfSlotCost(slotCost);

			String to = period[1];

			Float importValueIncVat = slotCost.getImportPrice();

			Float exportValueIncVat = slotCost.getExportPrice(); // can be null;

			boolean cheapestImport = Boolean.TRUE.equals(slotCost.getIsMinimumImportPrice());

			boolean bestExport = Boolean.TRUE.equals(slotCost.getIsMaximumExportPrice());

			boolean lessThanAverage = importValueIncVat < averageUnitCost;

			sb1 = new StringBuffer();

			int n = 0;

			if (importValueIncVat <= plunge) {

				sb1.append("<--- PLUNGE <= " + plunge + "p");
				n = sb1.length();

			} else {

				boolean aboveTarget = false;

				for (; n < importValueIncVat; n++) {

					if (target == n) {

						aboveTarget = true;

						if (ansi) {
							sb1.append(ANSI_COLOR_HI);
						}

						sb1.append('X');

					} else if (averageUnitCost == n) {

						sb1.append('A');

					} else if (maxWidth == n) {

						sb1.append('>');
						n++;
						break;

					} else {

						sb1.append('*');
					}
				}

				if (aboveTarget) {

					if (ansi) {
						sb1.append(ANSI_RESET);
					}
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

			if (extended > 0) {

				// calculate the average for 1hr/1.5hr/2hr... etc

				for (int i = 1; i < extended + 1; i++) {

					float accImport = 0;
					float accExport = 0;

					int count = 0;

					if (export) {

						if (index + i < pricesPerSlot.size()) {

							for (int j = index; j < index + i + 1; j++) {

								Float exportPrice = pricesPerSlot.get(j).getExportPrice();

								if (null != exportPrice) {

									accExport += exportPrice;
								}
							}
						}
					}

					if (index + i < pricesPerSlot.size()) {

						for (int j = index; j < index + i + 1; j++) {

							accImport += pricesPerSlot.get(j).getImportPrice();

							count++;
						}
					}

					if (count < (1 + i)) {

						sb3.append("     ");

					} else {

						// determine if this average price needs to be green/ANSI_COLOUR

						Boolean flagTimeGoodForImportOrExport = null;

						long slotEpoch = 0;

						if (ansi) {

							slotEpoch = slotCost.getEpochSecond();

							if (export) {

								long bestExportPeriodStartsAt = bestExportTime.get(i); // inclusive
								long bestExportPeriodEndBefore = (i + 1) * 1800 + bestExportPeriodStartsAt; // exclusive

								if (slotEpoch >= bestExportPeriodStartsAt && slotEpoch < bestExportPeriodEndBefore) {

									sb3.append(ANSI_COLOR_HI); // eg RED
									flagTimeGoodForImportOrExport = Boolean.TRUE;
								}
							}

							if (null == flagTimeGoodForImportOrExport) {

								long bestImportPeriodStartsAt = bestImportTime.get(i); // inclusive
								long bestImportPeriodEndBefore = (i + 1) * 1800 + bestImportPeriodStartsAt; // exclusive

								if (slotEpoch >= bestImportPeriodStartsAt && slotEpoch < bestImportPeriodEndBefore) {

									sb3.append(ANSI_COLOUR_LO); // eg GREEN
									flagTimeGoodForImportOrExport = Boolean.TRUE;
								}
							}
						}

						sb3.append(String.format("%5.2f",
								(Boolean.FALSE.equals(flagTimeGoodForImportOrExport) ? accExport / count
										: accImport / count)));

						if (null != flagTimeGoodForImportOrExport) { // ansi is implicit

							sb3.append(ANSI_RESET);
						}
					}

					sb3.append('|');
				}
			}

			String prices = sb3.toString();

			String clockHHMM = "";

			{

				Long epochSecond = slotCost.getEpochSecond();

				Instant instant = Instant.ofEpochSecond(epochSecond);

				LocalDateTime ldt = LocalDateTime.ofInstant(instant, ourZoneId);

				clockHHMM = ldt.format(formatter24HourClock);
			}

			String optionalExport = export
					? (ansi & bestExport ? ANSI_COLOR_HI : "")
							+ (null == exportValueIncVat ? "       "
									: String.format("%6.2f", exportValueIncVat) + "p  ")
							+ (ansi & bestExport ? ANSI_RESET : "")
					: "\t";

			String chargeSlot = null;

			for (int s = 0; s < schedule.length; s++) {

				if (0 == to.compareTo(schedule[s])) {

					chargeSlot = "S" + String.valueOf(1 + s);
					break;
				}
			}

			System.out.println(optionalExport + slotCost.getSimpleTimeStamp() + "  "
					+ (null == chargeSlot ? (cheapestImport ? "!" : " ") + (lessThanAverage ? "!" : " ") : chargeSlot)
					+ "  " + String.format("%5.2f", importValueIncVat) + "p  "
					+ (ansi && cheapestImport ? ANSI_COLOUR_LO : "") + asterisks
					+ (ansi && cheapestImport ? ANSI_RESET : "") + padding + prices + clockHHMM);
		}

	}

	/*
	 * This method ensures the friendly date/time returned has a 3 character month
	 * and thus us always the same width string in result
	 * 
	 */
	private String getSimpleDateTimestamp(LocalDateTime ldt) {

		String parts[] = ldt.format(simpleTime).split(" ");

		String result = parts[0] + " " + parts[1].substring(0, 3) + " " + parts[2] + " " + parts[3] + " " + parts[4]
				+ (parts.length > 5 ? " " + parts[5] : "");

		return result;
	}

	private List<SlotCost> buildListSlotCosts(long halfHourAgo, SortedSet<LocalDateTime> setOfLocalDateTime,
			Map<LocalDateTime, ImportExportData> vatIncPriceMap) {

		List<SlotCost> pricesPerSlot = new ArrayList<SlotCost>();

		for (LocalDateTime slot : setOfLocalDateTime) {

			long epochSecond = slot.atZone(ourZoneId).toEpochSecond();

			if (epochSecond >= halfHourAgo) {

				// this is recent: we are interested

				ImportExportData importExportData = vatIncPriceMap.get(slot);

				float importValueIncVat = importExportData.getImportPrice();

				Float exportValueIncVat = importExportData.getExportPrice(); // can be null if export=false

				SlotCost price = new SlotCost();

				// n.b the DateTimeFormatter based on "E MMM dd pph:mm a" will return a mixture
				// of 3 or 4 character month abbreviations, which is pretty nonintuitive

				price.setSimpleTimeStamp(getSimpleDateTimestamp(slot));

				price.setEpochSecond(slot.atZone(ourZoneId).toEpochSecond());

				price.setImportPrice(importValueIncVat);

				price.setExportPrice(exportValueIncVat);

				pricesPerSlot.add(price);
			}
		}

		return pricesPerSlot;
	}

	private Map<String, DayValues> buildElecMapDaily(ArrayList<V1PeriodConsumption> periodResults,
			Map<LocalDateTime, ImportExportData> priceMap) {

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

			Integer weekOfYear = Long.valueOf(ldt.getLong(ChronoField.ALIGNED_WEEK_OF_YEAR)).intValue();

			ImportExportData importExportData = priceMap.get(ldt);

			if (null == importExportData) {

				elecMapDaily.put(key, null); // flag this day as incomplete - ignore all time slots on this day

				continue;
			}

			Float halfHourPrice = importExportData.getImportPrice();

			if (null == halfHourPrice) {

				elecMapDaily.put(key, null); // flag this day as incomplete - ignore all time slots on this day

				continue;
			}

			Long epochKey = from.toEpochSecond();

			ConsumptionHistory latestConsumption = history.get(epochKey);

			if (null == latestConsumption) {

				System.err.println("error at " + v1PeriodConsumption.getIntervalStart());

			} else {

				latestConsumption.setConsumption(consumption);
				latestConsumption.setPrice(halfHourPrice);
				latestConsumption.setCost(Float.valueOf(consumption * halfHourPrice));

				history.put(epochKey, latestConsumption);
			}

			Float halfHourCharge = consumption * halfHourPrice;

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

				dayValues.setWeekOfYear(weekOfYear);

				dayValues.setSlotCount(1);

				dayValues.setDailyConsumption(consumption);

				dayValues.setDailyPrice(Float.valueOf(halfHourCharge.floatValue()));
			}

			elecMapDaily.put(key, dayValues);
		}

		return elecMapDaily;
	}

	private ArrayList<V1PeriodConsumption> updateHistory(String someDaysAgo,
			ArrayList<V1PeriodConsumption> periodResults, int howManyDaysHistory, int count) {

		// Add history data for any non-null consumptions

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

			// assume history up to someDaysAgo has consumption & price available

			V1PeriodConsumption entry = new V1PeriodConsumption();

			Float consumption = value.getConsumption();

			entry.setConsumption(consumption);
			entry.setIntervalStart(timestamp);

			if (null == value.getTo()) {

				entry.setIntervalEnd("null");

			} else {

				entry.setIntervalEnd(value.getTo().toString());
			}

			periodResults.add(entry);
		}

		return periodResults;

	}

}
