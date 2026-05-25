package edu.uiuc.cs427app.theme;

import edu.uiuc.cs427app.data.database.entities.User;

/**
 * Represents a UI Theme for the application, which contains background color, primary text color, and button color.
 */
public class Theme {
    private final String backgroundColor;
    private final String textColor;
    private final String buttonColor;

    /**
     * Constructs a Theme object with specified color values.
     *
     * @param backgroundColor the background color in hex format
     * @param textColor       the text color in hex format
     * @param buttonColor     the button color in hex format
     */
    public Theme(String backgroundColor, String textColor, String buttonColor) {
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.buttonColor = buttonColor;
    }

    /**
     * Returns the background color of the theme.
     *
     * @return background color as a hex string.
     */
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Returns the primary text color of the theme.
     *
     * @return text color as a hex string.
     */
    public String getTextColor() {
        return textColor;
    }

    /**
     * Returns the button color of the theme.
     *
     * @return button color as a hex string.
     */
    public String getButtonColor() {
        return buttonColor;
    }

    /**
     * Returns a default theme used as a fallback if the LLM response fails to parse or if there are API errors.
     *
     * @return a default Theme instance
     */
    public static Theme getDefaultTheme() {
        return new Theme("#FFFFFF", "#000000", "#6820EE");
    }

    /**
     * Resolves the theme for a logged-in user. 
     * If user is null or any of background, text, or button color is missing, 
     * returns the default theme.
     * @param user current user from GlobalApp, may be null
     * @return theme to apply in the UI
     */
    public static Theme fromUserOrDefault(User user) {
        if (user == null) {
            return getDefaultTheme();
        }
        String bg = user.getBackgroundColor();
        String txt = user.getTextColor();
        String btn = user.getButtonColor();
        if (bg == null || txt == null || btn == null) {
            return getDefaultTheme();
        }
        return new Theme(bg, txt, btn);
    }
}