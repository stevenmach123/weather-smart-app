package edu.uiuc.cs427app;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.uiuc.cs427app.activities.mainpage.UserViewActivity;
import edu.uiuc.cs427app.data.database.entities.City;
import edu.uiuc.cs427app.data.database.entities.User;

/**
 * Tests for up-to-date weather display for two cities (Chicago and Los Angeles),
 */
@RunWith(AndroidJUnit4.class)
public class WeatherTest {
    private static final int WEATHER_LOAD_WAIT = 5000;
    private GlobalApp app;
    private User testUser;

    /**
     * Sets up user session and inserts two test cities
     * (Chicago and Los Angeles) into the database.
     */
    @Before
    public void setUp() {
        app = (GlobalApp) getApplicationContext();

        app.db.cityDao().deleteAllCities();
        app.db.userDao().deleteAllUsers();

        testUser = new User();
        testUser.uid = 1L;
        testUser.username = "testuser";
        testUser.buttonColor = "#6200EE";

        app.currentUser = testUser;

        City chicago = new City();
        chicago.ownerUserId = testUser.uid;
        chicago.name = "Chicago";
        chicago.state = "IL";
        chicago.country = "US";
        chicago.latitude = 41.8781;
        chicago.longitude = -87.6298;

        City losAngeles = new City();
        losAngeles.ownerUserId = testUser.uid;
        losAngeles.name = "Los Angeles";
        losAngeles.state = "CA";
        losAngeles.country = "US";
        losAngeles.latitude = 34.0522;
        losAngeles.longitude = -118.2437;

        app.db.cityDao().insertCity(chicago);
        app.db.cityDao().insertCity(losAngeles);

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    /**
     * Removes test data and clears the session after each test.
     */
    @After
    public void tearDown() {
        app.db.cityDao().deleteAllCities();
        app.db.userDao().deleteAllUsers();
        app.currentUser = null;
    }

    /**
     * Verifies live weather UI displayed successfully for Chicago.
     *
     * @throws InterruptedException if the wait for weather loading is interrupted
     */
    @Test
    public void testWeather_Chicago() throws InterruptedException {
        try (ActivityScenario<UserViewActivity> ignored = ActivityScenario.launch(UserViewActivity.class)) {

            onView(allOf(
                    withText("Weather"),
                    hasSibling(withText(containsString("Chicago")))
            )).perform(click());

            Thread.sleep(WEATHER_LOAD_WAIT);

            onView(withId(R.id.weatherCityText))
                    .check(matches(withText(containsString("Chicago"))));

            onView(withId(R.id.temperatureText))
                    .check(matches(allOf(
                            withText(containsString("Temperature:")),
                            withText(containsString("°")),
                            not(withText(""))
                    )));

            onView(withId(R.id.weatherText))
                    .check(matches(allOf(
                            withText(containsString("Weather:")),
                            not(withText("Weather:")))
                    ));

            onView(withId(R.id.humidityText))
                    .check(matches(allOf(
                            withText(containsString("Humidity:")),
                            not(withText("")))
                    ));

            onView(withId(R.id.windText))
                    .check(matches(allOf(
                            withText(containsString("Wind:")),
                            not(withText("")))
                    ));
        }
    }

    /**
     * Verifies Verifies live weather UI displayed successfully for Los Angeles.
     *
     * @throws InterruptedException if the wait for weather loading is interrupted
     */
    @Test
    public void testWeather_LosAngeles() throws InterruptedException {
        try (ActivityScenario<UserViewActivity> ignored = ActivityScenario.launch(UserViewActivity.class)) {

            onView(allOf(
                    withText("Weather"),
                    hasSibling(withText(containsString("Los Angeles")))
            )).perform(click());

            Thread.sleep(WEATHER_LOAD_WAIT);

            onView(withId(R.id.weatherCityText))
                    .check(matches(withText(containsString("Los Angeles"))));

            onView(withId(R.id.temperatureText))
                    .check(matches(allOf(
                            withText(containsString("Temperature:")),
                            withText(containsString("°")),
                            not(withText(""))
                    )));

            onView(withId(R.id.weatherText))
                    .check(matches(allOf(
                            withText(containsString("Weather:")),
                            not(withText("Weather:")))
                    ));

            onView(withId(R.id.humidityText))
                    .check(matches(allOf(
                            withText(containsString("Humidity:")),
                            not(withText("")))
                    ));

            onView(withId(R.id.windText))
                    .check(matches(allOf(
                            withText(containsString("Wind:")),
                            not(withText("")))
                    ));
        }
    }
}