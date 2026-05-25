package edu.uiuc.cs427app.theme;

import org.json.JSONObject;

/**
 * Converts LLM-generated JSON into Theme objects.
 * Handles flat or nested JSON structures and validates hex colors.
 * Falls back to default theme when parsing fails or colors are invalid.
 */
public class ThemeParser {
    /**
     * Parses a JSON string returned by the LLM and converts it into a Theme object.
     *
     * @param jsonString raw JSON string returned by the LLM
     * @return Theme object if parsing succeeds and colors are valid, otherwise default Theme
     */
    public static Theme parseFromJson(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return Theme.getDefaultTheme();
        }

        try {
            JSONObject json = new JSONObject(jsonString);
            // Support nested structure: {"theme": {...}}
            if (json.has("theme") && json.opt("theme") instanceof JSONObject) {
                json = json.getJSONObject("theme");
            }

            String backgroundColor = optTrimmed(json, "backgroundColor");
            String textColor = optTrimmed(json, "textColor");
            String buttonColor = optTrimmed(json, "buttonColor");

            if (isNotValidHex(backgroundColor) || isNotValidHex(textColor) || isNotValidHex(buttonColor)) {
                return Theme.getDefaultTheme();
            }
            return new Theme(backgroundColor, textColor, buttonColor);
        } catch (Exception e) {
            return Theme.getDefaultTheme();
        }
    }

    /**
     * Get a trimmed string from JSON or return null if missing/empty
     *
     * @param json the JSON object to retrieve the value from
     * @param key the key to look up in the JSON object
     * @return trimmed string value, or null if the key is missing or empty
     */
    private static String optTrimmed(JSONObject json, String key) {
        String s = json.optString(key, "");
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    /**
     * Validate if a string has proper 6-digit hex format: #RRGGBB
     *
     * @param color the color string to validate
     * @return true if the color is null or not a valid hex format, false if valid
     */
    private static boolean isNotValidHex(String color) {
        return color == null || !color.matches("^#[0-9a-fA-F]{6}$");
    }
}