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
    private int games;
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

    // Call this after user login/registration to ensure stats exist in Firebase and always load the latest stats
    public void initStatsIfNeeded(Context context) {
        String uid = null;
        try {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {}
        if (uid != null) {
            com.google.firebase.database.DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
            // Ensure stats exist (initialize to 0 if missing, but do not overwrite existing)
            userRef.child("wins").get().addOnSuccessListener(snapshot -> {
                if (!snapshot.exists()) {
                    userRef.child("wins").setValue(0);
                } else {
                    wins = snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
                }
            });
            userRef.child("losses").get().addOnSuccessListener(snapshot -> {
                if (!snapshot.exists()) {
                    userRef.child("losses").setValue(0);
                } else {
                    losses = snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
                }
            });
            userRef.child("draws").get().addOnSuccessListener(snapshot -> {
                if (!snapshot.exists()) {
                    userRef.child("draws").setValue(0);
                } else {
                    draws = snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
                }
            });
            // Optionally, also load points from Firebase
            userRef.child("points").get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    points = snapshot.getValue(Integer.class) != null ? snapshot.getValue(Integer.class) : 0;
                }
            });
        }
    }

    public int getPoints() {
        return points;
    }

    // Removed resetPoints: Points should never be reset to 0 on login/logout.
    // public void resetPoints() {
    //     points = 0;
    //     streaks.clear();
    // }

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

    // Sync wins, losses, draws, and games count
    public void syncStats(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("wins", wins).apply();
        prefs.edit().putInt("losses", losses).apply();
        prefs.edit().putInt("draws", draws).apply();
        syncGamesCount(context);
        String uid = null;
        try {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {}
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("wins").setValue(wins);
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("losses").setValue(losses);
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("draws").setValue(draws);
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("games").setValue(getGamesCount());
        }
    }

    // Sync total games played (wins + losses + draws)
    public void syncGamesCount(Context context) {
        games = wins + losses + draws;
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        prefs.edit().putInt("games", games).apply();
        String uid = null;
        try {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {}
        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uid).child("games").setValue(games);
        }
    }

    public int getGamesCount() {
        return wins + losses + draws;
    }

    /**
     * Loads points, wins, losses, draws, games, and achievements from Firebase for the current user.
     * Overwrites local values and updates SharedPreferences. Call after login/account switch.
     * @param context the context
     * @param onComplete callback to run after loading is finished
     */
    public void loadStatsFromFirebase(Context context, Runnable onComplete) {
        String uid = null;
        try {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {}
        if (uid == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        com.google.firebase.database.DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                points = snapshot.child("points").getValue(Integer.class) != null ? snapshot.child("points").getValue(Integer.class) : 0;
                wins = snapshot.child("wins").getValue(Integer.class) != null ? snapshot.child("wins").getValue(Integer.class) : 0;
                losses = snapshot.child("losses").getValue(Integer.class) != null ? snapshot.child("losses").getValue(Integer.class) : 0;
                draws = snapshot.child("draws").getValue(Integer.class) != null ? snapshot.child("draws").getValue(Integer.class) : 0;
                games = snapshot.child("games").getValue(Integer.class) != null ? snapshot.child("games").getValue(Integer.class) : (wins + losses + draws);
                // Example: load achievements (if you store them as booleans or ints)
                // You can expand this section for each achievement you track
                // Example: boolean ach2048Genius = snapshot.child("ach_2048_genius").getValue(Boolean.class) != null ? snapshot.child("ach_2048_genius").getValue(Boolean.class) : false;
                // Save to SharedPreferences
                SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                prefs.edit().putInt("points", points).apply();
                prefs.edit().putInt("wins", wins).apply();
                prefs.edit().putInt("losses", losses).apply();
                prefs.edit().putInt("draws", draws).apply();
                prefs.edit().putInt("games", games).apply();
                // Example: prefs.edit().putBoolean("ach_2048_genius", ach2048Genius).apply();
            }
            if (onComplete != null) onComplete.run();
        }).addOnFailureListener(e -> {
            if (onComplete != null) onComplete.run();
        });
    }

    public void loadPoints(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        points = prefs.getInt("points", 0);
        wins = prefs.getInt("wins", 0);
        losses = prefs.getInt("losses", 0);
        draws = prefs.getInt("draws", 0);
        games = prefs.getInt("games", 0);
    }

    public int getWins() { return wins; }
    public int getLosses() { return losses; }
    public int getDraws() { return draws; }
}
