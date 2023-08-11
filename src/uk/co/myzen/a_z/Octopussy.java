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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

				loadProperties("octopussy.properties");

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

			LocalDateTime startOfTomorrow = now.withDayOfYear(dayOfYear + 1).withHour(00).withMinute(0).withSecond(0)
					.withNano(0);

			LocalDateTime startOfYesterday = now.withDayOfYear(dayOfYear - 1).withHour(0).withMinute(0).withSecond(0)
					.withNano(0);

			LocalDateTime endOfYesterday = now.withDayOfYear(dayOfYear - 1).withHour(23).withMinute(30).withSecond(0)
					.withNano(0);

			V1AgileFlex v1AgileFlex = instance.getV1AgileFlex(300, startOfYesterday.toString(), null);

			ArrayList<Agile> agileResults = v1AgileFlex.getAgileResults();

			Map<LocalDateTime, Float> vatIncPriceMap = new HashMap<LocalDateTime, Float>();

			int tally = 0;

			String earliestAvailable = null;
			String latestAvailable = null;

			long epochEarliest = 0;
			long epochLatest = 0;

			long epochNow = now.toEpochSecond(ZoneOffset.UTC);

			for (Agile agile : agileResults) {

				String validFrom = agile.getValidFrom().substring(0, 19);

				LocalDateTime ldt = LocalDateTime.parse(validFrom, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

				long epochSecond = ldt.toEpochSecond(ZoneOffset.UTC);

				// LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(timeInSeconds, 0,
				// ZoneOffset.UTC);

				if (null == earliestAvailable || epochSecond < epochEarliest) {

					earliestAvailable = validFrom;
					epochEarliest = epochSecond;
				}

				if (null == latestAvailable || epochSecond > epochLatest) {

					latestAvailable = validFrom;
					epochLatest = epochSecond;

				}

//				System.out.println(tally + "\t" + ldt.toString());

				tally++;
				float valueIncVat = agile.getValueIncVat();

				vatIncPriceMap.put(ldt, Float.valueOf(valueIncVat));

				if (epochSecond > epochNow) {

					System.out.println(validFrom + "\t" + valueIncVat + "p");
				}
			}
//			System.out.println("Periods: " + tally + " available");
			System.out.println("earliest:" + earliestAvailable);
			System.out.println("latest:  " + latestAvailable);

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

			V1ElectricityConsumption v1ElectricityConsumption = instance.getV1ElectricityConsumption(null, null,
					startOfYesterday.toString(), startOfTomorrow.toString()); // 48 half hour time slots per
																				// day

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

				System.out.println("\t\t" + intervalStart + "\t" + halfHourPrice + "\t* " + consumption + "\t="
						+ halfHourCharge + " p");

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

			for (String key : elecMapDaily.keySet()) {

				DayValues dayValues = elecMapDaily.get(key);

				float consumption = dayValues.getDailyConsumption();

				float agilePrice = dayValues.getDailyPrice();

				float agileCharge = Float.valueOf(properties.getProperty("agile.electricity.standing"));

				float standardPrice = consumption * Float.valueOf(properties.getProperty("flexible.electricity.unit"));

				float standardCharge = Float.valueOf(properties.getProperty("flexible.electricity.standing"));

				float difference = (standardPrice + standardCharge) - (agilePrice + agileCharge);

				System.out.println(key + "\t" + consumption + " kWhr Agile:\t" + agilePrice + "p\t+" + agileCharge
						+ "p\t(Standard: " + standardPrice + "p\t+" + standardCharge + "p)\tdifference: " + difference
						+ "p");
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

	private static void loadProperties(String name) throws IOException {

		properties = new Properties();

		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		InputStream is = cl.getResourceAsStream(name);

		properties.load(is);

		// expand properties substituting $key$ values

		for (String key : properties.stringPropertyNames()) {

			String value = properties.getProperty(key);

			while (value.contains("$")) {

				int p = value.indexOf('$');

				int q = value.indexOf('$', 1 + p);

				String propertyKey = value.substring(1 + p, q);

				value = value.substring(0, p) + properties.getProperty(propertyKey) + value.substring(1 + q);

				System.err.println(key + "\t" + propertyKey + "\t" + value);

				properties.setProperty(key, value);
			}

		}

	}

	private V1ElectricityConsumption getV1ElectricityConsumption(Integer page, Integer pageSize, String periodFrom,
			String periodTo) throws MalformedURLException, IOException {

		System.out.println("from " + periodFrom + " to " + periodTo);

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
				+ (null == periodTo ? "" : "&period_to=" + periodTo) + "&order_by=period";

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
