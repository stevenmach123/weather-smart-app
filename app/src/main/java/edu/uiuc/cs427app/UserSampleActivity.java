package edu.uiuc.cs427app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import edu.uiuc.cs427app.data.database.entities.SampleUser;
import edu.uiuc.cs427app.viewmodels.UserSampleViewModel;

/**
 * Sample Activity class to show user interactions on an Activity with the User.
 */
public class UserSampleActivity extends AppCompatActivity {
    private UserSampleViewModel userSampleViewModel;
    private TextView statusTextView;
    private TextView userTextView;
    private EditText userIdEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button insertButton;
    private Button updateButton;
    private Button deleteButton;
    private Button getUserButton;
    private Button randomButton;
    private Button randomAndInsertButton;

    /**
     * The onCreate function provided by Android Activity classes.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usersample);
        userSampleViewModel = new ViewModelProvider(this).get(UserSampleViewModel.class);

        statusTextView = findViewById(R.id.statusTextView);
        userTextView = findViewById(R.id.userTextView);

        userIdEditText = findViewById(R.id.userIdEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        insertButton = findViewById(R.id.insertButton);
        updateButton = findViewById(R.id.updateButton);
        deleteButton = findViewById(R.id.deleteButton);
        getUserButton = findViewById(R.id.getUserButton);
        randomButton = findViewById(R.id.randomButton);
        randomAndInsertButton = findViewById(R.id.randomAndInsertButton);

        userSampleViewModel.getRandomLogin().observe(this, login -> {
            if (login != null) {
                usernameEditText.setText(login.username);
                passwordEditText.setText(login.password);
            }
        });

        // Shows the status of database calls.
        userSampleViewModel.getOperationStatus().observe(this, status -> {
            if (status != null) {
                statusTextView.setText(status);
            }
        });

        // Shows the user's information using the current selected user.
        userSampleViewModel.getSelectedUser().observe(this, user -> {
            if (user != null) {
                userTextView.setText(
                        String.format("Id: %d\nName: %s\nPassword: %s", user.id, user.username, user.password)
                );
            } else {
                userTextView.setText("No user found");
            }
        });

        insertButton.setOnClickListener(this::onButtonClick);
        updateButton.setOnClickListener(this::onButtonClick);
        deleteButton.setOnClickListener(this::onButtonClick);
        getUserButton.setOnClickListener(this::onButtonClick);
        randomButton.setOnClickListener(v -> userSampleViewModel.fetchRandomUser());
        randomAndInsertButton.setOnClickListener(v -> userSampleViewModel.fetchRandomUserAndInsert());
    }

    /**
     * Generic function to run on button clicks.
     * @param view The application view.
     */
    private void onButtonClick(View view) {
        int buttonId = view.getId();
        String userId = userIdEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (buttonId == R.id.insertButton) {
            if (validateCredentials(username, password)) {
                return;
            }

            SampleUser sampleUser = new SampleUser();
            sampleUser.username = username;
            sampleUser.password = password;

            userSampleViewModel.insertUser(sampleUser);
        } else if (buttonId == R.id.updateButton) {
            int parsedId = parseUserId(userId);

            if (validateId(parsedId)) {
                return;
            }

            if (validateCredentials(username, password)) {
                return;
            }

            SampleUser sampleUser = new SampleUser();
            sampleUser.id = parsedId;
            sampleUser.username = username;
            sampleUser.password = password;
            userSampleViewModel.updateUser(sampleUser);

        } else if (buttonId == R.id.deleteButton) {
            int parsedId = parseUserId(userId);

            if (validateId(parsedId)) {
                return;
            }

            userSampleViewModel.deleteUser(parsedId);
        } else if (buttonId == R.id.getUserButton) {
            int parsedId = parseUserId(userId);

            if (validateId(parsedId)) {
                return;
            }

            userSampleViewModel.setCurrentUserId(parsedId);
        }
    }

    /**
     * Validates the user id is valid.
     * @param parsedId The input id.
     * @return True if failed validation, false if passed.
     */
    private boolean validateId(int parsedId) {
        if (parsedId <= 0) {
            userTextView.setText("Enter a valid ID");
            return true;
        }

        return false;
    }

    /**
     * Validates credentials are not empty.
     * @param username The input username.
     * @param password The input password.
     * @return True if failed validation, false if passed.
     */
    private boolean validateCredentials(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            userTextView.setText("Enter a valid username / password");
            return true;
        }

        return false;
    }

    /**
     * Parses a user id from String to int.
     * @param userId The string user id.
     * @return The int user id.
     */
    private static int parseUserId(String userId) {
        try {
            return Integer.parseInt(userId);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}