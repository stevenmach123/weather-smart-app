package edu.uiuc.cs427app.activities.weather;

import static java.lang.String.*;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.Schema;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import edu.uiuc.cs427app.BuildConfig;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.uiuc.cs427app.GlobalApp;
import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.activities.authen.AuthenticateActivity;
import edu.uiuc.cs427app.data.database.entities.ChatItem;
import edu.uiuc.cs427app.data.remote.models.Weather;
import edu.uiuc.cs427app.repositories.Adapter.ChatAdapter;
import edu.uiuc.cs427app.repositories.WeatherInfoRepository;
import edu.uiuc.cs427app.theme.Theme;
import edu.uiuc.cs427app.theme.ThemeApplier;

/**
 * Activity for weather. Handles displaying weather info.
 */
public class WeatherActivity extends AppCompatActivity {
    GlobalApp glo;
    private WeatherInfoRepository weatherInfoRepository;
    private Weather cityWeather;
    private String cityName;
    private String cityState;
    private String cityCountry;
    private double cityLongitude;
    private double cityLatitude;
    private TextView dateTimeText;
    private TextView temperatureText;
    private TextView weatherText;
    private TextView humidityText;
    private TextView windText;
    private TextView cityText;
    private ImageView v ;

    private Button insightsButton;
    private Dialog chatInsight;
    private ChatAdapter chatAdapter;
    private List<ChatItem> chatItemList;
    private RecyclerView recyclerView;
    private boolean startChat = true;
    private final ExecutorService ex = Executors.newSingleThreadExecutor();
    /**
     * onCreate is called when the weather activity is created.
     * It displays initial weather info.
     *
     * @param savedInstanceState is the state saved in the app
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather);
        glo = (GlobalApp) getApplication();
        // show username with the team # as Team# - username as title
        if (glo.currentUser != null) {
            String teamNumber = getString(R.string.app_name);
            String title = teamNumber + " - " + glo.currentUser.username;
            setTitle(title);
        }

        // initialize repository + apply theme
        weatherInfoRepository = glo.weatherInfoRepository;
        Theme theme = Theme.fromUserOrDefault(glo.currentUser);
        ThemeApplier.applyTheme(this, theme);
        // Load city data + bind weather UI views
        loadCityFromIntent();
        cityText = findViewById(R.id.weatherCityText);
        dateTimeText = findViewById(R.id.weatherDateTimeText);
        temperatureText = findViewById(R.id.temperatureText);
        weatherText = findViewById(R.id.weatherText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);


        loadWeather(cityName, cityState, cityCountry, cityLatitude, cityLongitude);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize chat/insight data + button styling
        System.out.println("weather act "+"my button insight");
        chatItemList = new ArrayList<>();
        insightsButton = findViewById(R.id.weatherInsightsButton);
        int color = glo.currentUser != null && !glo.currentUser.buttonColor.isEmpty()  ? Color.parseColor(glo.currentUser.buttonColor): ContextCompat.getColor(this, R.color.purple_500);
        insightsButton.setBackgroundColor(color);
        insightsButton.setScaleX(1.8f);
        insightsButton.setScaleY(1.8f);


        // Setup dialog for chat insights UI
        chatInsight = new Dialog(WeatherActivity.this);
        chatInsight.requestWindowFeature(Window.FEATURE_NO_TITLE);
        chatInsight.setContentView(R.layout.insight);
        ImageButton btnClose =chatInsight.findViewById(R.id.btnClosePopup);
        System.out.println("weather act "+"my button insight2");
        chatInsight.setCancelable(true);


        // configure dialog window size, position, and dim effect
        Window window = chatInsight.getWindow();
        //WindowManager.LayoutParams lp_window = new WindowManager.LayoutParams();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels );
            lp.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.85);
            window.setGravity(Gravity.BOTTOM | Gravity.END);
            lp.dimAmount = 0.09f; // light shadow; default is ~0.5
            window.setAttributes(lp);

            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
        // Set click listener for close button and start chat question control
        btnClose.setOnClickListener((v)->{
                chatInsight.dismiss();
        });
        insightsButton.setOnClickListener(v->{
            chatInsight.show();
            if(startChat)
                generateWeatherInsightQuestions(cityText.getText().toString(), temperatureText.getText().toString(), weatherText.getText().toString(), humidityText.getText().toString(), windText.getText().toString());
            startChat =false;
        });

        System.out.println("weather act "+"insight 2");
        recyclerView = chatInsight.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(chatItemList, new ChatAdapter.OnOptionClickListener() {
            @Override
            public void onOptionClick(String question) {
                answerWeatherInsightQuestion(cityText.getText().toString(), temperatureText.getText().toString(), weatherText.getText().toString(), humidityText.getText().toString(), windText.getText().toString(), question);
            }
        });
        recyclerView.setAdapter(chatAdapter);
        System.out.println("weather act "+"insight end");

    }

    /**
     * loadCityFromIntent loads city data from user view activity.
     */
    private void loadCityFromIntent() {
        cityName = getIntent().getStringExtra("cityName");
        cityState = getIntent().getStringExtra("cityState");
        cityCountry = getIntent().getStringExtra("cityCountry");
        cityLongitude = getIntent().getDoubleExtra("cityLongitude", Double.NaN);
        cityLatitude = getIntent().getDoubleExtra("cityLatitude", Double.NaN);
    }


    /**
     * loadWeather gets the json response and parses it into fields
     * like temperature, weather condition, humidity, wind speed. It
     * then saves the info as an instance of Weather and modifies the
     * display to show weather info. If an error occurs, a message will
     * tell the user to go back to user view activity.
     *
     * @param cityName is the name of the city
     * @param cityState is the state the city is located in
     * @param cityCountry is the country the city is located in
     * @param cityLatitude is the latitude of the city
     * @param cityLongitude is the longitude of the city
     */
    private void loadWeather(String cityName, String cityState, String cityCountry, double cityLatitude, double cityLongitude) {
        new Thread(() -> {
            try {
                JSONObject weather = weatherInfoRepository.getWeather(cityName, cityState, cityCountry, cityLatitude, cityLongitude);
                JSONObject queryOutput = weather.getJSONObject("current");

                double temperatureC = queryOutput.getDouble("temp_c");
                double temperatureF = queryOutput.getDouble("temp_f");
                String condition = queryOutput.getJSONObject("condition").getString("text");
                double windMph = queryOutput.getDouble("wind_mph");
                double humidity = queryOutput.getInt("humidity");

                cityWeather = new Weather(java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()), temperatureC, temperatureF, condition, humidity, windMph);

                runOnUiThread(() -> {
                    dateTimeText.setText(java.text.DateFormat.getDateTimeInstance().format(new java.util.Date()));
                    cityText.setText(cityName+", "+cityState);
                    temperatureText.setText(String.format("Temperature: %.1f°C / %.1f°F", cityWeather.temperatureC, cityWeather.temperatureF));
                    weatherText.setText(String.format("Weather: %s", cityWeather.weatherDescription));
                    humidityText.setText(String.format("Humidity: %.0f%%", cityWeather.humidity));
                    windText.setText(String.format("Wind: %.1f mph", cityWeather.windCondition));
                });
            } catch (Exception e) {
                Log.e("WeatherAPI", "Error getting weather", e);
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Error")
                            .setMessage("Could not fetch weather at the moment. Please check your API Key.")
                            .setPositiveButton("Go Back", (d, w) -> {
                                d.dismiss();
                                finish();
                            })
                            .show();
                });
            }
        }).start();
    }

    /**
     * onOptionsItemSelected is used to switch between options
     * in menu. The user can go back to main page or log off.
     *
     * @param item is the option selected in the menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // go back to previous activity
            return true;
        } else if (item.getItemId() == R.id.logoff) {
            logOff(); // go back to authentication page
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * onCreateOptionsMenu loads items in the menu onto the
     * main page
     *
     * @param menu is the menu to be created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    /**
     * logOff removes the current activity of the authenticated user
     * and transitions the user back to the authentication activity.
     */
    private void logOff() {
        glo.currentUser = null;
        Intent intent = new Intent(WeatherActivity.this, AuthenticateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Asynchronously generates weather insight questions using Gemini based on
     * current weather data, then publishes the generated questions to the UI.
     *
     * Expected JSON response:
     * {
     *   "question1": "string",
     *   "question2": "string"
     * }
     *
     * @param cityName           name of the city
     * @param temperature        current temperature
     * @param weatherCondition   weather summary/condition
     * @param humidity           humidity value
     * @param windCondition      wind information
     */
    private void generateWeatherInsightQuestions(
            String cityName,
            String temperature,
            String weatherCondition,
            String humidity,
            String windCondition
    ) {
        System.out.println("generating weather insights");
        Schema<String> q1Schema = Schema.Companion.str("question1", "First weather-related question");
        Schema<String> q2Schema = Schema.Companion.str("question2", "Second weather-related question");
        Schema<?> questionSchema = Schema.Companion.obj(
                "weatherQuestions",
                "Weather insight questions",
                q1Schema,
                q2Schema
        );

        GenerationConfig.Builder configBuilder = GenerationConfig.Companion.builder();
        configBuilder.responseMimeType = "application/json";
        configBuilder.responseSchema = questionSchema;

        GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash-lite",
                BuildConfig.GEMINI_API_KEY,
                configBuilder.build()
        );
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        String prompt =
                "Based on this weather data, generate exactly 2 short, practical questions a user might ask.\n\n" +
                        "City: " + cityName + "\n" +
                        temperature + "\n" +
                        weatherCondition + "\n" +
                        humidity + "\n" +
                        windCondition + "\n\n" +
                        "Make the questions useful for planning, clothing, outdoor activities, commuting, health, or preparation.";

        System.out.println("prompt: " + prompt);
        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        /**
         * Handles the asynchronous Gemini weather-question-generation response.
         *
         * This callback attempts to parse the structured JSON returned by the model,
         * extract the 2 generated questions and add them to the chat item list.
         */
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            /**
             * Called when the Gemini API request completes successfully.
             *
             * @param result the successful Gemini content generation response
             */
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    System.out.println("Weather question response: " + result.getText());

                    org.json.JSONObject json = new org.json.JSONObject(result.getText());
                    String question1 = json.getString("question1").trim();
                    String question2 = json.getString("question2").trim();

                    runOnUiThread(() -> {
                        chatItemList.add(new ChatItem(
                                "user",
                                Arrays.asList(question1, question2),
                                ""
                        ));
                        recyclerView.scrollToPosition(chatItemList.size() - 1);
                    });

                } catch (Exception e) {
                    Log.e("GEMINI API", "Error getting questions", e);
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(WeatherActivity.this)
                                .setTitle("Error")
                                .setMessage("Could not fetch weather questions at the moment. " + e.toString().split("\\.")[0])
                                .setPositiveButton("Retry", (d, w) -> {
                                    d.dismiss();
                                    generateWeatherInsightQuestions(cityText.getText().toString(), temperatureText.getText().toString(), weatherText.getText().toString(), humidityText.getText().toString(), windText.getText().toString());
                                })
                                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                                .show();
                    });
                }
            }

            /**
             * Called when the Gemini API request fails.
             *
             * @param t the error thrown during the Gemini API request
             */
            @Override
            public void onFailure(Throwable t) {
                System.out.println("Weather insight question generation failed: " + t.getMessage());
                t.printStackTrace();

                runOnUiThread(() -> {
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(WeatherActivity.this)
                                .setTitle("Error")
                                .setMessage("Could not fetch weather questions at the moment. " + t.getMessage().split("\\.")[0])
                                .setPositiveButton("Retry", (d, w) -> {
                                    d.dismiss();
                                    generateWeatherInsightQuestions(cityText.getText().toString(), temperatureText.getText().toString(), weatherText.getText().toString(), humidityText.getText().toString(), windText.getText().toString());
                                })
                                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                                .show();
                    });
                });
            }
        }, Executors.newSingleThreadExecutor());
    }


    /**
     * Asynchronously generates an answer for a selected weather insight question
     * using the current weather data and the user's chosen question.
     *
     * Expected JSON response:
     * {
     *   "answer": "string"
     * }
     *
     * @param cityName           name of the city
     * @param temperature        current temperature
     * @param weatherCondition   weather summary/condition
     * @param humidity           humidity value
     * @param windCondition      wind information
     * @param question           user-selected question
     */
    private void answerWeatherInsightQuestion(
            String cityName,
            String temperature,
            String weatherCondition,
            String humidity,
            String windCondition,
            String question
    ) {
        System.out.println("generating answer");
        Schema<String> answerSchema =
                Schema.Companion.str("answer", "Answer to the selected weather question");

        Schema<?> responseSchema =
                Schema.Companion.obj("weatherAnswer", "Weather insight answer", answerSchema);

        GenerationConfig.Builder configBuilder = GenerationConfig.Companion.builder();
        configBuilder.responseMimeType = "application/json";
        configBuilder.responseSchema = responseSchema;

        GenerativeModel gm = new GenerativeModel(
                "gemini-2.5-flash-lite",
                BuildConfig.GEMINI_API_KEY,
                configBuilder.build()
        );
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        String prompt =
                "Answer this weather-related question based on the weather data.\n\n" +
                        "City: " + cityName + "\n" +
                        temperature + "\n" +
                        weatherCondition + "\n" +
                        humidity + "\n" +
                        windCondition + "\n\n" +
                        "Question: " + question + "\n\n" +
                        "Give a short, practical answer.";

        System.out.println("prompt: " + prompt);
        Content content = new Content.Builder()
                .addText(prompt)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        /**
         * Handles the asynchronous Gemini weather-answer-generation response.
         *
         * This callback attempts to parse the structured JSON returned by the model,
         * extract the generated answer, add it to the chat item list, and call
         * generateWeatherInsightQuestions to generate 2 more questions.
         */
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            /**
             * Called when the Gemini API request completes successfully.
             *
             * @param result the successful Gemini content generation response
             */
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    org.json.JSONObject json = new org.json.JSONObject(result.getText());
                    String answer = json.getString("answer").trim();

                    runOnUiThread(() -> {
                        chatItemList.add(new ChatItem("ai", java.util.Arrays.asList(""), answer));
                        generateWeatherInsightQuestions(cityText.getText().toString(), temperatureText.getText().toString(), weatherText.getText().toString(), humidityText.getText().toString(), windText.getText().toString());
                        recyclerView.scrollToPosition(chatItemList.size() - 1);
                    });

                } catch (Exception e) {
                    Log.e("GEMINI API", "Error getting answer", e);
                    runOnUiThread(() -> {
                        new AlertDialog.Builder(WeatherActivity.this)
                                .setTitle("Error")
                                .setMessage("Could not fetch an answer at the moment. " + e.toString().split("\\.")[0])
                                .setPositiveButton("Retry", (d, w) -> {
                                    d.dismiss();
                                    answerWeatherInsightQuestion(cityText.getText().toString(), temperatureText.getText().toString(), weatherText.getText().toString(), humidityText.getText().toString(), windText.getText().toString(), question);
                                })
                                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                                .show();
                    });
                }
            }

            /**
             * Called when the Gemini API request fails.
             *
             * @param t the error thrown during the Gemini API request
             */
            @Override
            public void onFailure(Throwable t) {
                System.out.println("Weather answer generation failed: " + t.getMessage());
                t.printStackTrace();

                runOnUiThread(() -> {
                    new AlertDialog.Builder(WeatherActivity.this)
                            .setTitle("Error")
                            .setMessage("Could not fetch an answer at the moment. " + t.getMessage().split("\\.")[0])
                            .setPositiveButton("Retry", (d, w) -> {
                                d.dismiss();
                                answerWeatherInsightQuestion(cityText.getText().toString(), temperatureText.getText().toString(), weatherText.getText().toString(), humidityText.getText().toString(), windText.getText().toString(), question);
                            })
                            .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                            .show();
                });
            }
        }, Executors.newSingleThreadExecutor());
    }
}