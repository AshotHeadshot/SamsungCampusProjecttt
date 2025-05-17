package com.example.gamearena;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.pm.PackageManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsFragment extends Fragment {
    private static final int PICK_IMAGE = 1001;
    private EditText nicknameEdit;
    private Uri avatarUri;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Button logoutBtn = view.findViewById(R.id.logoutBtn);
        Button deleteAccountBtn = view.findViewById(R.id.deleteAccountBtn);
        Button saveNicknameBtn = view.findViewById(R.id.saveNicknameBtn);
        Button deleteAvatarBtn = view.findViewById(R.id.deleteAvatarBtn);
        Button rankedGamesInfoBtn = view.findViewById(R.id.rankedGamesInfoBtn);
        nicknameEdit = view.findViewById(R.id.nicknameEdit);
        TextView versionText = view.findViewById(R.id.versionText);
        TextView creditsText = view.findViewById(R.id.creditsText);

        rankedGamesInfoBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                .setTitle("Ranked Games & Points Info")
                .setMessage("\u2022 Ranked games contribute to your leaderboard position.\n\n" +
                        "\u2022 Points are earned for each win, and may be lost for defeats.\n\n" +
                        "\u2022 Your Win/Loss/Draw record is tracked for each ranked game.\n\n" +
                        "\u2022 The more you win, the higher your rank and points!\n\n" +
                        "\u2022 Cheating or leaving games early may result in penalties.")
                .setPositiveButton("OK", null)
                .show();
        });

        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", 0);
        nicknameEdit.setText(prefs.getString("nickname", ""));
        versionText.setText("Version: " + getAppVersion());
        creditsText.setText("Made by Ashot Hovhannisyan");

        // Save Nickname
        saveNicknameBtn.setOnClickListener(v -> {
            String newNick = nicknameEdit.getText().toString();
            if (newNick.isEmpty()) {
                Toast.makeText(getContext(), "Nickname cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Check if nickname is already used
            String uid = null;
            try {
                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            } catch (Exception e) {}
            if (uid == null) return;
            final String finalUid = uid;
            final String finalNewNick = newNick;
            FirebaseDatabase.getInstance().getReference("users").orderByChild("nickname").equalTo(finalNewNick)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        boolean taken = false;
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            if (!child.getKey().equals(finalUid)) {
                                taken = true;
                                break;
                            }
                        }
                        if (taken) {
                            Toast.makeText(getContext(), "This nickname is already taken!", Toast.LENGTH_SHORT).show();
                        } else {
                            prefs.edit().putString("nickname", finalNewNick).apply();
                            FirebaseDatabase.getInstance().getReference("users")
                                .child(finalUid).child("nickname").setValue(finalNewNick);
                            Toast.makeText(getContext(), "Nickname updated!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        Toast.makeText(getContext(), "Error checking nickname", Toast.LENGTH_SHORT).show();
                    }
                });
        });

        // Delete Avatar
        deleteAvatarBtn.setOnClickListener(v -> {
            prefs.edit().remove("avatarUri").apply();
            // Also remove from Firebase
            String uid = null;
            try {
                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            } catch (Exception e) {}
            if (uid != null) {
                FirebaseDatabase.getInstance().getReference("users")
                    .child(uid).child("avatarUri").removeValue();
            }
            Toast.makeText(getContext(), "Avatar deleted!", Toast.LENGTH_SHORT).show();
        });

        // Log out logic
        logoutBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("verified", false);
            editor.apply();
            Toast.makeText(getContext(), "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        // Delete account logic
        deleteAccountBtn.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Toast.makeText(getContext(), "Account deleted", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finish();
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == android.app.Activity.RESULT_OK && data != null && data.getData() != null) {
            String avatarUri = data.getData().toString();
            SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", 0);
            prefs.edit().putString("avatarUri", avatarUri).apply();
            // Save avatarUri to Firebase
            String uid = null;
            try {
                uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            } catch (Exception e) {}
            if (uid != null) {
                FirebaseDatabase.getInstance().getReference("users")
                    .child(uid).child("avatarUri").setValue(avatarUri);
            }
            Toast.makeText(getContext(), "Avatar updated!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getAppVersion() {
        try {
            return requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }
}
