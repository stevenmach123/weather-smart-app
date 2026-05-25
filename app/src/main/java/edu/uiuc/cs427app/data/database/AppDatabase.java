package edu.uiuc.cs427app.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import edu.uiuc.cs427app.BuildConfig;
import edu.uiuc.cs427app.data.database.daos.CityDao;
import edu.uiuc.cs427app.data.database.daos.SampleUserDao;
import edu.uiuc.cs427app.data.database.daos.UserDao;
import edu.uiuc.cs427app.data.database.entities.City;
import edu.uiuc.cs427app.data.database.entities.SampleUser;
import edu.uiuc.cs427app.data.database.entities.User;

@Database(
        entities = {
                // Add new Entities created.
                SampleUser.class,
                User.class,
                City.class
        },
        version = 1,
        exportSchema = false
)
/**
 * The main database class for the application, providing access to DAOs and entities.
 * Extends RoomDatabase and implements the singleton pattern.
 */
public abstract class AppDatabase extends RoomDatabase {
    // Add new Dao abstracts created.

    /**
     * Gets the DAO for SampleUser entity operations.
     * @return the SampleUserDao instance
     */
    public abstract SampleUserDao sampleUserDao();


    /**
     * Gets the DAO for User entity operations.
     * @return the UserDao instance
     */
    public abstract UserDao userDao();


    /**
     * Gets the DAO for City entity operations.
     * @return the CityDao instance
     */
    public abstract CityDao cityDao();

    private static volatile AppDatabase INSTANCE;

    /**
     * Singleton pattern to create a database.
     *
     * @param context The application context.
     * @return The application database.
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    if (BuildConfig.DEBUG) {
                        context.getApplicationContext().deleteDatabase("app-database");
                    }
                    System.out.println("start database");
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "app-database"
                            )
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}