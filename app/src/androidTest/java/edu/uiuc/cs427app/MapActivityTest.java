package edu.uiuc.cs427app;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import edu.uiuc.cs427app.activities.map.MapActivity;
import edu.uiuc.cs427app.data.database.entities.User;

/**
 * Test cases to validate the location feature (displaying correct city location information)
 * for two cities. This is achieved by parameterizing the tests.
 */
@RunWith(Parameterized.class)
public class MapActivityTest {

    private GlobalApp app;

    private final String cityName;
    private final String cityState;
    private final String cityCountry;
    private final double latitude;
    private final double longitude;

    /**
     * Constructor for the parameterized test class
     * @param cityName city name of the test input
     * @param cityState state of the test input
     * @param cityCountry country of the test input
     * @param latitude latitude of the test input
     * @param longitude longitude of the test input
     */
    public MapActivityTest(String cityName,
                           String cityState,
                           String cityCountry,
                           double latitude,
                           double longitude) {
        this.cityName = cityName;
        this.cityState = cityState;
        this.cityCountry = cityCountry;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Input for the parameterized test.
     * @return list of the inputs to test against (also contains the expected outputs).
     */
    @Parameters(name = "{index}: {0}")
    public static Collection<Object[]> testInput() {
        return Arrays.asList(new Object[][] {
                { "Champaign", "IL", "USA", 40.1164, -88.2434 },
                { "Chicago", "IL", "USA", 41.8781, -87.6298 }
        });
    }

    /**
     * Basic setup for the test cases
     */
    @Before
    public void setUp() {
        app = (GlobalApp) getApplicationContext();
        User user = new User();
        user.uid = 123;
        user.username = "testUser";
        app.currentUser = user;
    }

    /**
     * Verifies that the city information is displayed correctly based on the provided input.
     */
    @Test
    public void testCityDetailInformation() throws InterruptedException {
        Context context = getApplicationContext();
        Intent intent = new Intent(context, MapActivity.class);
        intent.putExtra("cityName", cityName);
        intent.putExtra("cityState", cityState);
        intent.putExtra("cityCountry", cityCountry);
        intent.putExtra("cityLatitude", latitude);
        intent.putExtra("cityLongitude", longitude);

        try (ActivityScenario<MapActivity> scenario = ActivityScenario.launch(intent)) {
            Thread.sleep(2500);
            // Validate City Name, State, and Country concatenated string
            String expectedTitle = String.format("%s, %s, %s", cityName, cityState, cityCountry);
            onView(withId(R.id.mapCityNameText))
                    .check(matches(withText(expectedTitle)));

            // Validate Latitude format (matching MapActivity's implementation)
            String expectedLatText = String.format(Locale.US, "Latitude: %f", latitude);
            onView(withId(R.id.mapLatitudeText))
                    .check(matches(withText(expectedLatText)));

            // Validate Longitude format (matching MapActivity's implementation)
            String expectedLongText = String.format(Locale.US, "Longitude: %f", longitude);
            onView(withId(R.id.mapLongitudeText))
                    .check(matches(withText(expectedLongText)));
        }
    }
}
