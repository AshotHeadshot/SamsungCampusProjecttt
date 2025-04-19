package com.example.gamearena;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {
    private TextView pointsText;
    private ValueEventListener pointsListener;
    private String uidFinal;
    private static final int PICK_IMAGE = 1010;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ImageView avatarImage = view.findViewById(R.id.avatarImage);
        TextView nicknameText = view.findViewById(R.id.nicknameText);
        pointsText = view.findViewById(R.id.pointsText);
        TextView winsText = view.findViewById(R.id.winsText);
        TextView drawsText = view.findViewById(R.id.drawsText);
        TextView lossesText = view.findViewById(R.id.lossesText);
        TextView rpsStatsText = view.findViewById(R.id.rpsStatsText);
        TextView quickMathStatsText = view.findViewById(R.id.quickMathStatsText);
        TextView snakeStatsText = view.findViewById(R.id.snakeStatsText);
        TextView levelText = view.findViewById(R.id.levelText);
        TextView tile2048StatsText = view.findViewById(R.id.tile2048StatsText);
        TextView ticTacToeStatsText = view.findViewById(R.id.ticTacToeStatsText);
        TextView achievementsText = view.findViewById(R.id.achievementsText);
        ImageButton editAvatarBtn = view.findViewById(R.id.editAvatarBtn);

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", 0);
        String nickname = prefs.getString("nickname", "");
        int wins = prefs.getInt("wins", 0);
        int draws = prefs.getInt("draws", 0);
        int losses = prefs.getInt("losses", 0);
        String avatarUri = prefs.getString("avatarUri", "");
        int rpsWins = prefs.getInt("rps_wins", 0);
        int rpsDraws = prefs.getInt("rps_draws", 0);
        int rpsLosses = prefs.getInt("rps_losses", 0);

        nicknameText.setText(nickname);
        if (!avatarUri.isEmpty()) {
            try {
                avatarImage.setImageURI(Uri.parse(avatarUri));
            } catch (SecurityException e) {
                avatarImage.setImageResource(R.drawable.splash_logo); // fallback to a bundled image
            }
        }
        rpsStatsText.setText("RPS: " + rpsWins + "W " + rpsDraws + "D " + rpsLosses + "L");

        // --- Tic-Tac-Toe Stats ---
        int tttWins = prefs.getInt("tictactoe_wins", 0);
        int tttLosses = prefs.getInt("tictactoe_losses", 0);
        int tttDraws = prefs.getInt("tictactoe_draws", 0);
        ticTacToeStatsText.setText("Tic-Tac-Toe:\nWins: " + tttWins + " / Losses: " + tttLosses + " / Draws: " + tttDraws);
        winsText.setText("Wins: " + tttWins);
        drawsText.setText("Draws: " + tttDraws);
        lossesText.setText("Losses: " + tttLosses);

        // --- Quick Math Stats ---
        int qmCorrect = prefs.getInt("quickmath_correct", 0);
        int qmWrong = prefs.getInt("quickmath_wrong", 0);
        int qmStreak = prefs.getInt("quickmath_streak", 0);
        quickMathStatsText.setText("Quick Math Challenge:\nCorrect Answers: " + qmCorrect + " / Wrong Answers: " + qmWrong + "\nBest Streak: " + qmStreak);

        // --- Snake Stats ---
        int snakeBest = prefs.getInt("snake_best", 0);
        int snakeFood = prefs.getInt("snake_food", 0);
        snakeStatsText.setText("Snake:\nHighest Score: " + snakeBest + "\nTotal Food Eaten: " + snakeFood);

        int points = prefs.getInt("points", 0);
        int level = points / 100 + 1;
        levelText.setText("Level: " + level);

        tile2048StatsText.setText("[2048]: Best Tile: " + prefs.getInt("2048_best_tile", 0));

        StringBuilder ach = new StringBuilder();
        if (prefs.getBoolean("ach_first_win", false)) ach.append("✅ First Win\n");
        if (prefs.getBoolean("ach_math_wizard", false)) ach.append("✅ Math Wizard\n");
        if (prefs.getBoolean("ach_snake_master", false)) ach.append("✅ Snake Master\n");
        if (prefs.getBoolean("ach_2048_genius", false)) ach.append("✅ 2048 Genius\n");
        achievementsText.setText(ach.length() > 0 ? ach.toString() : "No achievements yet.");

        String uid = null;
        try {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {}
        uidFinal = uid;
        if (uidFinal != null) {
            pointsListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int firebasePoints = snapshot.getValue(Integer.class);
                        pointsText.setText("Points: " + firebasePoints);
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            };
            FirebaseDatabase.getInstance().getReference("users").child(uidFinal).child("points").addValueEventListener(pointsListener);
        }

        pointsText.setOnClickListener(v -> {
            // Example: increment points by 10 for demonstration
            int newPoints = PointManager.getInstance().getPoints() + 10;
            pointsText.setText("Points: " + newPoints);
            // Save to SharedPreferences
            prefs.edit().putInt("points", newPoints).apply();
            // Save to Firebase Realtime Database
            if (uidFinal != null) {
                FirebaseDatabase.getInstance().getReference("users")
                    .child(uidFinal).child("points").setValue(newPoints);
            }
        });

        editAvatarBtn.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK);
            pickPhoto.setType("image/*");
            startActivityForResult(pickPhoto, PICK_IMAGE);
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            String avatarUri = data.getData().toString();
            requireContext().getSharedPreferences("user_prefs", 0).edit().putString("avatarUri", avatarUri).apply();
            ImageView avatarImage = getView().findViewById(R.id.avatarImage);
            try {
                avatarImage.setImageURI(Uri.parse(avatarUri));
            } catch (SecurityException e) {
                avatarImage.setImageResource(R.drawable.splash_logo);
            }
            // Save avatarUri to Firebase
            String uid = null;
            try {
                uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
            } catch (Exception e) {}
            if (uid != null) {
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users")
                    .child(uid).child("avatarUri").setValue(avatarUri);
            }
            Toast.makeText(getContext(), "Avatar updated!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always update points from PointManager on resume
        if (pointsText != null) {
            int points = PointManager.getInstance().getPoints();
            pointsText.setText("Points: " + points);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up Firebase listener
        if (uidFinal != null && pointsListener != null) {
            FirebaseDatabase.getInstance().getReference("users").child(uidFinal).child("points").removeEventListener(pointsListener);
        }
    }
}
