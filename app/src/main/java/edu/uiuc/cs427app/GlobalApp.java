package edu.uiuc.cs427app;

import android.app.Application;

import androidx.lifecycle.ViewModelProvider;

import edu.uiuc.cs427app.data.database.AppDatabase;
import edu.uiuc.cs427app.data.database.entities.User;
import edu.uiuc.cs427app.repositories.WeatherInfoRepository;
import edu.uiuc.cs427app.viewmodels.TestViewModel;

/**
 * Global application class that manages application-wide state and resources.
 * Provides access to the current user and database instance across activities.
 */
public class GlobalApp extends Application {
    public User currentUser; // can retain  global data across activities, try null go to signin.xml
    public AppDatabase db;
    public WeatherInfoRepository weatherInfoRepository;

    /**
     * Default constructor for the GlobalApp class.
     */
    public GlobalApp(){
    }

    /**
     * Application entry. Builds the database
     */
    @Override
    public void onCreate(){
        super.onCreate();
        System.out.println("globalapp");
        db =  AppDatabase.getInstance(this);
        weatherInfoRepository = new WeatherInfoRepository();
    }


}
