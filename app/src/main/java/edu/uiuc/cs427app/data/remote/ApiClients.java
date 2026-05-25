package edu.uiuc.cs427app.data.remote;

import edu.uiuc.cs427app.data.remote.api.SampleUserApiService;
import retrofit2.Retrofit;
import retrofit2.adapter.guava.GuavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Manage Service APIs. Add new Services here.
 */
public class ApiClients {
    private static final String SAMPLE_USER_BASE_URL = "https://randomuser.me/";

    private static final SampleUserApiService SAMPLE_USER_API_SERVICE =
            new Retrofit.Builder()
                    .baseUrl(SAMPLE_USER_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(GuavaCallAdapterFactory.create())
                    .build()
                    .create(SampleUserApiService.class);

    /**
     * Retrieves the service for SampleUserApiService.
     * @return The Sample User service.
     */
    public static SampleUserApiService getSampleUserService() {
        return SAMPLE_USER_API_SERVICE;
    }
}