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
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

import uk.co.myzen.a_z.json.ChargeDischarge;
import uk.co.myzen.a_z.json.ImportExport;
import uk.co.myzen.a_z.json.Prices;
import uk.co.myzen.a_z.json.Tariff;
import uk.co.myzen.a_z.json.V1Charges;
import uk.co.myzen.a_z.json.V1ElectricityConsumption;
import uk.co.myzen.a_z.json.V1ElectricityTariff;
import uk.co.myzen.a_z.json.V1PeriodConsumption;
import uk.co.myzen.a_z.json.V1ProductSpecific;
import uk.co.myzen.a_z.json.V1Profile;

/**
 * @author howard
 *
 */

public class Octopussy implements IOctopus {

	static final Instant now = Instant.now(); // earliest opportunity to log the time

	private static ZonedDateTime ourTimeNow; // initialised once we know the ZoneId

	public final static String DEFAULT_REFERRAL_PROPERTY = "https://share.octopus.energy/ice-camel-111";

	public final static String DEFAULT_VERSION_PROPERTY = "${project.version}";

	static final String slotStartTimes[] = { "00:00", "00:30", "01:00", "01:30", "02:00", "02:30", "03:00", "03:30",
			"04:00", "04:30", "05:00", "05:30", "06:00", "06:30", "07:00", "07:30", "08:00", "08:30", "09:00", "09:30",
			"10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30",
			"16:00", "16:30", "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00", "21:30",
			"22:00", "22:30", "23:00", "23:30" };

	static boolean execute = false;

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

//	private final static String KEY_API_SOLCAST = "api.solcast";
	private final static String KEY_APIKEY = "apiKey";
	private final static String KEY_BASE_URL = "base.url";
	private final static String KEY_ELECTRICITY_MPAN_IMPORT = "electricity.mpan.import";
	private final static String KEY_ELECTRICITY_MPAN_EXPORT = "electricity.mpan.export";

	private final static String KEY_ELECTRICITY_SN = "electricity.sn";

	private final static String KEY_REGION = "region";

	private final static String KEY_FIXED_PRODUCT_CODE = "fixed.product.code";
	private final static String KEY_FIXED_TARIFF_CODE = "fixed.tariff.code";
	private final static String KEY_FIXED_TARIFF_URL = "fixed.tariff.url";
	private final static String KEY_FIXED_ELECTRICITY_STANDING = "fixed.electricity.standing";
	private final static String KEY_FIXED_ELECTRICITY_UNIT = "fixed.electricity.unit";

	private final static String KEY_IMPORT_PRODUCT_CODE = "import.product.code";
	private final static String KEY_IMPORT_TARIFF_CODE = "import.tariff.code";
	private final static String KEY_IMPORT_TARIFF_URL = "import.tariff.url";
	private final static String KEY_IMPORT_ELECTRICITY_STANDING = "import.electricity.standing";

	private final static String KEY_EXPORT_PRODUCT_CODE = "export.product.code";
	private final static String KEY_EXPORT_TARIFF_CODE = "export.tariff.code";
	private final static String KEY_EXPORT_TARIFF_URL = "export.tariff.url";
	private final static String KEY_EXPORT_ELECTRICITY_STANDING = "export.electricity.standing";

	private final static String KEY_ZONE_ID = "zone.id";

	private final static String KEY_HISTORY_IMPORT = "history.import";
	private final static String KEY_HISTORY_EXPORT = "history.export";

	private final static String KEY_EXPORT = "export";

	private final static String KEY_BASELINE = "baseline";
	private final static String KEY_DAYS = "days";
	private final static String KEY_PLUNGE = "plunge";
	private final static String KEY_TARGET = "target";
	private final static String KEY_WIDTH = "width";

	private final static String KEY_ANSI = "ansi";
	private final static String KEY_COLOUR = "colour";
	private final static String KEY_COLOR = "color";
	private final static String KEY_SUNSHINE = "sunshine";
	private final static String KEY_SCORE = "score";

	private final static String KEY_YEARLY = "yearly";
	private final static String KEY_MONTHLY = "monthly";
	private final static String KEY_WEEKLY = "weekly";
	private final static String KEY_DAILY = "daily";

	private final static String KEY_DAY_FROM = "day.from";
	private final static String KEY_DAY_TO = "day.to";
	private final static String KEY_SHOW_RECENT = "show.recent";
	private final static String KEY_SHOW_SAVINGS = "show.savings";

	private final static String KEY_SETTING = "setting";
	private final static String KEY_MACRO = "macro";
	private final static String KEY_SOLAR = "solar";
	private final static String KEY_PERCENT = "percent";
	private final static String KEY_GRID = "grid";
	private final static String KEY_CONSUMPTION = "consumption";
	private final static String KEY_TEMPERATURE = "temperature";
	private final static String KEY_BATTERY = "battery";

	private final static String KEY_SUN = "sun";
	private final static String KEY_FORECAST = "forecast";
	private final static String KEY_FILE_SOLAR = "file.solar";
	private final static String KEY_MAX_SOLAR = "max.solar";
	private final static String KEY_MAX_RATE = "max.rate";

	private final static String KEY_LIMIT = "limit";

	private final static String KEY_REFERRAL = "referral";
	private final static String KEY_VERSION = "version";

	private final static String[] defaultPropertyKeys = { KEY_APIKEY, "#", KEY_BASE_URL, "#",
			KEY_ELECTRICITY_MPAN_IMPORT, KEY_ELECTRICITY_MPAN_EXPORT, KEY_ELECTRICITY_SN, "#", KEY_FIXED_PRODUCT_CODE,
			KEY_IMPORT_PRODUCT_CODE, KEY_EXPORT_PRODUCT_CODE, "#", KEY_FIXED_ELECTRICITY_UNIT,
			KEY_FIXED_ELECTRICITY_STANDING, KEY_IMPORT_ELECTRICITY_STANDING, KEY_IMPORT_TARIFF_CODE,
			KEY_IMPORT_TARIFF_URL, KEY_REGION, KEY_ZONE_ID, KEY_HISTORY_IMPORT, KEY_HISTORY_EXPORT, "#",
			KEY_EXPORT_TARIFF_CODE, KEY_EXPORT_TARIFF_URL, KEY_EXPORT, "#", KEY_BASELINE, KEY_DAYS, KEY_PLUNGE,
			KEY_TARGET, KEY_WIDTH, KEY_ANSI, KEY_COLOUR, KEY_COLOR, KEY_SUNSHINE, KEY_SCORE, "#", KEY_YEARLY,
			KEY_MONTHLY, KEY_WEEKLY, KEY_DAILY, KEY_DAY_FROM, KEY_DAY_TO, KEY_SHOW_RECENT, KEY_SHOW_SAVINGS, KEY_LIMIT,
			"#", KEY_SETTING, KEY_MACRO, KEY_SOLAR, KEY_PERCENT, KEY_GRID, KEY_CONSUMPTION, KEY_TEMPERATURE,
			KEY_BATTERY, KEY_SUN, KEY_FORECAST, "#", KEY_FILE_SOLAR, KEY_MAX_SOLAR, KEY_MAX_RATE, "#", KEY_REFERRAL,
			"#", KEY_VERSION };

//	private final static String DEFAULT_API_SOLCAST_PROPERTY = "blahblahblah";
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
	private final static String DEFAULT_FIXED_ELECTRICITY_UNIT_PROPERTY = "30.295124";
	private final static String DEFAULT_FIXED_ELECTRICITY_STANDING_PROPERTY = "47.9535";
	private final static String DEFAULT_IMPORT_ELECTRICITY_STANDING_PROPERTY = "42.7665";
	private final static String DEFAULT_EXPORT_ELECTRICITY_STANDING_PROPERTY = "0.0";

	private final static String DEFAULT_REGION_PROPERTY = "H";

	private final static String DEFAULT_SETTING_PROPERTY = "false";
	private final static String DEFAULT_MACRO_PROPERTY = "false";
	private final static String DEFAULT_SOLAR_PROPERTY = "false";
	private final static String DEFAULT_PERCENT_PROPERTY = "false";
	private final static String DEFAULT_GRID_PROPERTY = "false";
	private final static String DEFAULT_CONSUMPTION_PROPERTY = "false";
	private final static String DEFAULT_TEMPERATURE_PROPERTY = "false";
	private final static String DEFAULT_BATTERY_PROPERTY = "false";

	private final static String DEFAULT_SUN_PROPERTY = "false";

	private final static String DEFAULT_FORECAST_PROPERTY = "false";

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
	private final static String DEFAULT_SUNSHINE_PROPERTY = "YELLOW";
	private final static String DEFAULT_SCORE_PROPERTY = "CYAN";

	private final static String DEFAULT_PLUNGE_PROPERTY = "3";
	private final static String DEFAULT_TARGET_PROPERTY = "30";
	private final static String DEFAULT_ZONE_ID_PROPERTY = "Europe/London";

	private final static String DEFAULT_DAY_FROM_PROPERTY = "";
	private final static String DEFAULT_DAY_TO_PROPERTY = "";
	private final static String DEFAULT_SHOW_RECENT_PROPERTY = "true";
	private final static String DEFAULT_SHOW_SAVINGS_PROPERTY = "false";

	private final static String DEFAULT_LIMIT_PROPERTY = "20";

	private final static DateTimeFormatter simpleTime = DateTimeFormatter.ofPattern("E MMM dd pph:mm a");

	protected final static DateTimeFormatter formatter24HourClock = DateTimeFormatter.ofPattern("HH:mm");

	private final static DateTimeFormatter formatter12HourClock = DateTimeFormatter.ofPattern("h:mm a");

	private final static DateTimeFormatter formatterDayHourMinute = DateTimeFormatter.ofPattern("E HH:mm");

	private final static DateTimeFormatter formatterDayHourMinuteSecond = DateTimeFormatter.ofPattern("E HH:mm:ss");

	private final static DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	private final static DateTimeFormatter formatterLocalDate = DateTimeFormatter.ISO_LOCAL_DATE;

	static ZoneId ourZoneId; // package visibility for benefit of JUnit test

	private static ObjectMapper mapper;

	private static float agileImportStandingCharge;
	private static float agileExportStandingCharge;

	private static int width;

	private static int plunge;

	private static int target;

	private static boolean ansi;

	private static boolean export;

	private static boolean showRecent;

	private static boolean showSavings;

	private static float flatRateImport;

	private static float flatRateExport;

	static String setting = DEFAULT_SETTING_PROPERTY; // overridden by check=value in properties

	static String macro = DEFAULT_MACRO_PROPERTY; // overridden by charge=value in properties

	private static String solar = DEFAULT_SOLAR_PROPERTY; // overridden by solar=value in properties

	static String percent = DEFAULT_PERCENT_PROPERTY; // overridden by battery=value in properties

	private static String grid = DEFAULT_GRID_PROPERTY; // overridden by grid=value in properties

	private static String consumption = DEFAULT_CONSUMPTION_PROPERTY; // overridden by consumption=value in properties

	private static String maxSolar = DEFAULT_MAX_SOLAR_PROPERTY;// overridden by max.solar=value in properties

	static String maxRate = DEFAULT_MAX_RATE_PROPERTY;// overridden by value in properties

	static String temperature = DEFAULT_TEMPERATURE_PROPERTY;// overridden by temperature=value in properties

	static String battery = DEFAULT_BATTERY_PROPERTY;// overridden by battery=value in properties

	static String sun = DEFAULT_SUN_PROPERTY; // overridden by sun=value in properties

	static String forecast = DEFAULT_FORECAST_PROPERTY; // overridden by forecast=value in properties

	static String limit = DEFAULT_LIMIT_PROPERTY; // overridden by limit=value in properties

	static String propertyFileName = DEFAULT_PROPERTY_FILENAME;

	private static File fileSolar = null;

	private static boolean usingExternalPropertyFile = false;

	private static int extended = 0; // overridden by args[]

	private static Properties properties;

	private static Octopussy instance = null;

	private static String ANSI_COLOUR_LO; // typically GREEN
	private static String ANSI_COLOR_HI; // typically RED
	private static String ANSI_SUNSHINE; // typically YELLOW
	private static String ANSI_SCORE; // typically CYAN

	private static long epochFrom = 0;

	private static Map<Long, ConsumptionHistory> historyImport = null;
	private static Map<Long, ConsumptionHistory> historyExport = null;

	private static Map<String, ImportExportData> importExportPriceMap = null;

	private static SortedSet<String> ascendingKeysForPriceMap = null;

	private static int lowerSOCpc = -1;

	private int dayPartPowerDefault = 6000;

	private List<String> parts = null;

	private Set<String> dfs = null;

//	private String dayPartStartsAt24hr = null;

	private String dayPartEndsAt24hr = null;

	static String[] dischargeSchedule = null;

	static String[] chargeSchedule = null;

	private final List<String> ranges = new ArrayList<String>();

	private WatchSlotChargeHelperThread chargeMonitorThread = null;

	private WatchSlotDischargeHelperThread dischargeMonitorThread = null;

	boolean slotIsCancelled = false;

//	private static float pCummulative = 0.0f; // updated in logSolarData();

	private static String[] sunEvents;

	private static String bannerMessage = "";

	private static int countDays = -1;

	public static synchronized Octopussy getInstance() {

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

	private static void bootStrap(boolean verify) throws MalformedURLException, IOException {

		String region = "?";

		String mpan = properties.getProperty(KEY_ELECTRICITY_MPAN_IMPORT);

//		System.out.println(KEY_API_SOLCAST + "=" + properties.getProperty(KEY_API_SOLCAST));
		System.out.println(KEY_APIKEY + "=" + properties.getProperty(KEY_APIKEY));
		System.out.println("#");
		System.out.println(KEY_BASE_URL + "=" + properties.getProperty(KEY_BASE_URL));
		System.out.println("#");
		System.out.println(KEY_ELECTRICITY_MPAN_IMPORT + "=" + properties.getProperty(KEY_ELECTRICITY_MPAN_IMPORT));
		System.out.println(KEY_ELECTRICITY_MPAN_EXPORT + "=" + properties.getProperty(KEY_ELECTRICITY_MPAN_EXPORT));
		System.out.println(KEY_ELECTRICITY_SN + "=" + properties.getProperty(KEY_ELECTRICITY_SN));
		System.out.println("#");

		boolean supportExport = Boolean.parseBoolean(properties.getProperty(KEY_EXPORT, "false"));

		System.out.println(KEY_EXPORT + "=" + supportExport);
		System.out.println("#");

		if (verify) {

			V1Profile result = instance.getElectricityMeterPointRegion(mpan);

			region = result.getGsp().substring(1);

			System.out.println(KEY_REGION + "=" + region);
			System.out.println("#");

//			System.out.println("Region: " + result.getGsp() + "\tProfile: " + result.getProfileClass());

			// "OE-FIX-16M-25-02-12",

			// sequence must be fixed / import / export

			String[] products = { properties.getProperty(KEY_FIXED_PRODUCT_CODE),
					properties.getProperty(KEY_IMPORT_PRODUCT_CODE),
					supportExport ? properties.getProperty(KEY_EXPORT_PRODUCT_CODE) : null };

			String fullName = null;

//			String availableFrom = null;
//			String availableTo = null;

			for (int index = 0; index < products.length; index++) {

				String product = products[index];

				if (null == product) {

					continue;
				}

				V1ProductSpecific specific = null;

				specific = getV1ProductSpecific(product);

//				availableFrom = specific.getAvailableFrom();
//				availableTo = specific.getAvailableTo();

				String tariff = "E-1R-" + product + "-" + region;

				fullName = specific.getFullName();

//			System.out.println("Product: " + product + "\tin region " + properties.getProperty(KEY_REGION)
//					+ "\t(" + tariff + ") " + fullName);
//
//			System.out.println("\t" + specific.getDescription());
//			System.out.println(
//					"\tAvailable from: " + availableFrom + "\tto: " + availableTo);

				Integer pageSize = Integer.valueOf(100);
				String periodFrom = "2010-01-01";
				String periodTo = "";

//			fixed.product.code=OE-LOYAL-FIX-16M-25-02-12
//					fixed.tariff.code=E-1R-$fixed.product.code$-$region$
//					fixed.tariff.url=$base.url$/v1/products/$fixed.product.code$/electricity-tariffs/$fixed.tariff.code$

				V1Charges charges = instance.getV1ElectricityStandingCharges(product, tariff, pageSize, periodFrom,
						periodTo);

				List<Prices> standingCharges = charges.getPriceResults();

				Float standing = null;

				for (Prices prices : standingCharges) {

//					String from = prices.getValidFrom();
					String to = prices.getValidTo();

					if (null == to) {

						to = "\t\t";
					}

//					Float fExcVAT = prices.getValueExcVAT();
					Float fIncVAT = prices.getValueIncVAT();

//					System.out.println(
//							"\t\tStanding Charge: " + from + "\t" + to + "\t" + fExcVAT + "\t" + fIncVAT);

					if (null == standing) {

						standing = fIncVAT;
					}
				}

				V1Charges charges2 = instance.getV1ElectricityStandardUnitRates(product, tariff, pageSize, periodFrom,
						periodTo);

				List<Prices> standardUnitRates = charges2.getPriceResults();

				Float unit = null;

				for (Prices prices : standardUnitRates) {

//					String from = prices.getValidFrom();
					String to = prices.getValidTo();

					if (null == to) {

						to = "\t\t";
					}

//					Float fExcVAT = prices.getValueExcVAT();
					Float fIncVAT = prices.getValueIncVAT();

//					System.out.println(
//							"\t\tUnit Rate:       " + from + "\t" + to + "\t" + fExcVAT + "\t" + fIncVAT);

					if (null == unit) {

						unit = fIncVAT;
					}
				}

				switch (index) {

				case 0:

					System.out.println("# fixed just used for price comparison (" + fullName + ") in Region " + region);
					System.out.println(KEY_FIXED_PRODUCT_CODE + "=" + product);
					System.out.println(KEY_FIXED_ELECTRICITY_UNIT + "=" + unit);
					System.out.println(KEY_FIXED_ELECTRICITY_STANDING + "=" + standing);
					System.out.println(
							KEY_FIXED_TARIFF_CODE + "=E-1R-$" + KEY_FIXED_PRODUCT_CODE + "$-$" + KEY_REGION + "$");
					System.out.println(KEY_FIXED_TARIFF_URL + "=$" + KEY_BASE_URL + "$/v1/products/$"
							+ KEY_FIXED_PRODUCT_CODE + "$/electricity-tariffs/$" + KEY_FIXED_TARIFF_CODE + "$");
					break;

				case 1:

					System.out.println("# " + fullName);
					System.out.println(KEY_IMPORT_PRODUCT_CODE + "=" + product);
					System.out.println(KEY_IMPORT_ELECTRICITY_STANDING + "=" + standing);
					System.out.println(
							KEY_IMPORT_TARIFF_CODE + "=E-1R-$" + KEY_IMPORT_PRODUCT_CODE + "$-$" + KEY_REGION + "$");
					System.out.println(KEY_IMPORT_TARIFF_URL + "=$" + KEY_BASE_URL + "$/v1/products/$"
							+ KEY_IMPORT_PRODUCT_CODE + "$/electricity-tariffs/$" + KEY_IMPORT_TARIFF_CODE + "$");
					break;

				case 2:

					System.out.println("# " + fullName);
					System.out.println(KEY_EXPORT_PRODUCT_CODE + "=" + product);
					System.out.println(KEY_EXPORT_ELECTRICITY_STANDING + "=" + standing);
					System.out.println(
							KEY_EXPORT_TARIFF_CODE + "=E-1R-$" + KEY_EXPORT_PRODUCT_CODE + "$-$" + KEY_REGION + "$");
					System.out.println(KEY_EXPORT_TARIFF_URL + "=$" + KEY_BASE_URL + "$/v1/products/$"
							+ KEY_EXPORT_PRODUCT_CODE + "$/electricity-tariffs/$" + KEY_EXPORT_TARIFF_CODE + "$");
					break;

				}

				System.out.println("#");
			}
		}

		// HP

		String[] ignoreKeys = { KEY_APIKEY, KEY_BASE_URL, KEY_ELECTRICITY_MPAN_IMPORT, KEY_ELECTRICITY_MPAN_EXPORT,
				KEY_ELECTRICITY_SN, KEY_BASE_URL, KEY_REGION, verify ? KEY_FIXED_PRODUCT_CODE : null,
				KEY_FIXED_ELECTRICITY_STANDING, KEY_FIXED_TARIFF_CODE, KEY_FIXED_TARIFF_URL, KEY_FIXED_ELECTRICITY_UNIT,
				verify ? KEY_IMPORT_PRODUCT_CODE : null, KEY_IMPORT_ELECTRICITY_STANDING, KEY_IMPORT_TARIFF_CODE,
				KEY_IMPORT_TARIFF_URL, verify ? KEY_EXPORT_PRODUCT_CODE : null, KEY_EXPORT_ELECTRICITY_STANDING,
				KEY_EXPORT_TARIFF_CODE, KEY_EXPORT_TARIFF_URL, KEY_EXPORT, KEY_VERSION };

		Set<String> ignoreSet = new HashSet<String>(ignoreKeys.length);

		for (String key : ignoreKeys) {

			ignoreSet.add(key);
		}

		for (String propertyKey : defaultPropertyKeys) {

			if (ignoreSet.contains(propertyKey)) {

				continue;
			}

			if (KEY_REFERRAL.equals(propertyKey)) {

				System.out.println("#dfs1=17:30");
				System.out.println("#dfs2=18:00");
				System.out.println("#");
				System.out.println("part1=00:00");
				System.out.println("part2=08:00R");
				System.out.println("part3=12:00N");
				System.out.println("part4=19:00S");
				System.out.println("#");
				System.out.println("slots1=4:Night:2");
				System.out.println("slots2=3:Day");
				System.out.println("slots3=2:d");
				System.out.println("slots4=1");
				System.out.println("#");
				System.out.println("power1=5000:100:15");
				System.out.println("power2=2500:50:25");
				System.out.println("power3=2500:40:20");
				System.out.println("power4=3000:30:15");
				System.out.println("#");
				System.out.println(propertyKey + "=" + properties.getProperty(propertyKey, DEFAULT_REFERRAL_PROPERTY));

			} else if (KEY_ANSI.equals(propertyKey)) {

				System.out.println("#");
				System.out.println(
						"# in Windows console to show ANSI update Registry set REG_DWORD VirtualTerminalLevel=1 for Computer\\HKEY_CURRENT_USER\\Console");
				System.out.println("#");
				System.out.println("ansi=true");

			} else if (KEY_DAY_FROM.equals(propertyKey)) {

				System.out.println("#day.from=" + properties.getProperty(propertyKey, DEFAULT_DAY_FROM_PROPERTY));

			} else if (KEY_DAY_TO.equals(propertyKey)) {

				System.out.println("#day.to=" + properties.getProperty(propertyKey, DEFAULT_DAY_TO_PROPERTY));

			} else {

				System.out.println(
						propertyKey + ("#".equals(propertyKey) ? "" : "=" + properties.getProperty(propertyKey, "?")));
			}
		}
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

		String bootstrap = null; // the default

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

						bootstrap = args[2].trim();
					}
				}
			}

			usingExternalPropertyFile = instance.dealWithProperties(propertyFileName);

			//
			//
			//

			if (null != bootstrap || !usingExternalPropertyFile) {

				bootStrap(usingExternalPropertyFile);

				System.exit(-1);
			}

			// ASSUMPTION: closest instant to start of hour
			// (typically controlled by crontab schedule)

			ourTimeNow = now.atZone(ourZoneId);

			String timestamp = ourTimeNow.toString().substring(0, 19);

			long epochNow = ourTimeNow.toEpochSecond();

			String today = timestamp.substring(0, 10);

			//
			//
			//

			int[] sunData = hasSunRisenAndSet(today); // an array of 3 slot indexes for sunrise/noon/sunset

			System.out.println(bannerMessage);

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

				flatRateExport = exportTariffs.get(0).getValueExcVat();
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

				// we don't yet have all the results - pause and then get the next page

				Thread.sleep(5000); // this is just so we don't bombard the API provider

				page++;

				v1ElectricityExported = instance.getV1ElectricityExport(page, 48 * howManyDaysHistory,
						beginRecentPeriod, null);

				ArrayList<V1PeriodConsumption> pagePeriodResults = v1ElectricityExported.getPeriodResults();

				periodExportResults.addAll(pagePeriodResults);
			}

			periodExportResults = instance.updateHistory(false, historyExport, beginRecentPeriod, periodExportResults,
					howManyDaysHistory);
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

			periodImportResults = instance.updateHistory(true, historyImport, beginRecentPeriod, periodImportResults,
					howManyDaysHistory);

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

				SortedMap<String, PeriodicValues> yearlyImport = accumulateCostsByField(historyImport, ChronoField.YEAR,
						upToEpochSecond);

				System.out.println("Historical yearly results - import:");

				float costImportTotal = displayPeriodSummary("Year", yearlyImport, null, null);
				//
				SortedMap<String, PeriodicValues> yearlyExport = accumulateCostsByField(historyExport, ChronoField.YEAR,
						upToEpochSecond);

				System.out.println("Historical yearly results - export:");

				float costExportTotal = displayPeriodSummary("Year", yearlyExport, null, null);

				System.out.println("Running total electricity cost: " + (ansi ? ANSI_SUNSHINE : "") + " £"
						+ String.format("%6.2f", (costImportTotal - costExportTotal) / 100) + (ansi ? ANSI_RESET : "")
						+ "\n");
			}

			//
			//
			//

			if (Boolean.TRUE.equals(Boolean.valueOf(properties.getProperty(KEY_MONTHLY, DEFAULT_MONTHLY_PROPERTY)))) {

				SortedMap<String, PeriodicValues> monthlyImport = accumulateCostsByField(historyImport,
						ChronoField.MONTH_OF_YEAR, upToEpochSecond);

				System.out.println("Historical monthly results:");

				displayPeriodSummary("Month", monthlyImport, null, null);
			}

			//
			//
			//

			if (Boolean.TRUE.equals(Boolean.valueOf(properties.getProperty(KEY_WEEKLY, DEFAULT_WEEKLY_PROPERTY)))) {

				SortedMap<String, PeriodicValues> weeklyImport = accumulateCostsByField(historyImport,
						ChronoField.ALIGNED_WEEK_OF_YEAR, upToEpochSecond);

				System.out.println("Historical weekly results:");

				displayPeriodSummary("Week", weeklyImport, null, null);
			}
			//
			//
			//

			if (Boolean.TRUE.equals(Boolean.valueOf(properties.getProperty(KEY_DAILY, DEFAULT_DAILY_PROPERTY)))) {

				// get epochSecond for start of next day of range

				SortedMap<String, PeriodicValues> dailyImport = accumulateCostsByField(historyImport,
						ChronoField.EPOCH_DAY, requiredEpochSecond < 0 ? upToEpochSecond : requiredEpochSecond);

				System.out.println("Historical daily results: " + ("".equals(filterFrom) ? "" : " from " + filterFrom)
						+ ("".equals(filterTo) ? "" : " up to " + filterTo));

				displayPeriodSummary("Day", dailyImport, fromEpochDayIncl, toEpochDayIncl);
			}

			//
			//
			//

			int averageUnitCost = instance.dailyResults(today, elecMapDaily); // updates countDays

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

			chargeSchedule = instance.scheduleBatteryCharging(pricesPerSlotSinceMidnight, currentSlotIndex, kWhrSolar,
					gridImportExport, kWhrConsumption, percentBattery, chargeAndDischarge, timestamp, averageUnitCost,
					sunData, countDays);

			dischargeSchedule = instance.scheduleBatteryDischarging(pricesPerSlotSinceMidnight, currentSlotIndex);

			//
			//
			//

			ArrayList<Long> bestImportTime = instance.upcomingImport(pricesPerSlot);

			//
			//
			//

			instance.showAnalysis(pricesPerSlot, averageUnitCost, bestImportTime, bestExportTime);

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

			if (null != instance.chargeMonitorThread) {

				if (instance.chargeMonitorThread.isAlive()) {

					long futureSlotStart = pricesPerSlot.get(1).getEpochSecond();

					long millis = 1000 * (futureSlotStart - Instant.now().getEpochSecond() - 10); // knock of 10 seconds

					if (millis <= 0) { // assume slow terminating thread

						instance.logErrTime("WARNING: missed opportunity to interrupt charge thread");

					} else {

						instance.chargeMonitorThread.join(millis); // expect thread to die within millis

						if (instance.chargeMonitorThread.isAlive()) {

							instance.logErrTime("Forcing monitoring interrupt 10s before next slot starts");

							instance.chargeMonitorThread.interrupt();

						} else {

							instance.logErrTime("Confirmation charge thread no longer alive");
						}
					}
				}

				// restore charging rate to maximum (this overrides power[p]) due to day
				// time when we want to use all the solar energy available to fill battery

				instance.logErrTime("Resetting charging power to " + maxRate + " watts");

				instance.batteryChargePower(Integer.valueOf(maxRate));
			}

			//
			// Before terminating main, wait for the optional WatchSlotDischargeHelperThread
			// Thread is launched to monitor battery state while discharging is occurring
			//

			if (null != instance.dischargeMonitorThread) {

				if (instance.dischargeMonitorThread.isAlive()) {

					long futureSlotStart = pricesPerSlot.get(1).getEpochSecond();

					long millis = 1000 * (futureSlotStart - Instant.now().getEpochSecond() - 10); // knock of 10 seconds

					if (millis <= 0) { // assume slow terminating thread

						instance.logErrTime("WARNING: missed opportunity to interrupt discharge thread");

					} else {

						instance.dischargeMonitorThread.join(millis); // expect thread to die within millis

						if (instance.dischargeMonitorThread.isAlive()) {

							instance.logErrTime("Forcing monitoring interrupt 10s before next slot starts");

							instance.dischargeMonitorThread.interrupt();

						} else {

							instance.logErrTime("Confirmation discharge thread no longer alive");
						}
					}
				}

				instance.enableDcDischarge(false);
				instance.acChargeEnable(true);

				// restore discharging rate to maximum (this overrides power[p]) due to day
				// time when we want to use all the solar energy available to fill battery

				instance.logErrTime("Resetting discharging power to " + maxRate + " watts");

				instance.batteryDischargePower(Integer.valueOf(maxRate));
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

		if (!"false".equalsIgnoreCase(setting)) {

			int tally = 0;

			for (int s = 0; s < howMany; s++) {

				// what is the current 'to' time in the inverter for Slot 1/2/3/4/5?

				String hhmm = execRead(setting, readEndTimesParameter[s]);

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

	private static float displayPeriodSummary(String id, SortedMap<String, PeriodicValues> periodic, Integer fromIncl,
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

			float averageDailyCost = tallyCost / equivalentDays;

			System.out.println("Totals:\t\t" + String.format("%8.3f", tallyEnergy) + " kWhr\t"
					+ (ansi ? ANSI_SCORE : "") + "Average Daily Cost £"
					+ String.format("%5.2f", (averageDailyCost / 100)) + (ansi ? ANSI_RESET : "") + "   "
					+ String.format("%7.2f", equivalentDays) + " days   £" + String.format("%7.2f", (tallyCost / 100))
					+ "\t     Average: " + String.format("%6.3f", averageDailyEnergy) + " kWhr @ "
					+ String.format("%5.2f", tallyCost / tallyEnergy) + "p\n");
		}

		return tallyCost;
	}

	private static SortedMap<String, PeriodicValues> accumulateCostsByField(Map<Long, ConsumptionHistory> history,
			ChronoField field, Long upToEpochSecond) {

		SortedMap<String, PeriodicValues> result = new TreeMap<String, PeriodicValues>();

		for (Long key : history.keySet()) {

			if (null != upToEpochSecond) {

				if (key >= upToEpochSecond) {

					break;
				}
			}

			ConsumptionHistory data = history.get(key);

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

		setting = properties.getProperty(KEY_SETTING, DEFAULT_SETTING_PROPERTY).trim();

		macro = properties.getProperty(KEY_MACRO, DEFAULT_MACRO_PROPERTY).trim();

		solar = properties.getProperty(KEY_SOLAR, DEFAULT_SOLAR_PROPERTY).trim();

		percent = properties.getProperty(KEY_PERCENT, DEFAULT_PERCENT_PROPERTY).trim();

		grid = properties.getProperty(KEY_GRID, DEFAULT_GRID_PROPERTY).trim();

		consumption = properties.getProperty(KEY_CONSUMPTION, DEFAULT_CONSUMPTION_PROPERTY).trim();

		forecast = properties.getProperty(KEY_FORECAST, DEFAULT_FORECAST_PROPERTY).trim();

		limit = properties.getProperty(KEY_LIMIT, DEFAULT_LIMIT_PROPERTY).trim();

		sun = properties.getProperty(KEY_SUN, DEFAULT_SUN_PROPERTY).trim();

		maxSolar = properties.getProperty(KEY_MAX_SOLAR, DEFAULT_MAX_SOLAR_PROPERTY).trim();

		maxRate = properties.getProperty(KEY_MAX_RATE, DEFAULT_MAX_RATE_PROPERTY).trim();

		{
			String logNameForSolarData = properties.getProperty(KEY_FILE_SOLAR, DEFAULT_FILE_SOLAR_PROPERTY).trim();

			if (0 != "false".compareTo(logNameForSolarData)) {

				fileSolar = new File(logNameForSolarData);
			}
		}

		temperature = properties.getProperty(KEY_TEMPERATURE, DEFAULT_TEMPERATURE_PROPERTY).trim();

		battery = properties.getProperty(KEY_BATTERY, DEFAULT_BATTERY_PROPERTY).trim();

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

	// "https://api.octopus.energy/v1/electricity-meter-points/2000012611663/"

	private V1Profile getElectricityMeterPointRegion(String mprn) throws MalformedURLException, IOException {

		V1Profile result = null;

		String baseUrl = properties.getProperty(KEY_BASE_URL, DEFAULT_BASE_URL_PROPERTY).trim();

		String json = instance.getRequest(new URL(baseUrl + "/v1/electricity-meter-points/" + mprn));

		result = mapper.readValue(json, V1Profile.class);

		return result;
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

	private static V1ProductSpecific getV1ProductSpecific(String code) throws MalformedURLException, IOException {

		String spec = properties.getProperty(KEY_BASE_URL, DEFAULT_BASE_URL_PROPERTY).trim() + "/v1/products/" + code
				+ "/";

		String json = getRequest(new URL(spec), false, null);

		V1ProductSpecific result = mapper.readValue(json, V1ProductSpecific.class);

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

//	private static V1GridSupplyPoints getV1GridSupplyPoints(String postcode) throws MalformedURLException, IOException {
//
//		String spec = properties.getProperty(KEY_BASE_URL, DEFAULT_BASE_URL_PROPERTY).trim()
//				+ "/v1/industry/grid-supply-points/" + "?postcode=" + postcode;
//
//		String json = getRequest(new URL(spec), false);
//
//		V1GridSupplyPoints result = mapper.readValue(json, V1GridSupplyPoints.class);
//
//		return result;
//	}

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

					if (KEY_ANSI.equals(propertyKey)) {

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

	private Boolean dealWithProperties(String propertyFileName) {

		Boolean result = null;

		String keyValue = null;

//		String keyApiSolcast = null;

		try {

			// Check for existence of octopussy.properties in the current directory
			// if it exists, use it in preference to the built-in resource compiled into the
			// jar

			File externalProperties = new File(propertyFileName);

			// usingExternalPropertyFile
			result = Boolean.valueOf(loadProperties(externalProperties));

			keyValue = properties.getProperty(KEY_APIKEY, DEFAULT_API_OCTOPUS_PROPERTY).trim();

			if (null == keyValue) {

				throw new Exception(KEY_APIKEY);
			}

//			keyApiSolcast = properties.getProperty(KEY_API_SOLCAST, DEFAULT_API_SOLCAST_PROPERTY).trim();

//			if (null == keyApiSolcast) {
//
//				throw new Exception(KEY_API_SOLCAST);
//			}

			properties.setProperty("basic", "Basic " + Base64.getEncoder().encodeToString(keyValue.getBytes()));

			agileImportStandingCharge = Float.valueOf(properties.getProperty(KEY_IMPORT_ELECTRICITY_STANDING,
					DEFAULT_IMPORT_ELECTRICITY_STANDING_PROPERTY));

			agileExportStandingCharge = Float.valueOf(properties.getProperty(KEY_EXPORT_ELECTRICITY_STANDING,
					DEFAULT_EXPORT_ELECTRICITY_STANDING_PROPERTY));

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

			ANSI_SUNSHINE = colourMapForeground
					.get(properties.getProperty(KEY_SUNSHINE, DEFAULT_SUNSHINE_PROPERTY).trim());

			ANSI_SCORE = colourMapForeground.get(properties.getProperty(KEY_SCORE, DEFAULT_SCORE_PROPERTY).trim());

			showRecent = Boolean.valueOf(properties.getProperty(KEY_SHOW_RECENT, DEFAULT_SHOW_RECENT_PROPERTY).trim());

			showSavings = Boolean
					.valueOf(properties.getProperty(KEY_SHOW_SAVINGS, DEFAULT_SHOW_SAVINGS_PROPERTY).trim());

			flatRateImport = Float.valueOf(
					properties.getProperty(KEY_FIXED_ELECTRICITY_UNIT, DEFAULT_FIXED_ELECTRICITY_UNIT_PROPERTY));

			execute = 0 == DEFAULT_REFERRAL_PROPERTY.compareTo(properties.getProperty(KEY_REFERRAL, "false").trim());

		} catch (Exception e) {

			e.printStackTrace();

			System.exit(-1);
		}

		return result;
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

//				System.out.println(WatchSlotChargeHelperThread.SN(i) + ": "
//						+ pricesPerSlotSinceMidnight.get(nextIndex).getSimpleTimeStamp() + "\t"
//						+ pricesPerSlotSinceMidnight.get(nextIndex).getImportPrice() + "p");
			}
		}

		return result;
	}

	static String convertHHmmMinus1Minute(String hhmm) {

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

	private String[] scheduleBatteryDischarging(List<SlotCost> pricesPerSlotSinceMidnight, int currentSlotIndex) {

		String[] result = null;

		// assume scheduleBatteryCharging has been called previously and
		// chargeSchedule[] initialised for this day part
		// dayPartEndsAt24hr defines the last minute of the day part

		// firstly is discharging enabled?

		if (lowerSOCpc > 0) { // assume discharging is desired down to the % - but only if the price is right

			List<SlotCost> dischargeSlots = new ArrayList<SlotCost>();

			// deduce the number of slots between now (the start of this part of the day)
			// and the first charging slot (which could be zero or more likely more)

			// Also count the number of charging slots that have plunge pricing
			// according to our configured property plunge=value

			int plunges = 0;

			int indexEarliestTimeMatch = pricesPerSlotSinceMidnight.size();

			for (int index = 0; index < chargeSchedule.length; index++) {

				String slotEndTime = chargeSchedule[index]; // cheapest first not earliest - so not chronologically
															// ordered

				SlotCost sc = null;

				for (int si = currentSlotIndex; si < pricesPerSlotSinceMidnight.size(); si++) {

					sc = pricesPerSlotSinceMidnight.get(si);

					String endTime = sc.getSlotEndTime24hr();

					if (0 == endTime.compareTo(slotEndTime)) {

						if (si < indexEarliestTimeMatch) {

							indexEarliestTimeMatch = si;
						}

						break; // we've found the earliest - go no further
					}

					if (0 == endTime.compareTo(dayPartEndsAt24hr)) {

						break; // can go no further
					}
				}

				if (pricesPerSlotSinceMidnight.size() == indexEarliestTimeMatch) { // no match

					continue;
				}

				// as the chargeSchedule is ordered by price stop when the price has not plunged

				float importPrice = sc.getImportPrice();

				if (importPrice < (float) plunge) {

					plunges++;
				}
			}

			// We now know that we have a timespan from currentSlotIndex up to (but not
			// including) indexEarliestTimeMatch to schedule some discharge slot(s)
			// but only if there are some plunges to follow (where import implicitly occurs)

			// Example: say we have 2 plunges the first of which is at
			// indexEarliestTimeMatch. We will try to fit in two prior discharges to the
			// grid immediately before the earliest plunge.
			// Possibility there is only room for 1 discharge (or zero if the
			// current time slot has just reached indexEarliestTimeMatch)

			for (int pCount = 0, si = indexEarliestTimeMatch - 1; pCount < plunges
					&& si >= currentSlotIndex; si--, pCount++) {

				SlotCost sc = pricesPerSlotSinceMidnight.get(si);

				dischargeSlots.add(sc);
			}

			// Deal with ad-hoc Demand Flexibility Service (DFS) events typically as
			// advertised by energy provider
			// These events typically occur when there is insufficient grid power/reserves
			// in some or all DNO regions
			//
			// Strategy is to read properties file and add 1 or more discharge slots as
			// required. Assumption that some manual intervention has added dfs1=hh:mm

			for (int si = currentSlotIndex; si < pricesPerSlotSinceMidnight.size(); si++) {

				SlotCost sc = pricesPerSlotSinceMidnight.get(si);

				String slotStartTime24hr = sc.getSlotStartTime24hr();

				if (dfs.contains(slotStartTime24hr)) {

					dischargeSlots.add(sc);
				}

				// only check DFS slots on the current day - to avoid duplication tomorrow
				if ("23:30".equals(slotStartTime24hr)) {

					break;
				}
			}

			String currentSlotEndTime = pricesPerSlotSinceMidnight.get(currentSlotIndex).getSlotEndTime24hr();

//			String currentSlotStartTime = pricesPerSlotSinceMidnight.get(currentSlotIndex).getSlotStartTime24hr();

//			boolean schedulingTime = 0 == dayPartStartsAt24hr.compareTo(currentSlotStartTime);

			result = new String[dischargeSlots.size()];

			for (int r = 0; r < result.length; r++) {

				SlotCost sc = dischargeSlots.get(r);

				result[r] = sc.getSlotEndTime24hr();

				if (sc.getImportPrice() > 0) {

					if (0 == currentSlotEndTime.compareTo(result[r])) {

						batteryDischargePower(Integer.valueOf(maxRate));

						int scheduleIndex = r;

						String startTime = sc.getSlotStartTime24hr();

						String expiryTime = result[r];

						int minPercent = lowerSOCpc;

						resetDischargingSlot(scheduleIndex, startTime, expiryTime, minPercent);
					}

					if (0 == currentSlotEndTime.compareTo(result[r])) {

						dischargeMonitorThread = new WatchSlotDischargeHelperThread(this, currentSlotEndTime, 28, r,

								// experimental: 28 minutes instead of 29

								lowerSOCpc, Integer.valueOf(maxRate));

						dischargeMonitorThread.start();

						acChargeEnable(false);
						enableDcDischarge(true);
					}

				} else {

					logErrTime("ALERT: Cancelling this export slot because import price indicates free or better");

					slotIsCancelled = true;
				}
			}
		}

		return result;
	}

	// Enable DC Discharge
	void enableDcDischarge(boolean value) {

		execWrite(setting, "56", value ? "true" : "false");
	}

	// AC Charge Enable
	public void acChargeEnable(boolean value) {

		execWrite(setting, "66", value ? "true" : "false");
	}

	private String[] scheduleBatteryCharging(List<SlotCost> pricesPerSlotSinceMidnight, int currentSlotIndex,
			Float kWhrSolar, ImportExport gridImportExport, Float kWhrConsumption, Integer percentBattery,
			ChargeDischarge chargeAndDischarge, String timestamp, int averageUnitCost, int[] sunData, int countDays) {

		SlotCost currentSlotCost = pricesPerSlotSinceMidnight.get(currentSlotIndex);

		String rangeStartTime = currentSlotCost.getSlotStartTime24hr();

		String rangeEndTime = currentSlotCost.getSlotEndTime24hr();

		// BY convention we divide the day up into 4 parts - each is 24hr time HH:mm
		// EG 00:00, 08:00, 12:00, 18:00
		// However we will allow 0 to N parts with start times as HH:mm stored in
		// parts[N] and the times must be ascending and unique.

		int partNumber = 1;

		String key = "part1";

		LocalDateTime ldtPrevious = convertHHmm("00:00").minusMinutes(1L);

		parts = new ArrayList<String>();

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

				int sunIndex = -1;

				if ('R' == timeDynamic.charAt(5)) { // allow slot to start earlier than default (but not later)

					// sunrise yet ?

					sunIndex = sunData[0];

					if (-1 != sunIndex) {

						// is it before our default ?

						if (sunIndex < defaultIndex) {

							value = slotStartTimes[sunIndex];

							System.out.println("Due to selected option (R) adjusted " + key + "=" + defaultValue
									+ "R start time to " + value + " due to earlier  sunrise time " + sunEvents[0]);
						}
					}

				} else if ('C' == timeDynamic.charAt(5)) { // allow slot to start earlier than default (but not later)

					// culmination yet ? i.e., sun at highest point ?

					sunIndex = sunData[1];

					if (-1 == sunIndex) {

						// is it before our default ?

						if (sunIndex < defaultIndex) {

							value = slotStartTimes[sunIndex];

							System.out.println("Due to selected option (C) adjusted " + key + "=" + defaultValue
									+ "C start time to " + value + " due to earlier culmination at " + sunEvents[1]);
						}
					}

				} else if ('N' == timeDynamic.charAt(5)) { // allow slot to start later than default (but not earlier)

					// culmination yet ? i.e., sun at highest point ?

					sunIndex = sunData[1];

					if (-1 == sunIndex) {

						// No, not yet. Push the time to the next slot

						if (rangeStartIndex >= defaultIndex) {

							value = slotStartTimes[1 + rangeStartIndex];
						}

					} else {

						// yes.

						if (sunIndex > defaultIndex) {

							value = slotStartTimes[sunIndex];

							System.out.println("Due to selected option (N) adjusted " + key + "=" + defaultValue
									+ "N start time to " + value + " due to later solar noon time " + sunEvents[1]);
						}
					}

				} else if ('S' == timeDynamic.charAt(5)) {

					// sunset yet ?

					sunIndex = sunData[2];

					if (-1 == sunIndex) {

						// No, not yet. Push the time to the next slot

						if (rangeStartIndex >= defaultIndex) {

							value = slotStartTimes[1 + rangeStartIndex];
						}

					} else {

						// yes.

						if (sunIndex > defaultIndex) {

							value = slotStartTimes[sunIndex];

							System.out.println("Due to selected option (S) adjusted " + key + "=" + defaultValue
									+ "S start time to " + value + " due to later  time of sunset " + sunEvents[2]);
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

		System.out.println("The day and night has been configured as " + numberOfParts + " parts:");

		//
		//
		//

		dfs = new HashSet<String>();

		key = "dfs1";

		while (properties.containsKey(key)) {

			String defsEventStartAt = properties.getProperty(key);

			dfs.add(defsEventStartAt);

			int nextIndex = 1 + dfs.size();

			key = "dfs" + nextIndex;
		}

		//
		//
		//

		final String dayPartsEndAt24hr[] = new String[numberOfParts];

		final String dayPartsEndBefore12hr[] = new String[numberOfParts];

		final int slotsPerDayPart[] = new int[numberOfParts];

		final Character[] options = new Character[numberOfParts];

		final String[] optionParameters = new String[numberOfParts];

		final int powers[] = new int[numberOfParts];

		final int maxPercents[] = new int[numberOfParts];

		final int minPercents[] = new int[numberOfParts];

		float units = 0;

		StringBuffer sbScaledDown = null;

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

//			float wattHours = slotsPerDayPart[p] * powers[p] / 2;

			sbScaledDown = new StringBuffer(" ");

			if (null != optionParameters[p] && 'N' == options[p]) {

				int numScaledDown = Integer.parseInt(optionParameters[p]);

				if (numScaledDown > 0) {

					sbScaledDown.append('(');

					for (int index = slotsPerDayPart[p] - numScaledDown; index < slotsPerDayPart[p]; index++) {

						sbScaledDown.append(WatchSlotChargeHelperThread.SN(index));
					}

					sbScaledDown.append("reduced charge)");
				}
			}

			String range = String.valueOf(minPercents[p]) + "% to " + String.valueOf(maxPercents[p]) + "%";

			ranges.add(range);

			System.out.println("Part{" + (1 + p) + "} " + parts.get(p) + " to " + dayPartsEndAt24hr[p] + "\t"
					+ slotsPerDayPart[p] + " half-hour slot(s) up to " + powers[p] + " watts\tBattery " + range + "\t"
					+ (' ' == options[p] ? " - no option"
							: " + option:" + options[p] + (null == optionParameters[p] ? ""
									: ":" + optionParameters[p] + sbScaledDown.toString())));

			units += (+powers[p] * slotsPerDayPart[p]);
		}

		units = units / 2000;

		Integer solarForecastWhr = execReadForecastSolar();

		if (solarForecastWhr > Integer.parseInt(maxSolar)) {

			System.out.println("INFORMATION: Latest solar forcast " + solarForecastWhr + " exceeds value in property "
					+ KEY_MAX_SOLAR + "=" + maxSolar + " Advisable to update property file " + propertyFileName);
		}

		int p = whatPartfTheDay(rangeStartTime);

		int dayMinutesReduction = scaleSolarForcastRange0to29(solarForecastWhr, p);

		int nightMinutesReduction = scaleBatteryRange0to29(percentBattery);

		if (dfs.size() > 0) {

			System.out.println(dfs.size() + " Demand Flexibility Service event(s) have been configured:");

			for (String startAt : dfs) {

				System.out.println("\t\t" + startAt);
			}
		}

		dayPartPowerDefault = powers[p];

//		dayPartStartsAt24hr = parts.get(p);
		dayPartEndsAt24hr = dayPartsEndAt24hr[p];

		int numberofChargingSlotsInThisPartOfDay = slotsPerDayPart[p];

		chargeSchedule = instance.readChargingSchedule(numberofChargingSlotsInThisPartOfDay);

		lowerSOCpc = minPercents[p];

		Float kWhrCharge = chargeAndDischarge.getCharge();
		Float kWhrDischarge = chargeAndDischarge.getDischarge();

		String charge = String.format("%2.1f", kWhrCharge);
		String discharge = String.format("%2.1f", kWhrDischarge);

		Float kWhrGridImport = gridImportExport.getGridImport();
		Float kWhrGridExport = gridImportExport.getGridExport();

		String gridImportUnits = String.format("%4.1f", kWhrGridImport);
		String gridExportUnits = String.format("%4.1f", kWhrGridExport);

		String penceImport = String.format("%5.2f", currentSlotCost.getImportPrice());
		String penceExport = String.format("%5.2f", currentSlotCost.getExportPrice());

		String data = penceImport + "p Bat:" + percentBattery + "%" + " Sol:" + kWhrSolar + " Imp:" + gridImportUnits
				+ " Cha:" + charge + " Dis:" + discharge + " Con:" + kWhrConsumption + " Exp:" + gridExportUnits;

		final String opts[] = { "default", "day", "night" };

		String opt = 'D' == options[p] ? opts[1] : ('N' == options[p] ? opts[2] : opts[0]);

		logErrTime(rangeStartTime + " " + (1 + p) + "/" + numberOfParts + " (" + opt + ") " + data);

		String power = null;

		int minsDelayStart = 0;

		String chargeDischarge = String.format("%+2.1f", kWhrCharge - kWhrDischarge);

		// is sunrise or solar noon or sunset occurring within the current slot range?

		String event = " ";

		if (sunData[0] == currentSlotIndex) {

			event = "RISE:" + sunEvents[0];

		} else if (sunData[1] == currentSlotIndex) {

			event = "NOON:" + sunEvents[1];

		} else if (sunData[2] == currentSlotIndex) {

			event = "SET:" + sunEvents[2];
		}

		String csv = rangeStartTime + "," + String.valueOf(solarForecastWhr.intValue()) + "," + (1 + p) + ","
				+ numberOfParts + "," + percentBattery + "," + kWhrSolar + "," + kWhrGridImport + "," + chargeDischarge
				+ "," + kWhrConsumption + "," + kWhrGridExport;

		for (int n = p; n < numberOfParts; n++) {

			int comp = rangeStartTime.compareTo(parts.get(n));

			if (comp < 0) {

				break;
			}

			if (0 == comp) {

				power = String.valueOf(dayPartPowerDefault);

				String percentMin = String.valueOf(minPercents[p]);
				String percentMax = String.valueOf(maxPercents[p]);

				//

				if (null != solarForecastWhr) {

					if ('D' == options[p]) { // (day) option - delay start time by N minutes for a sunny day
												// more sunshine predicted: more delay

						minsDelayStart = dayMinutesReduction;

					} else if ('N' == options[p]) { // (night) option - depending on battery state
													// delay start time - more battery: more delay.

						minsDelayStart = nightMinutesReduction;
					}
				}

				logErrTime("Part " + (1 + p) + "/" + numberOfParts + " Schedule " + power + " W x "
						+ String.valueOf(chargeSchedule.length) + " slot(s) " + percentMin + "% to " + percentMax + "% "
						+ (minsDelayStart > 0 ? "delay:" + minsDelayStart + "m " : "") + "Solar:" + solarForecastWhr
						+ " / " + maxSolar);

				break; // No need to loop
			}
		}

		float[] costsSoFarToday = logGridInOutEvents(timestamp, csv, event, penceImport, penceExport);

		float importCostSoFarToday = costsSoFarToday[0] / 100;
		float exportCostSoFarToday = costsSoFarToday[1] / 100;
		float netCostSoFarToday = costsSoFarToday[2] / 100;

		float avUnitPriceToday = (costsSoFarToday[0] - agileImportStandingCharge) / (float) kWhrGridImport;

		System.out.println("\nToday's import cost: " + (ansi ? ANSI_SCORE : "") + "£"
				+ String.format("%5.2f", importCostSoFarToday) + (ansi ? ANSI_RESET : "") + "  (so far...) based on "
				+ gridImportUnits + " kWhr imported up to " + timestamp.substring(11, 19)
				+ " (including standing charge " + String.format("%5.2f", agileImportStandingCharge) + "p)  "
				+ String.format("%5.2f", avUnitPriceToday) + "p / kWhr");

		System.out.println(
				String.format("%2d", countDays) + " day (A)verage price: " + String.format("%3d", averageUnitCost)
						+ "p (assuming Octopus Agile import and Fixed export:15p) Fixed rate (F) " + flatRateImport
						+ "p " + (ansi ? ANSI_COLOUR_LO : "") + "Sun forecast: "
						+ String.format("%5d", solarForecastWhr) + (ansi ? ANSI_RESET : "") + "  "
						+ (ansi ? ANSI_SUNSHINE : "") + gridExportUnits + (ansi ? ANSI_RESET : ""));

		System.out.println("Plunge price is set to:  " + String.format("%2d", plunge)
				+ "p (System schedules e(X)port slots prior to price plunge slots <= " + plunge + "p) " + "Surplus: "
				+ (ansi ? (netCostSoFarToday < 0 ? ANSI_COLOR_HI : ANSI_SCORE) : "") + "£"
				+ String.format("%+5.2f", netCostSoFarToday) + (ansi ? ANSI_RESET : "") + " actual: "
				+ (ansi ? ANSI_COLOUR_LO : "") + String.format("%5.0f", 1000 * kWhrSolar) + (ansi ? ANSI_RESET : "")
				+ (ansi ? ANSI_SUNSHINE : "") + " £" + String.format("%5.2f", exportCostSoFarToday)
				+ (ansi ? ANSI_RESET : ""));

		int[] slots = null;

		Character deferredMacro = null;

		String deferredStartTime = null;

		if (null != power && numberofChargingSlotsInThisPartOfDay > 0) {

			slots = findOptimalCostSlotToday(numberofChargingSlotsInThisPartOfDay, pricesPerSlotSinceMidnight,
					currentSlotIndex, dayPartsEndBefore12hr[p]);

			if (null != charge && 0 != "false".compareTo(charge)) {

				for (int s = 0; s < numberofChargingSlotsInThisPartOfDay; s++) {

					// Normally slots.length == numberofChargingSlotsInThisPartOfDay
					// However if we configure more slots than can be fit in the remaining in the
					// part of the day slots.length will be < numberofChargingSlotsInThisPartOfDay
					// Exploit this to clear charging slots to 00:29

					char macroId = "ABCDEFGHIJ".charAt(s);

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

						chargeSchedule[s] = to;

						deferredMacro = macroId;
						deferredStartTime = from;

					} else {

						chargeSchedule[s] = execMacro(macro, macroId, from, to, maxPercents[p]);
					}
				}
			}
		}

		// create an array of prices related to the times in the schedule

		float[] schedulePrices = new float[chargeSchedule.length];

		int pricesInitialised = 0;

		for (int n = 0; n < chargeSchedule.length; n++) {

			// reminder: each schedule[n] gives the slot end time in 24hr

			boolean found = false;

			for (SlotCost sc : pricesPerSlotSinceMidnight) {

				if (0 == chargeSchedule[n].compareTo(sc.getSlotEndTime24hr())) {

					schedulePrices[n] = sc.getImportPrice();

					found = true;
					pricesInitialised++;
					break;
				}
			}

			if (!found) {

				// If a manual forced charge is occurring initiated by Inverter app
				// it will typically override the first slot with an invalid 23:64 or 23.72 etc
				// To detect/defend against this, if the hour is 23 and minutes NN are >59
				// assume we are
				// subject to this particular manual intervention

				String slot = chargeSchedule[n];

				String slotHH = null;
				Integer slotMM = null;

				try {
					slotHH = slot.substring(0, 2);

					slotMM = Integer.parseInt(slot.substring(3));

				} catch (Exception e) {

					logErrTime("WARNING: chargeSchedule[] has " + chargeSchedule.length + " slots and we require [" + n
							+ "] but the value is '" + slot + "'");
				}

				if (null == slotMM || 0 == "23".compareTo(slotHH) && (slotMM > 59 || 0 == slotMM)) {

					logErrTime("WARNING: Assume manually controlled charging in progress and has overriden schedule");

				} else {

					// We always expect to get a match within the current day part
					// Failure to match *probably* means the most recent scheduling has failed

					Exception e = new Exception("ERROR: Possible scheduling failure: no match for time "
							+ chargeSchedule[n] + " schedule[" + n + "]");

					e.printStackTrace();
				}
			}
		}

		int minPercent = minPercents[p];
		int maxPercent = maxPercents[p];

		int defaultChargeRate = powers[p];

		int chargingSlotPower = defaultChargeRate;

		// have we gone past all the scheduled time slots in this part of the day?

		boolean allChargeSlotsDone = true;

		for (int s = 0; s < chargeSchedule.length; s++) {

			int comp = rangeEndTime.compareTo(chargeSchedule[s]);

			if (comp <= 0) {

				allChargeSlotsDone = false;
				break;
			}
		}

		float currentSlotPrice = currentSlotCost.getImportPrice();

		// if no more scheduled slots in this part of the day, but the current price is
		// free or better still negative
		// replace S1 with the current time and force a charge immediately

		if (allChargeSlotsDone && currentSlotPrice <= 0) {

			schedulePrices[0] = currentSlotCost.getImportPrice();

			chargeSchedule[0] = rangeEndTime;

			logErrTime("At " + rangeEndTime + " " + penceImport + "p allSlotsDone:"
					+ (allChargeSlotsDone ? "true" : "false"));
		}

		slotIsCancelled = false;

		// is the current time a charging slot according to the current schedule?

		for (int s = 0; s < chargeSchedule.length; s++) {

			int comp = rangeEndTime.compareTo(chargeSchedule[s]);

			if (0 == comp) {

				// if price negative - charge now *irrespective* of profile

				if (schedulePrices[s] <= 0) {

					// higher charge rate for lowest cost slot

					defaultChargeRate = optimisePowerCost(Integer.parseInt(maxRate), schedulePrices, s);

					if (defaultChargeRate < 1) {

						logErrTime("Unscheduling S" + (1 + s)
								+ " Power optimisation suggests this charging slot not required");

						resetChargingSlot(s, rangeEndTime, rangeEndTime, 100);

						slotIsCancelled = true;

						break; // do not drop through to new WatchSlotChargeHelperThread
					}

					logErrTime("ALERT: Free energy! Overiding S" + (1 + s) + " start time & limit " + maxPercent
							+ "% / @ " + dayPartPowerDefault + "W with 100% / @ " + defaultChargeRate + " W");

					minPercent = 100; // will trigger a expedite:true within the WatchSlotChargeHelperThread
					maxPercent = 100;

					chargingSlotPower = -1; // special indicator for current electricity price free

				} else if (null != percentBattery && percentBattery >= maxPercents[p]) {

					logErrTime("Unscheduling S" + (1 + s) + " Battery charge. Already >= " + maxPercent
							+ "% Resetting begin/end to " + rangeEndTime);

					resetChargingSlot(s, rangeEndTime, rangeEndTime, 100);

					slotIsCancelled = true;

					break; // do not drop through to new WatchSlotChargeHelperThread

				} else {

					if (null != deferredMacro) {

						logErrTime("Starting charge soon at " + deferredStartTime + " Battery limit < " + maxPercent
								+ "%");

						chargeSchedule[s] = execMacro(macro, deferredMacro.charValue(), deferredStartTime, rangeEndTime,
								maxPercent);
					}

					String dateYYYY_MM_DD = logErrTime(
							"Time matches " + WatchSlotChargeHelperThread.SN(s) + "ending at " + rangeEndTime)
							.substring(0, 10);

					logErrTime("Adjusting average charging power to " + dayPartPowerDefault + " watts");

					if ('N' == options[p]) { // (night) option - charging will start at beginning of slot
												// however for at least 1 slot power scaled downwards depending on
												// solar
												// use option Night:2 to decrease in power 2 most expensive slots
												// i.e., slot3 & slot4 if there are 4 in this part of the day

						int slotsToReducePower = 1;

						if (null != optionParameters[p]) {

							slotsToReducePower = Integer.parseInt(optionParameters[p]);
						}

						if (s >= chargeSchedule.length - slotsToReducePower) {

							// ./solar.csv will contain the solar predictions
							// stored earlier at the slot scheduling time

							String suffix = parts.get(p).substring(0, 5); // typically "00:00"

							String[] cols = delogSolarData(dateYYYY_MM_DD, suffix);

							if (null == cols) {

								chargingSlotPower = Math.round(dayPartPowerDefault * 2 / 3);

								logErrTime("ERROR: Cannot get solar data for " + dateYYYY_MM_DD + " " + suffix
										+ " scale 67% by default");

							} else {

								// col[2] will hold solar prediction for current part (in Whr)

								Integer WHrToday = Integer.valueOf(cols[2]);

								// reduce chargingSlotPower if high prediction
								// leave chargingSlotPower if zero or very low predicted

								int scaled = 30 - scaleSolarForcastRange0to29(WHrToday, p);

								float chargeRate = (float) dayPartPowerDefault * (float) scaled / 30.0f;

								chargingSlotPower = Math.round(chargeRate);

								logErrTime("Option " + options[p] + ":" + optionParameters[p] + " "
										+ dayPartPowerDefault + " watts scaled down by (1-30): " + scaled
										+ "/30 due to Solar:" + cols[2] + " / " + maxSolar);
							}
						}

					} else if ('D' == options[p]) { // (day) option

						if (chargeSchedule.length == pricesInitialised && chargeSchedule.length > 1) {

							// we know all the prices in this part of the day
							// have been established for the slots in the schedule

							chargingSlotPower = optimisePowerCost(dayPartPowerDefault, schedulePrices, s);

							if (chargingSlotPower < 1) {

								logErrTime("Charging slot S" + (1 + s) + " not required." + " Resetting begin/end to "
										+ rangeEndTime);

								resetChargingSlot(s, rangeEndTime, rangeEndTime, 100);

								slotIsCancelled = true;

								break; // do not drop through to new WatchSlotChargeHelperThread
							}

						} else {

							chargingSlotPower = defaultChargeRate;
						}
					}
				}

				chargeMonitorThread = new WatchSlotChargeHelperThread(this, 29, s, minPercent, maxPercent,
						chargingSlotPower);

				chargeMonitorThread.start(); // this spawned thread will run no longer than HH:MM in schedule[s]
				// the slot will be reset when task complete

				break;
			}
		}

		return chargeSchedule;
	}

	private int whatPartfTheDay(String rangeStartTime) {

		int p = 0;

		// what part of the day are we in?

		final int numberOfParts = parts.size();

		for (; p < numberOfParts - 1; p++) {

			if (rangeStartTime.compareTo(parts.get(1 + p)) < 0) {

				break;
			}
		}

		return p;
	}

	// Battery Charge Power
	@Override
	public void batteryChargePower(int power) {

		execWrite(setting, "72", String.valueOf(power));
	}

	// Battery Discharge Power
	@Override
	public void batteryDischargePower(int power) {

		execWrite(setting, "73", String.valueOf(power));
	}

	@Override
	public void resetChargingSlot(int scheduleIndex, String startTime, String expiryTime, int socMaxPercent) {

		char macroId = "ABCDEFGHIJ".charAt(scheduleIndex);

		logErrTime(WatchSlotChargeHelperThread.SN(scheduleIndex) + "Reset " + startTime + "-" + expiryTime
				+ " Upper SOC limit " + socMaxPercent + "%");

		execMacro(macro, macroId, startTime, expiryTime, socMaxPercent);
	}

	@Override
	public void resetDischargingSlot(int scheduleIndex, String startTime, String expiryTime, int socMinPercent) {

		char macroId = "KLMNOPQRST".charAt(scheduleIndex);

		logErrTime(WatchSlotDischargeHelperThread.XN(scheduleIndex) + "Reset " + startTime + "-" + expiryTime
				+ " Lower SOC limit " + socMinPercent + "%");

		execMacro(macro, macroId, startTime, expiryTime, socMinPercent);
	}

	private float[] logGridInOutEvents(String timestamp, String data, String comment, String penceImport,
			String penceExport) {

		float pCummulativeImport = 0.0f;
		float pCummulativeExport = 0.0f;
		float fSystemScore = 0.0f;

		if (null != fileSolar) {

			try {

				long length;

				String extraData = "";

				try {
					RandomAccessFile randomAccessFile = new RandomAccessFile(fileSolar, "r");

					length = randomAccessFile.length();

					if (length > 190) {

						randomAccessFile.seek(length - 190);
					}

					long pos = 0;

					String lastLine = null;

					do {
						lastLine = randomAccessFile.readLine();

						pos = randomAccessFile.getFilePointer();

					} while (pos < length);

					randomAccessFile.close();

//					System.err.println("previous={" + lastLine + "}");
//					System.err.println("latest=                      {" + data + "}");

					// split the line into fields

					String previousFields[] = lastLine.split(",");

					// get the previous timestamp

					LocalDateTime ldtPrevious = LocalDateTime.parse(previousFields[0],
							DateTimeFormatter.ISO_LOCAL_DATE_TIME);

					ZonedDateTime zdtPrevious = ZonedDateTime.of(ldtPrevious, ourZoneId);

					// difference in seconds = typically around 1800 (== 30 mins)

					long sDiff = ourTimeNow.toEpochSecond() - zdtPrevious.toEpochSecond();

					float kWhrGridImport = Float.parseFloat(previousFields[7]);

					String latestFields[] = data.split(",");

					float kWhrGridImportNow = Float.parseFloat(latestFields[6]);

					float gridUnitsSinceLastLoggingImport = kWhrGridImportNow - kWhrGridImport;

					float pUnitPrice = previousFields.length < 14 ? 15.0f : Float.parseFloat(previousFields[13]);

					if (gridUnitsSinceLastLoggingImport < 0 || previousFields.length < 17) { // assume new day and
																								// meters have
						// reset

						comment = "RESET";

						pCummulativeImport = agileImportStandingCharge;

						gridUnitsSinceLastLoggingImport = kWhrGridImportNow;

					} else {

						pCummulativeImport = Float.parseFloat(previousFields[16]);
					}

					float pEstimateImport = pUnitPrice * gridUnitsSinceLastLoggingImport;

					pCummulativeImport += pEstimateImport;

					float kWhrGridExport = Float.parseFloat(previousFields[10]);

					float kWhrGridExportNow = Float.parseFloat(latestFields[9]);

					float gridUnitsSinceLastLoggingExport = kWhrGridExportNow - kWhrGridExport;

					float pUnitPriceExport = previousFields.length < 19 ? 15.0f : Float.parseFloat(previousFields[18]);

					if ("RESET".equals(comment) || previousFields.length < 22) {

						// reset

						pCummulativeExport = agileExportStandingCharge;

						gridUnitsSinceLastLoggingExport = kWhrGridExportNow;

					} else {

						pCummulativeExport = Float.parseFloat(previousFields[21]);
					}

					float pEstimateExport = pUnitPriceExport * gridUnitsSinceLastLoggingExport;

					pCummulativeExport += pEstimateExport;

					fSystemScore = pCummulativeExport - pCummulativeImport; // negative is deficit; positive is surplus

					StringBuffer sb = new StringBuffer();

					sb.append(",");
					sb.append(String.valueOf(sDiff));
					sb.append(",");
					sb.append(String.valueOf(penceImport));
					sb.append(",");
					sb.append(String.valueOf(gridUnitsSinceLastLoggingImport));
					sb.append(",");
					sb.append(String.valueOf(pEstimateImport));
					sb.append(",");
					sb.append(String.valueOf(pCummulativeImport));
					sb.append(",");
					sb.append(" ");
					sb.append(",");
					sb.append(String.valueOf(penceExport));
					sb.append(",");
					sb.append(String.valueOf(gridUnitsSinceLastLoggingExport));
					sb.append(",");
					sb.append(String.valueOf(pEstimateExport));
					sb.append(",");
					sb.append(String.valueOf(pCummulativeExport));
					sb.append(",");
					sb.append(String.valueOf(fSystemScore));

					extraData = sb.toString();

				} catch (FileNotFoundException e) {

				}

				FileWriter fileWriter = new FileWriter(fileSolar, true); // append to existing file

				BufferedWriter solarDataWriter = new BufferedWriter(fileWriter);

				solarDataWriter.append(timestamp);
				solarDataWriter.append(",");
				solarDataWriter.append(data);

				solarDataWriter.append(",");
				solarDataWriter.append(comment);

				solarDataWriter.append(extraData);
				solarDataWriter.newLine();

				solarDataWriter.flush();
				solarDataWriter.close();

				fileWriter.close();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		float[] result = new float[] { pCummulativeImport, pCummulativeExport, fSystemScore };

		return result;
	}

	private static Map<String, String> getSolarDataFromFile(Set<Integer> weekNumbers) {

		return getSolarDataFromFile(null, weekNumbers, null, null);
	}

//	private static Map<String, String> getSolarDataFromFile(String dateYYYY_MM_DD) {
//
//		return getSolarDataFromFile(dateYYYY_MM_DD, null, null, null);
//	}

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

					if (cols.length < 3) {

						System.out.println(cols.length + "\t{" + line + "}");

						break; // we're done
					}

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

							if (cols.length > 11) {

								if (cols[11].startsWith("SUNRISE:")) {

									result.put(key + "R", line);

								} else if (cols[11].startsWith("NOON:")) {

									result.put(key + "N", line);

								} else if (cols[11].startsWith("SUNSET:")) {

									result.put(key + "S", line);
								}

							} else {

								continue;
							}
						}
					}
				}

				solarDataReader.close();

				fileReader.close();

			} catch (FileNotFoundException e1) {

			} catch (IOException e) {

				e.printStackTrace();
			}
		}

		return result;

	}

	/*
	 * returns slot index for sunrise [0] and solar noon [1] and sunset [2]
	 * respectively or -1 if event not yet happened
	 * 
	 * typically [16, 24, 32] for 08:00, 12:00 and 16:00 respectively
	 * 
	 */

	private static int[] hasSunRisenAndSet(String today) {

		int[] sunRisenAndSet = new int[] { -1, -1, -1 };

		// assume an index into slotStartTimes[] where value 0 implies "00:00",
		// 1 is "00:30", 2 is "01:00" etc.

		sunEvents = execReadSunData();

		LocalTime ltSunrise = LocalTime.parse(sunEvents[0], DateTimeFormatter.ISO_LOCAL_TIME);
		LocalTime ltCulmination = LocalTime.parse(sunEvents[1], DateTimeFormatter.ISO_LOCAL_TIME);
		LocalTime ltSunset = LocalTime.parse(sunEvents[2], DateTimeFormatter.ISO_LOCAL_TIME);

		for (int i = 0; i < 48; i++) {

			LocalTime slotTime = LocalTime.parse(slotStartTimes[i], DateTimeFormatter.ISO_LOCAL_TIME);

			LocalTime nextSlotTime = slotTime.plusMinutes(30l);

			if (-1 == sunRisenAndSet[0] && ltSunrise.isAfter(slotTime) && ltSunrise.isBefore(nextSlotTime)) {

				sunRisenAndSet[0] = i;
			}

			if (-1 == sunRisenAndSet[1] && ltCulmination.isAfter(slotTime) && ltCulmination.isBefore(nextSlotTime)) {

				sunRisenAndSet[1] = i;
			}

			if (-1 == sunRisenAndSet[2] && ltSunset.isAfter(slotTime) && ltSunset.isBefore(nextSlotTime)) {

				sunRisenAndSet[2] = i;
			}
		}

		int dayLengthSeconds = ltSunset.toSecondOfDay() - ltSunrise.toSecondOfDay();

		int hours = dayLengthSeconds / 3600;

		int remainder1 = dayLengthSeconds % 3600; // 0 to 3599

		int minutes = remainder1 / 60; // 0 to 59

		int seconds = remainder1 % 60; // 0 to 59

		LocalDateTime ldt = LocalDateTime.now();

		LocalTime ltNow = ldt.toLocalTime();

		bannerMessage = "\nToday is " + DayOfWeek.of(ldt.get(ChronoField.DAY_OF_WEEK)).toString().substring(0, 3) + " "
				+ today + "\tSun up for " + hours + " hours " + minutes + " mins " + seconds + " secs. Sunrise "
				+ (ltNow.compareTo(ltSunrise) > 0 ? "was" : "is") + " at " + sunEvents[0] + " Solar Noon "
				+ (ltNow.compareTo(ltCulmination) > 0 ? "was" : "is") + " at " + sunEvents[1] + " Sunset "
				+ (ltNow.compareTo(ltSunset) > 0 ? "was" : "is") + " at " + sunEvents[2] + "\n";

		return sunRisenAndSet;
	}

	/*
	 * returns slot index for sunrise [0] and sunset [1] and solar noon [2]
	 * respectively or -1 if event not yet happened
	 * 
	 */

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

	private static int scaleSolarForcastRange0to29(Integer wHrSunshine, int partOfDayIndex) {

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

	public int[] testableOPC(int defaultPower, float[] prices, final int index) {

		final int numberInSchedule = prices.length;

		int[] powers = new int[numberInSchedule];

		int[] defaultPowers = new int[numberInSchedule];

		int targetPower = 0;

		for (int s = 0; s < numberInSchedule; s++) {

			targetPower += defaultPower;

			powers[s] = defaultPower;

			defaultPowers[s] = defaultPower;
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

		if (powers[numberInSchedule - 1] < 0) {

			powers[numberInSchedule - 2] += powers[numberInSchedule - 1];

			powers[numberInSchedule - 1] = 0;
		}

		// debug logging

		int power = 0;
		float cost = 0f;

		float withOptPrice = 0f;
		float withoutOptPrice = 0f;

		float accWith = 0f;

		float accWithout = 0f;

		for (int s = 0; s < numberInSchedule; s++) {

			prices[s] -= kludge;

			power += powers[s];

			cost += (powers[s] * prices[s]);

			withOptPrice = (float) powers[s] / 2000f * prices[s];

			accWith += withOptPrice;

			withoutOptPrice = (float) defaultPower / 2000f * prices[s];

			accWithout += withoutOptPrice;

			logErrTime((index == s ? "*" : " ") + WatchSlotChargeHelperThread.SN(s) + "Power:"
					+ String.format("%5d", powers[s]) + " @ " + String.format("%5.2f", prices[s]) + "p ("
					+ String.format("%5.2f", withOptPrice) + ") instead of: " + defaultPower + " @ "
					+ String.format("%5.2f", prices[s]) + "p (" + String.format("%5.2f", withoutOptPrice) + ")");
		}

		float costOfPower = cost / power;

		float nonOptCostOfPower = accWithout * 2000f / power;

		logErrTime("Tot Power:" + String.format("%5d", power) + " = " + String.format("%5.2f", costOfPower) + "p ("
				+ String.format("%5.2f", accWith) + ") instead of:" + String.format("%5d", power) + " = "
				+ String.format("%5.2f", nonOptCostOfPower) + "p (" + String.format("%5.2f", accWithout) + ")");

		if (accWithout <= accWith) {

			logErrTime("Speculative power optimisation no better: reseting power to default " + defaultPower);

			powers = defaultPowers;
		}

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

	private static String[] execReadSunData() {

		String result[] = null;

		if (!"false".equalsIgnoreCase(sun)) {

			String[] cmdarray = sun.split(" ");

			String value = exec(cmdarray).trim();

			if (null != value) {

				result = value.split(",");
			}
		}

		return result;
	}

	private static Integer execReadForecastSolar() {

		Integer result = null;

		if (!"false".equalsIgnoreCase(forecast)) {

			String[] cmdarray = forecast.split(" ");

			String value = exec(cmdarray);

			if (null != value) {

				String trimmed = value.trim();

				if (trimmed.length() > 0) {

					result = Integer.valueOf(trimmed);
				}
			}
		}

		if (null == result) {

			result = Integer.valueOf(0);
		}

		return result;
	}

	private static Float execReadSolar() {

		Float result = null;

		if (!"false".equalsIgnoreCase(solar)) {

			String[] cmdarray = solar.split(" ");

			String value = exec(cmdarray);

			if (null != value) {

				String trimmed = value.trim();

				if (trimmed.length() > 0) {

					result = Float.valueOf(trimmed);
				}
			}
		}

		if (null == result) {

			result = Float.valueOf(0);
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

		if (!"false".equalsIgnoreCase(battery)) {

			String[] cmdarray = battery.split(" ");

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

		String[] cmdarray = setting.split(" ");

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

		String[] cmdarray = setting.split(" ");

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

	static String execMacro(String template, char macroId, String from, String to, int soc) {

		// assume extra contains a cmdarray to execute in a separate process
		// java -jar plugs.jar ./SwindonIcarus.properties inverter setting %1 %2 %3 %4

		// substitute macro for %1 and from, to and power for %2, %3 and %4 respectively

		String[] cmdarray = template.split(" ");

		for (int n = 0; n < cmdarray.length; n++) {

			if ("%1".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = String.valueOf(macroId);

			} else if ("%2".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = from;

			} else if ("%3".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = to;

			} else if ("%4".equalsIgnoreCase(cmdarray[n])) {

				cmdarray[n] = String.valueOf(soc);
			}
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

		if (showRecent) {

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
		}

		countDays = 0;

		float accumulateDifference = 0;
		float accumulatePower = 0;
		float accumulateCost = 0;
		float accumulateExportUnits = 0;

		SortedSet<String> setOfDays = new TreeSet<String>();

		setOfDays.addAll(elecMapDaily.keySet());

		float flatStandingCharge = Float.valueOf(
				properties.getProperty(KEY_FIXED_ELECTRICITY_STANDING, DEFAULT_FIXED_ELECTRICITY_STANDING_PROPERTY));

//		float agileStandingCharge = Float.valueOf(
//				properties.getProperty(KEY_IMPORT_ELECTRICITY_STANDING, DEFAULT_IMPORT_ELECTRICITY_STANDING_PROPERTY));

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

			// kludge API is returning < 48 slots due to zero values

			if (dayValues.getSlotCount() < 24) {

				continue;
			}

			countDays++;

			float consumption = dayValues.getDailyImport();

			float agilePrice = dayValues.getDailyImportPrice();

			float flatImportPrice = consumption * flatRateImport;

			float agileCost = agilePrice + agileImportStandingCharge;

			float difference = (flatImportPrice + flatStandingCharge) - agileCost;

			float dailyAverageUnitPrice = agilePrice / consumption;

			String values = map.get(key);

			float dailyExportUnits = 0;

			if (null != values) {

				String cols[] = values.split(",");

				dailyExportUnits = Float.valueOf(cols[10]);
			}

			float agileCostInGBP = agileCost / 100;

			float differeceInGBP = difference / 100;

			float exportInGBP = (dailyExportUnits * 15f) / 100;

			boolean quidsIn = exportInGBP > agileCostInGBP;

			if (showRecent) {

				System.out.println(dayValues.getDayOfWeek() + (quidsIn ? " * " : "   ") + key + " £"
						+ String.format("%5.2f", agileCostInGBP) + String.format("%7.3f", consumption) + " kWhr @ "
						+ String.format("%5.2f", dailyAverageUnitPrice) + "p" + " A:"
						+ String.format("%8.4f", agilePrice) + "p +" + agileImportStandingCharge + "p (F: "
						+ String.format("%8.4f", flatImportPrice) + "p +" + flatStandingCharge + "p) Save: £"
						+ String.format("%5.2f", differeceInGBP) + " + Export:"
						+ String.format("%4.1f", dailyExportUnits) + " kWhr £" + String.format("%5.2f", exportInGBP));
			}

			accumulateDifference += difference;

			accumulatePower += consumption;

			accumulateCost += agilePrice;

			accumulateExportUnits += dailyExportUnits;
		}

		Float unitCostAverage = accumulateCost / accumulatePower;

		if (showSavings) {

			renderSummaryB(agileImportStandingCharge, flatStandingCharge, flatRateImport, flatRateExport, countDays,
					unitCostAverage, accumulatePower, accumulateDifference, accumulateExportUnits);
		}

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

		String recentAgileExclStandingChargeInGBP = String.format("%5.2f", recentAgileExclStandingCharge / 100);

		String recentFlatExclStandingChargeInGBP = String.format("%5.2f", recentFlatExclStandingCharge / 100);

		System.out.println("\n" + String.format("%2d", countDays) + " days (A)gile: £"
				+ recentAgileExclStandingChargeInGBP + " (" + averagePower + " kWhr @ " + averageCostPerUnit
				+ "p daily average)   vr   £" + recentFlatExclStandingChargeInGBP + " @ " + flatRateImport
				+ "p /unit flat rate (F)  excl. standing charges");

		System.out.println("Average daily Agile saving:\t£" + averagePounds2DP + " (" + countDays + " x " + subTot0
				+ "p = " + accumulateDifference + "p = [Σ(F)" + recentFlatExclStandingCharge + " + " + countDays + " x "
				+ flatStandingCharge + "]-[Σ(A)" + recentAgileExclStandingCharge + " + " + countDays + " x "
				+ agileStandingCharge + "])");

		System.out.println("Average solar/battery saving:\t£" + solarSaving + " (" + String.format("%.3f", solarPower)
				+ " kWhr less vr. historical consumption " + preSolarLongTermAverage + " kWhr @ (F) = £"
				+ historicDailyCostMinusStandingCharge + " + " + flatStandingCharge + "p = £"
				+ historicDailyCostInclStandingCharge + ")");

		System.out.println("Average daily export worth:\t£" + exportSaving + " (from " + unitsExported + " units @ "
				+ flatRateExport + "p  yielding £" + valueExported + " recently)");
		System.out.println("\t\t\t\t=====");
		System.out.println("Historical comparison:\t£" + historicDailyCostInclStandingCharge + " - £" + totalSaving
				+ "\t= £" + actualCost + " (represents effective recent cost per day on average)");
		System.out.println("\t\t\t\t=====");

	}

	private void showAnalysis(List<SlotCost> pricesPerSlot, int averageUnitCost, ArrayList<Long> bestImportTime,
			ArrayList<Long> bestExportTime) {// , String[] chargeSchedule) {

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

		int indexLimit = null != limit ? Integer.valueOf(limit) : pricesPerSlot.size();

		for (int index = 0; index < pricesPerSlot.size(); index++) {

			if (indexLimit == index) {

				break;
			}

			SlotCost slotCost = pricesPerSlot.get(index);

			String[] period = startAndFinishTimeOfSlotCost(slotCost, 0);

			String from = period[0];

			String to = period[1];

			if (0 == index) {

				int partIndex = whatPartfTheDay(from);

				System.out.println("---{" + (1 + partIndex) + "}---  " + ranges.get(partIndex) + "  ----");
			}

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

						sb1.append('F');

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

			String chargeOrdischargeSlot = null;

			if (execute) {

				// assume same time cannot be in both dischargeSchedule and chargeSchedule

				if (null != dischargeSchedule) {

					for (int d = 0; d < dischargeSchedule.length; d++) {

						if (0 == to.compareTo(dischargeSchedule[d])) {

							chargeOrdischargeSlot = WatchSlotDischargeHelperThread.XN(d, slotIsCancelled);
							break;
						}
					}
				}

				if (null == chargeOrdischargeSlot) {

					for (int s = 0; s < chargeSchedule.length; s++) {

						if (0 == to.compareTo(chargeSchedule[s])) {

							chargeOrdischargeSlot = WatchSlotChargeHelperThread.SN(s, slotIsCancelled);
							break;
						}
					}
				}
			}

			if (index > 0) {

				if (parts.contains(from)) {

					int x = 0;

					for (; x < parts.size(); x++) {

						if (from.equals(parts.get(x))) {

							x++;
							break;
						}
					}

					System.out.println("---{" + x + "}---  " + ranges.get(x - 1) + "  ----");
				}
			}

			System.out.println(optionalExport + slotCost.getSimpleTimeStamp() + "  "
					+ (null == chargeOrdischargeSlot
							? (cheapestImport ? "!" : " ") + (lessThanAverage ? "!" : " ") + " "
							: chargeOrdischargeSlot)
					+ " " + String.format("%5.2f", importValueIncVat) + "p  "
					+ (ansi && cheapestImport ? ANSI_COLOUR_LO : "") + asterisks
					+ (ansi && cheapestImport ? ANSI_RESET : "") + padding + prices + clockHHMM);
		}

		int lastSlot = pricesPerSlot.size() - 1;

		String startTime = pricesPerSlot.get(0).getSimpleTimeStamp();
		String stopTime = pricesPerSlot.get(lastSlot).getSimpleTimeStamp();

		int limitCap = null == limit ? lastSlot : Integer.parseInt(limit);

		int moreAvailable = lastSlot < limitCap ? 0 : lastSlot - limitCap + 1;

		System.out.println("\nPrice data available for " + pricesPerSlot.size() + " half-hour slots in the range "
				+ startTime + " and " + stopTime + " (limit=" + limit + " set) " + moreAvailable + " not shown");
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

			Integer weekOfYear = Long.valueOf(from.getLong(ChronoField.ALIGNED_WEEK_OF_YEAR)).intValue();

			ImportExportData importExportData = importExportPriceMap.get(intervalStart);

			if (null == importExportData) {

				continue;
			}

			Float halfHourExportPrice = importExportData.getExportPrice();

			if (null == halfHourExportPrice) {

				continue;
			}

			Long epochKey = from.toEpochSecond();

			ConsumptionHistory latestConsumption = historyExport.get(epochKey);

			if (null != latestConsumption) {

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

	private ArrayList<V1PeriodConsumption> updateHistory(boolean isImport, Map<Long, ConsumptionHistory> historyMap,
			String someDaysAgo, ArrayList<V1PeriodConsumption> periodResults, int howManyDaysHistory) {// , int count) {

		// Add history data for any non-null consumptions

		boolean blah = "blah!".equals(properties.getProperty("TEST"));

		int count = 0;

		for (int index = 0; index < periodResults.size(); index++) {

			V1PeriodConsumption periodResult = periodResults.get(index);

			String intervalStart = periodResult.getIntervalStart();

			OffsetDateTime odtStart = OffsetDateTime.parse(intervalStart);

			Long key = odtStart.toEpochSecond();

			Float consumption = periodResult.getConsumption();

			ConsumptionHistory consumptionHistory;

			boolean missingConsumptionHistory = !historyMap.containsKey(key);

			if (missingConsumptionHistory) {

				consumptionHistory = new ConsumptionHistory();

				consumptionHistory.setFrom(odtStart);

				String intervalEnd = periodResult.getIntervalEnd();

				OffsetDateTime odtEnd = OffsetDateTime.parse(intervalEnd);

				consumptionHistory.setTo(odtEnd);

				LocalDateTime ldt = odtStart.toLocalDateTime();

				String importExportPriceMapKey = ldt.toString(); // n.b not expecting seconds

				ImportExportData ied = importExportPriceMap.get(importExportPriceMapKey);

				float price = isImport ? ied.getImportPrice() : ied.getExportPrice();

				consumptionHistory.setPriceImportedOrExported(price);

				float cost = consumption * price;

				consumptionHistory.setCostImportedOrExported(cost);

			} else {

				consumptionHistory = historyMap.get(key);
			}

			consumptionHistory.setConsumption(consumption);

			// update the history map

			historyMap.put(key, consumptionHistory);

			if (blah && missingConsumptionHistory) {

				count++;

				if (1 == count) {

					System.err.println(
							"Adding more consumptionHistory data to the existing " + historyMap.size() + " records");
				}

				System.err.println(consumptionHistory.getConsumption() + ", " + consumptionHistory.getFrom().toString()
						+ ", " + consumptionHistory.getTo().toString() + ", "
						+ consumptionHistory.getPriceImportedOrExported() + ", "
						+ consumptionHistory.getCostImportedOrExported());
			}
		}

		if (0 != count) {

			System.err.println("");
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
