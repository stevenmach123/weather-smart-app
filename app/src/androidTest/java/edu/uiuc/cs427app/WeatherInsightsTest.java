package edu.uiuc.cs427app;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.content.Intent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import edu.uiuc.cs427app.activities.weather.WeatherActivity;
import edu.uiuc.cs427app.data.database.entities.ChatItem;
import edu.uiuc.cs427app.data.database.entities.User;
import edu.uiuc.cs427app.repositories.Adapter.ChatAdapter;
import edu.uiuc.cs427app.repositories.WeatherInfoRepository;

/**
 * Testing the “Weather Insight” feature (displaying LLM-generated questions and answers)
 * 1. Clicking the Weather Insights button opens the dialog and shows 2 questions.
 * 2. Clicking one of the generated questions displays an answer.
 */
@RunWith(AndroidJUnit4.class)
public class WeatherInsightsTest {

    private GlobalApp app;

    private static final String MockQuestion1 = "What should I wear today?";
    private static final String MockQuestion2 = "Should I bring an umbrella?";
    private static final String MockAnswer1 = "Test answer for: " + MockQuestion1;

    /**
     * Sets up a fake logged-in user and a mocked weather repository
     * so WeatherActivity can load successfully with deterministic weather data.
     */
    @Before
    public void setUp() throws Exception {
        app = (GlobalApp) getApplicationContext();

        app.db.cityDao().deleteAllCities();
        app.db.userDao().deleteAllUsers();

        User user = new User();
        user.uid = 1L;
        user.username = "testuser";
        user.buttonColor = "#6200EE";
        app.currentUser = user;

        MockWeatherInfoRepository mockRepo = new MockWeatherInfoRepository();
        JSONObject mockWeatherResponse = weatherResponse(10.0, 50.0, 70, 12.0, "Cloudy");
        mockRepo.setResponse(mockWeatherResponse);

        app.weatherInfoRepository = mockRepo;
    }

    /**
     * Cleans up database state after each test.
     */
    @After
    public void tearDown() {
        app.db.cityDao().deleteAllCities();
        app.db.userDao().deleteAllUsers();
    }

    /**
     * Verifies
     * 1. Clicking the Weather Insights button opens the dialog
     * and displays exactly 2 question options.
     *
     * 2. Clicking one Weather Insight question results in an answer
     * being displayed in the chat dialog.
     */
    @Test
    public void testWeatherInsightsDialogQuestionsAndAnswer() throws Exception {
        ActivityScenario<WeatherActivity> scenario = launchWeatherActivity();

        scenario.onActivity(activity -> {
            try {
                Field startChatField = WeatherActivity.class.getDeclaredField("startChat");
                startChatField.setAccessible(true);
                startChatField.set(activity, false);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(2000);

        onView(withId(R.id.weatherInsightsButton)).perform(click());

        // Assertion 1: dialog is visible
        onView(withId(R.id.btnClosePopup)).check(matches(isDisplayed()));

        scenario.onActivity(activity -> {
            try {
                Field chatItemListField = WeatherActivity.class.getDeclaredField("chatItemList");
                chatItemListField.setAccessible(true);
                List<ChatItem> chatItemList = (List<ChatItem>) chatItemListField.get(activity);

                Field recyclerViewField = WeatherActivity.class.getDeclaredField("recyclerView");
                recyclerViewField.setAccessible(true);
                RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(activity);

                ChatAdapter testAdapter = new ChatAdapter(chatItemList, new ChatAdapter.OnOptionClickListener() {
                    @Override
                    public void onOptionClick(String question) {
                        chatItemList.add(new ChatItem(
                                "ai",
                                Arrays.asList(""),
                                MockAnswer1
                        ));
                        activity.runOnUiThread(() -> recyclerView.getAdapter().notifyDataSetChanged());
                    }
                });

                recyclerView.setAdapter(testAdapter);

                chatItemList.clear();
                chatItemList.add(new ChatItem(
                        "user",
                        Arrays.asList(MockQuestion1, MockQuestion2),
                        ""
                ));

                activity.runOnUiThread(testAdapter::notifyDataSetChanged);
                activity.runOnUiThread(() -> recyclerView.scrollToPosition(chatItemList.size() - 1));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(2000);

        // Assertion 2: both questions are displayed
        onView(withText(MockQuestion1)).check(matches(isDisplayed()));
        onView(withText(MockQuestion2)).check(matches(isDisplayed()));

        // Assertion 3: exactly 2 question options were injected
        scenario.onActivity(activity -> {
            try {
                Field chatItemListField = WeatherActivity.class.getDeclaredField("chatItemList");
                chatItemListField.setAccessible(true);
                List<ChatItem> chatItemList = (List<ChatItem>) chatItemListField.get(activity);

                assertEquals(1, chatItemList.size());
                assertEquals(2, chatItemList.get(0).options.size());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        onView(withText(MockQuestion1)).perform(click());

        Thread.sleep(2000);

        // Assertion 4: answer is displayed after clicking a question
        onView(withText(MockAnswer1)).check(matches(isDisplayed()));
    }

    /**
     * Launches WeatherActivity with the city intent extras expected by the activity.
     *
     * @return launched ActivityScenario for WeatherActivity
     */
    private ActivityScenario<WeatherActivity> launchWeatherActivity() {
        Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
        intent.putExtra("cityName", "Chicago");
        intent.putExtra("cityState", "IL");
        intent.putExtra("cityCountry", "US");
        intent.putExtra("cityLatitude", 41.8781);
        intent.putExtra("cityLongitude", -87.6298);
        return ActivityScenario.launch(intent);
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