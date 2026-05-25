package edu.uiuc.cs427app.data.database.entities;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "sample_users",
        indices = {
                // Add new index for new unique values.
                @Index(value = {"username"}, unique = true)
        }
)
/**
 * Represents a sample user entity in the application database.
 * Contains user credentials for demonstration or testing purposes.
 */
public class SampleUser {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String username;
    public String password;
}