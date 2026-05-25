package edu.uiuc.cs427app.viewmodels;

import android.app.Application;
import android.provider.Settings;

import androidx.lifecycle.AndroidViewModel;

import edu.uiuc.cs427app.GlobalApp;
import edu.uiuc.cs427app.data.database.daos.UserDao;
import edu.uiuc.cs427app.data.database.entities.User;

/**
 * TestViewModel is a test implementation of AndroidViewModel.
 * 
 * This class is not currently in use and serves as a placeholder for
 * testing ViewModel and database functionality.
 */
public class TestViewModel extends AndroidViewModel {
    public int count=0;
    UserDao userDao;
    GlobalApp glo;
    
    /**
     * Constructs a TestViewModel instance.
     * 
     * @param app the Application context used to initialize the AndroidViewModel
     */
    public TestViewModel(Application app) {
        super(app);
        glo = (GlobalApp)getApplication();
        userDao =glo.db.userDao();

    }
    
    /**
     * Increments the count value.
     */
    public void upCount() {
        //User user = userDao.getUserByUserName("s");
    }
}
