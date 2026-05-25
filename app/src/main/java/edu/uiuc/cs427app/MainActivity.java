package edu.uiuc.cs427app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import edu.uiuc.cs427app.activities.authen.AuthenticateActivity;
import edu.uiuc.cs427app.activities.mainpage.UserViewActivity;

/**
 * Main entry point activity for the application.
 * Routes users to authentication or main screen based on session status.
 */
public class MainActivity  extends AppCompatActivity {

    /**
     * Routes the user to sign-in or the main user screen based on whether a session exists.
     * @param savedInstanceState Bundle containing most recent instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalApp globalapp = (GlobalApp)getApplication();
        Intent v;
        System.out.println("main act");
        if (globalapp.currentUser == null){
            v = new Intent(this, AuthenticateActivity.class);
        }
        else {
            v = new Intent(this, UserViewActivity.class);
        }
        startActivity(v);

    }

}
