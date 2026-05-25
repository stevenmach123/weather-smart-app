package edu.uiuc.cs427app.data.remote.models;

/**
 * Represents weather data for a specific date and time.
 */
public class Weather {
    
    public String dateTime;
    public double temperatureC;
    public double temperatureF;
    public String weatherDescription;
    public double humidity;
    public double windCondition;

    /**
     * Constructs a Weather object with the specified weather details.
     *
     * @param dateTime           The date and time of the weather data.
     * @param temperatureC       The temperature in Celsius.
     * @param temperatureF       The temperature in Fahrenheit.
     * @param weatherDescription The description of the weather.
     * @param humidity           The humidity percentage.
     * @param windCondition      The wind condition value.
     */
    public Weather(String dateTime, double temperatureC, double temperatureF, String weatherDescription,
                   double humidity, double windCondition) {
        this.dateTime = dateTime;
        this.temperatureC = temperatureC;
        this.temperatureF = temperatureF;
        this.weatherDescription = weatherDescription;
        this.humidity = humidity;
        this.windCondition = windCondition;
    }

}