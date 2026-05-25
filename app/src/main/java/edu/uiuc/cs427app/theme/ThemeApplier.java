package edu.uiuc.cs427app.theme;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Applies a Theme to an Activity.
 * Sets the Activity background color and primary text color
 */
public final class ThemeApplier {
    private static final String PRIMARY_TEXT_TAG = "primaryTextColor";

    /**
     * Prevents instantiation. Hides the default constructor
     */
    private ThemeApplier() {
    }

    /**
     * Applies a Theme to the entire Activity.
     *
     * @param activity the Activity to apply the theme
     * @param theme    the Theme object
     */
    public static void applyTheme(Activity activity, Theme theme) {

        // Apply background color
        View root = activity.findViewById(android.R.id.content);
        if (root == null) return;
        try {
            root.setBackgroundColor(Color.parseColor(theme.getBackgroundColor()));
        } catch (Exception ignored) {
        }

        // Apply text color to TextView elements with tag "primaryTextColor".
        applyTextColor(root, theme.getTextColor());

        // Apply button color to all Button views
        applyButtonColor(root, theme.getButtonColor());
    }

    /**
     * Recursively sets the text color for all TextView with tag "primaryTextColor".
     *
     * @param view  the root view
     * @param color the text color in hex format
     */
    private static void applyTextColor(View view, String color) {
        if (view == null) return;
        // Apply to all EditTexts regardless of tag.
        if (view instanceof EditText) {
            try {
                ((EditText) view).setTextColor(Color.parseColor(color));
                ((EditText) view).setHintTextColor(Color.parseColor(color));
            } catch (Exception ignored) {}

        } else if (view instanceof TextView && PRIMARY_TEXT_TAG.equals(view.getTag())) {
            // Regular TextViews only colored if tagged
            try {
                ((TextView) view).setTextColor(Color.parseColor(color));
            } catch (Exception ignored) {}
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyTextColor(group.getChildAt(i), color);
            }
        }
    }

    /**
     * Recursively sets the button color for all Button.
     *
     * @param view  the root view
     * @param buttonColor the button color in hex format
     */
    private static void applyButtonColor(View view, String buttonColor) {
        int parsedColor;
        try {
            parsedColor = Color.parseColor(buttonColor);
        } catch (Exception e) {
            return;
        }

        if (view instanceof Button) {
            view.setBackgroundColor(parsedColor);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyButtonColor(group.getChildAt(i), buttonColor);
            }
        }
    }
}