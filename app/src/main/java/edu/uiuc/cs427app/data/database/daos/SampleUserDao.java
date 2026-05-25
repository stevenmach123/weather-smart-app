package edu.uiuc.cs427app.data.database.daos;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.google.common.util.concurrent.ListenableFuture;

import edu.uiuc.cs427app.data.database.entities.SampleUser;

@Dao
/**
 * Data Access Object (DAO) interface for sample user-related database operations.
 * Provides methods to insert, update, delete, and query SampleUser entities.
 */
public interface SampleUserDao {
    /**
     * Inserts a user into the database.
     * @param sampleUser A user entity.
     * @return A future with the inserted item's row.
     */
    @Insert
    ListenableFuture<Long> insertUser(SampleUser sampleUser);

    /**
     * Updates a user in the database.
     * @param sampleUser A user entity.
     * @return A future with the amount of rows updated.
     */
    @Update
    ListenableFuture<Integer> updateUser(SampleUser sampleUser);

    /**
     * Deletes a user in the database using the user's id.
     * @param userId A user's id.
     * @return A future with the amount of rows deleted.
     */
    @Query("DELETE FROM sample_users WHERE id = :userId")
    ListenableFuture<Integer> deleteUserById(int userId);

    /**
     * Gets a user in the database using the user's id.
     * @param userId A user's id.
     * @return A LiveData with the user entity.
     */
    @Query("SELECT * FROM sample_users WHERE id = :userId LIMIT 1")
    LiveData<SampleUser> getUserById(int userId);
}