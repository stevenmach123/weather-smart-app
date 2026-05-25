package edu.uiuc.cs427app.repositories;


import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.Schema;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.uiuc.cs427app.BuildConfig;
import edu.uiuc.cs427app.data.database.AppDatabase;
import edu.uiuc.cs427app.data.database.daos.UserDao;
import edu.uiuc.cs427app.data.database.entities.User;

/**
 * Repository class for handling user authentication and registration logic.
 */
public class AuthenRepository {
    UserDao userDao;
    private ExecutorService executorService;
    public MutableLiveData<String> noticeStatus = new MutableLiveData<>("");


    public MutableLiveData<User> noticeUser = new MutableLiveData<>();

    /**
     * Constructs an AuthenRepository with the given database.
     *
     * @param db The application database instance used to access user data.
     */
    public AuthenRepository(AppDatabase db){
        userDao = db.userDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * SignIn authenticates the user by checking if the user with
     * (username, password) exists in the database using UserDao
     * operations. If the user does not exist in the database, then
     * an error message is posted.
     *
     * @param username is the user input for username field
     * @param password is the user input for password field
     */
    public void SignIn(String username,String password){
        noticeUser.postValue(null);
        noticeStatus.postValue("Loading...");
        executorService.execute(()-> {
            try{
                if(userDao.getUserByUserNameAndPassword(username,password) !=null){
                    User user = userDao.getUserByUserNameAndPassword(username,password);
                    noticeUser.postValue(user);
                    noticeStatus.postValue("");
                    return;
                }
                noticeStatus.postValue("SignIn failed: "+"Invalid username or password");
            }
            catch(Exception e){
                Log.e("AuthenRepo","signin repo" + e.getMessage());
                noticeStatus.postValue("SignIn failed: "+e.getMessage());
            }
        });
    }

    /**
     * SignUp authenticates the user by checking if the user with
     * (username) exists in the database using UserDao
     * operations. If the user exists in the database, then
     * an error message is posted. Otherwise, the user is inserted
     * into the database and registered llm theme
     *
     * @param username is the username  to be authenticated
     * @param password is the pass  to be authenticated
     * @param customUi is the customUi  to be authenticated
     */
    public void SignUp(String username,String password,String  customUi){
        noticeStatus.postValue("Loading...");
        executorService.execute(()-> {
            try{
                if(userDao.getUserByUserName(username) ==null){

                    generateThemeAndSignup(username,password,customUi);
                    return;
                }
                noticeStatus.postValue("SignUp failed: "+"username is already registered");
            }
            catch(Exception e){
                Log.e("AuthenRepo","signup repo " + e.getMessage());
                noticeStatus.postValue("SignUp failed: "+e.getMessage());
            }
        });
    }



    /**
     * Asynchronously generates a UI theme using Gemini API based on user description,
     * then creates and persists a new User entity.
     *
     * @param username     The account username.
     * @param password     The account password.
     * @param customUi     The text description of the desired theme.
     */
    private void generateThemeAndSignup(String username, String password, String customUi) {
        // Define JSON schema for structured AI response
        Schema<String> bgSchema = Schema.Companion.str("backgroundColor", "Hex code starting with #");
        Schema<String> textSchema = Schema.Companion.str("textColor", "Hex code starting with #");
        Schema<String> buttonSchema = Schema.Companion.str("buttonColor", "Hex code starting with #");
        Schema<?> themeSchema = Schema.Companion.obj("theme", "UI Theme Colors", bgSchema, textSchema, buttonSchema);

        // Configure model with the defined schema and JSON mime type
        GenerationConfig.Builder configBuilder = GenerationConfig.Companion.builder();
        configBuilder.responseMimeType = "application/json";
        configBuilder.responseSchema = themeSchema;
        //BuildConfig.GEMINI_API_KEY
        GenerativeModel gm = new GenerativeModel("gemini-3-flash-preview", BuildConfig.GEMINI_API_KEY, configBuilder.build());
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        // Prepare prompt and initiate async content generation
        String prompt = "Generate a mobile UI color palette based on this description: " + customUi + ". Make sure all chosen colors maintain strong contrast for readability and accessibility.";
        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        /**
         * Handles the asynchronous Gemini theme-generation response.
         *
         * This callback attempts to parse the structured JSON returned by the model,
         * extract the generated UI theme colors, normalize them into valid hex format,
         * and create a new User with the generated theme. If parsing fails or
         * the API request itself fails, the callback falls back to a default theme.
         */
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            /**
             * Called when the Gemini API request completes successfully.
             *
             * This method parses the JSON payload from the model response, extracts
             * backgroundColor, textColor, and buttonColor, normalizes them to ensure
             * they begin with #, then creates and signs up a new user with the generated
             * theme. If parsing or theme extraction fails for any reason, a fallback user
             * with default theme colors is created instead.
             *
             * @param result the successful Gemini content generation response
             */
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    // Parse hex colors from structured JSON response
                    System.out.println("on Success " +result.getText());
                    org.json.JSONObject json = new org.json.JSONObject(result.getText());
                    String bg = json.getString("backgroundColor").trim();
                    String txt = json.getString("textColor").trim();
                    String btn = json.getString("buttonColor").trim();

                    // Standardize hex format (ensuring '#' prefix)
                    if (!bg.startsWith("#")) bg = "#" + bg;
                    if (!txt.startsWith("#")) txt = "#" + txt;
                    if (!btn.startsWith("#")) btn = "#" + btn;

                    // Map results to User entity
                    User newUser = new User();
                    newUser.username = username;
                    newUser.password = password;
                    newUser.customUi = customUi;
                    newUser.backgroundColor = bg;
                    newUser.textColor = txt;
                    newUser.buttonColor = btn;

                    userDao.insertUser(newUser);
                    noticeStatus.postValue("SignUp success: User registered");
                } catch (Exception e) {
                    // Fallback: create user with default Black/White theme if parsing fails
                    User fallbackUser = new User();
                    fallbackUser.username = username;
                    fallbackUser.password = password;
                    fallbackUser.backgroundColor = "#FFFFFF";
                    fallbackUser.textColor = "#000000";
                    fallbackUser.buttonColor = "#6820EE";

                    userDao.insertUser(fallbackUser);
                    noticeStatus.postValue("SignUp success: User registered");
                }
            }

            /**
             * Called when the Gemini API request fails.
             *
             * This method notifies the user that the API call failed and proceeds
             * with account creation using a default theme so signup can still continue.
             *
             * @param t the error thrown during the Gemini API request
             */
            @Override
            public void onFailure(Throwable t) {

                System.out.println("onFailure llm");
                // Fallback: create user with default Black/White theme if API fails
                User fallbackUser = new User();
                fallbackUser.username = username;
                fallbackUser.password = password;
                fallbackUser.backgroundColor = "#FFFFFF";
                fallbackUser.textColor = "#000000";
                fallbackUser.buttonColor = "#6820EE";
                userDao.insertUser(fallbackUser);
                noticeStatus.postValue("SignUp success: User registered");

            }
        }, Executors.newSingleThreadExecutor());
    }

}
