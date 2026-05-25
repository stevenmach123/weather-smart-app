package edu.uiuc.cs427app;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import edu.uiuc.cs427app.activities.mainpage.UserViewActivity;
import edu.uiuc.cs427app.data.database.entities.City;
import edu.uiuc.cs427app.data.database.entities.User;

@RunWith(AndroidJUnit4.class)
public class AddingCityTest {

    private GlobalApp app;
    private ActivityScenario<UserViewActivity> activity;

    @Before
    public void setUp() {
        app = (GlobalApp) getApplicationContext();
        User user = new User();
        user.uid = 123;
        user.username = "testUsername";
        app.currentUser = user;
    }

    @After
    public void tearDown() {
        app.db.cityDao().deleteAllCities();
        app.db.userDao().deleteAllUsers();
    }

    /**
     * Verifies valid city input adds exactly one persisted city for the test user.
     */
    @Test
    public void testAdditionOfNewCity() throws InterruptedException {
        activity = ActivityScenario.launch(UserViewActivity.class);

        onView(withId(R.id.addCityButton)).perform(click());

        onView(withId(R.id.cityInput)).perform(click(), typeText("San Francisco"), closeSoftKeyboard());
        onView(withId(R.id.stateInput)).perform(click(), typeText("CA"), closeSoftKeyboard());
        onView(withId(R.id.countryInput)).perform(click(), typeText("United States"), closeSoftKeyboard());

        onView(withId(R.id.confirmAddCityButton)).perform(click());

        Thread.sleep(5000);

        List<City> cities = app.db.cityDao().getCitiesForUser(123L);
        assertEquals(1, cities.size());

        City saved = cities.get(0);
        assertEquals(123L, saved.ownerUserId);
        assertNotNull(saved.state);
    }

    /**
     * Confirms invalid city does not insert a city row.
     */
    @Test
    public void testAddingWronglyFormattedCityName() throws InterruptedException {
        activity = ActivityScenario.launch(UserViewActivity.class);

        onView(withId(R.id.addCityButton)).perform(click());

        onView(withId(R.id.cityInput)).perform(typeText("NOT CITY"), closeSoftKeyboard());
        onView(withId(R.id.stateInput)).perform(typeText("TEST"), closeSoftKeyboard());
        onView(withId(R.id.countryInput)).perform(typeText("USA"), closeSoftKeyboard());

        onView(withId(R.id.confirmAddCityButton)).perform(click());

        Thread.sleep(5000);

        assertEquals(0, app.db.cityDao().getCitiesForUser(123L).size());
    }
}