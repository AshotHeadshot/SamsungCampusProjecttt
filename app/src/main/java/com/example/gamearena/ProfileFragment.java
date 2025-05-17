package com.example.gamearena;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

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
        TextView gamesPlayedValue = view.findViewById(R.id.gamesPlayedValue);
        TextView winRateValue = view.findViewById(R.id.winRateValue);
        TextView totalPointsValue = view.findViewById(R.id.totalPointsValue);
        TextView rankValue = view.findViewById(R.id.rankValue);
        TextView joinDateValue = view.findViewById(R.id.joinDateValue);
        ImageButton editAvatarBtn = view.findViewById(R.id.editAvatarBtn);

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", 0);
        String nickname = prefs.getString("nickname", "");
        int wins = prefs.getInt("wins", 0);
        int draws = prefs.getInt("draws", 0);
        int losses = prefs.getInt("losses", 0);
        String avatarUri = prefs.getString("avatarUri", "");

        // Always load avatarUri from Firebase on profile open
        String firebaseUid = null;
        try {
            firebaseUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {}
        if (firebaseUid != null) {
            Log.d("ProfileFragment", "Loading avatarUri from Firebase for uid: " + firebaseUid);
            FirebaseDatabase.getInstance().getReference("users").child(firebaseUid).child("avatarUri").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String fbAvatarUri = snapshot.getValue(String.class);
                    Log.d("ProfileFragment", "Firebase avatarUri value: " + fbAvatarUri);
                    if (fbAvatarUri != null && !fbAvatarUri.isEmpty()) {
                        try {
                            avatarImage.setImageURI(Uri.parse(fbAvatarUri));
                            Log.d("ProfileFragment", "Set avatar from Firebase URI");
                        } catch (SecurityException e) {
                            avatarImage.setImageResource(R.drawable.ic_profile_default);
                            Log.e("ProfileFragment", "SecurityException on setImageURI", e);
                        }
                        // Update SharedPreferences for cache
                        prefs.edit().putString("avatarUri", fbAvatarUri).apply();
                    } else {
                        avatarImage.setImageResource(R.drawable.ic_profile_default);
                        Log.d("ProfileFragment", "No avatarUri in Firebase, using default");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    avatarImage.setImageResource(R.drawable.ic_profile_default);
                    Log.e("ProfileFragment", "Firebase avatarUri load cancelled", error.toException());
                }
            });
        } else {
            // Not logged in, fallback to local cache
            if (!avatarUri.isEmpty()) {
                try {
                    avatarImage.setImageURI(Uri.parse(avatarUri));
                } catch (SecurityException e) {
                    avatarImage.setImageResource(R.drawable.ic_profile_default);
                }
            } else {
                avatarImage.setImageResource(R.drawable.ic_profile_default);
            }
        }

        int rpsWins = prefs.getInt("rps_wins", 0);
        int rpsDraws = prefs.getInt("rps_draws", 0);
        int rpsLosses = prefs.getInt("rps_losses", 0);
        int snakeBest = prefs.getInt("snake_best", 0);

        nicknameText.setText(nickname);
        if (!avatarUri.isEmpty()) {
            try {
                avatarImage.setImageURI(Uri.parse(avatarUri));
            } catch (SecurityException e) {
                avatarImage.setImageResource(R.drawable.ic_profile_default); // fallback to your custom default
            }
        } else {
            avatarImage.setImageResource(R.drawable.ic_profile_default); // fallback to your custom default
        }

        // --- Stats for profile cards ---
        int gamesPlayed = wins + draws + losses;
        double winRate = gamesPlayed > 0 ? (wins * 100.0) / gamesPlayed : 0.0;
        gamesPlayedValue.setText(String.valueOf(gamesPlayed));
        winRateValue.setText(String.format("%.1f%%", winRate));
        // Always load points from Firebase for profile display
        String pointsUid = null;
        try {
            pointsUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {}
        if (pointsUid != null) {
            FirebaseDatabase.getInstance().getReference("users").child(pointsUid).child("points").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer firebasePoints = snapshot.getValue(Integer.class);
                    if (firebasePoints != null) {
                        totalPointsValue.setText(String.format("%,d", firebasePoints));
                        // Optionally update SharedPreferences cache
                        prefs.edit().putInt("points", firebasePoints).apply();
                    } else {
                        totalPointsValue.setText("0");
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    totalPointsValue.setText("0");
                }
            });
        } else {
            totalPointsValue.setText("0");
        }

        // --- Only sync wins, draws, losses if changed locally ---
        String syncUid = null;
        try {
            syncUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {}
        if (syncUid != null) {
            // Optionally, check if local values differ from Firebase before updating (not shown here)
            FirebaseDatabase.getInstance().getReference("users").child(syncUid).child("wins").setValue(wins);
            FirebaseDatabase.getInstance().getReference("users").child(syncUid).child("draws").setValue(draws);
            FirebaseDatabase.getInstance().getReference("users").child(syncUid).child("losses").setValue(losses);
        }
        // Games played and win rate are computed locally and in leaderboard from wins/draws/losses

        // --- Real Rank ---
        String tmpUid = null;
        try {
            tmpUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } catch (Exception e) {}
        final String currentUid = tmpUid;
        if (currentUid != null) {
            FirebaseDatabase.getInstance().getReference("users").orderByChild("points").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int total = (int) snapshot.getChildrenCount();
                    int rank = total;
                    int position = 1;
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        String uid = userSnap.getKey();
                        if (uid != null && uid.equals(currentUid)) {
                            rank = total - position + 1; // Firebase orders ascending, so reverse
                            break;
                        }
                        position++;
                    }
                    rankValue.setText("Rank #" + rank);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    rankValue.setText("Rank --");
                }
            });
        } else {
            rankValue.setText("Rank --");
        }

        // --- Real Account Creation Date ---
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            long creationTimestamp = FirebaseAuth.getInstance().getCurrentUser().getMetadata().getCreationTimestamp();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.ENGLISH);
            String creationDate = sdf.format(new java.util.Date(creationTimestamp));
            joinDateValue.setText(" • Joined " + creationDate);
        } else {
            joinDateValue.setText(" • Joined Jan 2023");
        }

        // Retrieve points from SharedPreferences
        int points = prefs.getInt("points", 0);

        // --- Achievements List ---
        RecyclerView achievementsRecycler = view.findViewById(R.id.achievementsRecycler);
        achievementsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        List<Achievement> achievements = new ArrayList<>();
        // Date formatting removed
        // First Win Achievement
        if (wins > 0) {
            achievements.add(new Achievement("First Win", R.drawable.ic_trophy));
        }
        // High Score in Snake
        if (snakeBest > 0) {
            achievements.add(new Achievement("High Score in Snake", R.drawable.ic_trophy));
        }
        // 100 Total Points
        if (points >= 100) {
            achievements.add(new Achievement("100 Total Points", R.drawable.ic_trophy));
        }
        // Add more dynamic achievements as desired
        if (achievements.isEmpty()) {
            achievements.add(new Achievement("No achievements yet", R.drawable.ic_trophy));
        }
        AchievementAdapter achievementAdapter = new AchievementAdapter(getContext(), achievements);
        achievementsRecycler.setAdapter(achievementAdapter);

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
                        totalPointsValue.setText(String.format("%,d", firebasePoints));
                        // Optionally, update SharedPreferences to keep local cache in sync
                        SharedPreferences.Editor editor = requireContext().getSharedPreferences("user_prefs", 0).edit();
                        editor.putInt("points", firebasePoints);
                        editor.apply();
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            };
            FirebaseDatabase.getInstance().getReference("users").child(uidFinal).child("points").addValueEventListener(pointsListener);
        }



        editAvatarBtn.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK);
            pickPhoto.setType("image/*");
            startActivityForResult(pickPhoto, PICK_IMAGE);
        });


        // Ranked games: RPS vs Bot, Quick Math Challenge, Snake, 2048, Ping Pong vs Blocks, TicTacToe vs Bot, Imposter Color
        // RPS
        int rpsPlayed = rpsWins + rpsLosses + rpsDraws;
        int rpsWin = rpsWins;
        // Quick Math
        int qmCorrect = prefs.getInt("qm_correct", 0);
        int qmWrong = prefs.getInt("qm_wrong", 0);
        int qmPlayed = qmCorrect + qmWrong;
        int qmWin = qmCorrect;
        // Snake (best > 0 means played and win)
        int snakePlayed = snakeBest > 0 ? 1 : 0;
        int snakeWin = snakeBest > 0 ? 1 : 0;
        // 2048 (best > 0 means played and win)
        int game2048Best = prefs.getInt("2048_best", 0);
        int game2048Played = game2048Best > 0 ? 1 : 0;
        int game2048Win = game2048Best > 0 ? 1 : 0;
        // Ping Pong vs Blocks
        int pingpongWins = prefs.getInt("pingpong_wins", 0);
        int pingpongLosses = prefs.getInt("pingpong_losses", 0);
        int pingpongDraws = prefs.getInt("pingpong_draws", 0);
        int pingpongPlayed = pingpongWins + pingpongLosses + pingpongDraws;
        int pingpongWin = pingpongWins;
        // TicTacToe vs Bot
        int tttWins = prefs.getInt("ttt_wins", 0);
        int tttLosses = prefs.getInt("ttt_losses", 0);
        int tttDraws = prefs.getInt("ttt_draws", 0);
        int tttPlayed = tttWins + tttLosses + tttDraws;
        int tttWin = tttWins;
        // Imposter Color
        int imposterWins = prefs.getInt("imposter_wins", 0);
        int imposterLosses = prefs.getInt("imposter_losses", 0);
        int imposterDraws = prefs.getInt("imposter_draws", 0);
        int imposterPlayed = imposterWins + imposterLosses + imposterDraws;
        int imposterWin = imposterWins;
        // Totals
        int totalGames = PointManager.getInstance().getWins() + PointManager.getInstance().getLosses() + PointManager.getInstance().getDraws();
        gamesPlayedValue.setText(String.valueOf(totalGames));
        winRateValue.setText(String.format("%.1f%%", winRate));
        totalPointsValue.setText(String.format("%,d", points));
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            String avatarUri = data.getData().toString();
            requireContext().getSharedPreferences("user_prefs", 0).edit().putString("avatarUri", avatarUri).apply();
            // Save avatarUri to Firebase
            String uid = null;
            try {
                uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
            } catch (Exception e) {}
            if (uid != null && avatarUri != null && !avatarUri.isEmpty()) {
    android.util.Log.d("ProfileFragment", "Saving avatarUri to Firebase: " + avatarUri + " for uid: " + uid);
    com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users")
        .child(uid).child("avatarUri").setValue(avatarUri)
        .addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                android.util.Log.e("ProfileFragment", "Failed to save avatarUri to Firebase", task.getException());
                Toast.makeText(getContext(), "Failed to update avatar in cloud", Toast.LENGTH_SHORT).show();
            } else {
                android.util.Log.d("ProfileFragment", "avatarUri successfully saved to Firebase");
            }
        });
} else {
    android.util.Log.e("ProfileFragment", "Not saving avatarUri: uid or avatarUri is null or empty. uid=" + uid + ", avatarUri=" + avatarUri);
}
            Toast.makeText(getContext(), "Avatar updated!", Toast.LENGTH_SHORT).show();
            // Force reload avatar from Firebase to ensure UI consistency
            if (uid != null) {
                android.util.Log.d("ProfileFragment", "Reloading avatarUri from Firebase after update");
                FirebaseDatabase.getInstance().getReference("users").child(uid).child("avatarUri").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        String fbAvatarUri = snapshot.getValue(String.class);
                        ImageView avatarImage = getView().findViewById(R.id.avatarImage);
                        if (fbAvatarUri != null && !fbAvatarUri.isEmpty()) {
                            try {
                                avatarImage.setImageURI(Uri.parse(fbAvatarUri));
                                android.util.Log.d("ProfileFragment", "Set avatar from Firebase URI after update");
                            } catch (SecurityException e) {
                                avatarImage.setImageResource(R.drawable.ic_profile_default);
                                android.util.Log.e("ProfileFragment", "SecurityException on setImageURI after update", e);
                            }
                            // Update SharedPreferences for cache
                            requireContext().getSharedPreferences("user_prefs", 0).edit().putString("avatarUri", fbAvatarUri).apply();
                        } else {
                            avatarImage.setImageResource(R.drawable.ic_profile_default);
                            android.util.Log.d("ProfileFragment", "No avatarUri in Firebase after update, using default");
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                        ImageView avatarImage = getView().findViewById(R.id.avatarImage);
                        avatarImage.setImageResource(R.drawable.ic_profile_default);
                        android.util.Log.e("ProfileFragment", "Firebase avatarUri reload cancelled after update", error.toException());
                    }
                });
            }
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
