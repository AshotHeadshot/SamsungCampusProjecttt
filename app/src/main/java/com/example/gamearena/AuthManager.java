package com.example.gamearena;

public class AuthManager {
    // Simulate user verification check (replace with real storage logic)
    public static boolean isUserVerified(android.content.Context context) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("user_prefs", 0);
        return prefs.getBoolean("verified", false);
    }
    // Other auth-related methods will go here
}
