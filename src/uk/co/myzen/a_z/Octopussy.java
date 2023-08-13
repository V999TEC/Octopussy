/**
 * 
 */
package uk.co.myzen.a_z;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import uk.co.myzen.a_z.json.Agile;
import uk.co.myzen.a_z.json.V1AgileFlex;
import uk.co.myzen.a_z.json.V1ElectricityConsumption;
import uk.co.myzen.a_z.json.V1GasConsumption;
import uk.co.myzen.a_z.json.V1PeriodConsumption;

/**
 * @author howard
 *
 */

public class Octopussy {

	private final static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36";
	private final static String contentType = "application/json";

	private final ObjectMapper mapper;

	private static boolean hide = false; // overriden by hide=value in properties

	private static Properties properties;

	private static Octopussy instance = null;

	private static synchronized Octopussy getInstance() {

		if (null == instance) {

			instance = new Octopussy();
		}

		return instance;
	}

	private Octopussy() {

		mapper = new ObjectMapper();

		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) {

		try {
			String keyValue = null;

			try {

				hide = loadProperties("octopussy.properties");

				keyValue = properties.getProperty("apiKey", "").trim();

			} catch (IOException e) {

				e.printStackTrace();
			}

			if (null == keyValue || 0 == keyValue.length()) {

				if (0 == args.length) {

					System.err.println(
							"Require apiKey=value in octopussy.properties or supply value as last parameter to octopussy.jar");
				} else {

					keyValue = args[args.length - 1].trim();
				}
			}

			properties.setProperty("basic", "Basic " + Base64.getEncoder().encodeToString(keyValue.getBytes()));

			instance = getInstance();

			// calculate date for for 00:00 'yesterday'

			LocalDateTime now = LocalDateTime.now();

			int dayOfYear = now.getDayOfYear();

			int howManyDaysHistory = Integer.valueOf(properties.getProperty("days", "2").trim());

			LocalDateTime startOfPreviousDays = now.withDayOfYear(dayOfYear - howManyDaysHistory).withHour(0)
					.withMinute(0).withSecond(0).withNano(0);

			LocalDateTime startOfToday = now.withDayOfYear(dayOfYear).withHour(0).withMinute(0).withSecond(0)
					.withNano(0);

//			LocalDateTime startOfTomorrow = now.withDayOfYear(dayOfYear + 1).withHour(00).withMinute(0).withSecond(0)
//					.withNano(0);

//			LocalDateTime startOfDayAfter = now.withDayOfYear(dayOfYear + 2).withHour(00).withMinute(0).withSecond(0)
//					.withNano(0);

			V1AgileFlex v1AgileFlex = instance.getV1AgileFlex(300, startOfPreviousDays.toString(), null);

			System.out.println("\nPeriods of unit price data obtained: " + v1AgileFlex.getCount());

			ArrayList<Agile> agileResults = v1AgileFlex.getAgileResults();

			Map<LocalDateTime, Float> vatIncPriceMap = new HashMap<LocalDateTime, Float>();

			String earliestAvailable = null;
			String latestAvailable = null;

			long epochEarliest = 0;
			long epochLatest = 0;

			ZoneId ourZoneId = ZoneId.of("Europe/London");

			long epochNow = now.atZone(ourZoneId).toEpochSecond();

			String today = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochNow), ourZoneId).toString().substring(0,
					10);

			System.out.println("\nFuture half-hour unit prices:");

			for (Agile agile : agileResults) {

				String validFrom = agile.getValidFrom().substring(0, 19);

				LocalDateTime ldt = LocalDateTime.parse(validFrom, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

				// assume time obtained is zulu/UTC - need to convert to our local time (such as
				// BST)

				ZonedDateTime instant = ZonedDateTime.of(ldt, ZoneId.of("UTC"));
				LocalDateTime actual = instant.withZoneSameInstant(ourZoneId).toLocalDateTime();

				long epochSecond = ldt.toEpochSecond(ZoneOffset.UTC);

				if (null == earliestAvailable || epochSecond < epochEarliest) {

					earliestAvailable = validFrom;
					epochEarliest = epochSecond;
				}

				if (null == latestAvailable || epochSecond > epochLatest) {

					latestAvailable = validFrom;
					epochLatest = epochSecond;
				}

				float valueIncVat = agile.getValueIncVat();
				float valueExcVat = agile.getValueExcVat();

				vatIncPriceMap.put(actual, Float.valueOf(valueIncVat));

				if (epochSecond >= epochNow) {

					StringBuffer sb = new StringBuffer();

					for (int n = 0; n < valueIncVat; n++) {

						sb.append('*');
					}

					System.out.println("\t" + actual + "\t(" + String.format("%7.4f", valueExcVat) + "p) "
							+ String.format("%7.4f", valueIncVat) + "p\t" + sb.toString());
				}
			}
//			System.out.println("Periods: " + tally + " available");
//			System.out.println("earliest:" + earliestAvailable);
//			System.out.println("latest:  " + latestAvailable);

			// display the known future half-hour prices between now and latest

			String json;

//			V1GasConsumption v1GasConsumption = instance.getV1GasConsumption("2023-08-09T00:00Z", "2023-08-10T23:30Z");
//
//			// print
//			// System.out.println(instance.mapper.writeValueAsString(v1GasConsumption));
//
//			// pretty print
//			String json = instance.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(v1GasConsumption);
//
//			System.out.println(json);

			V1ElectricityConsumption v1ElectricityConsumption = instance.getV1ElectricityConsumption(null,
					48 * howManyDaysHistory, startOfPreviousDays.toString(), startOfToday.toString()); // 48
																										// half
																										// hour
																										// time
																										// slots
																										// per
																										// day

			System.out.println("\nPeriods of previous consumption data obtained: " + v1ElectricityConsumption.getCount()
					+ "\tExpected at least: " + 48 * howManyDaysHistory + " ( = " + howManyDaysHistory + " day(s) )");

//			System.out.println("next:" + v1ElectricityConsumption.getNext());

			Map<String, DayValues> elecMapDaily = new HashMap<String, DayValues>();

			for (V1PeriodConsumption v1PeriodConsumption : v1ElectricityConsumption.getPeriodResults()) {

				Float consumption = v1PeriodConsumption.getConsumption();

				String intervalStart = v1PeriodConsumption.getIntervalStart().substring(0, 16);

				LocalDateTime ldt = LocalDateTime.parse(intervalStart);

				Float halfHourPrice = vatIncPriceMap.get(ldt);

				if (null == halfHourPrice) {

					System.err.println("Unknown unit price for half hour starting at " + intervalStart);

					System.exit(-1);

				}

				Float halfHourCharge = consumption * halfHourPrice;

				if (!hide) {

					System.out.println("\t" + intervalStart + "\t" + halfHourPrice + "\t* " + consumption + "\t="
							+ String.format("%10.6f", halfHourCharge) + " p");
				}

				String key = intervalStart.substring(0, 10);

				DayValues dayValues = null;

				if (elecMapDaily.containsKey(key)) {

					dayValues = elecMapDaily.get(key);

					dayValues.setDailyConsumption(consumption + dayValues.getDailyConsumption());

					dayValues.setDailyPrice(halfHourCharge.floatValue() + dayValues.getDailyPrice());

				} else {

					dayValues = new DayValues();

					dayValues.setDailyConsumption(consumption);

					dayValues.setDailyPrice(Float.valueOf(halfHourCharge.floatValue()));
				}

				elecMapDaily.put(key, dayValues);
			}

			System.out.println("\nDaily result(s):");

			int countDays = 0;

			float accumulateDifference = 0;
			float accumulatePower = 0;

			for (String key : elecMapDaily.keySet()) {

				// ignore today because consumption data will not yet be fully complete

				if (0 == today.compareTo(key)) {

					continue;
				}

				countDays++;

				DayValues dayValues = elecMapDaily.get(key);

				float consumption = dayValues.getDailyConsumption();

				float agilePrice = dayValues.getDailyPrice();

				float agileCharge = Float.valueOf(properties.getProperty("agile.electricity.standing"));

				float standardPrice = consumption * Float.valueOf(properties.getProperty("flexible.electricity.unit"));

				float standardCharge = Float.valueOf(properties.getProperty("flexible.electricity.standing"));

				float difference = (standardPrice + standardCharge) - (agilePrice + agileCharge);

				System.out.println("\t" + key + "\t" + String.format("%8.4f", consumption) + " kWhr\tAgile: "
						+ String.format("%8.4f", agilePrice) + "p +" + agileCharge + "p\t(Standard: "
						+ String.format("%8.4f", standardPrice) + "p +" + standardCharge + "p)\tdifference: "
						+ String.format("%8.4f", difference) + "p");

				accumulateDifference += difference;

				accumulatePower += consumption;
			}

			String pounds2DP = String.format("%.2f", accumulateDifference / 100);

			String averagePounds2DP = String.format("%.2f", accumulateDifference / 100 / countDays);

			System.out.println("\nOver the last " + countDays + " days, using " + accumulatePower
					+ " kWhr, Octopus Agile tariff has saved £" + pounds2DP
					+ " compared to the standard flat rate tariff. Average saving per day: £" + averagePounds2DP);

//			json = instance.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(v1ElectricityConsumption);
//
//			System.out.println(json);
//
//			json = instance.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(v1AgileFlex);
//
//			System.out.println(json);

		} catch (IOException e) {

			e.printStackTrace();

			System.exit(-1);
		}

		System.exit(0);
	}

	private static boolean loadProperties(String name) throws IOException {

		properties = new Properties();

		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		InputStream is = cl.getResourceAsStream(name);

		properties.load(is);

		// expand properties substituting $key$ values

		boolean hide = Boolean.valueOf(properties.getProperty("hide", "2").trim());

		for (String key : properties.stringPropertyNames()) {

			String value = properties.getProperty(key);

			while (value.contains("$")) {

				int p = value.indexOf('$');

				int q = value.indexOf('$', 1 + p);

				String propertyKey = value.substring(1 + p, q);

				value = value.substring(0, p) + properties.getProperty(propertyKey) + value.substring(1 + q);

				if (!hide) {

					System.err.println(key + "\t" + propertyKey + "\t" + value);
				}

				properties.setProperty(key, value);
			}

		}

		return hide;

	}

	private V1ElectricityConsumption getV1ElectricityConsumption(Integer page, Integer pageSize, String periodFrom,
			String periodTo) throws MalformedURLException, IOException {

//		System.out.println("from " + periodFrom + " to " + periodTo);

		String mprn = properties.getProperty("electricity.mprn").trim();

		String sn = properties.getProperty("electricity.sn").trim();

		String json = instance.getRequest(new URL(
				"https://api.octopus.energy/v1/electricity-meter-points/" + mprn + "/meters/" + sn + "/consumption/" +

						"?order_by=period" + (null == page ? "" : "&page=" + page)
						+ (null == pageSize ? "" : "&page_size=" + pageSize)
						+ (null == periodFrom ? "" : "&period_from=" + periodFrom)
						+ (null == periodTo ? "" : "&period_to=" + periodTo) + "&order_by=period"));

		V1ElectricityConsumption result = mapper.readValue(json, V1ElectricityConsumption.class);

		return result;
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

	private V1AgileFlex getV1AgileFlex(Integer pageSize, String periodFrom, String periodTo)
			throws MalformedURLException, IOException {

		String spec = properties.getProperty("tariff.url").trim() + "/standard-unit-rates/" + "?page_size=" + pageSize
				+ (null == periodFrom ? "" : "&period_from=" + periodFrom)
				+ (null == periodTo ? "" : "&period_to=" + periodTo);

		String json = instance.getRequest(new URL(spec), false);

		V1AgileFlex result = mapper.readValue(json, V1AgileFlex.class);

		return result;
	}

	private String getRequest(URL url) throws IOException {

		return getRequest(url, true);
	}

	private String getRequest(URL url, boolean authorisationRequired) throws IOException {

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

}
