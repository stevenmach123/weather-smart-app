package edu.uiuc.cs427app;

import edu.uiuc.cs427app.activities.authen.AuthenticateActivity;
import edu.uiuc.cs427app.activities.mainpage.UserViewActivity;
import edu.uiuc.cs427app.data.database.daos.UserDao;
import edu.uiuc.cs427app.data.database.entities.User;

import android.view.WindowManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/**
 * Minimal Espresso test suite for AuthenticateActivity sign-in.
 *
 * Covers the four scenarios that prove sign-in works end-to-end:
 *   1. Screen renders the required inputs.
 *   2. Invalid credentials show an error notice.
 *   3. Valid credentials navigate to UserViewActivity.
 *   4. Register link navigates away from sign-in.
 *
 * Naming convention: {scenarioPerformed}_{expectedOutcome}
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SignInTest {

    private static final String VALID_USERNAME = "alice12";
    private static final String VALID_PASSWORD = "password123";
    private static final String VALID_CUSTOM_UI = "default";

    private static final long STEP_DELAY_MS = 1500;
    private static final long ASYNC_DELAY_MS = 800;
    private GlobalApp app;

    @Rule
    public ActivityScenarioRule<AuthenticateActivity> activityRule =
            new ActivityScenarioRule<>(AuthenticateActivity.class);

    private static boolean userSeeded = false;

    /**
     * Seed the test user once. Runs as @Before because glo.db is initialized by
     * the activity launch, which ActivityScenarioRule triggers before @Before runs.
     */
    @Before
    public void registerTestUser() throws Exception {
        if (userSeeded) return;
        GlobalApp glo = (GlobalApp) ApplicationProvider.getApplicationContext();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            UserDao userDao = glo.db.userDao();
            if (userDao.getUserByUserNameAndPassword(VALID_USERNAME, VALID_PASSWORD) == null) {
                User user = new User();
                user.username = VALID_USERNAME;
                user.password = VALID_PASSWORD;
                user.customUi = VALID_CUSTOM_UI;
                user.backgroundColor = "#FFFFFF";
                user.textColor = "#000000";
                user.buttonColor = "#6820EE";
                userDao.insertUser(user);
            }
        }).get(5, TimeUnit.SECONDS);
        executor.shutdown();
        userSeeded = true;
    }

    /**
     * A helper method that sets up the authentication activity.
     */
    @Before
    public void setUp() {
        Intents.init();
        app = (GlobalApp) getApplicationContext();
        // Force the activity awake and past the keyguard so Espresso doesn't time
        // out waiting for window focus on emulators that auto-lock.
        activityRule.getScenario().onActivity(activity ->
                activity.getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD));
        pauseBetweenSteps();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Checks and asserts that the authentication page renders
     * correctly.
     */
    @Test
    public void signInScreenLaunched_displaysAllInputFields() {
        // Action: focus the username input to confirm interaction is possible.
        onView(withId(R.id.username_input)).perform(click());
        pauseBetweenSteps();

        // Assertions: every input on signin.xml is on screen.
        onView(withId(R.id.username_input)).check(matches(isDisplayed()));
        pauseBetweenSteps();
        onView(withId(R.id.password_input)).check(matches(isDisplayed()));
        pauseBetweenSteps();
        onView(withId(R.id.submit_button)).check(matches(isDisplayed()));
        pauseBetweenSteps();
        onView(withId(R.id.textRegister)).check(matches(isDisplayed()));
    }

    /**
     * Checks and asserts that user that is not authenticated
     * may not sign in.
     */
    @Test
    public void invalidCredentialsSubmitted_showsInvalidCredentialsNotice() {
        // Action: enter unregistered creds and submit.
        typeCredentials("nobody11", "wrongPass123");
        pauseBetweenSteps();
        onView(withId(R.id.submit_button)).perform(click());
        waitForAsync();

        // Assertions: notice frame becomes visible with the error text.
        onView(withId(R.id.notice_frame))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
        pauseBetweenSteps();
        onView(withId(R.id.notice_text))
                .check(matches(withText(containsString("Invalid username or password"))));
    }

    /**
     * Checks and asserts that an authenticated user
     * can sign in.
     */
    @Test
    public void validCredentialsSubmitted_navigatesToUserViewActivity() {
        // Action.
        typeCredentials(VALID_USERNAME, VALID_PASSWORD);
        pauseBetweenSteps();
        onView(withId(R.id.submit_button)).perform(click());
        waitForAsync();

        // Assertions: launched UserViewActivity with the seeded user's name.
        intended(IntentMatchers.hasComponent(UserViewActivity.class.getName()));
        pauseBetweenSteps();
        intended(IntentMatchers.hasExtra("username", VALID_USERNAME));
    }

    /**
     * Checks and asserts that the user may navigate to
     * the sign up page when clicking "Account Register".
     */
    @Test
    public void registerLinkClicked_navigatesAwayFromSignInScreen() {
        // Action: tap the "Account Register" text.
        onView(withId(R.id.textRegister)).perform(click());
        pauseBetweenSteps();

        // Assertion: signin.xml-specific labels are no longer on screen.
        onView(withText("Sign in")).check(doesNotExist());
        pauseBetweenSteps();
        onView(withText("Account Register")).check(doesNotExist());
    }

    /**
     * A helper method that inputs username and password
     * on the sign in page.
     */
    private void typeCredentials(String username, String password) {
        onView(withId(R.id.username_input))
                .perform(clearText(), typeText(username), closeSoftKeyboard());
        pauseBetweenSteps();
        onView(withId(R.id.password_input))
                .perform(clearText(), typeText(password), closeSoftKeyboard());
    }

    /** Short pause to let the UI settle between sequential steps. */
    private void pauseBetweenSteps() {
        try {
            Thread.sleep(STEP_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Longer pause after submit. SignIn() runs on an ExecutorService and posts
     * back via LiveData, so Espresso's main-thread sync doesn't cover it.
     */
    private void waitForAsync() {
        try {
            Thread.sleep(ASYNC_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}