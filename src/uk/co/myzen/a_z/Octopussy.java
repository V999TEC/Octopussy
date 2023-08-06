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
import java.util.Base64;
import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author howard
 *
 */

public class Octopussy {

	private static Properties properties;

	private final static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36";
	private final static String contentType = "application/json";

	private final ObjectMapper mapper;

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
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	}

	/**
	 * @param args
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws MalformedURLException, IOException {
		// TODO Auto-generated method stub

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

				// properties.setProperty("apiKey", keyValue);
			}
		}

		properties.setProperty("basic", "Basic " + Base64.getEncoder().encodeToString(keyValue.getBytes()));

		instance = getInstance();

		// expect {"count":0,"next":null,"previous":null,"results":[]}

		System.out.println(instance.getV1ElectricityConsumption());

		System.out.println(instance.getV1GasConsumption());

		System.out.println(instance.getV1AgileRates());

	}

	private static void loadProperties(String name) throws IOException {

		properties = new Properties();

		ClassLoader cl = Thread.currentThread().getContextClassLoader();

		InputStream is = cl.getResourceAsStream(name);

		properties.load(is);

	}

	private String getV1ElectricityConsumption() throws MalformedURLException, IOException {

		String mprn = properties.getProperty("electricity.mprn").trim();

		String sn = properties.getProperty("electricity.sn").trim();

		String result = instance.getRequest(new URL(
				"https://api.octopus.energy/v1/electricity-meter-points/" + mprn + "/meters/" + sn + "/consumption/"));

		return result;
	}

	private String getV1GasConsumption() throws MalformedURLException, IOException {

		String mprn = properties.getProperty("gas.mprn").trim();

		String sn = properties.getProperty("gas.sn").trim();

		String result = instance.getRequest(
				new URL("https://api.octopus.energy/v1/gas-meter-points/" + mprn + "/meters/" + sn + "/consumption/"));

		return result;
	}

	private String getV1AgileRates() throws MalformedURLException, IOException {

		String result = instance.getRequest(new URL(
				"https://api.octopus.energy/v1/products/AGILE-FLEX-22-11-25/electricity-tariffs/E-1R-AGILE-FLEX-22-11-25-H/standard-unit-rates/"),
				false);

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
