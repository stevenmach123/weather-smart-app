package edu.uiuc.cs427app.data.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

import edu.uiuc.cs427app.data.database.entities.User;

@Dao
/**
 * Data Access Object (DAO) interface for user-related database operations.
 * Provides methods to insert, update, delete, and query User entities.
 */
public interface UserDao {

    /**
     * Inserts a single user into the database.
     * @param user the User entity to insert
     * @return the row ID of the newly inserted user
     */
    @Insert
    long insertUser(User user);

    // Insert with ListenableFuture for background tasks

    /**
     * Inserts multiple users into the database.
     * @param users one or more User entities to insert
     */
    @Insert
    void insertAllUsers(User... users);

    /**
     * Updates an existing user in the database.
     * @param user the User entity to update
     */
    @Update
    void updateUser(User user);

    /**
     * Deletes a user from the database.
     * @param user the User entity to delete
     */
    @Delete
    void deleteUser(User user);

    /**
     * Deletes all users from the database.
     */
    @Query("DELETE FROM users")
    void deleteAllUsers();

    /**
     * Retrieves a user by their unique ID as LiveData.
     * @param uid the unique user ID
     * @return LiveData containing the User entity, or null if not found
     */
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    LiveData<User> getUserByIdLive(long uid);

    /**
     * Retrieves a user by their username as LiveData.
     * @param username the username to search for
     * @return LiveData containing the User entity, or null if not found
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    LiveData<User>  getUserByUserNameLive(String username);


    /**
     * Retrieves a user by their username.
     * @param username the username to search for
     * @return the User entity, or null if not found
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUserName(String username);

    // Get user by username and password (for login)

    /**
     * Retrieves a user by their username and password (for login).
     * @param username the username to search for
     * @param password the password to match
     * @return the User entity, or null if not found
     */
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User getUserByUserNameAndPassword(String username, String password);

    // Get all users. observable for ui

    /**
     * Retrieves all users, ordered by username descending, as LiveData.
     * @return LiveData containing a list of all User entities
     */
    @Query("SELECT * FROM users ORDER BY username DESC")
    LiveData<List<User>> getAllUsers();

    // Get user count

    /**
     * Retrieves the total number of users in the database.
     * @return the user count
     */
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();
}