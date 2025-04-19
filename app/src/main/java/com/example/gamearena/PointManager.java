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

    // Rock-Paper-Scissors
    public void updateRPSResult(String result) {
        switch (result) {
            case "win": points += 5; break;
            case "lose": points -= 5; break;
            case "draw": points += 2; break;
        }
    }

    // Quick Math Challenge
    public void updateQuickMath(boolean correct) {
        if (correct) {
            points += 2;
            int streak = streaks.getOrDefault("quick_math", 0) + 1;
            streaks.put("quick_math", streak);
            if (streak == 10) {
                points += 10;
            }
        } else {
            points -= 2;
            streaks.put("quick_math", 0);
        }
    }

    // Snake
    public void addSnakeFood() {
        points += 2;
    }
    public void addSnakeBonus(int score) {
        if (score >= 100) {
            points += 25;
        } else if (score >= 50) {
            points += 10;
        }
    }

    // 2048
    public void add2048Merge() {
        points += 1;
    }
    public void add2048Tile(int tile) {
        switch (tile) {
            case 128: points += 10; break;
            case 256: points += 25; break;
            case 512: points += 50; break;
            case 1024: points += 100; break;
            case 2048: points += 200; break;
        }
    }

    // Tic-Tac-Toe
    public void updateTicTacToeResult(Context context, String result) {
        result = result.toLowerCase().trim();
        Toast.makeText(context, "[PointManager] Before: " + points, Toast.LENGTH_SHORT).show();
        switch (result) {
            case "win": points += 5; break;
            case "lose": points -= 5; break;
            case "draw": points += 2; break;
        }
        Toast.makeText(context, "[PointManager] After: " + points, Toast.LENGTH_SHORT).show();
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
        int spPoints = prefs.getInt("points", -999);
        Toast.makeText(context, "[PointManager] Synced points: " + points + ", SP points: " + spPoints, Toast.LENGTH_LONG).show();
    }

    public void loadPoints(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        points = prefs.getInt("points", 0);
    }
}
