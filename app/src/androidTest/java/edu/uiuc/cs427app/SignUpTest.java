package edu.uiuc.cs427app;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Ordering;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import edu.uiuc.cs427app.activities.authen.AuthenticateActivity;
import edu.uiuc.cs427app.activities.authen.IdleEspresso;
import edu.uiuc.cs427app.activities.mainpage.UserViewActivity;
import edu.uiuc.cs427app.data.database.daos.UserDao;
import edu.uiuc.cs427app.data.database.entities.User;
import androidx.test.espresso.IdlingRegistry;
@RunWith(AndroidJUnit4.class)
public class SignUpTest {
    private GlobalApp app;
    private User common_user;
    private UserDao userDao;
    /**
     * Sets up the test environment before each test.
     * Initializes database, registers IdlingResource, and clears existing data.
     */
    @Before
    public void setUp() {
        app = (GlobalApp) getApplicationContext();
        userDao = app.db.userDao();
        common_user = new User(1L,"yvesy2","password123","sky","#FFFFFF","#000000","#6820EE");
        IdlingRegistry.getInstance().register(IdleEspresso.getIdlingResource());

        clearDatabase();
        waitForAsyncOperation();
    }
    /**
     * Cleans up after each test by unregistering the IdlingResource.
     */
    @After
    public void cleanUp(){
        IdlingRegistry.getInstance().unregister(IdleEspresso.getIdlingResource());

    }
    /**
     * Clears all users from the database to ensure a fresh test state
     */
    private void clearDatabase() {
        userDao.deleteAllUsers();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * Launches the AuthenticateActivity for each test case.
     */
    @Rule
    public ActivityScenarioRule<AuthenticateActivity> rule =
            new ActivityScenarioRule<>(AuthenticateActivity.class);
    /**
     * Navigates from the sign-in screen to the sign-up screen.
     */
    private void navigateToSignup() {
        onView(withId(R.id.textRegister)).perform(click());
        waitForAsyncOperation();
    }

    /**
     * Navigates from the sign-up screen back to the sign-in screen.
     */
    private void navigateToSignIn(){
        onView(withId(R.id.signinButton)).perform(click());
        waitForAsyncOperation();
    }
    /**
     * Provides a simple delay to allow asynchronous UI operations to finish.
     */
    private void  waitForAsyncOperation(){
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * Verifies that signup fails with invalid input,
     * and no user is inserted into the database.
     */
    @Test
    public void TEST_SIGNUP_WRONG_FORMAT() throws InterruptedException {
        // Navigate to signup page
        navigateToSignup();

        // Type invalid username (no number)
        onView(withId(R.id.inputUserName)).perform(typeText("john"), closeSoftKeyboard());
        // Type invalid password (less than 8 chars)
        onView(withId(R.id.inputPassword)).perform(typeText("pass12"), closeSoftKeyboard());
        // Type theme
        onView(withId(R.id.inputCustomUi)).perform(typeText("sky"), closeSoftKeyboard());

        // Click signup button
        onView(withId(R.id.signupButton)).perform(click());
        waitForAsyncOperation();

        // Verify UI still shows signup page (signinButton should be visible)
        onView(withId(R.id.signinButton)).check(matches(isDisplayed()));

        // Verify no user was inserted in DB
        User user = userDao.getUserByUserName("john");
        assertNull(user);
        waitForAsyncOperation();
        Thread.sleep(1000);
    }
    /**
     * Verifies successful signup with valid inputs
     * and confirms the user is stored correctly in the database.
     */
    @Test
    public void TEST_SIGNUP_SUCCESS() throws InterruptedException {
        // Navigate to signup page
        navigateToSignup();

        // Type valid username (letters + numbers)
        onView(withId(R.id.inputUserName)).perform(typeText(common_user.username), closeSoftKeyboard());
        // Type valid password (8+ chars)
        onView(withId(R.id.inputPassword)).perform(typeText(common_user.password), closeSoftKeyboard());
        // Type theme
        onView(withId(R.id.inputCustomUi)).perform(typeText(common_user.customUi), closeSoftKeyboard());

        // Click signup button
        onView(withId(R.id.signupButton)).perform(click());
        waitForAsyncOperation();
        // Verify user was inserted in DB
        User user = userDao.getUserByUserName(common_user.username);
        assertNotNull("User should be inserted in DB", user);
        assertEquals(common_user.username, user.getUsername());
        assertEquals(common_user.password, user.getPassword());
        waitForAsyncOperation();
        Thread.sleep(1000);
    }
    /**
     * Verifies that duplicate usernames are not allowed
     * and the original user data remains unchanged.
     */
    @Test
    public void TEST_SIGNUP_USERNAME_ALREADY_EXISTS() throws InterruptedException {
        // First signup - success

        navigateToSignup();
        onView(withId(R.id.inputUserName)).perform(typeText("alice456"), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(typeText("password123"), closeSoftKeyboard());
        onView(withId(R.id.inputCustomUi)).perform(typeText("beach"), closeSoftKeyboard());
        onView(withId(R.id.signupButton)).perform(click());
        // Second signup with same username


        System.out.println("navigate signup");
        waitForAsyncOperation();
        navigateToSignup();

        onView(withId(R.id.inputUserName)).perform(typeText("alice456"), closeSoftKeyboard());
        onView(withId(R.id.inputPassword)).perform(typeText("newpassword789"), closeSoftKeyboard());
        onView(withId(R.id.inputCustomUi)).perform(typeText("cyber"), closeSoftKeyboard());
        onView(withId(R.id.signupButton)).perform(click());
        waitForAsyncOperation();

        // Verify only one user exists in DB with original password
        User existingUser = userDao.getUserByUserName("alice456");
        assertNotNull("User should still exist", existingUser);
        assertEquals("password123", existingUser.getPassword()); // original password should be unchanged
        waitForAsyncOperation();
        Thread.sleep(1000);
    }
}
