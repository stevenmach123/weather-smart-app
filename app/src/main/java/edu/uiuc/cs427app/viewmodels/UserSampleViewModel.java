package edu.uiuc.cs427app.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import edu.uiuc.cs427app.data.database.AppDatabase;
import edu.uiuc.cs427app.data.database.entities.SampleUser;
import edu.uiuc.cs427app.data.remote.ApiClients;
import edu.uiuc.cs427app.data.remote.api.SampleUserApiService;
import edu.uiuc.cs427app.data.remote.models.SampleUserApiResponse;
import edu.uiuc.cs427app.repositories.SampleUserRepository;

/**
 * Sample ViewModel class to show user interactions on an Activity with the User.
 */
public class UserSampleViewModel extends AndroidViewModel {
    private final SampleUserRepository repository;
    private final MutableLiveData<String> operationStatus = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentUserId = new MutableLiveData<>();
    private final LiveData<SampleUser> selectedUser;
    private final MutableLiveData<SampleUserApiResponse.Login> randomLogin = new MutableLiveData<>();


    /**
     * Constructor for User Sample class.
     * Connects to the repository and stores UI state.
     *
     * @param application The application.
     */
    public UserSampleViewModel(@NonNull Application application) {
        super(application);

        AppDatabase db = AppDatabase.getInstance(application);
        SampleUserApiService service = ApiClients.getSampleUserService();
        repository = new SampleUserRepository(db.sampleUserDao(), service);

        selectedUser = Transformations.switchMap(currentUserId, repository::getUserById);
    }

    /**
     * Inserts a user after clicking the insert button.
     *
     * @param sampleUser The user information.
     */
    public void insertUser(SampleUser sampleUser) {
        // Message prior to background thread requesting data.
        operationStatus.setValue("Loading...");
        ListenableFuture<Long> future = repository.insertUser(sampleUser);

        Futures.addCallback(future, new FutureCallback<Long>() {
            /**
             * Called when the user insertion operation succeeds.
             *
             * @param result The ID of the inserted user.
             */
            @Override
            public void onSuccess(Long result) {
                operationStatus.postValue("Inserted id: " + result);
            }

            /**
             * Called when the user insertion operation fails.
             *
             * @param t The throwable that caused the failure.
             */
            @Override
            public void onFailure(Throwable t) {
                operationStatus.postValue("Insert failed: " + t.getMessage());
            }
        }, MoreExecutors.directExecutor());
    }

    /**
     * Updates a user after clicking the update button.
     *
     * @param sampleUser The user information.
     */
    public void updateUser(SampleUser sampleUser) {
        operationStatus.setValue("Loading...");
        ListenableFuture<Integer> future = repository.updateUser(sampleUser);

        Futures.addCallback(future, new FutureCallback<Integer>() {
            /**
             * Called when the user update operation succeeds.
             *
             * @param result The number of rows updated.
             */
            @Override
            public void onSuccess(Integer result) {
                operationStatus.postValue("Updated rows: " + result);
            }

            /**
             * Called when the user update operation fails.
             *
             * @param t The throwable that caused the failure.
             */
            @Override
            public void onFailure(Throwable t) {
                operationStatus.postValue("Update failed: " + t.getMessage());
            }
        }, MoreExecutors.directExecutor());
    }

    /**
     * Deletes a user after clicking the delete button.
     *
     * @param id The user information.
     */
    public void deleteUser(int id) {
        operationStatus.setValue("Loading...");
        ListenableFuture<Integer> future = repository.deleteUser(id);

        Futures.addCallback(future, new FutureCallback<Integer>() {
            /**
             * Called when the user deletion operation succeeds.
             *
             * @param result The number of rows deleted.
             */
            @Override
            public void onSuccess(Integer result) {
                operationStatus.postValue("Deleted rows: " + result);
            }

            /**
             * Called when the user deletion operation fails.
             *
             * @param t The throwable that caused the failure.
             */
            @Override
            public void onFailure(Throwable t) {
                operationStatus.postValue("Delete failed: " + t.getMessage());
            }
        }, MoreExecutors.directExecutor());
    }

    /**
     * Retrieves a random username and password from an external API.
     */
    public void fetchRandomUser() {
        operationStatus.setValue("Loading...");
        ListenableFuture<SampleUserApiResponse.Login> future = repository.fetchRandomLogin();

        Futures.addCallback(
                future,
                new FutureCallback<SampleUserApiResponse.Login>() {
                    /**
                     * Called when fetching a random user login succeeds.
                     *
                     * @param login The fetched login information.
                     */
                    @Override
                    public void onSuccess(SampleUserApiResponse.Login login) {
                        randomLogin.postValue(login);
                        operationStatus.postValue("Loaded random user: " + login.username);
                    }

                    /**
                     * Called when fetching a random user login fails.
                     *
                     * @param t The throwable that caused the failure.
                     */
                    @Override
                    public void onFailure(Throwable t) {
                        operationStatus.postValue("Could not fetch random user: " + t.getMessage());
                    }
                }, MoreExecutors.directExecutor());
    }

    /**
     * Retrieves a random username and password from an external API and makes it a user.
     */
    public void fetchRandomUserAndInsert() {
        ListenableFuture<Long> future = repository.fetchRandomUserAndInsert();

        Futures.addCallback(
                future,
                new FutureCallback<Long>() {
                    /**
                     * Called when fetching and inserting a random user succeeds.
                     *
                     * @param result The ID of the inserted random user.
                     */
                    @Override
                    public void onSuccess(Long result) {
                        operationStatus.postValue("Random user inserted id: " + result);
                    }

                    /**
                     * Called when fetching and inserting a random user fails.
                     *
                     * @param t The throwable that caused the failure.
                     */
                    @Override
                    public void onFailure(Throwable t) {
                        operationStatus.postValue("Random insert failed: " + t.getMessage());
                    }
                },
                MoreExecutors.directExecutor()
        );
    }

    /**
     * Gets the operational status.
     *
     * @return The operational status.
     */
    public LiveData<String> getOperationStatus() {
        return operationStatus;
    }

    /**
     * Sets the current user id.
     *
     * @param id The current user id.
     */
    public void setCurrentUserId(int id) {
        currentUserId.setValue(id);
    }

    /**
     * Gets the current user id.
     *
     * @return The current user id.
     */
    public LiveData<SampleUser> getSelectedUser() {
        return selectedUser;
    }

    /**
     * Gets the random login information from the fetch return.
     * @return Random login information.
     */
    public LiveData<SampleUserApiResponse.Login> getRandomLogin() {
        return randomLogin;
    }
}