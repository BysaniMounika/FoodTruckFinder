import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Calendar;
import java.util.TimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FoodTruckFinder {
	final static String BASE_URL = "https://data.sfgov.org/resource/jjew-r69b.json";
	final static int LIMIT = 10;

	public static void main(String[] args) {
   		try {
   			Scanner userInput = new Scanner(System.in);
			String input;

			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("US/Pacific-New"));
			int dayOrder = calendar.get(Calendar.DAY_OF_WEEK);
			String time = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE));

			int page = 1;

			do {
				URL url = generateURL(dayOrder, time, page++);
				JSONArray trucks = getOpenTrucksInfo(url);
				printTrucksInfo(trucks);
				if (trucks.size() < LIMIT) {
					break;
				}

				System.out.print("Press y | Y to load info in next Page: ");
				input = userInput.next();
			} while (input.equals("y") || input.equals("Y"));

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static URL generateURL (int dayOrder, String time, int page) throws MalformedURLException {
		int offset = (page - 1) * LIMIT;
		String query = "SELECT applicant, location, start24, end24 WHERE dayorder = " + dayOrder + " AND start24 <= '" + time + "' AND end24 >= '" + time + "' ORDER BY applicant LIMIT 10 OFFSET " + offset;
		URL url = new URL(BASE_URL + "?$query=" + URLEncoder.encode(query, StandardCharsets.UTF_8));

		return url;
	}

	public static JSONArray getOpenTrucksInfo (URL url) throws IOException, ParseException {
		StringBuilder result = new StringBuilder();

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");

		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		rd.close();
		conn.disconnect();

		JSONParser parse = new JSONParser();
		JSONArray jsonArray;
		jsonArray = (JSONArray)parse.parse(result.toString());

		return jsonArray;
	}
	public static void printTrucksInfo (JSONArray trucks) {
		JSONObject jsonObject;
		int size = trucks.size();
		System.out.println("NAME - ADDRESS");
		for (int i = 0; i < size; ++ i) {
			jsonObject = (JSONObject)trucks.get(i);
			System.out.println(jsonObject.get("applicant") + " - " + jsonObject.get("location"));
		}
	}
}

//Compile - javac -cp .;./json_simple-1.1.jar FoodTruckFinder.java
//run - java -cp .;./json_simple-1.1.jar FoodTruckFinder