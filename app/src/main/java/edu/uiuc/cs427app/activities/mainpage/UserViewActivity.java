package edu.uiuc.cs427app.activities.mainpage;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import edu.uiuc.cs427app.GlobalApp;
import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.activities.authen.AuthenticateActivity;
import edu.uiuc.cs427app.activities.weather.WeatherActivity;
import edu.uiuc.cs427app.activities.map.MapActivity;
import edu.uiuc.cs427app.data.database.entities.City;

import edu.uiuc.cs427app.data.database.entities.User;
import edu.uiuc.cs427app.repositories.CityRepository;
import edu.uiuc.cs427app.theme.Theme;
import edu.uiuc.cs427app.theme.ThemeApplier;
import edu.uiuc.cs427app.viewmodels.TestViewModel;

/**
 * Activity for displaying and managing the user's city list and related UI actions.
 * Handles city addition, removal,  and theme application for the logged-in user.
 */
public class UserViewActivity extends AppCompatActivity {
    GlobalApp glo;

    private CityRepository cityRepository;

    private LinearLayout cityListContainer;
    private LinearLayout addCitySection;
    private EditText cityInput;
    private EditText stateInput;
    private EditText countryInput;
    private ImageButton addCityButton;

    private Button confirmAddCityButton;

    /**
     * Called when the activity is first created. Initializes the activity
     * @param savedInstanceState The saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userview);
        glo = (GlobalApp) getApplication();
        Theme theme = Theme.fromUserOrDefault(glo.currentUser);

        cityRepository = new CityRepository(glo.db);

        addCityButton = findViewById(R.id.addCityButton);
        addCitySection = findViewById(R.id.addCitySection);
        cityInput = findViewById(R.id.cityInput);
        stateInput = findViewById(R.id.stateInput);
        countryInput = findViewById(R.id.countryInput);
        cityListContainer = findViewById(R.id.cityListContainer);
        confirmAddCityButton = findViewById(R.id.confirmAddCityButton);

        // show username with the team # as Team# - username as title
        if (glo.currentUser != null) {
            String teamNumber = getString(R.string.app_name);
            String title = teamNumber + " - " + glo.currentUser.username;
            setTitle(title);
        }

        // Apply theme to current static views
        ThemeApplier.applyTheme(this, theme);

        addCityButton.setOnClickListener(view -> {
            addCitySection.setVisibility(View.VISIBLE);
            cityInput.setText("");
            stateInput.setText("");
            countryInput.setText("");
            cityInput.requestFocus();
        });

        confirmAddCityButton.setOnClickListener(view -> addCityAndRefresh());

        loadCitiesForCurrentUser();
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
     * onOptionsItemSelected is used to switch between items in
     * the main page settings menu. For now it only contains the
     * logoff option
     *
     * @param item is the option selected in the menu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logoff) {
            logOff();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * From database, load cities for logged-in user. If no user, creates an empty arrayList of cities
     */
    private void loadCitiesForCurrentUser() {
        if (glo.currentUser == null) {
            runOnUiThread(() -> renderCityList(new ArrayList<>()));
            return;
        }

        new Thread(() -> {
            List<City> cities = glo.db.cityDao().getCitiesForUser(glo.currentUser.uid);
            runOnUiThread(() -> renderCityList(cities));
        }).start();
    }

    /**
     * Clears and rebuilds the city list for the logged-in user
     *
     * @param cities if the user has any cities, they are provided here
     */
    private void renderCityList(List<City> cities) {
        cityListContainer.removeAllViews();
        Theme theme = Theme.fromUserOrDefault(glo.currentUser);

        if (cities == null || cities.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No saved cities yet.");
            emptyView.setTag("primaryTextColor");
            emptyView.setTextColor(Color.parseColor(theme.getTextColor()));
            emptyView.setTextSize(18);
            emptyView.setPadding(16, 16, 16, 16);
            cityListContainer.addView(emptyView);

            // Re-apply theme so newly added view get themed
            ThemeApplier.applyTheme(this, theme);
            return;
        }

        for (City city : cities) {
            final City cityToRemove = city;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(16, 48, 24, 48);
            row.setBackgroundColor(0xFFEEEEEE);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            rowParams.bottomMargin = 24;
            row.setLayoutParams(rowParams);

            TextView label = new TextView(this);
            label.setText(cityToRemove.name + ", " + cityToRemove.state);
            label.setTextColor(0xFF000000);
            label.setTextSize(20);

            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            labelParams.gravity = Gravity.CENTER_VERTICAL;
            label.setLayoutParams(labelParams);

            Button mapButton = new Button(this);
            mapButton.setText("Map");
            LinearLayout.LayoutParams mapParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            mapButton.setLayoutParams(mapParams);

            mapButton.setOnClickListener(view -> {
                Intent intent = new Intent(UserViewActivity.this, MapActivity.class);
                intent.putExtra("cityName", cityToRemove.name);
                intent.putExtra("cityState", cityToRemove.state);
                intent.putExtra("cityCountry", cityToRemove.country);
                intent.putExtra("cityLongitude", cityToRemove.longitude);
                intent.putExtra("cityLatitude", cityToRemove.latitude);
                startActivity(intent);
            });

            Button weatherButton = new Button(this);
            weatherButton.setText("Weather");
            LinearLayout.LayoutParams weatherParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            weatherButton.setLayoutParams(weatherParams);
            weatherButton.setOnClickListener(v -> {
                Intent intent = new Intent(UserViewActivity.this, WeatherActivity.class);
                intent.putExtra("cityName", cityToRemove.name);
                intent.putExtra("cityState", cityToRemove.state);
                intent.putExtra("cityCountry", cityToRemove.country);
                intent.putExtra("cityLongitude", cityToRemove.longitude);
                intent.putExtra("cityLatitude", cityToRemove.latitude);
                startActivity(intent);
            });

            ImageButton deleteButton = new ImageButton(this);
            deleteButton.setImageResource(android.R.drawable.ic_delete);
            deleteButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#B5B5B5")));
            deleteButton.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            deleteButton.setAlpha(0.50f);
            deleteButton.setContentDescription("Remove " + cityToRemove.name);

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(48, 48);
            btnParams.gravity = Gravity.CENTER_VERTICAL;
            deleteButton.setLayoutParams(btnParams);

            deleteButton.setOnClickListener(view -> showRemoveCityConfirmDialog(cityToRemove));

            row.addView(label);
            row.addView(mapButton);
            row.addView(weatherButton);
            row.addView(deleteButton);
            cityListContainer.addView(row);
        }
    }

    /**
     * Displays a dialog box that asks the user if they would like to remove a city
     *
     * @param city the city the user selected to delete
     */
    private void showRemoveCityConfirmDialog(City city) {
        new AlertDialog.Builder(this)
                .setTitle("Remove city")
                .setMessage("Remove " + city.name + ", " + city.state + " from your list?")
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Confirm", (d, w) -> deleteCityAndRefresh(city))
                .show();
    }

    /**
     * Adds city to the current user if:
     * the city exists
     * it's not already in the list of cities
     * Refreshes the city list once it's successfully added.
     * Throws alerts/errors if it isn't
     */
    private void addCityAndRefresh() {
        if (glo.currentUser == null) {
            return;
        }
        new Thread(() -> {

            long userId = glo.currentUser.uid;
            String city = cityInput.getText().toString().trim();
            String state = stateInput.getText().toString().trim();
            String country = countryInput.getText().toString().trim();

            City validatedCity = cityRepository.validateCity(city, state, country);

            if (validatedCity == null) {
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("City doesn't exist")
                            .setMessage(String.format("%s, %s, %s doesn't exist!", city, state, country))
                            .setPositiveButton("Confirm", (d, w) -> d.dismiss())
                            .show();
                });
                return;
            }

            // Checks if city is already in list
            if (cityRepository.checkIfCityAlreadyInList(
                    userId,
                    validatedCity.name,
                    validatedCity.state,
                    validatedCity.country)) {
                runOnUiThread(() -> {
                    Toast.makeText(this,
                            String.format("%s, %s is already added.", city, state),
                            Toast.LENGTH_SHORT).show();
                });
                return;
            }

            cityRepository.addCity(userId, validatedCity);
            runOnUiThread(() -> {
                Toast.makeText(this,
                        String.format("%s, %s added!", city, state),
                        Toast.LENGTH_SHORT).show();
            });
            runOnUiThread(this::loadCitiesForCurrentUser);
        }).start();
    }

    /**
     * Proceeds with deleting the selected and confirmed city from the user's city list.
     *
     * @param city the city the user selected to delete
     */
    private void deleteCityAndRefresh(City city) {
        if (glo.currentUser == null) {
            return;
        }

        new Thread(() -> {
            try {
                glo.db.cityDao().deleteCity(city);
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Unable to remove city", Toast.LENGTH_SHORT).show());
                return;
            }
            runOnUiThread(this::loadCitiesForCurrentUser);
        }).start();
    }


    /**
     * logOff removes the user view activity of the current authenticated user
     * and transitions the user back to the authentication activity. It is triggered
     * upon clicking the logoff button.
     */
    private void logOff() {
        glo.currentUser = null;
        Intent intent = new Intent(UserViewActivity.this, AuthenticateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
