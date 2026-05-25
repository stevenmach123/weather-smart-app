package edu.uiuc.cs427app.activities.authen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import edu.uiuc.cs427app.GlobalApp;
import edu.uiuc.cs427app.R;
import edu.uiuc.cs427app.data.database.entities.Testclasses;
import edu.uiuc.cs427app.data.database.entities.User;
import edu.uiuc.cs427app.activities.mainpage.UserViewActivity;
import edu.uiuc.cs427app.repositories.AuthenRepository;
import edu.uiuc.cs427app.viewmodels.TestViewModel;

import com.google.ai.client.generativeai.GenerativeModel;

/**
 * Activity for user authentication, including sign-in and sign-up flows.
 * Handles user credential validation, UI updates, and navigation to the main user view.
 */
public class AuthenticateActivity extends AppCompatActivity {
    private AuthenRepository authenRepository;
    GlobalApp glo;
    private TextView registerText;
    private TextInputEditText signInPass;
    private TextInputEditText signInUserName;
    private Button signInBut ;

    private GenerativeModel gm;

    private FrameLayout noticeFrame;
    private TextView noticeText;
    private TestViewModel testViewModel;

    /**
     * onCreate is called when the authenticate activity is created.
     * It initializes the app and shows the signin page.
     *
     * @param savedstate is the state saved in the app
     */
    @Override
    public void onCreate(Bundle savedstate) {
        super.onCreate(savedstate);
        this.glo = (GlobalApp)getApplication();
        showSignin();
    }


    /**
     * Called when the authenticate activity becomes visible to the user
     */
    @Override
    public void onStart(){
        super.onStart();
    }

    /**
     * showSignin shows the user the signin page in authenticate
     * activity. It takes user input from the page (username,
     * password) and validate that the user exists in the database
     * using UserDao operations.
     */
    void showSignin() { // refer to signin.xml
        setContentView(R.layout.signin);

        // get input from signin page
        registerText = findViewById(R.id.textRegister);
        noticeFrame  =  findViewById(R.id.notice_frame);
        noticeText = findViewById(R.id.notice_text);
        signInBut = findViewById(R.id.submit_button) ;
        signInPass = findViewById(R.id.password_input);
        signInUserName =findViewById(R.id.username_input);
        
        if(authenRepository != null) {
            authenRepository.noticeStatus.observe(this, (notice) -> {
                if (notice.contains("SignUp success"))
                    this.setTextNoticeStatus(notice, signInBut);
            });
        }
        testViewModel = new ViewModelProvider(this).get(TestViewModel.class);
        authenRepository =new AuthenRepository(glo.db);

        registerText.setOnClickListener(v -> {
            showSignup();
        });
        signInBut.setOnClickListener(v->{
            String username = signInUserName.getText().toString().trim();
            String password  =  signInPass.getText().toString().trim();
            testViewModel.upCount();
            authenRepository.SignIn(username,password);
        });
        authenRepository.noticeUser.observe(this, user->{
            glo.currentUser = user;
            if(user != null){
                Intent v = new Intent(this, UserViewActivity.class);
                v.putExtra("username", user.username);
                v.putExtra("customUiDescription", user.customUi);
                startActivity(v);
            }
        });
        authenRepository.noticeStatus.observe(this,notice->{
            System.out.println("sign in notice: "+notice);
            if(!notice.equals("")){
                this.setTextNoticeStatus(notice,signInBut);
            }else{
                noticeFrame.setVisibility(FrameLayout.GONE);
            }
        });
    }

    /**
     * showSignup shows the user the signup page in authenticate
     * activity. It takes user input from the page (username,
     * password, description of custom theme) and validate that
     * the format is correct. It creates a new user with the input
     * and adds to the database using UserDao operations.
     */
    void showSignup() { // refer to signup.xml
        setContentView(R.layout.signup);
        noticeText = findViewById(R.id.notice_text);
        noticeFrame=  findViewById(R.id.notice_frame); //keep this for frame status notice
        authenRepository = new AuthenRepository(glo.db);

        // Get input from signup page
        TextInputLayout usernameLayout = findViewById(R.id.usernameLayout);
        TextInputLayout passwordLayout = findViewById(R.id.passwordLayout);
        TextInputLayout customUiLayout = findViewById(R.id.customUiLayout);

        EditText userInputUsername = findViewById(R.id.inputUserName);
        EditText userInputPassword  = findViewById(R.id.inputPassword);
        EditText userInputCustomUi = findViewById(R.id.inputCustomUi);

        Button signupButton = findViewById(R.id.signupButton);

        // Validate and create new user upon clicking signup button
        signupButton.setOnClickListener(v -> {
            String username = userInputUsername.getText().toString().trim();
            String password = userInputPassword.getText().toString().trim();
            String customUi = userInputCustomUi.getText().toString();
            usernameLayout.setError(null);
            passwordLayout.setError(null);
            customUiLayout.setError(null);

            boolean hasError = false;
            // tell espresso UI busy
            IdleEspresso.increment();
            authenRepository.noticeStatus.postValue(""); // refresh status frame
            if (username.isEmpty() || !username.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]+$")) {
                usernameLayout.setError("Please enter an valid username");
                hasError = true;
            }

            if (customUi.isEmpty()) {
                customUiLayout.setError("Please describe your preferred theme");
                hasError = true;
            }

            if (password.isEmpty() || password.length() < 8) {
                passwordLayout.setError("Please enter valid password");
                hasError = true;
            }

            if (hasError) {
                // tell espresso UI is free to detect
                IdleEspresso.decrement();
                return;
            }

            authenRepository.SignUp(username,password,customUi );
        });

        // Go to signin page upon clicking signin button
        TextView signInText = findViewById(R.id.signinButton);
        signInText.setOnClickListener(v -> {
            showSignin();
        });

        // get status notice from authenRepository with UserDao operation
        authenRepository.noticeStatus.observe(this,notice->{
            System.out.println("sign up notice: "+notice);
            if(!notice.equals("")){
                this.setTextNoticeStatus(notice,signupButton);
                if(notice.contains("SignUp success")) {
                    showSignin();
                    // tell espresso UI is free to detect
                    IdleEspresso.decrement();
                }else if(!notice.contains("Loading"))
                    IdleEspresso.decrement();
            }else{
                noticeFrame.setVisibility(FrameLayout.GONE);
            }
        });
    }

    /**
     * setTextNoticeStatus sets notice messages if user succeeded
     * or failed upon authenticating on the signin or signup page.
     *
     * @param notice is the state of authentication activity
     * @param submitBut is the signin or signup button
     */
    void setTextNoticeStatus(String notice,Button submitBut){
        noticeFrame.setVisibility(FrameLayout.VISIBLE);
        noticeText.setTextColor(ContextCompat.getColor(this, R.color.load));
        noticeText.setText(notice);
        submitBut.setEnabled(true);
        submitBut.setAlpha(0.5f); // faded (blur-like effect)
        if(notice.contains("Loading")){
            submitBut.setEnabled(false);
        }else if(notice.contains("success")){
            submitBut.setAlpha(1f);
            noticeText.setTextColor(ContextCompat.getColor(this, R.color.success));
        }else if(notice.contains("failed")){
            submitBut.setAlpha(1f);
            noticeText.setTextColor(ContextCompat.getColor(this, R.color.error));
        }
    }

    /**
     * onDestroy is called when the authentication activity is
     * destroyed
     */
    @Override
    public void onDestroy(){
        super.onDestroy();
        System.out.println("destroy authen act");
    }


}