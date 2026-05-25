package edu.uiuc.cs427app;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertEquals;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.containsString;

import java.util.List;

import edu.uiuc.cs427app.activities.mainpage.UserViewActivity;
import edu.uiuc.cs427app.data.database.entities.City;
import edu.uiuc.cs427app.data.database.entities.User;

@RunWith(AndroidJUnit4.class)
public class RemoveCityTest {

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
     * Asserts that removing a city clears it from DB and returns the empty-list UI.
     */
    @Test
    public void testRemovalOfNewCity() throws InterruptedException {
        activity = ActivityScenario.launch(UserViewActivity.class);

        onView(withId(R.id.addCityButton)).perform(click());

        onView(withId(R.id.cityInput)).perform(click(), typeText("San Francisco"), closeSoftKeyboard());
        onView(withId(R.id.stateInput)).perform(click(), typeText("CA"), closeSoftKeyboard());
        onView(withId(R.id.countryInput)).perform(click(), typeText("United States"), closeSoftKeyboard());

        onView(withId(R.id.confirmAddCityButton)).perform(click());

        Thread.sleep(3000);

        List<City> afterAdd = app.db.cityDao().getCitiesForUser(123L);
        assertEquals(1, afterAdd.size());
        City saved = afterAdd.get(0);

        onView(withContentDescription(containsString("Remove " + saved.name))).perform(click());
        Thread.sleep(2000);
        onView(withText("Confirm")).inRoot(isDialog()).perform(click());
        Thread.sleep(3000);

        assertEquals(0, app.db.cityDao().getCitiesForUser(123L).size());
        onView(withText("No saved cities yet.")).check(matches(isDisplayed()));
    }
}