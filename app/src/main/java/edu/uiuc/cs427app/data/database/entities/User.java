package edu.uiuc.cs427app.data.database.entities;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import edu.uiuc.cs427app.theme.Theme;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(tableName = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
/**
 * Represents a user entity in the application database.
 */
public class User {
    @PrimaryKey(autoGenerate = true)
    public long uid;
    public String username;
    public String password;
    public String customUi;
    public String backgroundColor;
    public String textColor;
    public String buttonColor;
}