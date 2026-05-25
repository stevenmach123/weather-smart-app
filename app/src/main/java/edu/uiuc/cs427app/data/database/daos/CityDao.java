package edu.uiuc.cs427app.data.database.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import edu.uiuc.cs427app.data.database.entities.City;



@Dao
/**
 * Data Access Object (DAO) interface for city-related database operations.
 * Provides methods to insert, delete, and query City entities.
 */
public interface CityDao {

    /**
     * Inserts a city into the database.
     * @param city the City entity to insert
     * @return the row ID of the newly inserted city
     */
    @Insert
    long insertCity(City city);


    /**
     * Deletes a city from the database.
     * @param city the City entity to delete
     */
    @Delete
    void deleteCity(City city);


    /**
     * Deletes all cities from the database.
     */
    @Query("DELETE FROM cities")
    void deleteAllCities();


    /**
     * Retrieves all cities for a specific user.
     * @param userId the user ID to filter cities by
     * @return a list of City entities owned by the user
     */
    @Query("SELECT * FROM cities WHERE ownerUserId = :userId")
    List<City> getCitiesForUser(long userId);


    /**
     * Retrieves a specific city for a user by name, state, and country.
     * @param userId the user ID to filter cities by
     * @param city the city name
     * @param state the state name
     * @param country the country name
     * @return a list containing the matching City entity, or empty if not found
     */
    @Query("SELECT * FROM cities WHERE" +
            " ownerUserId = :userId AND " +
            "name = :city AND " +
            "state = :state AND " +
            "country = :country" +
            " LIMIT 1")
    List<City> getCityForUser(long userId, String city, String state, String country);
}