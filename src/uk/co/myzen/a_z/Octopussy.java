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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
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

	private final static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36";
	private final static String contentType = "application/json";

	private final static DateTimeFormatter simpleTime = DateTimeFormatter.ofPattern("E MMM dd  HH:mm");

	private static ObjectMapper mapper;

	private static boolean extra = false; // overriden by extra=true|false in properties

	private static Properties properties;

	private static Octopussy instance = null;

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
	}

	private Octopussy() {
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

				extra = loadProperties("octopussy.properties");

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

			instance = getInstance();

			properties.setProperty("basic", "Basic " + Base64.getEncoder().encodeToString(keyValue.getBytes()));

			// calculate date for for 00:00 'yesterday'

			LocalDateTime now = LocalDateTime.now();

			int dayOfYear = now.getDayOfYear();

			int howManyDaysHistory = Integer.valueOf(properties.getProperty("days", "2").trim());

			LocalDateTime startOfPreviousDays = now.withDayOfYear(dayOfYear - howManyDaysHistory).withHour(0)
					.withMinute(0).withSecond(0).withNano(0);

			LocalDateTime startOfToday = now.withDayOfYear(dayOfYear).withHour(0).withMinute(0).withSecond(0)
					.withNano(0);

			V1AgileFlex v1AgileFlex = instance.getV1AgileFlex(300, startOfPreviousDays.toString(), null);

			if (extra) {

				System.out.println("\nPeriods of unit price data obtained: " + v1AgileFlex.getCount());
			}

			ArrayList<Agile> agileResults = v1AgileFlex.getAgileResults();

			Map<LocalDateTime, Float> vatIncPriceMap = new HashMap<LocalDateTime, Float>();

			ZoneId ourZoneId = ZoneId.of("Europe/London");

			long epochNow = now.atZone(ourZoneId).toEpochSecond();

			long halfHourAgo = epochNow - 1800;

			String today = LocalDateTime.ofInstant(Instant.ofEpochSecond(epochNow), ourZoneId).toString().substring(0,
					10);

			int plunge = Integer.valueOf(properties.getProperty("plunge", "0").trim()).intValue();
			int target = Integer.valueOf(properties.getProperty("target", "30").trim()).intValue();

			for (Agile agile : agileResults) {

				String validFrom = agile.getValidFrom().substring(0, 19);

				LocalDateTime ldt = LocalDateTime.parse(validFrom, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

				// assume time obtained is zulu/UTC - need to convert to our local time (such as
				// BST)

				ZonedDateTime instant = ZonedDateTime.of(ldt, ZoneId.of("UTC"));
				LocalDateTime actual = instant.withZoneSameInstant(ourZoneId).toLocalDateTime();

				float valueIncVat = agile.getValueIncVat();

				vatIncPriceMap.put(actual, Float.valueOf(valueIncVat));
			}

			V1ElectricityConsumption v1ElectricityConsumption = instance.getV1ElectricityConsumption(null,
					48 * howManyDaysHistory, startOfPreviousDays.toString(), startOfToday.toString()); // 48
																										// half
																										// hour
																										// time
																										// slots
																										// per
																										// day

			if (extra) {

				System.out.println(
						"\nPeriods of previous consumption data obtained: " + v1ElectricityConsumption.getCount()

								+ "\tExpected at least: " + 48 * howManyDaysHistory + " ( = " + howManyDaysHistory
								+ " day(s) )");
			}

//			System.out.println("next:" + v1ElectricityConsumption.getNext());

			Map<String, DayValues> elecMapDaily = new HashMap<String, DayValues>();

			for (V1PeriodConsumption v1PeriodConsumption : v1ElectricityConsumption.getPeriodResults()) {

				Float consumption = v1PeriodConsumption.getConsumption();

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

				Float halfHourPrice = vatIncPriceMap.get(ldt);

				if (null == halfHourPrice) {

					elecMapDaily.put(key, null); // flag this day as incomplete - ignore all time slots on this day

					continue;
				}

				Float halfHourCharge = consumption * halfHourPrice;

				if (extra) {

					System.out.println("\t" + intervalStart + "\t" + halfHourPrice + "\t* " + consumption + "\t="
							+ String.format("%10.6f", halfHourCharge) + " p");
				}

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
			float accumulateCost = 0;

			for (String key : elecMapDaily.keySet()) {

				// ignore today because consumption data will not yet be fully complete

				if (0 == today.compareTo(key)) {

					continue;
				}

				DayValues dayValues = elecMapDaily.get(key);

				// ignore other incomplete days

				if (null == dayValues) {

					continue;
				}

				countDays++;

				float consumption = dayValues.getDailyConsumption();

				float agilePrice = dayValues.getDailyPrice();

				float agileCharge = Float.valueOf(properties.getProperty("agile.electricity.standing"));

				float standardPrice = consumption * Float.valueOf(properties.getProperty("flexible.electricity.unit"));

				float standardCharge = Float.valueOf(properties.getProperty("flexible.electricity.standing"));

				float difference = (standardPrice + standardCharge) - (agilePrice + agileCharge);

				System.out.println("\t" + key + "\t" + String.format("%8.4f", consumption) + " kWhr\tAgile: "
						+ String.format("%8.4f", agilePrice) + "p +" + agileCharge + "p (Standard: "
						+ String.format("%8.4f", standardPrice) + "p +" + standardCharge + "p)  difference: "
						+ String.format("%8.4f", difference) + "p");

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

			System.out.println("\nOver the last " + countDays + " days, using " + accumulatePower
					+ " kWhr, Octopus Agile tariff has saved £" + pounds2DP
					+ " compared to the standard flat rate tariff.");
			System.out.println("Average saving per day: £" + averagePounds2DP + " and average cost per unit (A): "
					+ averageCostPerUnit + "p  The average daily electricity usage is: " + averagePower + " kWhr\n");

			System.out.println("Current & future half-hour unit prices:");

			SortedSet<LocalDateTime> setOfLocalDateTime = new TreeSet<LocalDateTime>();

			setOfLocalDateTime.addAll(vatIncPriceMap.keySet());

			for (LocalDateTime slot : setOfLocalDateTime) {

				long epochSecond = slot.atZone(ourZoneId).toEpochSecond();

				float valueIncVat = vatIncPriceMap.get(slot);

				if (epochSecond >= halfHourAgo) {

					StringBuffer sb = new StringBuffer();

					if (valueIncVat < plunge) {

						sb.append(" <--- PLUNGE BELOW " + plunge + "p !!! - use as much energy as you want!");

					} else {

						for (int n = 0; n < valueIncVat; n++) {

							sb.append(target == n ? 'X' : (averageUnitCost == n ? 'A' : '*'));
						}
					}

					System.out.println(
							"\t" + slot.format(simpleTime) + "   " + (valueIncVat < averageUnitCost ? "!" : " ") + "\t"
									+ String.format("%7.4f", valueIncVat) + "p\t" + sb.toString());
				}
			}

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

		boolean extra = Boolean.valueOf(properties.getProperty("extra", "false").trim());

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

		return extra;

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

}
