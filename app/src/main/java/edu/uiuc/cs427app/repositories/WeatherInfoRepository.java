package edu.uiuc.cs427app.repositories;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.uiuc.cs427app.BuildConfig;

/**
 * Repository class for fetching weather info from weather server.
 */

public class WeatherInfoRepository {


    /**
     * Constructs WeatherInfoRepository.
     */
    public WeatherInfoRepository() {}

    /**
     * getWeather fetches weather using city name, state, and
     * country. If city name is null, then it tries to fetch weather
     * info via latitude and longitude coordinate. Otherwise, it
     * throws an exception.
     *
     * @param cityName is the name of the city
     * @param state is the state the city is located in
     * @param country is the country the city is located in
     * @param latitude is the latitude of the city
     * @param longitude is the longitude of the city
     */
    public JSONObject getWeather(String cityName, String state, String country, double latitude, double longitude) throws Exception {
        JSONObject response = null;

        if (cityName != null && !cityName.isEmpty()) {
            String location = cityName;
            if (state != null && !state.isEmpty()) {
                location += "," + state;
            }
            if (country != null && !country.isEmpty()) {
                location += "," + country;
            }
            response = callWeatherAPI(location);
        }

        if (response == null && !Double.isNaN(latitude) && !Double.isNaN(longitude)) {
            Log.d("WeatherAPI", "Could not query by City Name");
            String coordQuery = String.format("%.4f,%.4f", latitude, longitude);
            response = callWeatherAPI(coordQuery);
        }

        if (response == null) {
            throw new Exception("Error getting weather by city name and coordinates");
        }

        return response;
    }

    /**
     * calWeatherAPI gets weather info from weather server by constructing
     * an url with the query and makes a http GET request to weatherapi.com.
     * It then retries the response as in json format.
     *
     * @param query is the city name or coordinate that is being queried
     */
    private JSONObject callWeatherAPI(String query) {
        HttpURLConnection connection = null;
        try {
            String url = String.format(
                    "https://api.weatherapi.com/v1/current.json?key=%s&q=%s&aqi=no",
                    BuildConfig.WEATHER_INFO_API_KEY, query
            );

            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("WeatherAPI", "Bad response");
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONObject json = new JSONObject(response.toString());
            Log.d("WeatherAPI", "Response for query " + query + ": " + json.toString(2));
            return json;

        } catch (Exception e) {
            Log.e("WeatherAPI", "Error fetching weather", e);
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}