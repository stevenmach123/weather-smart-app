
package edu.uiuc.cs427app.data.remote.models;
import java.util.List;


/**
 * Represents the API response for a sample user request.
 */
public class SampleUserApiResponse {
    public List<Result> results;

    /**
     * Represents a single user result in the API response.
     */
    public static class Result {
        public Login login;
    }

    /**
     * Represents login credentials for a user.
     */
    public static class Login {
        public String username;
        public String password;
    }
}