package edu.uiuc.cs427app.repositories;

import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import edu.uiuc.cs427app.data.database.daos.SampleUserDao;
import edu.uiuc.cs427app.data.database.entities.SampleUser;
import edu.uiuc.cs427app.data.remote.api.SampleUserApiService;
import edu.uiuc.cs427app.data.remote.models.SampleUserApiResponse;

/**
 * Repository class for managing SampleUser data from local database.
 */
public class SampleUserRepository {
    private final SampleUserDao sampleUserDao;
    private final SampleUserApiService sampleUserApiService;

    /**
     * Creates the user repository.
     *
     * @param sampleUserDao The user DAO.
     * @param sampleUserApiService The user API service.
     */
    public SampleUserRepository(SampleUserDao sampleUserDao, SampleUserApiService sampleUserApiService) {
        this.sampleUserDao = sampleUserDao;
        this.sampleUserApiService = sampleUserApiService;
    }

    /**
     * Inserts a user using the DAO.
     * @param sampleUser A user entity.
     * @return The DAO response.
     */
    public ListenableFuture<Long> insertUser(SampleUser sampleUser) {
        return sampleUserDao.insertUser(sampleUser);
    }

    /**
     * Updates a user using the DAO.
     * @param sampleUser A user entity.
     * @return The DAO response.
     */
    public ListenableFuture<Integer> updateUser(SampleUser sampleUser) {
        return sampleUserDao.updateUser(sampleUser);
    }

    /**
     * Deletes a user using the DAO.
     * @param id A user id.
     * @return The DAO response.
     */
    public ListenableFuture<Integer> deleteUser(int id) {
        return sampleUserDao.deleteUserById(id);
    }

    /**
     * Gets a user using the DAO.
     * @param id A user entity.
     * @return The DAO response.
     */
    public LiveData<SampleUser> getUserById(int id) {
        return sampleUserDao.getUserById(id);
    }

    /**
     * Fetches random user information.
     * @return The random user information.
     */
    public ListenableFuture<SampleUserApiResponse.Login> fetchRandomLogin() {
        return Futures.transform(
                sampleUserApiService.getRandomUser("login"),
                response -> {
                    SampleUserApiResponse body = response.body();

                    if (!response.isSuccessful()
                            || body == null
                            || body.results == null
                            || body.results.isEmpty()
                            || body.results.get(0).login == null) {
                        throw new IllegalStateException("Invalid API response");
                    }

                    return body.results.get(0).login;
                },
                MoreExecutors.directExecutor()
        );
    }

    /**
     * Fetches a random user information and inserts into database.
     * @return The DAO response.
     */
    public ListenableFuture<Long> fetchRandomUserAndInsert() {
        return Futures.transformAsync(
                fetchRandomLogin(),
                login -> {
                    SampleUser sampleUser = new SampleUser();
                    sampleUser.username = login.username;
                    sampleUser.password = login.password;
                    return insertUser(sampleUser);
                }, MoreExecutors.directExecutor());
    }
}