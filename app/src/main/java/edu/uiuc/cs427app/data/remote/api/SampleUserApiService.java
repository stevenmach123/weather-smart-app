package edu.uiuc.cs427app.data.remote.api;

import com.google.common.util.concurrent.ListenableFuture;

import edu.uiuc.cs427app.data.remote.models.SampleUserApiResponse;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;


/**
 * Interface for accessing sample user API endpoints.
 */
public interface SampleUserApiService {
    /**
     * Request to get sample random user data.
     * @param includedFields URL params.
     * @return API response.
     */
    @GET("api/")
    ListenableFuture<Response<SampleUserApiResponse>> getRandomUser(
            @Query("inc") String includedFields
    );
}