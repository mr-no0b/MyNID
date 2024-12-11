import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherFetcher {
    //d6f20afef12a1cf5f5d464b8368c26d6
    private static final String API_KEY = "d6f20afef12a1cf5f5d464b8368c26d6"; // Replace with your OpenWeather API key
    private static final String API_URL = "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s";

    public static String getTemperature(String city) {
        try {
            String urlString = String.format(API_URL, city, API_KEY);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            JSONObject json = new JSONObject(content.toString());
            double temp = json.getJSONObject("main").getDouble("temp");

            return String.format("%.1f°C", temp);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }
}