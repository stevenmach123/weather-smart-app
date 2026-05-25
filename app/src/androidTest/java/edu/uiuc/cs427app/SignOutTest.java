package edu.uiuc.cs427app;

import edu.uiuc.cs427app.activities.authen.AuthenticateActivity;
import edu.uiuc.cs427app.activities.weather.WeatherActivity;
import edu.uiuc.cs427app.activities.map.MapActivity;
import edu.uiuc.cs427app.data.database.daos.UserDao;
import edu.uiuc.cs427app.data.database.entities.User;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class SignOutTest {

    private static final String USERNAME = "alice12";
    private static final String PASSWORD = "password123";
    private static final String CUSTOM_UI = "default";
    private static final long STEP_DELAY_MS = 1500;
    private UserDao userDao;
    private GlobalApp app;
    /**
     * Sets up test data before each test.
     * Inserts a test user into the database and assigns it
     * as the current user in the application state.
     */
    @Before
    public void registerTestUser() {
        app = ApplicationProvider.getApplicationContext();
        userDao = app.db.userDao();
        User user = new User();
        user.username = USERNAME;
        user.password = PASSWORD;
        user.customUi = CUSTOM_UI;
        user.backgroundColor = "#FFFFFF";
        user.textColor = "#000000";
        user.buttonColor = "#6820EE";
        userDao.insertUser(user);
        app.currentUser = user;
    }

    /**
     * Verifies that a signed-in user can log off successfully
     * from the AuthenticateActivity, returning to the sign-in UI
     * and clearing the current user state.
     */
    @Test
    public void testLogOffFromUserViewActivity() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), AuthenticateActivity.class);
        try(ActivityScenario<?> ignored =ActivityScenario.launch(i)){
            typeCredentials();
            onView(withId(R.id.submit_button)).perform(click());
            pauseBetweenSteps();
            verifyLogOff();
        }
    }

    /**
     * Verifies that logging off from MapActivity
     * returns the user to the sign-in screen and clears session state.
     */
    @Test
    public void testLogOffFromMapActivity() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), MapActivity.class);
        i.putExtra("username", USERNAME);
        try(ActivityScenario<?> ignored = ActivityScenario.launch(i)){
            pauseBetweenSteps();
            verifyLogOff();
        }
    }

    /**
     * Verifies that logging off from WeatherActivity
     * returns the user to the sign-in screen and clears session state.
     */
    @Test
    public void testLogOffFromWeatherActivity() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), WeatherActivity.class);
        i.putExtra("username", USERNAME);
        i.putExtra("cityName", "Champaign");
        i.putExtra("state", "Illinois");
        try(ActivityScenario<?> ignored = ActivityScenario.launch(i)){
            pauseBetweenSteps();
            verifyLogOff();
        }
    }
    /**
     * Performs the logoff action via the menu, and verifies
     * that the app navigates back to the sign-in screen
     * and clears the current user.
     */
    private void verifyLogOff() {
        openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
        onView(withText("LOG OFF")).perform(click());
        onView(withId(R.id.username_input))
                .check(matches(isDisplayed()));
        assertNull(app.currentUser);
        pauseBetweenSteps();
    }
    /**
     * Perform type input of username and password in signin layout.
     */
    private void typeCredentials() {
        onView(withId(R.id.username_input))
                .perform(clearText(), typeText(USERNAME), closeSoftKeyboard());
        onView(withId(R.id.password_input))
                .perform(clearText(), typeText(PASSWORD), closeSoftKeyboard());
    }
    /**
     * Pause between each action for view confirm.
     */
    private void pauseBetweenSteps() {
        try {
            Thread.sleep(STEP_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}