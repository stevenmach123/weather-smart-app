package edu.uiuc.cs427app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.Instrumentation;
import android.widget.TextView;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.uiuc.cs427app.activities.mainpage.UserViewActivity;
import edu.uiuc.cs427app.activities.weather.WeatherActivity;
import edu.uiuc.cs427app.data.database.entities.City;
import edu.uiuc.cs427app.data.database.entities.User;
import edu.uiuc.cs427app.repositories.WeatherInfoRepository;

@RunWith(AndroidJUnit4.class)
public class MockCityWeatherTest {

    private GlobalApp app;
    private City testCity;

    /**
     * Set up of the test cases, such as adding a new user and city to test with.
     */
    @Before
    public void setUp() {
        app = (GlobalApp) getApplicationContext();

        app.db.cityDao().deleteAllCities();
        app.db.userDao().deleteAllUsers();

        // Fake user login
        User user = new User();
        user.uid = 1L;
        user.username = "testuser";
        user.buttonColor = "#6200EE";

        app.currentUser = user;

        // Fake city
        testCity = new City();
        testCity.ownerUserId = user.uid;
        testCity.name = "Chicago";
        testCity.state = "IL";
        testCity.country = "US";
        testCity.latitude = 41.8781;
        testCity.longitude = -87.6298;

        app.db.cityDao().insertCity(testCity);

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    /**
     * Clean up of the test cases, such as cleaning the DB.
     */
    @After
    public void tearDown() {
        app.db.cityDao().deleteAllCities();
        app.db.userDao().deleteAllUsers();
    }

    /**
     * Tests a city with mocked weather values.
     * @throws Exception Any exception thrown.
     */
    @Test
    public void testMockCityWeatherValues() throws Exception {
        // Mock the weather info repository
        MockWeatherInfoRepository mockRepo = new MockWeatherInfoRepository();
        JSONObject mockWeatherResponse = weatherResponse(10.0, 50.0, 70, 12.0, "Cloudy");
        mockRepo.setResponse(mockWeatherResponse);

        app.weatherInfoRepository = mockRepo;

        // Get the assertion expected values
        JSONObject current = mockWeatherResponse.getJSONObject("current");
        double tempC = current.getDouble("temp_c");
        double tempF = current.getDouble("temp_f");
        int humidity = current.getInt("humidity");
        double windMph = current.getDouble("wind_mph");
        String condition = current.getJSONObject("condition").getString("text");

        ActivityScenario.launch(UserViewActivity.class);

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(WeatherActivity.class.getName(), null, false);

        // Navigate to the Weather screen
        onView(allOf(
                withText("Weather"),
                hasSibling(withText(testCity.name + ", " + testCity.state))
        )).perform(click());

        WeatherActivity weatherActivity = (WeatherActivity) monitor.waitForActivityWithTimeout(5000);
        assertNotNull("WeatherActivity did not launch", weatherActivity);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            TextView cityText = weatherActivity.findViewById(R.id.weatherCityText);
            TextView temperatureText = weatherActivity.findViewById(R.id.temperatureText);
            TextView weatherText = weatherActivity.findViewById(R.id.weatherText);
            TextView humidityText = weatherActivity.findViewById(R.id.humidityText);
            TextView windText = weatherActivity.findViewById(R.id.windText);

            assertEquals(
                    testCity.name + ", " + testCity.state,
                    cityText.getText().toString()
            );

            // Mock Weather Assertions
            assertEquals(
                    String.format("Temperature: %.1f°C / %.1f°F", tempC, tempF),
                    temperatureText.getText().toString()
            );
            assertEquals(
                    "Weather: " + condition,
                    weatherText.getText().toString()
            );
            assertEquals(
                    "Humidity: " + humidity + "%",
                    humidityText.getText().toString()
            );
            assertEquals(
                    String.format("Wind: %.1f mph", windMph),
                    windText.getText().toString()
            );
        });

        Thread.sleep(2000);
    }

    /**
     * Mock WeatherInfoRepository.
     */
    private static class MockWeatherInfoRepository extends WeatherInfoRepository {
        private JSONObject response;

        /**
         * Sets the mock response from a getWeather() call.
         * @param response The JSONObject response.
         */
        void setResponse(JSONObject response) {
            this.response = response;
        }

        /**
         * Returns a mocked response of the getWeather() call.
         * @param cityName is the name of the city
         * @param cityState is the state the city is located in
         * @param cityCountry is the country the city is located in
         * @param cityLatitude is the latitude of the city
         * @param cityLongitude is the longitude of the city
         * @return A mocked JSONObject.
         */
        @Override
        public JSONObject getWeather(String cityName, String cityState, String cityCountry,
                                     double cityLatitude, double cityLongitude) {
            return response;
        }
    }

    /**
     * The mocked weather response from the weather repository.
     * @param c Temperature in C
     * @param f Temperature in F
     * @param h Humidity
     * @param w Wind in MPH
     * @param cond Conditions
     * @return The mocked weather response.
     * @throws Exception Exception from JSONObject.
     */
    private static JSONObject weatherResponse(double c, double f, int h, double w, String cond) throws Exception {
        return new JSONObject()
                .put("current", new JSONObject()
                .put("temp_c", c)
                .put("temp_f", f)
                .put("humidity", h)
                .put("wind_mph", w)
                .put("condition", new JSONObject()
                .put("text", cond)));
    }
}