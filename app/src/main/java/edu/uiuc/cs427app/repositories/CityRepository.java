package edu.uiuc.cs427app.repositories;

import android.util.Log;

import com.google.maps.addressvalidation.v1.AddressComponent;
import com.google.maps.addressvalidation.v1.AddressValidationClient;
import com.google.maps.addressvalidation.v1.AddressValidationSettings;
import com.google.maps.addressvalidation.v1.ValidateAddressRequest;
import com.google.maps.addressvalidation.v1.ValidateAddressResponse;
import com.google.type.PostalAddress;

import java.util.List;

import edu.uiuc.cs427app.BuildConfig;
import edu.uiuc.cs427app.data.database.AppDatabase;
import edu.uiuc.cs427app.data.database.daos.CityDao;
import edu.uiuc.cs427app.data.database.entities.City;

/**
 * A collection of methods to validate & addCities to an authenticated user.
 */
public class CityRepository {

    private final CityDao cityDao;
    private AddressValidationClient addressValidationClient;

    private static final String VALIDATED_CITY_NAME_FIELD = "locality";
    private static final String VALIDATED_POSTAL_TOWN_NAME_FIELD = "postal_town";
    private static final String VALIDATED_STATE_FIELD_NAME = "administrative_area_level_1";
    private static final String VALIDATE_COUNTRY_FIELD_NAME = "country";
    private static final List<String> COMPONENTS = List.of(
            VALIDATED_CITY_NAME_FIELD,
            VALIDATED_POSTAL_TOWN_NAME_FIELD,
            VALIDATED_STATE_FIELD_NAME,
            VALIDATE_COUNTRY_FIELD_NAME
    );

    /**
     * Constructs a CityRepository with the given AppDatabase.
     *
     * @param db The AppDatabase instance used to access city data.
     */
    public CityRepository(AppDatabase db) {
        cityDao = db.cityDao();
    }

    /**
     * Adds a city for the user
     *
     * @param userId        user to add the city for
     * @param validatedCity validated city object to add for the user
     */
    public void addCity(long userId, City validatedCity) {
        validatedCity.setOwnerUserId(userId);
        cityDao.insertCity(validatedCity);
    }

    /**
     * Checks if a user already has a city in their list
     * @param userId user we're checking for
     * @param cityInput city we're checking
     * @param stateInput state the city is in
     * @param countryInput country of the city
     * @return true if city already in user list, false otherwise
     */
    public boolean checkIfCityAlreadyInList(long userId, String cityInput, String stateInput, String countryInput) {
        List<City> currentCity = cityDao.getCityForUser(
                userId,
                cityInput,
                stateInput,
                countryInput);

        return !currentCity.isEmpty();
    }

    /**
     * Validates if the inputs provided by the user can be resolved into a valid city.
     * Utilizes Google's AddressValidation API to validate if the city, state & country combination
     * is valid.
     *
     * @param cityInput user provided city name.
     * @param stateInput user provided state name.
     * @param countryInput user provided country name.
     * @return a City object with the standard city properties returned by the API along
     * with positional data if resolvable. Null if unable to resolve city.
     */
    public City validateCity(String cityInput, String stateInput, String countryInput) {

        if (addressValidationClient == null) {
            try {
                addressValidationClient = AddressValidationClient.create(AddressValidationSettings.newBuilder()
                        .setApiKey(BuildConfig.MAPS_API_KEY)
                        .build());
            } catch (Exception e) {
                Log.e("AddressValidation", "Unable to initialize address validation client", e);
                return null;
            }
        }

        PostalAddress address = PostalAddress.newBuilder()
                .addAddressLines(String.format("%s, %s, %s", cityInput, stateInput, countryInput))
                .build();

        ValidateAddressRequest request = ValidateAddressRequest.newBuilder()
                .setAddress(address)
                .build();

        ValidateAddressResponse response;

        try {
            response = addressValidationClient.validateAddress(request);
        } catch (Exception e) {
            Log.e("AddressValidation", "Error when calling the validateAddress API", e);
            return null;
        }

        Log.d("AddressValidation", response.toString());

        String validatedCity = "";
        String validatedState = "";
        String validatedCountry = "";

        for (AddressComponent component :response.getResult().getAddress().getAddressComponentsList()) {
            if (COMPONENTS.contains(component.getComponentType())
                    && !component.getConfirmationLevel().equals(AddressComponent.ConfirmationLevel.CONFIRMED)) {
                return null;
            }

            String validatedText = component.getComponentName().getText();

            switch (component.getComponentType()) {
                case VALIDATED_CITY_NAME_FIELD:
                case VALIDATED_POSTAL_TOWN_NAME_FIELD:
                    validatedCity = validatedText;
                    break;
                case VALIDATED_STATE_FIELD_NAME:
                    validatedState = validatedText;
                    break;
                case VALIDATE_COUNTRY_FIELD_NAME:
                    validatedCountry = validatedText;
                    break;
                default:
                    Log.d("AddressValidation", String.format("Not a %s, %s", COMPONENTS, validatedText));
            }
        }

        // Unable to validate, erroring on the side of caution.
        if (validatedCity.isEmpty() || validatedState.isEmpty() || validatedCountry.isEmpty()) {
            return null;
        }

        return City.builder()
                .name(validatedCity)
                .state(validatedState)
                .country(validatedCountry)
                .longitude(response.getResult().getGeocode().getLocation().getLongitude())
                .latitude(response.getResult().getGeocode().getLocation().getLatitude())
                .build();
    }

}
