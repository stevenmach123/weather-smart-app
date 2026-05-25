package edu.uiuc.cs427app.data.database.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(
        tableName = "cities"
)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
/**
 * Represents a city entity in the application database.
 * Contains city details, location, and ownership information.
 */
public class City {
    @PrimaryKey(autoGenerate = true)
    public long cityId;

    public long ownerUserId;

    public String name;
    public String state;
    public String country;
    public double latitude;
    public double longitude;
}