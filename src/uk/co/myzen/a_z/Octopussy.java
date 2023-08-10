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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

	private final static DateTimeFormatter formatterIn = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ");
	private final static DateTimeFormatter formatterOut = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	private final static DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

	private final static DateTimeFormatter ISO_OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	private final static ZoneId zoneIdDefault = ZoneId.systemDefault();

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

			// expect {"count":0,"next":null,"previous":null,"results":[]}

			V1GasConsumption v1GasConsumption = instance.getV1GasConsumption();

			// print
			// System.out.println(instance.mapper.writeValueAsString(v1GasConsumption));

			// pretty print
			String json = instance.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(v1GasConsumption);

			System.out.println(json);

			V1ElectricityConsumption v1ElectricityConsumption = instance.getV1ElectricityConsumption();

			Map<String, DayValues> elecMapDaily = new HashMap<String, DayValues>();

			for (V1PeriodConsumption v1PeriodConsumption : v1ElectricityConsumption.getPeriodResults()) {

				Float consumption = v1PeriodConsumption.getConsumption();

				String intervalStart = v1PeriodConsumption.getIntervalStart();

//				String interalEnd = v1PeriodConsumption.getIntervalEnd();
////
////				LocalDateTime ldtStart = LocalDateTime.parse(interalStart, formatterIn);
////				LocalDateTime ldtEnd = LocalDateTime.parse(interalEnd, formatterIn);
//
//				// ISO_OFFSET_FORMATTER
//
//				LocalDateTime ldtStart = LocalDateTime.parse(interalStart, ISO_OFFSET_FORMATTER);
//				LocalDateTime ldtEnd = LocalDateTime.parse(interalEnd, ISO_OFFSET_FORMATTER);
//
//				ZoneOffset zoneOffsetStart = zoneIdDefault.getRules().getOffset(ldtStart);
//				ZoneOffset zoneOffsetEnd = zoneIdDefault.getRules().getOffset(ldtEnd);
//
//				long startEpochMilli = ldtStart.toInstant(zoneOffsetStart).toEpochMilli();
//				long endEpochMilli = ldtEnd.toInstant(zoneOffsetEnd).toEpochMilli();

				String key = intervalStart.substring(0, 10);

				DayValues dayValues = null;

				if (elecMapDaily.containsKey(key)) {

					dayValues = elecMapDaily.get(key);

					dayValues.setDailyConsumption(consumption + dayValues.getDailyConsumption());

//					long valueStart = dayValues.getStartEpochMilli();
//
//					if (startEpochMilli < valueStart) {
//
//						dayValues.setStartEpochMilli(startEpochMilli);
//					}
//
//					long valueEnd = dayValues.getEndEpochMilli();
//
//					if (endEpochMilli > valueEnd) {
//
//						dayValues.setEndEpochMilli(endEpochMilli);
//					}

				} else {

					dayValues = new DayValues();

					dayValues.setDailyConsumption(consumption);
//
//					dayValues.setStartEpochMilli(startEpochMilli);
//					dayValues.setEndEpochMilli(endEpochMilli);
				}

				elecMapDaily.put(key, dayValues);
			}

			for (String key : elecMapDaily.keySet()) {

				DayValues dayValues = elecMapDaily.get(key);

				float consumption = dayValues.getDailyConsumption();

//				LocalDateTime ldtS = Instant.ofEpochMilli(dayValues.getStartEpochMilli()).atZone(zoneIdDefault)
//						.toLocalDateTime();
//
//				LocalDateTime ldtE = Instant.ofEpochMilli(dayValues.getEndEpochMilli()).atZone(zoneIdDefault)
//						.toLocalDateTime();
//
//				String sTimestamp = ldtS.format(DateTimeFormatter.ISO_DATE_TIME);
//				String eTimestamp = ldtE.format(DateTimeFormatter.ISO_DATE_TIME);

				System.out.println(key + "\t" + consumption);

			}

			json = instance.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(v1ElectricityConsumption);

			System.out.println(json);

			V1AgileFlex v1AgileFlex = instance.getV1AgileFlex();

//			ArrayList<Agile> agileResults = v1AgileFlex.getAgileResults();

			json = instance.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(v1AgileFlex);

			System.out.println(json);

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

	}

	private V1ElectricityConsumption getV1ElectricityConsumption() throws MalformedURLException, IOException {

		String mprn = properties.getProperty("electricity.mprn").trim();

		String sn = properties.getProperty("electricity.sn").trim();

		String json = instance.getRequest(new URL(
				"https://api.octopus.energy/v1/electricity-meter-points/" + mprn + "/meters/" + sn + "/consumption/"));

		V1ElectricityConsumption result = mapper.readValue(json, V1ElectricityConsumption.class);

		return result;
	}

	private V1GasConsumption getV1GasConsumption() throws MalformedURLException, IOException {

		String mprn = properties.getProperty("gas.mprn").trim();

		String sn = properties.getProperty("gas.sn").trim();

		String json = instance.getRequest(
				new URL("https://api.octopus.energy/v1/gas-meter-points/" + mprn + "/meters/" + sn + "/consumption/"));

		V1GasConsumption result = mapper.readValue(json, V1GasConsumption.class);

		return result;
	}

	private V1AgileFlex getV1AgileFlex() throws MalformedURLException, IOException {

		String spec = properties.getProperty("agile").trim();

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
