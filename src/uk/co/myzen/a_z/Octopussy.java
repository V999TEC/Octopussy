/**
 * 
 */
package uk.co.myzen.a_z;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.co.myzen.a_z.json.Agreement;
import uk.co.myzen.a_z.json.ChargeDischarge;
import uk.co.myzen.a_z.json.Detail;
import uk.co.myzen.a_z.json.ElectricityMeterPoint;
import uk.co.myzen.a_z.json.ImportExport;
import uk.co.myzen.a_z.json.Meter;
import uk.co.myzen.a_z.json.Prices;
import uk.co.myzen.a_z.json.Tariff;
import uk.co.myzen.a_z.json.V1Account;
import uk.co.myzen.a_z.json.V1Charges;
import uk.co.myzen.a_z.json.V1ElectricityConsumption;
import uk.co.myzen.a_z.json.V1ElectricityTariff;
import uk.co.myzen.a_z.json.V1GSP;
import uk.co.myzen.a_z.json.V1GridSupplyPoints;
import uk.co.myzen.a_z.json.V1PeriodConsumption;
import uk.co.myzen.a_z.json.forecast.solar.ResultMessage;
import uk.co.myzen.a_z.json.forecast.solar.SolarResult;
import uk.co.myzen.a_z.json.forecast.solcast.SolcastForecast;
import uk.co.myzen.a_z.json.forecast.solcast.SolcastMessage;

/**
 * @author howard
 *
 */

public class Octopussy implements IOctopus {

	public static final Instant now = Instant.now(); // earliest opportunity to log the time

	private final static String slotStartTimes[] = { "00:00", "00:30", "01:00", "01:30", "02:00", "02:30", "03:00",
			"03:30", "04:00", "04:30", "05:00", "05:30", "06:00", "06:30", "07:00", "07:30", "08:00", "08:30", "09:00",
			"09:30", "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00",
			"15:30", "16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00",
			"21:30", "22:00", "22:30", "23:00", "23:30" };

	private static boolean execute = false;

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
	//
	private final static String KEY_API_SOLCAST = "api.solcast";
	private final static String KEY_APIKEY = "apiKey";
	private final static String KEY_BASE_URL = "base.url";
	private final static String KEY_ELECTRICITY_MPAN_IMPORT = "electricity.mpan.import";
	private final static String KEY_ELECTRICITY_MPAN_EXPORT = "electricity.mpan.export";

	private final static String KEY_ELECTRICITY_SN = "electricity.sn";
	private final static String KEY_GAS_SN = "gas.sn";

	private final static String KEY_FLEXIBLE_ELECTRICITY_VIA_DIRECT_DEBIT = "flexible.electricity.via.direct.debit";
	private final static String KEY_FLEXIBLE_ELECTRICITY_PRODUCT_CODE = "flexible.electricity.product.code";
	private final static String KEY_FLEXIBLE_ELECTRICITY_UNIT = "flexible.electricity.unit";
	private final static String KEY_FLEXIBLE_ELECTRICITY_STANDING = "flexible.electricity.standing";
	private final static String KEY_AGILE_ELECTRICITY_STANDING = "agile.electricity.standing";

	private final static String KEY_IMPORT_PRODUCT_CODE = "import.product.code";
	private final static String KEY_IMPORT_TARIFF_CODE = "import.tariff.code";
	private final static String KEY_IMPORT_TARIFF_URL = "import.tariff.url";

	private final static String KEY_REGION = "region";
	private final static String KEY_POSTCODE = "postcode";

	private final static String KEY_ZONE_ID = "zone.id";
	private final static String KEY_HISTORY_IMPORT = "history.import";
	private final static String KEY_HISTORY_EXPORT = "history.export";

	private final static String KEY_EXPORT_PRODUCT_CODE = "export.product.code";
	private final static String KEY_EXPORT_TARIFF_CODE = "export.tariff.code";
	private final static String KEY_EXPORT_TARIFF_URL = "export.tariff.url";
	private final static String KEY_EXPORT = "export";

	private final static String KEY_BASELINE = "baseline";
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
	private final static String KEY_SOLAR = "solar";
	private final static String KEY_PERCENT = "percent";
	private final static String KEY_GRID = "grid";
	private final static String KEY_CONSUMPTION = "consumption";
	private final static String KEY_TEMPERATURE = "temperature";
	private final static String KEY_BATTERY = "battery";

	private final static String KEY_FORECAST_SOLAR = "forecast.solar";
	private final static String KEY_FORECAST_SOLCAST = "forecast.solcast";

	private final static String KEY_FILE_SOLAR = "file.solar";
	private final static String KEY_MAX_SOLAR = "max.solar";
	private final static String KEY_MAX_RATE = "max.rate";

	private final static String KEY_REFERRAL = "referral";

	private final static String[] defaultPropertyKeys = { KEY_API_SOLCAST, KEY_APIKEY, "#", KEY_BASE_URL, "#",
			KEY_ELECTRICITY_MPAN_IMPORT, KEY_ELECTRICITY_MPAN_EXPORT, KEY_ELECTRICITY_SN, KEY_GAS_SN,
			KEY_FLEXIBLE_ELECTRICITY_VIA_DIRECT_DEBIT, KEY_FLEXIBLE_ELECTRICITY_PRODUCT_CODE,
			KEY_FLEXIBLE_ELECTRICITY_UNIT, KEY_FLEXIBLE_ELECTRICITY_STANDING, KEY_AGILE_ELECTRICITY_STANDING,
			KEY_IMPORT_PRODUCT_CODE, KEY_IMPORT_TARIFF_CODE, KEY_IMPORT_TARIFF_URL, KEY_REGION, KEY_POSTCODE,
			KEY_ZONE_ID, KEY_HISTORY_IMPORT, KEY_HISTORY_EXPORT, "#", KEY_EXPORT_PRODUCT_CODE, KEY_EXPORT_TARIFF_CODE,
			KEY_EXPORT_TARIFF_URL, KEY_EXPORT, "#", KEY_BASELINE, KEY_DAYS, KEY_PLUNGE, KEY_TARGET, KEY_WIDTH, KEY_ANSI,
			KEY_COLOUR, KEY_COLOR, "#", KEY_YEARLY, KEY_MONTHLY, KEY_WEEKLY, KEY_DAILY, KEY_DAY_FROM, KEY_DAY_TO, "#",
			KEY_CHECK, KEY_EXTRA, KEY_SOLAR, KEY_PERCENT, KEY_GRID, KEY_CONSUMPTION, KEY_TEMPERATURE, KEY_BATTERY, "#",
			KEY_FORECAST_SOLAR, KEY_FORECAST_SOLCAST, KEY_API_SOLCAST, KEY_FILE_SOLAR, KEY_MAX_SOLAR, KEY_MAX_RATE, "#",
			KEY_REFERRAL };

	private final static String DEFAULT_API_SOLCAST_PROPERTY = "blahblahblah";
	private final static String DEFAULT_API_OCTOPUS_PROPERTY = "blah_BLAH2pMoreBlahPIXOIO72aIO1blah:";
	private final static String DEFAULT_BASE_URL_PROPERTY = "https://api.octopus.energy";
	private final static String DEFAULT_HISTORY_IMPORT_PROPERTY = "octopus.import.csv";
	private final static String DEFAULT_HISTORY_EXPORT_PROPERTY = "octopus.export.csv";
	private final static String DEFAULT_TARIFF_URL_PROPERTY = "";
	private final static String DEFAULT_BASELINE_PROPERTY = "15";
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
	private final static String DEFAULT_SOLAR_PROPERTY = "false";
	private final static String DEFAULT_PERCENT_PROPERTY = "false";
	private final static String DEFAULT_GRID_PROPERTY = "false";
	private final static String DEFAULT_CONSUMPTION_PROPERTY = "false";
	private final static String DEFAULT_TEMPERATURE_PROPERTY = "false";
	private final static String DEFAULT_BATTERY_PROPERTY = "false";

	private final static String DEFAULT_FORECAST_SOLAR_PROPERTY = "false";
	private final static String DEFAULT_FORECAST_SOLCAST_PROPERTY = "false";

	private final static String DEFAULT_MAX_SOLAR_PROPERTY = "false";
	private final static String DEFAULT_MAX_RATE_PROPERTY = "6000";
	private final static String DEFAULT_FILE_SOLAR_PROPERTY = "false";

	private final static String DEFAULT_ELECTRICITY_MPAN_IMPORT_PROPERTY = "200001010101";
	private final static String DEFAULT_ELECTRICITY_MPAN_EXPORT_PROPERTY = "201010101010";
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

	private final static String DEFAULT_REFERRAL_PROPERTY = "https://share.octopus.energy/ice-camel-111";

	private final static DateTimeFormatter simpleTime = DateTimeFormatter.ofPattern("E MMM dd pph:mm a");

	protected final static DateTimeFormatter formatter24HourClock = DateTimeFormatter.ofPattern("HH:mm");

	private final static DateTimeFormatter formatter12HourClock = DateTimeFormatter.ofPattern("h:mm a");

	private final static DateTimeFormatter formatterDayHourMinute = DateTimeFormatter.ofPattern("E HH:mm");

	private final static DateTimeFormatter formatterDayHourMinuteSecond = DateTimeFormatter.ofPattern("E HH:mm:ss");

	private final static DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	private final static DateTimeFormatter formatterLocalDate = DateTimeFormatter.ISO_LOCAL_DATE;

	private final static DateTimeFormatter formatterSolar = DateTimeFormatter.ofPattern("YYYY-MM-dd");

	private static ZoneId ourZoneId;

	private static ObjectMapper mapper;

	private static int width;

	private static int plunge;

	private static int target;

	private static boolean ansi;

	private static boolean export;

	private static String check = DEFAULT_CHECK_PROPERTY; // overridden by check=value in properties

	private static String extra = DEFAULT_EXTRA_PROPERTY; // overridden by extra=value in properties

	private static String solar = DEFAULT_SOLAR_PROPERTY; // overridden by solar=value in properties

	private static String percent = DEFAULT_PERCENT_PROPERTY; // overridden by battery=value in properties

	private static String grid = DEFAULT_GRID_PROPERTY; // overridden by grid=value in properties

	private static String consumption = DEFAULT_CONSUMPTION_PROPERTY; // overridden by consumption=value in properties

	private static String forecastSolar = DEFAULT_FORECAST_SOLAR_PROPERTY;// overridden by forcecast.solar=value in
																			// properties

	private static String forecastSolcast = DEFAULT_FORECAST_SOLCAST_PROPERTY; // overridden by forcecast.solcast=value
																				// properties

	private static String maxSolar = DEFAULT_MAX_SOLAR_PROPERTY;// overridden by max.solar=value in properties

	private static String maxRate = DEFAULT_MAX_RATE_PROPERTY;// overridden by max.solar=value in properties

	private static String temperature = DEFAULT_TEMPERATURE_PROPERTY;// overridden by temperature=value in properties

	private static String chargeDischarge = DEFAULT_BATTERY_PROPERTY;// overridden by charge=value in properties

	private static File fileSolar = null;

	private static boolean usingExternalPropertyFile = false;

	private static int extended = 0; // overridden by args[]

	private static Properties properties;

	private static Octopussy instance = null;

	private static String ANSI_COLOUR_LO; // typically GREEN
	private static String ANSI_COLOR_HI; // typically RED

	private static long epochFrom = 0;

	private static Map<Long, ConsumptionHistory> historyImport = null;
	private static Map<Long, ConsumptionHistory> historyExport = null;

	private static Map<String, ImportExportData> importExportPriceMap = null;

	private static SortedSet<String> ascendingKeysForPriceMap = null;

	static String[] schedule = null;

	private WatchSlotChargeHelperThread wd = null;

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

		instance = getInstance();

		Thread currentThread = Thread.currentThread();

		String idHexString = Long.toHexString(currentThread.getId());

		currentThread.setName("Main-" + idHexString);

		File importData = null;
		File exportData = null;

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

			// ASSUMPTION: closest instant to start of hour
			// (typically controlled by crontab schedule)

			ZonedDateTime ourTimeNow = now.atZone(ourZoneId);

			String timestamp = ourTimeNow.toString().substring(0, 19);

			long epochNow = ourTimeNow.toEpochSecond();

			String today = timestamp.substring(0, 10);

			//
			//
			//
			// HOWARD TESTING

//			int defaultPower = 4000;
//
//			float[] schedulePrices = { 1.5f, 1.75f, 2f };
//
//			int index = 2;
//
//			int[] powers = instance.testableOPC(defaultPower, schedulePrices, index);
//
//			System.exit(-1);
			//
			//
			//

			//
			// Each of the following execs takes significant seconds
			//

			Float kWhrSolar = execReadSolar();

			ImportExport gridImportExport = execReadGridImportExport();

			Float kWhrConsumption = execReadConsumption();

			Integer percentBattery = instance.execReadBatteryPercent();

			ChargeDischarge chargeAndDischarge = instance.execReadChargeDischarge();

			//
			//
			//

			// Good place to insert temporary test code

			//
			//
			//

			if (null != propertyAccountId) { // optional verification of Account verses dot.properties

				V1Account details = instance.getV1AccountData(1, propertyAccountId);

				System.out.println("# Verifying these values in the properties file " + propertyFileName + "\n");

				Map<String, String> verify = instance.displayAccountDetails(details, ourTimeNow);

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

			importData = new File(properties.getProperty(KEY_HISTORY_IMPORT, DEFAULT_HISTORY_IMPORT_PROPERTY).trim());

			historyImport = instance.readHistory(importData); // this will reset epochFrom

			Long importEpochFrom = epochFrom;

			// here the octopus.import.csv or similar will exist even if empty

			exportData = new File(properties.getProperty(KEY_HISTORY_EXPORT, DEFAULT_HISTORY_EXPORT_PROPERTY).trim());

			historyExport = instance.readHistory(exportData); // this will reset epochFrom

			Long exportEpochFrom = epochFrom;

			// here the octopus.export.csv or similar will exist even if empty

			//
			//
			//

			int dayOfYearToday = ourTimeNow.getDayOfYear();

			// We define a recent history starting at the configured number of days ago
			// and include the remainder of today in the time span

			// N.B. we are unlikely to get data beyond 22:30 local if this is run before
			// 16:00 at which time when the schedule of prices appear for the next day

			// since we will get a variable amount of data returned we will not pin the
			// periodEnd
			// and instead just grab whatever is available at the time the method is called

			int howManyDaysHistory = Integer.valueOf(properties.getProperty(KEY_DAYS, DEFAULT_DAYS_PROPERTY).trim());

			ZonedDateTime timeRecent;

			if (dayOfYearToday <= howManyDaysHistory) {

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

			V1ElectricityTariff tariff = instance.getV1ElectricityTariffImport(page, pageSize, beginRecentPeriod, null);

			ArrayList<Tariff> importTariffs = tariff.getTariffResults();

			while (null != tariff.getNext()) {

				// we don't yet have all the results - pause and then get the next page

				Thread.sleep(5000); // this is just so we don't bombard the API provider

				page++;

				tariff = instance.getV1ElectricityTariffImport(page, pageSize, beginRecentPeriod, null);

				ArrayList<Tariff> pageAgileResults = tariff.getTariffResults();

				importTariffs.addAll(pageAgileResults);
			}

			ArrayList<Tariff> exportTariffs = null; // only populated if export=true in octopussy.properties

			if (export) {

				tariff = instance.getV1ElectricityTariffExport(48 * howManyDaysHistory, beginRecentPeriod, null);

				exportTariffs = tariff.getTariffResults();
			}

			importExportPriceMap = instance.createPriceMap(importTariffs, exportTariffs);

			ascendingKeysForPriceMap = new TreeSet<String>();

			ascendingKeysForPriceMap.addAll(importExportPriceMap.keySet());

			//
			//
			//

			page = 1;

			// it's possible the recent export data is not yet available

			V1ElectricityConsumption v1ElectricityExported = null;

			ArrayList<V1PeriodConsumption> periodExportResults = null;

			int tries = 0;

			while (null == periodExportResults) {

				if (0 != tries) {

					if (10 == tries) {

						instance.logErrTime("Tried " + tries
								+ " times to getV1ElectricityConsumption() for export without success");

						throw new Exception(
								"\r\nDouble check the apiKey, electricity.mprn & electricity.sn values in the octopussy.properties\r\n"
										+ "An option is to create a new octopussy.properties file in the current directory with the correct values\r\n"
										+ "A template octopussy.properties file can be redisplayed by deleting the octopus.import.csv and running again\r\n"
										+ "Alternatively replace the resource octopussy.properties inside the jar file using 7-Zip or similar\r\n");
					}

					Thread.sleep(60000l); // wait a minute and try again
				}

				// we hope to get this in a single page

				v1ElectricityExported = instance.getV1ElectricityExport(page, 48 * howManyDaysHistory,
						beginRecentPeriod, null);

				periodExportResults = v1ElectricityExported.getPeriodResults();

				tries++;
			}

			while (null != v1ElectricityExported.getNext()) {

//				if (null == v1ElectricityExported.getCount()) {
//
//					break;
//				}

				// we don't yet have all the results - pause and then get the next page

				Thread.sleep(5000); // this is just so we don't bombard the API provider

				page++;

				v1ElectricityExported = instance.getV1ElectricityExport(page, 48 * howManyDaysHistory,
						beginRecentPeriod, null);

				ArrayList<V1PeriodConsumption> pagePeriodResults = v1ElectricityExported.getPeriodResults();

				periodExportResults.addAll(pagePeriodResults);
			}

			periodExportResults = instance.updateHistory(historyExport, beginRecentPeriod, periodExportResults,
					howManyDaysHistory, v1ElectricityExported.getCount());

			//
			//
			//

			page = 1;

			// it's possible the recent import data is not yet available

			V1ElectricityConsumption v1ElectricityImported = null;

			ArrayList<V1PeriodConsumption> periodImportResults = null;

			tries = 0;

			while (null == periodImportResults) {

				if (0 != tries) {

					if (10 == tries) {

						instance.logErrTime("Tried " + tries
								+ " times to getV1ElectricityConsumption() for import without success");

						throw new Exception(
								"\r\nDouble check the apiKey, electricity.mprn & electricity.sn values in the octopussy.properties\r\n"
										+ "An option is to create a new octopussy.properties file in the current directory with the correct values\r\n"
										+ "A template octopussy.properties file can be redisplayed by deleting the octopus.import.csv and running again\r\n"
										+ "Alternatively replace the resource octopussy.properties inside the jar file using 7-Zip or similar\r\n");
					}

					Thread.sleep(60000l); // wait a minute and try again
				}

				// we hope to get this in a single page

				v1ElectricityImported = instance.getV1ElectricityImport(page, 48 * howManyDaysHistory,
						beginRecentPeriod, null);

				periodImportResults = v1ElectricityImported.getPeriodResults();

				tries++;
			}

			while (null != v1ElectricityImported.getNext()) {

				// we don't yet have all the results - pause and then get the next page

				Thread.sleep(5000); // this is just so we don't bombard the API provider

				page++;

				v1ElectricityImported = instance.getV1ElectricityImport(page, 48 * howManyDaysHistory,
						beginRecentPeriod, null);

				ArrayList<V1PeriodConsumption> pagePeriodResults = v1ElectricityImported.getPeriodResults();

				periodImportResults.addAll(pagePeriodResults);
			}

			periodImportResults = instance.updateHistory(historyImport, beginRecentPeriod, periodImportResults,
					howManyDaysHistory, v1ElectricityImported.getCount());

			//
			//
			//

			Map<String, DayValues> elecMapDaily = instance.buildElecMapDaily(periodImportResults, periodExportResults);

			//
			//
			//

			instance.appendToHistoryFile(importData, importEpochFrom, historyImport);

			instance.appendToHistoryFile(exportData, exportEpochFrom, historyExport);

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

				SortedMap<String, PeriodicValues> yearly = accumulateCostsByField(ChronoField.YEAR, upToEpochSecond);

				System.out.println("\nHistorical yearly results:");

				displayPeriodSummary("Year", yearly, null, null);
			}

			//
			//
			//

			if (Boolean.TRUE.equals(Boolean.valueOf(properties.getProperty(KEY_MONTHLY, DEFAULT_MONTHLY_PROPERTY)))) {

				SortedMap<String, PeriodicValues> monthly = accumulateCostsByField(ChronoField.MONTH_OF_YEAR,
						upToEpochSecond);

				System.out.println("\nHistorical monthly results:");

				displayPeriodSummary("Month", monthly, null, null);
			}

			//
			//
			//

			if (Boolean.TRUE.equals(Boolean.valueOf(properties.getProperty(KEY_WEEKLY, DEFAULT_WEEKLY_PROPERTY)))) {

				SortedMap<String, PeriodicValues> weekly = accumulateCostsByField(ChronoField.ALIGNED_WEEK_OF_YEAR,
						upToEpochSecond);

				System.out.println("\nHistorical weekly results:");

				displayPeriodSummary("Week", weekly, null, null);
			}
			//
			//
			//

			if (Boolean.TRUE.equals(Boolean.valueOf(properties.getProperty(KEY_DAILY, DEFAULT_DAILY_PROPERTY)))) {

				// get epochSecond for start of next day of range

				SortedMap<String, PeriodicValues> daily = accumulateCostsByField(ChronoField.EPOCH_DAY,
						requiredEpochSecond < 0 ? upToEpochSecond : requiredEpochSecond);

				System.out.println("\nHistorical daily results: " + ("".equals(filterFrom) ? "" : " from " + filterFrom)
						+ ("".equals(filterTo) ? "" : " up to " + filterTo));

				displayPeriodSummary("Day", daily, fromEpochDayIncl, toEpochDayIncl);
			}

			//
			//
			//

			int averageUnitCost = instance.dailyResults(today, elecMapDaily);

			//
			//
			//

			// find time at start of day

			LocalDateTime startOfDay = LocalDateTime.ofInstant(now, ourZoneId).withHour(0).withMinute(0).withSecond(0)
					.withNano(0);

			long epochAtStartOfDay = startOfDay.atZone(ourZoneId).toEpochSecond();

			List<SlotCost> pricesPerSlotSinceMidnight = instance.buildListSlotCosts(epochAtStartOfDay,
					ascendingKeysForPriceMap);

			List<SlotCost> pricesPerSlot = new ArrayList<SlotCost>();

			// Create a subset filtered by half an hour ago

			final long halfAnHourAgo = epochNow - 1800;

			for (SlotCost sc : pricesPerSlotSinceMidnight) {

				if (sc.getEpochSecond() >= halfAnHourAgo) {

					pricesPerSlot.add(sc);
				}
			}

			int currentSlotIndex = pricesPerSlotSinceMidnight.size() - pricesPerSlot.size();

			ArrayList<Long> bestExportTime = null;

			if (export) {

				bestExportTime = instance.upcomingExport(pricesPerSlot);
			}

			//
			//
			//

			if (execute) {

				schedule = instance.scheduleBatteryCharging(pricesPerSlotSinceMidnight, currentSlotIndex, kWhrSolar,
						gridImportExport, kWhrConsumption, percentBattery, chargeAndDischarge, timestamp);
			}

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

			//
			// Before terminating main, wait for the optional WatchSlotChargeHelperThread
			// Thread is launched to monitor battery state while charging is occurring
			//

			if (null != instance.wd) {

				if (instance.wd.isAlive()) {

					long futureSlotStart = pricesPerSlot.get(1).getEpochSecond();

					long millis = 1000 * (futureSlotStart - Instant.now().getEpochSecond() - 10); // knock of 10 seconds

					instance.wd.join(millis); // expect thread to die within millis

					if (instance.wd.isAlive()) {

						instance.logErrTime("Forcing Watch thread interrupt 10s before next slot starts");

						instance.wd.interrupt();

					} else {

						instance.logErrTime("Confirmation Watch thread no longer alive");
					}
				}

				// restore charging rate to maximum (this overrides power[p]) due to day
				// time when we want to use all the solar energy available to fill battery

				instance.logErrTime("Resetting charging power to " + maxRate + " watts");

				instance.resetChargingPower(Integer.valueOf(maxRate));
			}

			System.exit(0);

		} catch (Exception e) {

			System.out.flush();

			e.printStackTrace();

			System.exit(-1);
		}
	}

	private String[] readChargingSchedule(final int howMany) {

		String[] readEndTimesParameter = { "65", "103", "106", "109", "112", "115", "118", "121", "124", "127" };

		List<String> endTimes = new ArrayList<String>();

		if (!"false".equalsIgnoreCase(check)) {

			int tally = 0;

			for (int s = 0; s < howMany; s++) {

				// what is the current 'to' time in the inverter for Slot 1/2/3/4/5?

				String hhmm = execRead(check, readEndTimesParameter[s]);

				try {
					Thread.sleep(2500l);

				} catch (InterruptedException e) {

					e.printStackTrace();
					break;
				}

				if ("".equals(hhmm)) {

					tally++;

					instance.logErrTime("Try #" + tally + " fail in readChargingSchedule(" + howMany
							+ ") execRead check [" + s + "]");

					if (5 == tally) {

						endTimes.add(hhmm);
						break;
					}

					try {
						Thread.sleep(60000l); // pause 1 minute to avoid DoS

					} catch (InterruptedException e) {

						e.printStackTrace();
						break;
					}

					// try again
					s--;

				} else {

					endTimes.add(hhmm);
				}
			}
		}

		String[] result = new String[endTimes.size()];

		result = endTimes.toArray(result);

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

				String[] elements = line.split("\t");

				if (20 == elements[0].length() || ('T' == elements[0].charAt(10) && elements[0].endsWith("Z"))) {

					String value = elements[1].trim();

					float power = 0;

					power = "0.0".equals(value) ? 0 : Float.parseFloat(value);

					Integer duration = Integer.parseInt(elements[3].trim());

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

	private void compressPowerList(String fileName, List<PowerDuration> averageOffsetPowerList) throws IOException {

		File file = new File(fileName);

		if (file.createNewFile()) {

		} else {

			// file already exists
		}

		OffsetDateTime odt = OffsetDateTime.ofInstant(now, ourZoneId).withNano(0).withSecond(0);

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

		LocalDateTime ldt = LocalDateTime.ofInstant(now, ourZoneId);

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

	private Map<String, String> displayAccountDetails(V1Account account, ZonedDateTime timeNow) throws Exception {

		Map<String, String> result = new HashMap<String, String>();

		result.put(KEY_BASE_URL, DEFAULT_BASE_URL_PROPERTY);

		List<Detail> details = account.getProperties();

		Detail detail = details.get(0);

		String postcode = detail.getPostcode().substring(0, 4).trim();

		result.put(KEY_POSTCODE, postcode);

		List<ElectricityMeterPoint> electricityMeterPoints = detail.getElectrictyMeterPoints();

		for (int index = 0; index < electricityMeterPoints.size(); index++) {

			ElectricityMeterPoint electricityMeterPoint = electricityMeterPoints.get(index);

			List<Meter> meters = electricityMeterPoint.getMeters();

			for (int m = 0; m < meters.size(); m++) {

				Meter meter = meters.get(m);

				result.put(KEY_ELECTRICITY_SN, meter.getSerialNumber());

//				List<Register> registers = meter.getRegisters();
//
//				for (int r = 0; r < registers.size(); r++) {
//
//					Register register = registers.get(r);
//
//					System.out.println("TODO: id[" + r + "]=" + register.getIdentifier());
//					System.out.println("TODO: rate[" + r + "]=" + register.getRate());
//					System.out.println("TODO: IsSettlementRegister[" + r + "]=" + register.getIsSettlementRegister());
//				}
			}

			String mpan = electricityMeterPoint.getMpan();

			boolean isExportProfile = false;

			switch (electricityMeterPoint.getProfileClass()) {

			case 8:
				result.put(KEY_ELECTRICITY_MPAN_EXPORT, mpan);

				isExportProfile = true;
				break;

			case 1:
			default:

				result.put(KEY_ELECTRICITY_MPAN_IMPORT, mpan);

				break;
			}

			// find active electricity agreement

			String tariffCode = null;

			Agreement activeAgreement = null;

			for (Agreement agreement : electricityMeterPoint.getAgreements()) {

				tariffCode = agreement.getTariffCode();

				String from = agreement.getValidFrom();

				String to = agreement.getValidTo(); // can be in past, future or null (meaning open-ended?)

				ZonedDateTime zdtTo = null == to ? null : ZonedDateTime.parse(to, defaultDateTimeFormatter);

				ZonedDateTime zdtFrom = ZonedDateTime.parse(from, defaultDateTimeFormatter);

				if (null == zdtTo || (timeNow.isAfter(zdtFrom) && timeNow.isBefore(zdtTo))) {

//					System.out.println((isExportProfile ? "Export" : "Import") + " tariffCode " + tariffCode
//							+ " is valid from " + from);

					activeAgreement = agreement;
					break;

				} else {

//					System.out.println((isExportProfile ? "Export" : "Import") + " tariffCode " + tariffCode
//							+ " was in use from " + agreement.getValidFrom() + " to " + agreement.getValidTo());
				}
			}

			if (null == activeAgreement) {

				System.err.println("No active agreement for electricty_meter_point " + mpan);
				continue;
			}

			if (isExportProfile) {

				result.put(KEY_EXPORT_TARIFF_CODE, tariffCode);

				String exportProductCode = tariffCode.substring(5, tariffCode.length() - 2);

				result.put(KEY_EXPORT_PRODUCT_CODE, exportProductCode);

				result.put(KEY_EXPORT_TARIFF_URL,
						result.get(KEY_BASE_URL) + "/v1/products/" + result.get(KEY_EXPORT_PRODUCT_CODE)
								+ "/electricity-tariffs/" + result.get(KEY_EXPORT_TARIFF_CODE));

			} else {

				result.put(KEY_IMPORT_TARIFF_CODE, tariffCode);

				String importProductCode = tariffCode.substring(5, tariffCode.length() - 2);

				result.put(KEY_IMPORT_PRODUCT_CODE, importProductCode);

				result.put(KEY_IMPORT_TARIFF_URL,
						result.get(KEY_BASE_URL) + "/v1/products/" + result.get(KEY_IMPORT_PRODUCT_CODE)
								+ "/electricity-tariffs/" + result.get(KEY_IMPORT_TARIFF_CODE));

				String region = tariffCode.substring(tariffCode.length() - 1);

				result.put(KEY_REGION, region);

				String prefix = tariffCode.substring(0, 5);

				String periodFrom = activeAgreement.getValidFrom().substring(0, 10);

				V1Charges agileElectricityStandingCharges = getV1ElectricityStandingCharges(importProductCode,
						tariffCode, 100, periodFrom, null);

				result.put(KEY_AGILE_ELECTRICITY_STANDING,
						String.valueOf(agileElectricityStandingCharges.getPriceResults().get(0).getValueIncVAT()));

				boolean directDebitPrices = Boolean
						.parseBoolean(properties.getProperty(KEY_FLEXIBLE_ELECTRICITY_VIA_DIRECT_DEBIT,
								DEFAULT_FLEXIBLE_ELECTRICITY_VIA_DIRECT_DEBIT_PROPERTY));

				result.put(KEY_FLEXIBLE_ELECTRICITY_VIA_DIRECT_DEBIT, (directDebitPrices ? "true" : "false"));

				String flexibleElectricityProductCode = properties.getProperty(KEY_FLEXIBLE_ELECTRICITY_PRODUCT_CODE,
						DEFAULT_FLEXIBLE_ELECTRICITY_PRODUCT_CODE_PROPERTY);

				result.put(KEY_FLEXIBLE_ELECTRICITY_PRODUCT_CODE, flexibleElectricityProductCode);

				String flexibleElectricityTariffCode = prefix + flexibleElectricityProductCode + "-" + region;

				V1Charges flexibleElectricityStandingCharges = getV1ElectricityStandingCharges(
						flexibleElectricityProductCode, flexibleElectricityTariffCode, 100, periodFrom, null);

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
			}

		}

		return result;
	}

	private static void displayPeriodSummary(String id, SortedMap<String, PeriodicValues> periodic, Integer fromIncl,
			Integer toIncl) {

		String datestamp = null;

		Float tallyEnergy = Float.valueOf(0);

		Float tallyCost = Float.valueOf(0);

		int tallyHalfHours = 0;

		int count = 0;

		for (String key : periodic.keySet()) {

			Integer number = Integer.parseInt(key.substring(5));

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

			PeriodicValues periodData = periodic.get(key);

			Integer countHalfHours = periodData.getCountHalfHours();

			tallyHalfHours += countHalfHours;

			if (id.startsWith("D")) {

				// get the day as YYYY-MM-DD

				Long epochSecond = (long) number * 86400;

				LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), ourZoneId);

				DayOfWeek dow = ldt.getDayOfWeek();

				datestamp = " " + dow.name().substring(0, 3) + " " + ldt.toString().substring(0, 10);

			} else if (id.startsWith("Y")) {

				// assume year

				datestamp = String.format("%6s", id) + ":" + String.format("%4d", number) + "    ";

			} else {

				// assume month or week

				datestamp = String.format("%6s", id) + ":" + String.format("%3d", number) + "/" + key.substring(0, 4);
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

			System.out.println(datestamp + " " + String.format("%8.3f", accConsumption) + " kWhr "
					+ String.format("%9.2f", accCost) + "p  " + String.format("%5d", countHalfHours) + " half-hours ~ "
					+ String.format("%6.2f", equivalentDays) + " days @ £"
					+ String.format("%7.2f", equivalentDailyAverageCost) + " Daily equivalent: "
					+ String.format("%6.3f", equivalentDailyEnergy) + " kWhr @ "
					+ String.format("%5.2f", averagePricePerUnit) + "p per unit");
		}

		if (count > 1) {

			float equivalentDays = tallyHalfHours / (float) 48;

			float averageDailyEnergy = tallyEnergy / equivalentDays;

			System.out.println("Totals:\t\t" + String.format("%8.3f", tallyEnergy) + " kWhr\t\t\t\t    "
					+ String.format("%7.2f", equivalentDays) + " days   £" + String.format("%7.2f", (tallyCost / 100))
					+ "\t     Average: " + String.format("%6.3f", averageDailyEnergy) + " kWhr @ "
					+ String.format("%5.2f", tallyCost / tallyEnergy) + "p");
		}
	}

	private static SortedMap<String, PeriodicValues> accumulateCostsByField(ChronoField field, Long upToEpochSecond) {

		SortedMap<String, PeriodicValues> result = new TreeMap<String, PeriodicValues>();

		for (Long key : historyImport.keySet()) {

			if (null != upToEpochSecond) {

				if (key >= upToEpochSecond) {

					break;
				}
			}

			ConsumptionHistory data = historyImport.get(key);

			Float price = data.getPriceImportedOrExported();

			if (null == price) {

				continue;
			}

			Float consumption = data.getConsumption();

			if (null == consumption) {

				continue;
			}

			LocalDateTime ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(key), ourZoneId);

			String calendarElement = String.valueOf(ldt.getYear()) + "_" + String.format("%02d", ldt.getLong(field));

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

		extra = properties.getProperty(KEY_EXTRA, DEFAULT_EXTRA_PROPERTY).trim();

		solar = properties.getProperty(KEY_SOLAR, DEFAULT_SOLAR_PROPERTY).trim();

		percent = properties.getProperty(KEY_PERCENT, DEFAULT_PERCENT_PROPERTY).trim();

		grid = properties.getProperty(KEY_GRID, DEFAULT_GRID_PROPERTY).trim();

		consumption = properties.getProperty(KEY_CONSUMPTION, DEFAULT_CONSUMPTION_PROPERTY).trim();

		forecastSolar = properties.getProperty(KEY_FORECAST_SOLAR, DEFAULT_FORECAST_SOLAR_PROPERTY).trim();

		forecastSolcast = properties.getProperty(KEY_FORECAST_SOLCAST, DEFAULT_FORECAST_SOLCAST_PROPERTY).trim();

		maxSolar = properties.getProperty(KEY_MAX_SOLAR, DEFAULT_MAX_SOLAR_PROPERTY).trim();

		maxRate = properties.getProperty(KEY_MAX_RATE, DEFAULT_MAX_RATE_PROPERTY).trim();

		{
			String logNameForSolarData = properties.getProperty(KEY_FILE_SOLAR, DEFAULT_FILE_SOLAR_PROPERTY).trim();

			if (0 != "false".compareTo(logNameForSolarData) && 0 != "false".compareTo(forecastSolar)) {

				fileSolar = new File(logNameForSolarData);
			}
		}

		temperature = properties.getProperty(KEY_TEMPERATURE, DEFAULT_TEMPERATURE_PROPERTY).trim();

		chargeDischarge = properties.getProperty(KEY_BATTERY, DEFAULT_BATTERY_PROPERTY).trim();

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

	private V1ElectricityConsumption getV1ElectricityExport(Integer page, Integer pageSize, String periodFrom,
			String periodTo) throws MalformedURLException, IOException {

		String mprn = properties.getProperty(KEY_ELECTRICITY_MPAN_EXPORT, DEFAULT_ELECTRICITY_MPAN_EXPORT_PROPERTY)
				.trim();

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

	private V1ElectricityConsumption getV1ElectricityImport(Integer page, Integer pageSize, String periodFrom,
			String periodTo) throws MalformedURLException, IOException {

		String mprn = properties.getProperty(KEY_ELECTRICITY_MPAN_IMPORT, DEFAULT_ELECTRICITY_MPAN_IMPORT_PROPERTY)
				.trim();

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

	private V1ElectricityTariff getV1ElectricityTariffImport(Integer page, Integer pageSize, String periodFrom,
			String periodTo) throws MalformedURLException, IOException {

		V1ElectricityTariff result = null;

		String spec = properties.getProperty(KEY_IMPORT_TARIFF_URL, DEFAULT_TARIFF_URL_PROPERTY).trim()
				+ "/standard-unit-rates/" + "?page_size=" + pageSize + (null == page ? "" : "&page=" + page)
				+ (null == periodFrom ? "" : "&period_from=" + periodFrom)
				+ (null == periodTo ? "" : "&period_to=" + periodTo);

		int tries = 0;

		boolean waitingForGoodResult = true;

		while (waitingForGoodResult) {

			tries++;

			try {
				String json = getRequest(new URL(spec), false);

				result = mapper.readValue(json, V1ElectricityTariff.class);

				waitingForGoodResult = false;

			} catch (Exception e) {

				System.out.println(tries + ")\tpage:" + page + "\tpageSize:" + pageSize + "\tperiodFrom:" + periodFrom
						+ "\tperiodTo:" + periodTo + "\t" + e.getMessage() + "\n" + spec);

				try {

					Thread.sleep(60000L); // 1 minute pause before hitting API again

				} catch (InterruptedException e1) {

					e1.printStackTrace();
				}
			}
		}

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

	private V1ElectricityTariff getV1ElectricityTariffExport(Integer pageSize, String periodFrom, String periodTo)
			throws MalformedURLException, IOException {

		String spec = properties.getProperty(KEY_EXPORT_TARIFF_URL, DEFAULT_EXPORT_TARIFF_URL_PROPERTY).trim()
				+ "/standard-unit-rates/" + "?page_size=" + pageSize
				+ (null == periodFrom ? "" : "&period_from=" + periodFrom)
				+ (null == periodTo ? "" : "&period_to=" + periodTo);

		String json = getRequest(new URL(spec), false);

		V1ElectricityTariff result = mapper.readValue(json, V1ElectricityTariff.class);

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

		return getRequest(url, authorisationRequired, null);
	}

	private static String getRequest(URL url, boolean authorisationRequired, String bearer) throws IOException {

		int status = 0;

		HttpURLConnection con = null;

		con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");

		con.setRequestProperty("Accept", contentType);
		con.setRequestProperty("Content-Type", contentType);
		con.setRequestProperty("user-agent", userAgent);

		if (authorisationRequired) {

			if (null == bearer) {

				con.setRequestProperty("Authorization", properties.getProperty("basic"));

			} else {

				con.setRequestProperty("Authorization", "Bearer " + bearer);
			}
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

	private Map<Long, ConsumptionHistory> readHistory(File file)
			throws NumberFormatException, FileNotFoundException, IOException {

		//
		// read historical consumption data from octopus.import.csv and/or
		// octopus.export.csv
		//

		Map<Long, ConsumptionHistory> history = new TreeMap<Long, ConsumptionHistory>();

		epochFrom = 0;

		Scanner myReader = null;

		if (file.createNewFile()) {

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

			myReader = new Scanner(file);

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

					if (!fields[3].contains("nu")) {

						price = Float.valueOf(fields[3].trim());

						if (null != consumption) {

							cost = 1000 * Float.valueOf(consumption * price);

							Integer thousandTimesToBig = Math.round(cost);

							cost = thousandTimesToBig.floatValue() / 1000f;
						}
					}
				}

				ch.setPriceImportedOrExported(price);
				ch.setCostImportedOrExported(cost);

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

	private void appendToHistoryFile(File file, Long epochFrom, Map<Long, ConsumptionHistory> history)
			throws IOException {

		FileWriter fw = new FileWriter(file, true);

		BufferedWriter bw = new BufferedWriter(fw);

		for (Long key : history.keySet()) { // implicitly in ascending key based on epochSecond

			if (key > epochFrom) {

				// data to append to history

				if (0 == epochFrom) {

					bw.write("Consumption (kWh), Start, End, Price(p), Cost(p)");
					// n.b Cost is derived from consumption * price

					epochFrom = key;
				}

				ConsumptionHistory ch = history.get(key);

				if (null != ch.getConsumption()) {

					String entry = String.format("%6.3f", ch.getConsumption()) + ", "
							+ ch.getFrom().format(defaultDateTimeFormatter) + ", "
							+ ch.getTo().format(defaultDateTimeFormatter) +

							(null == ch.getPriceImportedOrExported() ? "" :

									", " + String.format("%5.2f", ch.getPriceImportedOrExported()) + ", "
											+ String.format("%6.3f", ch.getCostImportedOrExported()));

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

		String keyApiSolcast = null;

		try {

			// Check for existence of octopussy.properties in the current directory
			// if it exists, use it in preference to the built-in resource compiled into the
			// jar

			File externalProperties = new File(propertyFileName);

			usingExternalPropertyFile = loadProperties(externalProperties);

			keyValue = properties.getProperty(KEY_APIKEY, DEFAULT_API_OCTOPUS_PROPERTY).trim();

			if (null == keyValue) {

				throw new Exception(KEY_APIKEY);
			}

			keyApiSolcast = properties.getProperty(KEY_API_SOLCAST, DEFAULT_API_SOLCAST_PROPERTY).trim();

			if (null == keyApiSolcast) {

				throw new Exception(KEY_API_SOLCAST);
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

			execute = 0 == DEFAULT_REFERRAL_PROPERTY.compareTo(properties.getProperty(KEY_REFERRAL, "false").trim());

		} catch (Exception e) {

			e.printStackTrace();

			System.exit(-1);
		}

	}

	private ArrayList<Long> upcomingExport(List<SlotCost> pricesPerSlot) {

		if (!ansi) {

			System.out.println("\nUpcoming best export price periods:");
		}

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

					// As we are on a fixed export tariff (typically 15p at any time)
					// use the ImportPrice (which follows the wholesale price, knowing that
					// the wholesale export price will track the import)
					// This will highlight (in red) on the analysis the best time for the network
					// when our provider would like us to export

//					Float exportPrice = pricesPerSlot.get(i).getExportPrice();
					Float exportPrice = pricesPerSlot.get(i).getImportPrice();

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

			if (!ansi) {

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
		}

		return bestStartTime;
	}

	private int[] findOptimalCostSlotToday(final int howMany, List<SlotCost> pricesPerSlotSinceMidnight,
			final int currentSlotIndex, String untilBefore12hr) {

		int[] result = new int[0];

		if (howMany > 0) {

			String simpleTimeStamp = pricesPerSlotSinceMidnight.get(0).getSimpleTimeStamp();

			String[] elements = simpleTimeStamp.split(" ");

			String today = elements[0] + " " + elements[1] + " " + elements[2];

			String time12hr = elements[elements.length - 2] + " " + elements[elements.length - 1];

			List<Float> prices = new ArrayList<Float>();

			int index = currentSlotIndex;

			do {

				simpleTimeStamp = pricesPerSlotSinceMidnight.get(index).getSimpleTimeStamp();

				elements = simpleTimeStamp.split(" ");

				String testToday = elements[0] + " " + elements[1] + " " + elements[2];

				time12hr = elements[elements.length - 2] + " " + elements[elements.length - 1];

				if (0 != testToday.compareTo(today)) {

					break;
				}

				if (0 == untilBefore12hr.compareTo(time12hr)) {

					break;
				}

				prices.add(pricesPerSlotSinceMidnight.get(index).getImportPrice());

				index++;

			} while (true);

			Collections.sort(prices);

			final int limit = prices.size();

			List<Integer> timeIndex = new ArrayList<Integer>(limit);

			for (index = 0; index < limit; index++) {

				Float f = prices.get(index); // this is the next lowest price

				// which time has this price (which we have not already stored in timeList)?

				for (int i = 0; i < limit; i++) {

					Float importPrice = pricesPerSlotSinceMidnight.get(i + currentSlotIndex).getImportPrice();

					if (importPrice == f) {

						if (timeIndex.contains(i + currentSlotIndex)) {

							continue; // iterate
						}

						timeIndex.add(Integer.valueOf(i + currentSlotIndex));
					}
				}
			}

			result = new int[timeIndex.size() < howMany ? timeIndex.size() : howMany];

			for (int i = 0; i < result.length; i++) {

				Integer nextIndex = timeIndex.get(i);

				result[i] = nextIndex.intValue();

				System.out.println(WatchSlotChargeHelperThread.SN(i) + ": "
						+ pricesPerSlotSinceMidnight.get(nextIndex).getSimpleTimeStamp() + "\t"
						+ pricesPerSlotSinceMidnight.get(nextIndex).getImportPrice() + "p");
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

	/*
	 * copes with HH:MM and HHMM
	 */
	private static LocalDateTime convertHHmm(String hhmm) {

		String hh = hhmm.substring(0, 2);

		String mm = hhmm.substring(hhmm.length() - 2);

		LocalDateTime ldt = LocalDateTime.ofInstant(now, ourZoneId);

		LocalDateTime ldtAdjusted = ldt.withHour(Integer.parseInt(hh)).withMinute(Integer.parseInt(mm)).withSecond(0)
				.withNano(0);

		return ldtAdjusted;
	}

	private String[] scheduleBatteryCharging(List<SlotCost> pricesPerSlotSinceMidnight, int currentSlotIndex,
			Float kWhrSolar, ImportExport gridImportExport, Float kWhrConsumption, Integer percentBattery,
			ChargeDischarge chargeAndDischarge, String timestamp) {

		SlotCost currentSlotCost = pricesPerSlotSinceMidnight.get(currentSlotIndex);

		Long sEpoch = currentSlotCost.getEpochSecond();

		Instant instantRangeStart = Instant.ofEpochSecond(sEpoch);
//
		LocalDateTime ldtRangeStart = LocalDateTime.ofInstant(instantRangeStart, ourZoneId);

		String keyToday = ldtRangeStart.format(formatterSolar); // YYYY-MM-DD
//
//		String rangeStartTime = ldtRangeStart.format(formatter24HourClock);
//
//		String rangeEndTime = ldtRangeStart.plusMinutes(29).format(formatter24HourClock);

		String rangeStartTime = currentSlotCost.getSlotStartTime24hr();

		String rangeEndTime = currentSlotCost.getSlotEndTime24hr();

		// BY convention we divide the day up into 4 parts - each is 24hr time HH:mm
		// EG 00:00, 08:00, 12:00, 18:00
		// However we will allow 0 to N parts with start times as HH:mm stored in
		// parts[N] and the times must be ascending and unique.

		int partNumber = 1;

		String key = "part1";

		LocalDateTime ldtPrevious = convertHHmm("00:00").minusMinutes(1L);

		List<String> parts = new ArrayList<String>();

		int[] sunRisenAndSetIndicies = hasSunRisenAndSet(ldtRangeStart.format(formatterSolar), kWhrSolar,
				rangeStartTime);

		// what slot index is the time now?

		int rangeStartIndex = currentSlotIndex;

		while (properties.containsKey(key)) {

			String timeDynamic = properties.getProperty(key);

			String value = timeDynamic.substring(0, 5);

			String defaultValue = value;

			if (parts.contains(value)) {

				System.err.println("Invalid part value\t" + key + ":" + value);
				parts.clear();
				break;
			}

			if (timeDynamic.length() > 5) { // assume dynamic adjustment to value depending on solar

				int defaultIndex = -1;

				// what slot index is default for this part?

				for (int i = 0; i < slotStartTimes.length; i++) {

					if (slotStartTimes[i].equals(value)) {

						defaultIndex = i;
						break;
					}
				}

				if ('-' == timeDynamic.charAt(5)) { // allow slot to start earlier than default (but not later)

					// have we detected sunrise yet?

					int sunRisenIndex = sunRisenAndSetIndicies[0];

					if (-1 != sunRisenIndex) {

						// is it before our default ?

						if (sunRisenIndex < defaultIndex) {

							value = slotStartTimes[sunRisenIndex];

							System.out.println("Selected option (-) changed " + key + " start time from " + defaultValue
									+ " to " + value + " due to sunrise detection");
						}
					}

				} else if ('+' == timeDynamic.charAt(5)) { // allow slot to start later than default (but not earlier)

					// have we detected sunset yet?

					int sunSetIndex = sunRisenAndSetIndicies[1];

					if (-1 == sunSetIndex) {

						// No, not yet. Push the time to the next slot

						if (rangeStartIndex >= defaultIndex) {

							value = slotStartTimes[1 + rangeStartIndex];
						}

					} else {

						// yes.

						if (sunSetIndex > defaultIndex) {

							value = slotStartTimes[sunSetIndex];

							System.out.println("Selected option (+) changed " + key + " start time from " + defaultValue
									+ " to " + value + " due to sunset detection");
						}
					}
				}
			}

			LocalDateTime ldt = convertHHmm(value);

			if (ldt.compareTo(ldtPrevious) <= 0) {

				System.err.println("Invalid part value\t" + key + ":" + value + " needs to be > previous values");
				parts.clear();
				break;
			}

			parts.add(value);

			key = "part" + String.valueOf(++partNumber);
		}

		final int numberOfParts = parts.size();

		final String dayPartsEndAt24hr[] = new String[numberOfParts];

		final String dayPartsEndBefore12hr[] = new String[numberOfParts];

		final int slotsPerDayPart[] = new int[numberOfParts];

		final Character[] options = new Character[numberOfParts];

		final String[] optionParameters = new String[numberOfParts];

		final int powers[] = new int[numberOfParts];

		final int maxPercents[] = new int[numberOfParts];

		final int minPercents[] = new int[numberOfParts];

		float units = 0;

		System.out.println("\nConfigured charging schedule as follows:");

		for (int p = 0; p < numberOfParts; p++) {

			String partsKey = (p == numberOfParts - 1) ? parts.get(0) : parts.get(1 + p);

			dayPartsEndBefore12hr[p] = convertHHmmTo12Hr(partsKey);

			dayPartsEndAt24hr[p] = convertHHmmMinus1Minute(partsKey);

			String slots = properties.getProperty("slots" + String.valueOf(1 + p));

			int endIndex = slots.indexOf(':'); // if > 0 assume number of slots followed by :Sxxx

			optionParameters[p] = null;

			if (-1 == endIndex) {

				slotsPerDayPart[p] = Integer.valueOf(slots);
				options[p] = ' ';

			} else {

				slotsPerDayPart[p] = Integer.valueOf(slots.substring(0, endIndex));

				options[p] = slots.toUpperCase().charAt(1 + endIndex); // eg 'N' for :Night 'D' for :day

				int paramsIndex = slots.indexOf(':', 1 + endIndex);

				if (paramsIndex > -1) {

					optionParameters[p] = slots.substring(1 + paramsIndex);
				}
			}

			String powerKey = "power" + String.valueOf(1 + p);

			String powerValue = properties.getProperty(powerKey);

			String pvs[] = powerValue.split(":");

			powers[p] = Integer.valueOf(pvs[0]);

			minPercents[p] = 0;

			if (pvs.length > 1) {

				maxPercents[p] = Integer.valueOf(pvs[1]);

				if (pvs.length > 2) {

					minPercents[p] = Integer.valueOf(pvs[2]);
				}

			} else {

				maxPercents[p] = 100;
			}

			float wattHours = slotsPerDayPart[p] * powers[p] / 2;

			StringBuffer sbScaledDown = new StringBuffer(" ");

			if (null != optionParameters[p] && 'N' == options[p]) {

				int numScaledDown = Integer.parseInt(optionParameters[p]);

				if (numScaledDown > 0) {

					sbScaledDown.append('(');

					for (int index = slotsPerDayPart[p] - numScaledDown; index < slotsPerDayPart[p]; index++) {

						sbScaledDown.append(WatchSlotChargeHelperThread.SN(index));
					}

					sbScaledDown.append("reduced)");
				}
			}

			System.out.println("Part" + (1 + p) + ": " + parts.get(p) + " to " + dayPartsEndAt24hr[p] + "\t"
					+ slotsPerDayPart[p] + " half-hour slot(s) @ " + powers[p] + " watts (" + Math.round(wattHours)
					+ " Whr)\tBattery " + String.valueOf(minPercents[p]) + "% to " + String.valueOf(maxPercents[p])
					+ "%\t"
					+ (' ' == options[p] ? " - no option"
							: " + option:" + options[p] + (null == optionParameters[p] ? ""
									: ":" + optionParameters[p] + sbScaledDown.toString())));

			units += (+powers[p] * slotsPerDayPart[p]);
		}

		units = units / 2000;

		System.out
				.println("\nPotential import of up to " + units + " kWhr optionally constrained by limits as follows:");
		System.out.println(
				"For option:D(ay)\t30-min slot charging will be reduced in minutes according to solar forecast");
		System.out.println(
				"For option:N(ight)\t30-min slot charging will be reduced in minutes according to battery level");
		System.out.println("\t\t\tMost expensive slot(s) may have reduced charge rate according to solar forecast");
		System.out.println(
				"No option:\t\tCharging slots full length - no consideration of battery state or solar forecast");
		int p = 0;

		// what part of the day are we in?

		for (; p < numberOfParts - 1; p++) {

			if (rangeStartTime.compareTo(parts.get(1 + p)) < 0) {

				break;
			}
		}

		int numberofChargingSlotsInThisPartOfDay = slotsPerDayPart[p];

		schedule = instance.readChargingSchedule(numberofChargingSlotsInThisPartOfDay);

		Float kWhrCharge = chargeAndDischarge.getCharge();
		Float kWhrDischarge = chargeAndDischarge.getDischarge();

		String charge = String.format("%2.1f", kWhrCharge);
		String discharge = String.format("%2.1f", kWhrDischarge);

		Float kWhrGridImport = gridImportExport.getGridImport();
		Float kWhrGridExport = gridImportExport.getGridExport();

		String gridImportUnits = String.format("%2.1f", kWhrGridImport);
		String gridExportUnits = String.format("%2.1f", kWhrGridExport);

		String penceImport = String.format("%5.2f", currentSlotCost.getImportPrice());

		String data = penceImport + "p Bat:" + percentBattery + "%" + " Sol:" + kWhrSolar + " Imp:" + gridImportUnits
				+ " Cha:" + charge + " Dis:" + discharge + " Con:" + kWhrConsumption + " Exp:" + gridExportUnits;

		final String opts[] = { "default", "day", "night" };

		String opt = 'D' == options[p] ? opts[1] : ('N' == options[p] ? opts[2] : opts[0]);

		logErrTime(rangeStartTime + " " + (1 + p) + "/" + numberOfParts + " (" + opt + ") " + data);

		String power = null;

		int minsDelayStart = 0;

		String chargeDischarge = String.format("%+2.1f", kWhrCharge - kWhrDischarge);

		String csv = rangeStartTime + "," + "SOLAR" + "," + (1 + p) + "," + numberOfParts + "," + percentBattery + ","
				+ kWhrSolar + "," + kWhrGridImport + "," + chargeDischarge + "," + kWhrConsumption + ","
				+ kWhrGridExport + "," + "SOLCAST";

		for (int n = p; n < numberOfParts; n++) {

			int comp = rangeStartTime.compareTo(parts.get(n));

			if (comp < 0) {

				break;
			}

			if (0 == comp) {

				power = String.valueOf(powers[p]);

				String percent = String.valueOf(maxPercents[p]);

//				String keyToday = ldtRangeStart.format(formatterSolar); // YYYY-MM-DD

				Integer todayWHr = null;

				// 1st of 2 solar estimates - this one is our backup forecast in case
				// api.forecast.solar is offline
				SolcastMessage solcastForecast = getForecastSolcast(); // limited number of calls to this API for
																		// free
				Float pvSolcast = null;

				if (null == solcastForecast) {

					logErrTime("ERROR: (1): data not currently available from " + forecastSolcast);

					pvSolcast = 0f;

				} else {

					pvSolcast = pvGuesstimate(solcastForecast, "00:00", "23:59");
				}

				String pvStr = String.format("%2.3f", pvSolcast);

				// 2nd of 2 solar estimates - this is actually our primary forecast
				ResultMessage solarForcast = getForecastSolar(); // limited number of calls to this API for free

				SolarResult solarResult = null;

				if (null != solarForcast) {

					solarResult = solarForcast.getResult();
				}

				if (null == solarResult) { // possibly web site offline / result unavailable at this moment

					logErrTime("ERROR: (2): data not currently available from " + forecastSolar);

					// try once more after a delay. If that fails use the solcast estimate

					try {
						Thread.sleep(60000); // wait a minute

					} catch (InterruptedException e) {

						e.printStackTrace();
					}

					solarForcast = getForecastSolar(); // limited number of calls to this API for free
					solarResult = solarForcast.getResult();

					if (null == solarResult) {

						logErrTime("ERROR: (3): data not currently available from " + forecastSolar);

						// take the Solcast forecast instead

						if (0f == pvSolcast) {

							// try once more after a delay.

							try {
								Thread.sleep(60000); // wait a minute

							} catch (InterruptedException e) {

								e.printStackTrace();
							}

							if (null == solcastForecast) {

								logErrTime("ERROR: (4): data not currently available from " + forecastSolcast);

								todayWHr = 0;

							} else {

								logErrTime("WARNING: (5): solar forcast obtained on 2nd attempt");

								pvSolcast = pvGuesstimate(solcastForecast, "00:00", "23:59");

								todayWHr = Float.valueOf(pvSolcast * 1000).intValue(); // convert to wHr
							}

						} else {

							todayWHr = Float.valueOf(pvSolcast * 1000).intValue(); // convert to wHr
						}

					} else {

						logErrTime("WARNING: (6): solar forcast obtained on 2nd attempt");

						todayWHr = solarResult.getValue(keyToday);

					}
				} else {

					todayWHr = solarResult.getValue(keyToday);
				}

				//

				String substitute = csv.replace("SOLAR", String.valueOf(todayWHr.intValue()));

				csv = substitute.replace("SOLCAST", pvStr);

				//

				if (null != todayWHr) {

					if ('D' == options[p]) { // (day) option - delay start time by N minutes for a sunny day
												// more sunshine predicted: more delay

						minsDelayStart = scaleSolarForcastRange0to29(todayWHr, pvSolcast, p);

					} else if ('N' == options[p]) { // (night) option - depending on battery state
													// delay start time - more battery: more delay.

						minsDelayStart = scaleBatteryRange0to29(percentBattery);
					}
				}

				logErrTime("Part " + (1 + p) + "/" + numberOfParts + " Schedule " + power + " W x "
						+ String.valueOf(schedule.length) + " slot(s) " + percent + "% "
						+ (minsDelayStart > 0 ? "delay:" + minsDelayStart + "m " : "") + "Solar:" + todayWHr + " / "
						+ maxSolar);

				break; // No need to loop
			}
		}

		logSolarData(timestamp, csv);

		int[] slots = null;

		Character deferredMacro = null;

		String deferredStartTime = null;

		if (null != power && numberofChargingSlotsInThisPartOfDay > 0) {

			slots = findOptimalCostSlotToday(numberofChargingSlotsInThisPartOfDay, pricesPerSlotSinceMidnight,
					currentSlotIndex, dayPartsEndBefore12hr[p]);

			if (null != extra && 0 != "false".compareTo(extra)) {

				for (int s = 0; s < numberofChargingSlotsInThisPartOfDay; s++) {

					// Normally slots.length == numberofChargingSlotsInThisPartOfDay
					// However if we configure more slots than can be fit in the remaining in the
					// part of the day slots.length will be < numberofChargingSlotsInThisPartOfDay
					// Exploit this to clear charging slots to 00:29

					char macro = "ABCDEFGHIJ".charAt(s);

					String from = "00:29";
					String to = "00:29";

					if (s < slots.length) {

						SlotCost sc = pricesPerSlotSinceMidnight.get(slots[s]);

						String[] period = startAndFinishTimeOfSlotCost(sc, minsDelayStart);

						from = period[0];
						to = period[1];

						float importPrice = sc.getImportPrice();

						logErrTime("Scheduling  " + WatchSlotChargeHelperThread.SN(s) + " for " + (29 - minsDelayStart)
								+ " mins between " + from + " and " + to + " @ " + importPrice + "p / unit");

					} else {

						logErrTime("Resetting Slot" + (1 + s) + " to end at 00:29");
					}

					// if to == rangeEndTime then defer execMacro until we checked battery level

					if (0 == rangeEndTime.compareTo(to)) {

						schedule[s] = to;

						deferredMacro = macro;
						deferredStartTime = from;

					} else {

						schedule[s] = execMacro(macro, extra, from, to, maxPercents[p]);
					}
				}
			}
		}

		// create an array of prices related to the times in the schedule

		float[] schedulePrices = new float[schedule.length];

		int pricesInitialised = 0;

		for (int n = 0; n < schedule.length; n++) {

			// reminder: each schedule[n] gives the slot end time in 24hr

			boolean found = false;

			for (SlotCost sc : pricesPerSlotSinceMidnight) {

				if (0 == schedule[n].compareTo(sc.getSlotEndTime24hr())) {

					schedulePrices[n] = sc.getImportPrice();

					found = true;
					pricesInitialised++;
					break;
				}
			}

			if (!found) {

				Exception e = new Exception("logic error: no match for time " + schedule[n] + " schedule[" + n + "]");

				e.printStackTrace();
			}
		}

		int minPercent = minPercents[p];
		int maxPercent = maxPercents[p];

		int defaultChargeRate = powers[p];

		int chargingSlotPower = defaultChargeRate;

		// is the current time a charging slot according to the current schedule?

		for (int s = 0; s < schedule.length; s++) {

			int comp = rangeEndTime.compareTo(schedule[s]);

			if (0 == comp) {

				// if price negative - charge now *irrespective* of profile

				if (schedulePrices[s] < 0) {

					// higher charge rate for lowest cost slot

					defaultChargeRate = optimisePowerCost(Integer.parseInt(maxRate), schedulePrices, s);

					logErrTime("ALERT: Overiding limit " + maxPercent + "% / @ " + powers[p] + "W with 100% / @ "
							+ defaultChargeRate + " W");

					minPercent = 100; // will trigger a restart:true within the WatchSlotChargeHelperThread
					maxPercent = 100;

					chargingSlotPower = -1; // special indicator for current electricity price negative

				} else if (null != percentBattery && percentBattery >= maxPercents[p]) {

					logErrTime("Unscheduling S" + (1 + s) + " Battery charge. Already >= " + maxPercent
							+ "% Resetting begin/end to " + rangeEndTime);

					resetSlot(s, rangeEndTime, rangeEndTime, 100);

					break; // do not drop through to new WatchSlotChargeHelperThread

				} else {

					if (null != deferredMacro) {

						logErrTime("Starting charge soon at " + deferredStartTime + " Battery limit < " + maxPercent
								+ "%");

						schedule[s] = execMacro(deferredMacro.charValue(), extra, deferredStartTime, rangeEndTime,
								maxPercent);
					}

					String dateYYYY_MM_DD = logErrTime(
							"Time matches " + WatchSlotChargeHelperThread.SN(s) + "ending at " + rangeEndTime)
							.substring(0, 10);

					logErrTime("Adjusting charging power according to selected option " + options[p]
							+ (null != optionParameters[p] ? ":" + optionParameters[p] : ""));

					if ('N' == options[p]) { // (night) option - charging will start at beginning of slot
												// however for at least 1 slot power scaled downwards depending on
												// solar
												// use option Night:2 to decrease in power 2 most expensive slots
												// i.e., slot3 & slot4 if there are 4 in this part of the day

						int slotsToReducePower = 1;

						if (null != optionParameters[p]) {

							slotsToReducePower = Integer.parseInt(optionParameters[p]);
						}

						if (s >= schedule.length - slotsToReducePower) {

							// ./solar.csv will contain the solar predictions
							// stored earlier at the slot scheduling time

							String suffix = parts.get(p).substring(0, 5); // typically "00:00"

							String[] cols = delogSolarData(dateYYYY_MM_DD, suffix);

							if (null == cols) {

								chargingSlotPower = Math.round(powers[p] / 2);

								logErrTime("ERROR: Cannot get solar data for " + dateYYYY_MM_DD + " " + suffix
										+ " scale 50% by default");

							} else {

								// col[2] will hold solar prediction for current part (in Whr)

								Integer WHrToday = Integer.valueOf(cols[2]);

								Float pvSolcast = Float.valueOf(cols[cols.length - 1]);

								// reduce chargingSlotPower if high prediction
								// leave chargingSlotPower if zero or very low predicted

								int scaled = 30 - scaleSolarForcastRange0to29(WHrToday, pvSolcast, p);

								float chargeRate = (float) powers[p] * (float) scaled / 30.0f;

								chargingSlotPower = Math.round(chargeRate);

								logErrTime("Part " + (1 + p) + " Sol: " + cols[2] + "/" + maxSolar + " " + powers[p]
										+ " watts scaled down by (1-30): " + scaled + "/30");
							}
						}

					} else if ('D' == options[p]) { // (day) option

						if (schedule.length == pricesInitialised && schedule.length > 1) {

							// we know all the prices in this part of the day
							// have been established for the slots in the schedule

							chargingSlotPower = optimisePowerCost(powers[p], schedulePrices, s);

						} else {

							chargingSlotPower = defaultChargeRate;
						}
					}
				}

				wd = new WatchSlotChargeHelperThread(this, 29, s, maxPercent, minPercent, defaultChargeRate);

				wd.start(); // this spawned thread will run no longer than HH:MM in schedule[s]
				// the slot will be reset when task complete

				if (chargingSlotPower > 0) {

					logErrTime("Charging limits set to " + chargingSlotPower + " watts and battery range " + minPercent
							+ "% - " + maxPercent + "%");

					resetChargingPower(chargingSlotPower);
				}

				break;
			}
		}

		return schedule;
	}

	@Override
	public void resetChargingPower(int power) {

		execWrite(check, "72", String.valueOf(power));
	}

	@Override
	public void resetSlot(int scheduleIndex, String startTime, String expiryTime, Integer maxPercent) {

		char macro = "ABCDEFGHIJ".charAt(scheduleIndex);

		logErrTime(WatchSlotChargeHelperThread.SN(scheduleIndex) + "Reset " + startTime + "-" + expiryTime + " "
				+ maxPercent + "%");

		execMacro(macro, extra, startTime, expiryTime, maxPercent);
	}

	private Float pvGuesstimate(SolcastMessage solcastForecast, String beginAt24hrHHMM, String endAt24hrHHMM) {

		Float result = 0f;

		int hoursBegin = Integer.parseInt(beginAt24hrHHMM.substring(0, 2));
		int minsBegin = Integer.parseInt(beginAt24hrHHMM.substring(3));

		LocalDateTime ldtBegin = LocalDateTime.now(ourZoneId).withHour(hoursBegin).withMinute(minsBegin).withSecond(0)
				.withNano(0);

		int hoursEnd = Integer.parseInt(endAt24hrHHMM.substring(0, 2));
		int minsEnd = Integer.parseInt(endAt24hrHHMM.substring(3));

		LocalDateTime ldtEnd = LocalDateTime.now(ourZoneId).withHour(hoursEnd).withMinute(minsEnd).withSecond(0)
				.withNano(0);

		for (SolcastForecast forecast : solcastForecast.getForecasts()) {

			String periodEnd = forecast.getPeriodEnd();

			LocalDateTime ldt = LocalDateTime.parse(periodEnd, defaultDateTimeFormatter);

			if (ldt.isAfter(ldtBegin) || ldt.isEqual(ldtBegin)) {

				if (ldt.isBefore(ldtEnd)) {

					Float est = forecast.getPvEstimate();

					result += est;

				} else {

					break; // since list chronologically supplied ascending time we know no more matches
				}
			}
		}

		return result;
	}

	private void logSolarData(String timestamp, String data) {

		if (null != fileSolar) {

			try {

				FileWriter fileWriter = new FileWriter(fileSolar, true);

				BufferedWriter solarDataWriter = new BufferedWriter(fileWriter);

				solarDataWriter.append(timestamp);
				solarDataWriter.append(",");
				solarDataWriter.append(data);
				solarDataWriter.newLine();

				solarDataWriter.flush();
				solarDataWriter.close();

				fileWriter.close();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}
	}

	private static Map<String, String> getSolarDataFromFile(Set<Integer> weekNumbers) {

		return getSolarDataFromFile(null, weekNumbers, null, null);
	}

	private static Map<String, String> getSolarDataFromFile(String dateYYYY_MM_DD) {

		return getSolarDataFromFile(dateYYYY_MM_DD, null, null, null);
	}

	private static Map<String, String> getSolarDataFromFile(String dateYYYY_MM_DD, Float kWhrSolNow,
			String rangeStartTime) {

		return getSolarDataFromFile(dateYYYY_MM_DD, null, kWhrSolNow, rangeStartTime);
	}

	private static Map<String, String> getSolarDataFromFile(String thisDateOnly, Set<Integer> weekNumbers,
			Float kWhrSolNow, String rangeStartTime) {

		Map<String, String> result = new HashMap<String, String>();

		Integer currentPart = 0;

		String numberOfParts = "0";

		if (null != fileSolar) {

			try {

				FileReader fileReader = new FileReader(fileSolar);

				BufferedReader solarDataReader = new BufferedReader(fileReader);

				String line = null;

				String prevSol = null;

				Integer sunHasRisenPart = null; // typically 1 or 2

				Integer sunHasSetPart = null; // typically 3 or 4

				boolean isThisDate = false;

				boolean moreData = true;

				while (moreData) {

					line = solarDataReader.readLine();

					if (null == line) {

						line = null == kWhrSolNow || null == rangeStartTime ? null
								: thisDateOnly + "T" + rangeStartTime + ":00," + rangeStartTime + ",solar,"
										+ currentPart.toString() + "," + numberOfParts + ",00,"
										+ String.valueOf(kWhrSolNow);

						moreData = false;

						if (null == line) {

							break; // we're done
						}
					}

					String cols[] = line.split(",");

					String isoLocalDate = cols[0].substring(0, 10);

					isThisDate = isoLocalDate.equals(thisDateOnly);

					if (null != thisDateOnly && !isThisDate) {

						continue; // only interested in solar data logged on specified date
					}

					if (null != weekNumbers) { // select dates only in the supplied week numbers

						LocalDate ld = LocalDate.parse(isoLocalDate);

						Integer weekOfYear = Long.valueOf(ld.getLong(ChronoField.ALIGNED_WEEK_OF_YEAR)).intValue();

						if (!weekNumbers.contains(weekOfYear)) {

							continue; // only interested in solar data logged in the specified week numbers
						}
					}

					String key = isoLocalDate + "_";

					result.put(key + cols[1], line);

					String sol = cols[6];

					currentPart = Integer.parseInt(cols[3]);

					// midnight recording of data gives yesterday's totals
					// but not reliably - because sometimes values have already been reset to zero -

					if ("00:00".equals(cols[1])) {

						LocalDateTime timestampThen = LocalDateTime.parse(cols[0],
								DateTimeFormatter.ISO_LOCAL_DATE_TIME);

						String yesterday = timestampThen.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

						if ("0.0".equals(sol)) { // sol is reporting zero - probably values have been
													// reset

							// select the data from yesterday_part# as a closer approximation for the solar

							line = result.get(yesterday + "_" + numberOfParts);
						}

						result.put(yesterday, line);
					}

					if (null == weekNumbers) {

						// the following is all to do with detecting sunrise & sunset on the specified
						// day (typically today)
						// Assume (not unreasonably) that a sunset can only occur *after* a sunrise
						// and implicitly, not in the same part of the day.

						if (isThisDate && !"00:00".equals(cols[1])) {

							if (currentPart < 3) {
								// we are towards the beginning of the day

								// see if solar generation has started ( i.e., > "0.0" for cols[6])

								if (!"0.0".equals(sol)) {

									// have we detected the rise already ?

									if (null == sunHasRisenPart) {

										if (result.containsKey(key + "R")) {

											String rising = result.get(key + "R");

											String risingCols[] = rising.split(",");

											sunHasRisenPart = Integer.parseInt(risingCols[3]);

										} else {

											result.put(key + "R", line); // assume solar generation has started
																			// - sun has (R)isen

											sunHasRisenPart = Integer.parseInt(cols[3]);
										}
									}
								}
							}

							// what part of the day did the sun set (expecting > 2)

							if (currentPart > 2) {

								// we are towards the end of the day

								// (implies light going into dark)

								if (sol.equals(prevSol)) {

									// have we detected the sunset already ?

									if (null == sunHasSetPart) {

										if (result.containsKey(key + "S")) {

											String setting = result.get(key + "S");

											String settingCols[] = setting.split(",");

											sunHasSetPart = Integer.parseInt(settingCols[3]);

										} else {

											result.put(key + "S", line); // assume solar generation has ended
											// - sun has (S)et

											sunHasSetPart = Integer.parseInt(cols[3]);
										}
									}
								}
							}

							prevSol = sol; // used to detect if no further PV generation on this day
						}
					}
				}

				solarDataReader.close();

				fileReader.close();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		return result;

	}

	/*
	 * returns slot index for sunrise [0] and sunset [1] respectively or -1 if not
	 * yet happened
	 * 
	 */

	private static int[] hasSunRisenAndSet(String dateYYYY_MM_DD, Float kWhrSolNow, String rangeStartTime) {

		int[] sunRisenAndSet = new int[2];

		boolean alreadyAddedCurrentSlot = false;

		if (null != fileSolar) {

			String key = dateYYYY_MM_DD + "_S";

			Map<String, String> map = getSolarDataFromFile(dateYYYY_MM_DD);

			if (!map.containsKey(key)) { // it may be right now in the current slot

				// update map to include current slot

				map = getSolarDataFromFile(dateYYYY_MM_DD, kWhrSolNow, rangeStartTime);

				alreadyAddedCurrentSlot = true;
			}

			if (map.containsKey(key)) {

				String line = map.get(key);

				String split[] = line.split(",");

				for (int i = 0; i < slotStartTimes.length; i++) {

					if (slotStartTimes[i].equals(split[1])) {

						sunRisenAndSet[1] = i;
						break;
					}
				}

			} else {

				sunRisenAndSet[1] = -1;
			}

			key = dateYYYY_MM_DD + "_R";

			if (!map.containsKey(key)) { // it may be right now in the current slot

				if (!alreadyAddedCurrentSlot) {

					// update map to include current slot

					map = getSolarDataFromFile(dateYYYY_MM_DD, kWhrSolNow, rangeStartTime);

					alreadyAddedCurrentSlot = true;
				}
			}

			if (map.containsKey(key)) {

				String line = map.get(key);

				String split[] = line.split(",");

				for (int i = 0; i < slotStartTimes.length; i++) {

					if (slotStartTimes[i].equals(split[1])) {

						sunRisenAndSet[0] = i;
						break;
					}
				}

			} else {

				sunRisenAndSet[0] = -1;
			}
		}

		return sunRisenAndSet;
	}

	private static String[] delogSolarData(String dateYYYY_MM_DD, String suffix) {

		String result[] = null;

		if (null != fileSolar) {

			Map<String, String> map = getSolarDataFromFile(dateYYYY_MM_DD, null, null);

			String key = dateYYYY_MM_DD + "_" + suffix;

			if (map.containsKey(key)) {

				String line = map.get(key);

				result = line.split(",");
			}
		}

		return result;
	}

	private static int scaleSolarForcastRange0to29(Integer wHrSunshine, Float pvSolcast, int partOfDayIndex) {

		int result = 0;

		if (0 != "false".compareTo(maxSolar)) {

			final float maxSolarAssumed = Float.valueOf(maxSolar);

			final float wHr = Float.valueOf(String.valueOf(wHrSunshine.intValue()));

			int sunny = Math.round(29 * wHr / maxSolarAssumed);

			result = sunny > 29 ? 29 : sunny;
		}

		return result; // require 0 to 29 result
	}

	private static int scaleBatteryRange0to29(Integer batteryPercent) {

		int result = 0;

		if (null != batteryPercent) {

			final float pc = (float) batteryPercent;

			int value = Math.round(29 * pc / 100);

			result = value > 29 ? 29 : value;
		}

		return result; // require 0 to 29 result
	}

	private int[] testableOPC(int defaultPower, float[] prices, final int index) {

		final int numberInSchedule = prices.length;

		int[] powers = new int[numberInSchedule];

		int targetPower = 0;

		for (int s = 0; s < numberInSchedule; s++) {

			targetPower += defaultPower;

			powers[s] = defaultPower;
		}

		// S1 is cheapest - increase to maxrate and reduce most expensive

		float kludge = 0f;

		if (prices[0] < 0) {

			kludge = 1 - prices[0];
		}

		float[] ratios = new float[numberInSchedule];

		for (int s = 0; s < numberInSchedule; s++) {

			prices[s] += kludge;

			ratios[s] = prices[0] / prices[s]; // hence ratios[0] will always be 1.0
		}

		// rebase highest power on cheapest slot

		powers[0] = Integer.parseInt(maxRate);

		int sumPower = 0;

		int increment = 0;

		int diff = 0;

		do {

			increment += 250;

			sumPower = powers[0];

			for (int s = 1; s < numberInSchedule; s++) {

				Float value = ratios[s] * powers[s];

				powers[s] = value.intValue() + increment;

				if (powers[s] > powers[0]) {

					powers[s] = powers[0];
				}

				sumPower += powers[s];
			}

		} while (sumPower < targetPower);

		diff = sumPower - targetPower;

		powers[numberInSchedule - 1] -= diff;

		// debug logging

		int power = 0;
		float cost = 0f;

		for (int s = 0; s < numberInSchedule; s++) {

			prices[s] -= kludge;

			power += powers[s];

			cost += (powers[s] * prices[s]);

			logErrTime((index == s ? "*" : " ") + WatchSlotChargeHelperThread.SN(s) + "Power:"
					+ String.format("%5d", powers[s]) + " @ " + String.format("%5.2f", prices[s]) + "p");
		}

		float costOfPower = cost / power;

		logErrTime("Tot Power:" + String.format("%5d", power) + "   " + String.format("%5.2f", costOfPower) + "p");

		return powers;
	}

	private int optimisePowerCost(int defaultPower, float[] schedulePrices, int index) {

		int[] powers = testableOPC(defaultPower, schedulePrices, index);

		int result = powers[index];

		return result;
	}

	public synchronized String logErrTime(String text) {

		ZonedDateTime zdt = ZonedDateTime.now();

		String threadName = Thread.currentThread().getName();

		String result = zdt.format(defaultDateTimeFormatter).substring(0, 19);

		System.err.println(result + " " + threadName + ": " + text);

		return result;
	}

	private ResultMessage getForecastSolar() {

		ResultMessage result = null;

		if (!"false".equalsIgnoreCase(forecastSolar)) {

			try {

				URL url = new URL(forecastSolar);

				String json = getRequest(url, false);

				if (null == json || 0 == json.trim().length()) {

					result = new ResultMessage();// empty object

				} else {

					result = mapper.readValue(json, ResultMessage.class);
				}

			} catch (Exception e) {

				e.printStackTrace();

				result = null;
			}
		}

		return result;
	}

	private SolcastMessage getForecastSolcast() {

		SolcastMessage result = null;

		if (!"false".equalsIgnoreCase(forecastSolcast)) {

			try {

				URL url = new URL(forecastSolcast);

				String json = getRequest(url, true, properties.getProperty(KEY_API_SOLCAST));

				if (null == json || 0 == json.trim().length()) {

					result = new SolcastMessage(); // empty object n.b. not null ;-)

				} else {

					result = mapper.readValue(json, SolcastMessage.class);
				}

			} catch (Exception e) {

				e.printStackTrace();

				result = null;
			}
		}

		return result;
	}

	private static Float execReadSolar() {

		Float result = null;

		if (!"false".equalsIgnoreCase(solar)) {

			String[] cmdarray = solar.split(" ");

			String value = exec(cmdarray);

			if (null != value) {

				result = Float.valueOf(value.trim());
			}
		}

		return result;
	}

	public Integer execReadBatteryPercent() {

		Integer result = null;

		if (!"false".equalsIgnoreCase(percent)) {

			String[] cmdarray = percent.split(" ");

			String value = exec(cmdarray);

			if (null != value) {

				result = Integer.valueOf(value.trim());
			}
		}

		return result;
	}

	public Float execReadTemperature() {

		Float result = null;

		if (!"false".equalsIgnoreCase(temperature)) {

			String[] cmdarray = temperature.split(" ");

			String value = exec(cmdarray);

			if (null != value) {

				result = Float.valueOf(value.trim());
			}
		}

		return result;
	}

	public Float execReadCharge() {

		Float result = null;

		ChargeDischarge cd = null;

		cd = execReadChargeDischarge();

		if (null != cd && null != cd.getCharge()) {

			result = cd.getCharge();
		}

		return result;
	}

	public ChargeDischarge execReadChargeDischarge() {

		ChargeDischarge result = null;

		if (!"false".equalsIgnoreCase(chargeDischarge)) {

			String[] cmdarray = chargeDischarge.split(" ");

			String json = exec(cmdarray);

			if (null == json || 0 == json.trim().length()) {

				System.err.println("Error obtaining charge/discharge data. Check the apiKey!");

				result = new ChargeDischarge(); // empty object

			} else {

				try {
					result = mapper.readValue(json, ChargeDischarge.class);

				} catch (JsonProcessingException e) {

					e.printStackTrace();
				}
			}
		}

		return result;
	}

	private static ImportExport execReadGridImportExport() {

		ImportExport result = null;

		if (!"false".equalsIgnoreCase(grid)) {

			String[] cmdarray = grid.split(" ");

			String json = exec(cmdarray);

			if (null == json || 0 == json.trim().length()) {

				System.err.println("Error obtaining import/export data. Check the apiKey!");

				result = new ImportExport(); // empty object

			} else {

				try {
					result = mapper.readValue(json, ImportExport.class);

				} catch (JsonProcessingException e) {

					e.printStackTrace();
				}
			}
		}

		return result;
	}

	private static Float execReadConsumption() {

		Float result = null;

		if (!"false".equalsIgnoreCase(consumption)) {

			String[] cmdarray = consumption.split(" ");

			String value = exec(cmdarray);

			if (null != value) {

				result = Float.valueOf(value.trim());
			}
		}

		return result;
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

		String result = "";

		if (null != value) {

			int beginIndex = value.indexOf("\"value\" : \"") + 11;
			int endIndex = value.indexOf("\"", beginIndex);

			String test = value.substring(beginIndex, endIndex);

			if (5 == test.length()) {

				result = test;
			}
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

		String result = "00:00";

		if (null != value) {

			int beginIndex = value.indexOf("\"value\" : \"") + 11;
			int endIndex = value.indexOf("\"", beginIndex);

			String test = value.substring(beginIndex, endIndex);

			if (5 == test.length()) {

				result = test;
			}
		}

		return result;
	}

	private String[] startAndFinishTimeOfSlotCost(SlotCost slotCost, int minsDelayStart) {

		Instant instantStart = Instant.ofEpochSecond(slotCost.getEpochSecond());

		LocalDateTime ldtStart = LocalDateTime.ofInstant(instantStart, ourZoneId);

		LocalDateTime ldtFinish = ldtStart.plusMinutes(29);

		String[] result = new String[] { ldtStart.plusMinutes(minsDelayStart).format(formatter24HourClock),
				ldtFinish.format(formatter24HourClock) };

		return result;
	}

	static String execMacro(char macro, String template, String from, String to, int soc) {

		// assume extra contains a cmdarray to execute in a separate process
		// java -jar plugs.jar ./SwindonIcarus.properties inverter setting %1 %2 %3 %4

		// substitute macro for %1 and from, to and power for %2, %3 and %4 respectively

		String[] cmdarray = template.split(" ");

		for (int n = 0; n < cmdarray.length; n++) {

			if ("%1".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = String.valueOf(macro);

			} else if ("%2".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = from;

			} else if ("%3".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = to;

			} else if ("%4".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = String.valueOf(soc);
			}

			// System.out.println("param[" + n + "]=" + cmdarray[n]);
		}

		exec(cmdarray);

		return to;
	}

	private ArrayList<Long> upcomingImport(List<SlotCost> pricesPerSlot) {

		if (!ansi) {

			System.out.println("\nUpcoming best import price periods:");
		}

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

			if (!ansi) {

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

		}

		return bestStartTime;
	}

	private static String exec(String[] cmdarray) {

		String result = null;

		if (execute) {

			Process process;

			try {

				InputStream inputStream = null;
				InputStreamReader isr = null;
				InputStream errorStream = null;
				InputStreamReader esr = null;

				try {

					process = Runtime.getRuntime().exec(cmdarray);

					inputStream = process.getInputStream();
					isr = new InputStreamReader(inputStream);

					int n1;
					char[] c1 = new char[1024];
					StringBuffer sbOutput = new StringBuffer();

					while ((n1 = isr.read(c1)) > 0) {

						sbOutput.append(c1, 0, n1);
					}

					result = sbOutput.toString();

					errorStream = process.getErrorStream();
					esr = new InputStreamReader(errorStream);

					int n2;
					char[] c2 = new char[1024];

					StringBuffer sbError = new StringBuffer();

					while ((n2 = esr.read(c2)) > 0) {

						sbError.append(c2, 0, n2);
					}

					String err = sbError.toString();

					if (0 != err.length() && !err.startsWith("Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF8")) {

						if (err.contains("EXCEPTION:")) {

							instance.logErrTime("ERROR: " + err);
						}
					}

				} finally {

					if (null != isr) {

						isr.close();
					}

					if (null != inputStream) {

						inputStream.close();
					}

					if (null != esr) {

						esr.close();
					}

					if (null != errorStream) {

						errorStream.close();
					}
				}

			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		return result;
	}

	private Float getPriceFromTariff(ArrayList<Tariff> tariffs, int indexHint, long atEpochSecond) {

		Float result = null;

		Tariff tariff = null;

		for (int index = indexHint; index < tariffs.size(); index++) {

			tariff = tariffs.get(index);

			String validFrom = tariff.getValidFrom();

			OffsetDateTime odtFrom = OffsetDateTime.parse(validFrom, defaultDateTimeFormatter);

			long epochSecond = odtFrom.toEpochSecond();

			if (atEpochSecond < epochSecond) {

				continue; // look at next tariff time period
			}

			if (atEpochSecond == epochSecond) {

				// matched exactly on current tariff
				break;
			}

			// we know for sure atEpochSecond > epochSecond
			// but is it less than the end of the current period?

			String validTo = tariff.getValidTo();

			if (null == validTo) {

				// assume open ended tariff period
				break;
			}

			OffsetDateTime odtTo = OffsetDateTime.parse(validTo, defaultDateTimeFormatter);

			epochSecond = odtTo.toEpochSecond();

			if (atEpochSecond <= epochSecond) {

				break;
			}
		}

		if (null != tariff) {

			result = tariff.getValueExcVat();
		}

		return result;
	}

	private Map<String, ImportExportData> createPriceMap(ArrayList<Tariff> importTariffs,
			ArrayList<Tariff> exportTariffs) {

		// key is fixed width string YYYY-MM-DDTHH:MM

		Map<String, ImportExportData> priceMap = new HashMap<String, ImportExportData>();

//		String validTo = null;

		// deal with import tariffs

		for (int index = 0; index < importTariffs.size(); index++) {

			Tariff tariff = importTariffs.get(index);

			String validFrom = tariff.getValidFrom();

			String key = validFrom.substring(0, 16);

			OffsetDateTime odtFrom = OffsetDateTime.parse(validFrom, defaultDateTimeFormatter);

			//
			//
			//

			long epochActualFrom = odtFrom.toEpochSecond();

			float importPriceExcVat = getPriceFromTariff(importTariffs, index, epochActualFrom);

			float exportPriceExcVat = getPriceFromTariff(exportTariffs, 0, epochActualFrom);

			ImportExportData importExportPrices = null;

			if (priceMap.containsKey(key)) {

				importExportPrices = priceMap.get(key);

			} else {

				importExportPrices = new ImportExportData();
			}

			importExportPrices.setImportPrice(importPriceExcVat);

			importExportPrices.setExportPrice(exportPriceExcVat);

			priceMap.put(key, importExportPrices);

			//
			// TODO
			//

//			ConsumptionHistory consumptionLatest = new ConsumptionHistory();
//
//			consumptionLatest.setFrom(odtFrom);
//
//			String validTo = tariff.getValidTo();
//
//			OffsetDateTime odtTo = OffsetDateTime.parse(validTo, defaultDateTimeFormatter);
//
//			consumptionLatest.setTo(odtTo);
//
//			consumptionLatest.setPriceImportedOrExported(Float.valueOf(importPriceExcVat));
//
//			Float cost = null;
//
//			if (null != consumptionLatest.getConsumption()) {
//
//				cost = importPriceExcVat * consumptionLatest.getConsumption();
//			}
//
//			consumptionLatest.setCostImportedOrExported(cost);
//
//			consumptionLatest.setConsumption(Float.valueOf(0f));
//
//			consumptionLatest.setCostImportedOrExported(Float.valueOf(0f));
//
//			historyImport.put(epochActualFrom, consumptionLatest);
//
//			//
//			//
//			//
//
//			consumptionLatest.setPriceImportedOrExported(Float.valueOf(exportPriceExcVat));
//
//			historyExport.put(epochActualFrom, consumptionLatest);

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

		Map<String, String> map = getSolarDataFromFile(weekNumbers);

		// key is made up of date+"_"+part (where part is 1 ... 4)
		// part 1 will give the totals for the previous day as it is logged at midnight
		// field [9] of value will hold the export units
		// however, there is also a key without "_"+part giving the totals for that date

		StringBuffer sb = new StringBuffer();

		sb.append("\nRecent daily costs (in week ");

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
		float accumulateExportUnits = 0;

		float flatRateExport = Float.valueOf(15);

		float flatRateImport = Float.valueOf(
				properties.getProperty(KEY_FLEXIBLE_ELECTRICITY_UNIT, DEFAULT_FLEXIBLE_ELECTRICITY_UNIT_PROPERTY));

		SortedSet<String> setOfDays = new TreeSet<String>();

		setOfDays.addAll(elecMapDaily.keySet());

		float flatStandingCharge = Float.valueOf(properties.getProperty(KEY_FLEXIBLE_ELECTRICITY_STANDING,
				DEFAULT_FLEXIBLE_ELECTRICITY_STANDING_PROPERTY));

		float agileStandingCharge = Float.valueOf(
				properties.getProperty(KEY_AGILE_ELECTRICITY_STANDING, DEFAULT_AGILE_ELECTRICITY_STANDING_PROPERTY));

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

			float consumption = dayValues.getDailyImport();

			float agilePrice = dayValues.getDailyImportPrice();

			float flatImportPrice = consumption * flatRateImport;

			float agileCost = agilePrice + agileStandingCharge;

			float difference = (flatImportPrice + flatStandingCharge) - agileCost;

			float lowestPrice = dayValues.getLowestImportPrice();

			float dailyAverageUnitPrice = agilePrice / consumption;

			String values = map.get(key);

			float dailyExportUnits = 0;

			if (null != values) {

				String cols[] = values.split(",");

				dailyExportUnits = Float.valueOf(cols[10]);
			}

			// + " @ " + String.format("%.2f", dailyAverageUnitPrice)+ "p"

			System.out.println(dayValues.getDayOfWeek() + (lowestPrice < plunge ? " * " : "   ") + key + "  £"
					+ String.format("%5.2f", agileCost / 100) + " " + String.format("%7.3f", consumption) + " kWhr @ "
					+ String.format("%5.2f", dailyAverageUnitPrice) + "p" + " A: " + String.format("%8.4f", agilePrice)
					+ "p +" + agileStandingCharge + "p (X: " + String.format("%8.4f", flatImportPrice) + "p +"
					+ flatStandingCharge + "p) Save: £" + String.format("%5.2f", (difference / 100)) + " + Export:"
					+ dailyExportUnits);

			accumulateDifference += difference;

			accumulatePower += consumption;

			accumulateCost += agilePrice;

			accumulateExportUnits += dailyExportUnits;
		}

		Float unitCostAverage = accumulateCost / accumulatePower;

		renderSummaryB(agileStandingCharge, flatStandingCharge, flatRateImport, flatRateExport, countDays,
				unitCostAverage, accumulatePower, accumulateDifference, accumulateExportUnits);

		return unitCostAverage.intValue();
	}

	private void renderSummaryB(float agileStandingCharge, float flatStandingCharge, float flatRateImport,
			float flatRateExport, int countDays, float unitCostAverage, float accumulatePower,
			float accumulateDifference, float accumulateExportUnits) {

		float subTot0 = accumulateDifference / countDays;

		float subTot1 = subTot0 / 100;

		String averagePounds2DP = String.format("%.2f", subTot1);

		String averageCostPerUnit = String.format("%.2f", unitCostAverage);

		float recentAverage = accumulatePower / countDays;

		String averagePower = String.format("%.3f", recentAverage);

		Float preSolarLongTermAverage = Float
				.valueOf(properties.getProperty(KEY_BASELINE, DEFAULT_BASELINE_PROPERTY).trim());

		float solarPower = preSolarLongTermAverage - recentAverage;

		float subTot2 = flatRateImport * solarPower / 100;

		String solarSaving = String.format("%.2f", subTot2);

		String unitsExported = String.format("%.1f", accumulateExportUnits);

		float valueOfExportUnits = flatRateExport * accumulateExportUnits;

		String valueExported = String.format("%.2f", valueOfExportUnits / 100);

		float subTot3 = valueOfExportUnits / 100 / countDays;

		String exportSaving = String.format("%.2f", subTot3);

		float total = subTot1 + subTot2 + subTot3;

		String totalSaving = String.format("%.2f", total);

		float historic = preSolarLongTermAverage * flatRateImport / 100;

		String historicDailyCostMinusStandingCharge = String.format("%.2f", historic);

		float historicIncl = historic + flatStandingCharge / 100;

		float costDailyAverage = historicIncl - total;

		String actualCost = String.format("%.2f", costDailyAverage);

		String historicDailyCostInclStandingCharge = String.format("%.2f", historicIncl);

		float recentAgileExclStandingCharge = accumulatePower * unitCostAverage;

		float recentFlatExclStandingCharge = accumulatePower * flatRateImport;

		System.out.println("\n" + String.format("%.3f", accumulatePower) + " kWhr imported over " + countDays
				+ " days. Average (A)gile price " + averageCostPerUnit + "p and daily grid import " + averagePower
				+ " kWhr. FYI flat rate (X) " + flatRateImport + "p");

		System.out.println("Average daily Agile saving:\t£" + averagePounds2DP + " (" + countDays + " x " + subTot0
				+ "p = " + accumulateDifference + "p = [Σ(X)" + recentFlatExclStandingCharge + " + " + countDays + " x "
				+ flatStandingCharge + "]-[Σ(A)" + recentAgileExclStandingCharge + " + " + countDays + " x "
				+ agileStandingCharge + "])");

		System.out.println("Average solar/battery saving:\t£" + solarSaving + " (" + String.format("%.3f", solarPower)
				+ " kWhr less vr. historical consumption " + preSolarLongTermAverage + " kWhr @ (X) = £"
				+ historicDailyCostMinusStandingCharge + " + " + flatStandingCharge + "p = £"
				+ historicDailyCostInclStandingCharge + ")");

		System.out.println("Average daily export worth:\t£" + exportSaving + " (from " + unitsExported + " units @ "
				+ flatRateExport + "p  yielding £" + valueExported + " recently)");
		System.out.println("\t\t\t\t=====");
		System.out.println("Total daily saving:\t£" + historicDailyCostInclStandingCharge + " - £" + totalSaving
				+ "\t= £" + actualCost + " (cost per day on average)");
		System.out.println("\t\t\t\t=====");

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

			String[] period = startAndFinishTimeOfSlotCost(slotCost, 0);

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

			if (execute) {

				for (int s = 0; s < schedule.length; s++) {

					if (0 == to.compareTo(schedule[s])) {

						chargeSlot = WatchSlotChargeHelperThread.SN(s);
						break;
					}
				}
			}

			System.out.println(optionalExport + slotCost.getSimpleTimeStamp() + "  "
					+ (null == chargeSlot ? (cheapestImport ? "!" : " ") + (lessThanAverage ? "!" : " ") + " "
							: chargeSlot)
					+ " " + String.format("%5.2f", importValueIncVat) + "p  "
					+ (ansi && cheapestImport ? ANSI_COLOUR_LO : "") + asterisks
					+ (ansi && cheapestImport ? ANSI_RESET : "") + padding + prices + clockHHMM);
		}

	}

	/*
	 * This method ensures the friendly date/time returned has a 3 character month
	 * and thus us always the same width string in result
	 * 
	 */
	private String getSimpleDateTimestamp(ZonedDateTime zdt) {

		String elements[] = zdt.format(simpleTime).split(" ");

		String result = elements[0] + " " + elements[1].substring(0, 3) + " " + elements[2] + " " + elements[3] + " "
				+ elements[4] + (elements.length > 5 ? " " + elements[5] : "");

		return result;
	}

	private List<SlotCost> buildListSlotCosts(long someTimeAgo, SortedSet<String> keys) {

		List<SlotCost> pricesPerSlot = new ArrayList<SlotCost>();

		for (String key : keys) {

			OffsetDateTime zulu = OffsetDateTime.parse(key + ":00Z");

			long epochSecond = zulu.toEpochSecond();

			if (epochSecond >= someTimeAgo) {

				// this is recent: we are interested

				ImportExportData importExportData = importExportPriceMap.get(key);

				float importValueIncVat = importExportData.getImportPrice();

				Float exportValueIncVat = importExportData.getExportPrice(); // can be null if export=false

				SlotCost price = new SlotCost();

				// n.b the DateTimeFormatter based on "E MMM dd pph:mm a" will return a mixture
				// of 3 or 4 character month abbreviations, which is pretty nonintuitive

				// the simpleTimeStamp should be in local time (i.e, BST in British Summer) and
				// not Zulu

				ZonedDateTime zdt = zulu.atZoneSameInstant(ourZoneId);

				price.setSimpleTimeStamp(getSimpleDateTimestamp(zdt));

				price.setEpochSecond(epochSecond);

				price.setSlotStartTime24hr(zdt.format(formatter24HourClock));

				price.setSlotEndTime24hr(zdt.plusMinutes(29).format(formatter24HourClock));

				price.setImportPrice(importValueIncVat);

				price.setExportPrice(exportValueIncVat);

				pricesPerSlot.add(price);
			}
		}

		return pricesPerSlot;
	}

	private Map<String, DayValues> buildElecMapDaily(ArrayList<V1PeriodConsumption> periodImportResults,
			ArrayList<V1PeriodConsumption> periodExportResults) {

		Map<String, DayValues> elecMapDaily = new HashMap<String, DayValues>();

		// key is YYYY-MM-DD

		// imported results

		for (V1PeriodConsumption v1PeriodConsumption : periodImportResults) {

			Float consumption = v1PeriodConsumption.getConsumption();

			OffsetDateTime from = OffsetDateTime.parse(v1PeriodConsumption.getIntervalStart(),
					defaultDateTimeFormatter);

			String intervalStart = v1PeriodConsumption.getIntervalStart().substring(0, 16);

			String dayYYYY_MM_DD = intervalStart.substring(0, 10);

			if (elecMapDaily.containsKey(dayYYYY_MM_DD)) {

				// it's possible that this day has already been marked incomplete
				// if so then iterate rather than processing the consumption declared for the
				// time slot

				if (null == elecMapDaily.get(dayYYYY_MM_DD)) {

					continue;
				}
			}

//			LocalDateTime ldt = LocalDateTime.parse(intervalStart);

			Integer weekOfYear = Long.valueOf(from.getLong(ChronoField.ALIGNED_WEEK_OF_YEAR)).intValue();

			ImportExportData importExportData = importExportPriceMap.get(intervalStart);

			if (null == importExportData) {

				elecMapDaily.put(dayYYYY_MM_DD, null); // flag this day as incomplete - ignore all time slots on this
														// day

				continue;
			}

			Float halfHourImportPrice = importExportData.getImportPrice();

			if (null == halfHourImportPrice) {

				elecMapDaily.put(dayYYYY_MM_DD, null); // flag this day as incomplete - ignore all time slots on this
														// day

				continue;
			}

			Long epochKey = from.toEpochSecond();

			ConsumptionHistory latestConsumption = historyImport.get(epochKey);

			if (null == latestConsumption) {

				// System.err.println("error at " + v1PeriodConsumption.getIntervalStart());

			} else {

				latestConsumption.setConsumption(consumption);
				latestConsumption.setPriceImportedOrExported(halfHourImportPrice);
				latestConsumption.setCostImportedOrExported(Float.valueOf(consumption * halfHourImportPrice));

				historyImport.put(epochKey, latestConsumption);
			}

			Float halfHourImportCharge = consumption * halfHourImportPrice;

			DayValues dayValues = null;

			if (elecMapDaily.containsKey(dayYYYY_MM_DD)) {

				dayValues = elecMapDaily.get(dayYYYY_MM_DD);

				if (null == dayValues.getLowestImportPrice()
						|| halfHourImportPrice < dayValues.getLowestImportPrice()) {

					dayValues.setLowestImportPrice(halfHourImportPrice);
				}

				dayValues.setSlotCount(1 + dayValues.getSlotCount());

				dayValues.setDailyImport(consumption + dayValues.getDailyImport());

				if (null == dayValues.getDailyImportPrice()) {

					dayValues.setDailyImportPrice(halfHourImportCharge);

				} else {

					dayValues.setDailyImportPrice(halfHourImportCharge + dayValues.getDailyImportPrice());
				}

			} else {

				dayValues = new DayValues();

				dayValues.setDayOfWeek(from.format(DateTimeFormatter.ofPattern("E")));

				dayValues.setWeekOfYear(weekOfYear);

				dayValues.setSlotCount(1);

				dayValues.setDailyImport(consumption);

				dayValues.setDailyImportPrice(halfHourImportCharge);

				dayValues.setLowestImportPrice(dayValues.getDailyImportPrice());

				//

				dayValues.setDailyExport(0f);

				dayValues.setDailyExportPrice(null);

				dayValues.setHighestExportPrice(null);
			}

			elecMapDaily.put(dayYYYY_MM_DD, dayValues);
		}

		// exported results

		for (V1PeriodConsumption v1PeriodConsumption : periodExportResults) {

			Float consumption = v1PeriodConsumption.getConsumption();

			OffsetDateTime from = OffsetDateTime.parse(v1PeriodConsumption.getIntervalStart(),
					defaultDateTimeFormatter);

			String intervalStart = v1PeriodConsumption.getIntervalStart().substring(0, 16);

			String dayYYYY_MM_DD = intervalStart.substring(0, 10);

			if (elecMapDaily.containsKey(dayYYYY_MM_DD)) {

				// it's possible that this day has already been marked incomplete
				// if so then iterate rather than processing the consumption declared for the
				// time slot

				if (null == elecMapDaily.get(dayYYYY_MM_DD)) {

					continue;
				}
			}

//			LocalDateTime ldt = LocalDateTime.parse(intervalStart);

			Integer weekOfYear = Long.valueOf(from.getLong(ChronoField.ALIGNED_WEEK_OF_YEAR)).intValue();

			ImportExportData importExportData = importExportPriceMap.get(intervalStart);

			if (null == importExportData) {

//				elecMapDaily.put(key, null); // flag this day as incomplete - ignore all time slots on this day

				continue;
			}

			Float halfHourExportPrice = importExportData.getExportPrice();

			if (null == halfHourExportPrice) {

//				elecMapDaily.put(key, null); // flag this day as incomplete - ignore all time slots on this day

				continue;
			}

			Long epochKey = from.toEpochSecond();

			ConsumptionHistory latestConsumption = historyExport.get(epochKey);

			if (null == latestConsumption) {

				// System.err.println("error at " + v1PeriodConsumption.getIntervalStart());

			} else {

				latestConsumption.setConsumption(consumption);
				latestConsumption.setPriceImportedOrExported(halfHourExportPrice);
				latestConsumption.setCostImportedOrExported(Float.valueOf(consumption * halfHourExportPrice));

				historyExport.put(epochKey, latestConsumption);
			}

			Float halfHourExportCharge = consumption * halfHourExportPrice;

			DayValues dayValues = null;

			if (elecMapDaily.containsKey(dayYYYY_MM_DD)) {

				dayValues = elecMapDaily.get(dayYYYY_MM_DD);

				// NOTE: kludge because export prices fixed at 15p (use highest import price
				// instead)

				Float halfHourImportPrice = importExportData.getImportPrice();

				if (null == dayValues.getHighestExportPrice()
						|| importExportData.getImportPrice() > dayValues.getHighestExportPrice()) {

					dayValues.setHighestExportPrice(halfHourImportPrice);
				}

//				dayValues.setSlotCount(1 + dayValues.getSlotCount());

				dayValues.setDailyExport(consumption + dayValues.getDailyExport());

				if (null == dayValues.getDailyExportPrice()) {

					dayValues.setDailyExportPrice(halfHourExportCharge);

				} else {

					dayValues.setDailyExportPrice(halfHourExportCharge + dayValues.getDailyExportPrice());
				}

			} else {

				dayValues = new DayValues();

				dayValues.setDayOfWeek(from.format(DateTimeFormatter.ofPattern("E")));

				dayValues.setWeekOfYear(weekOfYear);

				dayValues.setSlotCount(1);

				dayValues.setDailyExport(consumption);

				dayValues.setDailyExportPrice(halfHourExportCharge);

				dayValues.setHighestExportPrice(dayValues.getDailyExportPrice());

				//
				dayValues.setDailyImport(0f);

				dayValues.setDailyImportPrice(null);

				dayValues.setLowestImportPrice(null);
			}

			elecMapDaily.put(dayYYYY_MM_DD, dayValues);
		}

		return elecMapDaily;
	}

	private ArrayList<V1PeriodConsumption> updateHistory(Map<Long, ConsumptionHistory> historyMap, String someDaysAgo,
			ArrayList<V1PeriodConsumption> periodResults, int howManyDaysHistory, int count) {

		// Add history data for any non-null consumptions

		for (int index = 0; index < periodResults.size(); index++) {

			V1PeriodConsumption periodResult = periodResults.get(index);

			String intervalStart = periodResult.getIntervalStart();

			OffsetDateTime odtStart = OffsetDateTime.parse(intervalStart);

			Long key = odtStart.toEpochSecond();

			ConsumptionHistory consumptionHistory;

			if (historyMap.containsKey(key)) {

				consumptionHistory = historyMap.get(key);

			} else {

				consumptionHistory = new ConsumptionHistory();

				consumptionHistory.setFrom(odtStart);

				String intervalEnd = periodResult.getIntervalEnd();

				OffsetDateTime odtEnd = OffsetDateTime.parse(intervalEnd);

				consumptionHistory.setTo(odtEnd);
			}

			Float consumption = periodResult.getConsumption();

			consumptionHistory.setConsumption(consumption);

			// update the history map

			historyMap.put(key, consumptionHistory);
		}

		return periodResults;
	}

	@SuppressWarnings("unused")
	private void dumpPowerList(String fileName, List<PowerDuration> offsetPowerList) throws IOException {

		File file = new File(fileName);

		if (file.createNewFile()) {

		} else {

			// file already exists
		}

		OffsetDateTime odt = OffsetDateTime.ofInstant(now, ourZoneId).withNano(0).withSecond(0);

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

}
