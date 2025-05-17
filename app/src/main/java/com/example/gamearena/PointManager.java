package com.example.gamearena;

import java.util.HashMap;
import java.util.Map;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class PointManager {
    private static PointManager instance;
    private int points;
    private int wins;
    private int losses;
    private int draws;
    private Map<String, Integer> streaks;

    private PointManager() {
        points = 0;
        streaks = new HashMap<>();
    }

    public static synchronized PointManager getInstance() {
        if (instance == null) {
            instance = new PointManager();
        }
        return instance;
    }

    public int getPoints() {
        return points;
    }

    public void resetPoints() {
        points = 0;
        streaks.clear();
    }

    // Unified session-based point and stats management
    public int applySessionPoints(Context context, int sessionPoints, int sessionWins, int sessionLosses, int sessionDraws) {
        points += sessionPoints;
        wins += sessionWins;
        losses += sessionLosses;
        draws += sessionDraws;
        syncPoints(context);
        syncStats(context);
        return points;
    }

    // Call this after any point change to sync with SharedPreferences and Firebase
    public void syncPoints(Context context) {
        // Save to SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("points", points).apply();
        // Save to Firebase
        String uid = null;
        try {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {
            // Not logged in, skip firebase sync
        }
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("points").setValue(points);
        }
        // Removed notification Toast for silent syncing.
    }

    // Sync wins, losses, draws
    public void syncStats(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("wins", wins).apply();
        prefs.edit().putInt("losses", losses).apply();
        prefs.edit().putInt("draws", draws).apply();
        String uid = null;
        try {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {}
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("wins").setValue(wins);
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("losses").setValue(losses);
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("draws").setValue(draws);
        }
    }

    public void loadPoints(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        points = prefs.getInt("points", 0);
        wins = prefs.getInt("wins", 0);
        losses = prefs.getInt("losses", 0);
        draws = prefs.getInt("draws", 0);
    }

    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getDraws() { return draws; }
}
