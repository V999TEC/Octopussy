/**
 * 
 */
package uk.co.myzen.a_z;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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

	private final static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36";
	private final static String contentType = "application/json";

	private final static DateTimeFormatter simpleTime = DateTimeFormatter.ofPattern("E MMM dd  HH:mm");

	private final static DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

	private static ZoneId ourZoneId;

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

		File importData = null;

		Scanner myReader = null;

		FileWriter fw = null;

		BufferedWriter bw = null;

		try {

			String keyValue = null;

			try {

				extra = loadProperties("octopussy.properties");

				keyValue = properties.getProperty("apiKey", "").trim();

				ourZoneId = ZoneId.of(properties.getProperty("zone.id", "Europe/London").trim());

				if (null == ourZoneId) {

					throw new Exception("zone.id");
				}

				importData = new File(properties.getProperty("history", ".\\octopus.import.csv").trim());

			} catch (Exception e) {

				e.printStackTrace();

				System.exit(-1);
			}

			Map<Long, ConsumptionHistory> history = new TreeMap<Long, ConsumptionHistory>();

			long epochFrom = 0;

			if (!importData.createNewFile()) {

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

			// epochFrom holds the latest timestamp from the data read from file
			// zero indicates no file/data

			if (null != myReader) {

				myReader.close();
			}

			myReader = null;

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

			LocalDateTime lowestPriceAt = null;

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

				long epochSecond = actual.atZone(ourZoneId).toEpochSecond();

				if (null == lowestPriceAt || (epochSecond > halfHourAgo
						&& valueIncVat < vatIncPriceMap.get(lowestPriceAt).getImportPrice())) {

					lowestPriceAt = actual;
				}
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

					dayValues.setSlotCount(1 + dayValues.getSlotCount());

					dayValues.setDailyConsumption(consumption + dayValues.getDailyConsumption());

					dayValues.setDailyPrice(halfHourCharge.floatValue() + dayValues.getDailyPrice());

				} else {

					dayValues = new DayValues();

					dayValues.setSlotCount(1);

					dayValues.setDailyConsumption(consumption);

					dayValues.setDailyPrice(Float.valueOf(halfHourCharge.floatValue()));
				}

				elecMapDaily.put(key, dayValues);
			}

			// now append to history file

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

			System.out.println("\nDaily result(s):");

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

				int slotCount = dayValues.getSlotCount();

				System.out.println("\t" + key + (48 == slotCount ? "\t" : " (" + String.format("%2d", slotCount) + ") ")
						+ String.format("%8.4f", consumption) + " kWhr\tAgile: " + String.format("%8.4f", agilePrice)
						+ "p +" + agileCharge + "p (Flat Rate: " + String.format("%8.4f", standardPrice) + "p +"
						+ standardCharge + "p)  difference: " + String.format("%8.4f", difference) + "p");

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

			System.out.println("\nOver " + countDays + " days, using " + accumulatePower
					+ " kWhr, Octopus Agile tariff has saved £" + pounds2DP + " compared to the " + flatRate
					+ "p (X) flat rate tariff");
			System.out.println("Average saving per day: £" + averagePounds2DP + " and average cost per unit (A): "
					+ averageCostPerUnit + "p  The average daily electricity usage is: " + averagePower + " kWhr\n");

			System.out.println("Current & future half-hour unit prices:");

			SortedSet<LocalDateTime> setOfLocalDateTime = new TreeSet<LocalDateTime>();

			setOfLocalDateTime.addAll(vatIncPriceMap.keySet());

			for (LocalDateTime slot : setOfLocalDateTime) {

				long epochSecond = slot.atZone(ourZoneId).toEpochSecond();

				ImportExportData importExportData = vatIncPriceMap.get(slot);

				float importValueIncVat = importExportData.getImportPrice();

				Float exportValueIncVat = importExportData.getExportPrice(); // can be null if export=false

				if (epochSecond >= halfHourAgo) {

					StringBuffer sb = new StringBuffer();

					if (importValueIncVat < plunge) {

						sb.append(" <--- PLUNGE BELOW " + plunge + "p !!!");

					} else {

						for (int n = 0; n < importValueIncVat; n++) {

							sb.append(target == n ? 'X' : (averageUnitCost == n ? 'A' : '*'));
						}
					}

					System.out.println((export ? String.format("%6.2f", exportValueIncVat) + "   " : "\t")
							+ slot.format(simpleTime) + "  " + (lowestPriceAt == slot ? "!" : " ")
							+ (importValueIncVat < averageUnitCost ? "!" : " ") + "\t"
							+ String.format("%7.4f", importValueIncVat) + "p\t" + sb.toString());
				}
			}

//			json = instance.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(v1ElectricityConsumption);
//
//			System.out.println(json);
//
//			json = instance.mapper.writerWithDefaultPrettyPrinter().writeValueAsString(v1AgileFlex);
//
//			System.out.println(json);

			System.exit(0);

		} catch (IOException e) {

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

				e.printStackTrace();
				System.exit(-2);
			}
		}

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

}
