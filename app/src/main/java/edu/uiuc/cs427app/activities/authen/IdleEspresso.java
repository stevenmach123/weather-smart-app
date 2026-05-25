package edu.uiuc.cs427app.activities.authen;

import androidx.test.espresso.idling.*;
import androidx.test.espresso.*;
/**
 * IdlingResource for Espresso to synchronize asynchronous UI operations.
 *
 * This class uses a CountingIdlingResource to let the frontend explicitly notify
 * Espresso when a background task  (signup process or layout transition)
 * starts and finishes. Calling increment() signals that Espresso should wait,
 * while decrement() indicates the operation is complete and testing can resume.
 *
 * This is especially useful when Espresso cannot automatically detect UI changes,
 * such as root layout replacements.
 */
public class IdleEspresso {
    private static final String RESOURCE = "SIGNUP";
    private static final CountingIdlingResource countingIdlingResource =
            new CountingIdlingResource(RESOURCE);

    public static void increment() {
        countingIdlingResource.increment();
    }

    public static void decrement() {
        if (!countingIdlingResource.isIdleNow()) {
            countingIdlingResource.decrement();
        }
    }

    public static CountingIdlingResource getIdlingResource() {
        return countingIdlingResource;
    }
}