package edu.uiuc.cs427app.activities.map;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.uiuc.cs427app.GlobalApp;
import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.activities.authen.AuthenticateActivity;
import edu.uiuc.cs427app.theme.Theme;
import edu.uiuc.cs427app.theme.ThemeApplier;

/**
 * Activity for displaying a map for each city.
 */
public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    GlobalApp glo;
    Double latitude;
    Double longitude;

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        glo = (GlobalApp) getApplication();
        if (glo.currentUser != null) {
            String teamNumber = getString(R.string.app_name);
            setTitle(teamNumber + " - " + glo.currentUser.username);
        }

        Theme theme = Theme.fromUserOrDefault(glo.currentUser);
        ThemeApplier.applyTheme(this, theme);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String cityName = getIntent().getStringExtra("cityName");
        String cityState = getIntent().getStringExtra("cityState");
        String cityCountry = getIntent().getStringExtra("cityCountry");
        Double cityLongitude = getIntent().getDoubleExtra("cityLongitude", 0);
        Double cityLatitude = getIntent().getDoubleExtra("cityLatitude", 0);

        latitude = cityLatitude;
        longitude = cityLongitude;

        TextView cityText = findViewById(R.id.mapCityNameText);
        TextView latText = findViewById(R.id.mapLatitudeText);
        TextView longTxt = findViewById(R.id.mapLongitudeText);

        cityText.setText(cityName + ", " + cityState + ", " + cityCountry);
        latText.setText(String.format(java.util.Locale.US, "Latitude: %f", cityLatitude));
        longTxt.setText(String.format(java.util.Locale.US, "Longitude: %f", cityLongitude));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
            finish();
            return true;
        } else if (item.getItemId() == R.id.logoff) {
            logOff();
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
     * Configure the starting map position & zoom.
     * @param googleMap map object that is rendered in the MapActivity.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng city = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions()
                .position(city)
                .title("City"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(city, 10));
    }

    /**
     * logOff removes the current activity of the authenticated user
     * and transitions the user back to the authentication activity.
     */
    private void logOff() {
        glo.currentUser = null;
        Intent intent = new Intent(this, AuthenticateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
