package com.novus.navigo;

/**
 * Created by sahajbedi on 11-Jan-16.
 */
public class NavigoApplication {
    private final String TAG = "NavigoApplication";
    private static boolean activityVisible;
    private static boolean isThereInternet;
    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    public static boolean isInternetConnected() {
        return isThereInternet;
    }

    public static void internetConnected() {
        isThereInternet = true;
    }

    public static void internetDisconnected() {
        isThereInternet = false;
    }
//To create object for Application class
    /*MyApplication app = (MyApplication)Context.getApplicationContext();
        String privData = app.getPrivData();*/
}
