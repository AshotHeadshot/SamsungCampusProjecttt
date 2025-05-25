package com.example.gamearena;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerLink;
    private TextView testUserLink;
    private FirebaseAuth mAuth; // ProgressBar removed, not in new layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        loginButton = findViewById(R.id.login_button);
        registerLink = findViewById(R.id.register_link);
        testUserLink = findViewById(R.id.test_user_link);

        loginButton.setOnClickListener(v -> userLogin());
        registerLink.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));

        testUserLink.setOnClickListener(v -> {
            emailInput.setText("individualproject2025@gmail.com");
            passwordInput.setText("Samsung2025");
            userLogin();
        });
    }

    private void userLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordInput.setError("Minimum password length is 6 characters");
            passwordInput.requestFocus();
            return;
        }

        // No progressBar in new layout

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser().isEmailVerified()) {
                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            prefs.edit().putBoolean("verified", true).apply();
                            // Fetch nickname and points from Realtime Database and save to SharedPreferences
                            String uid = mAuth.getCurrentUser().getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
                            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String nickname = dataSnapshot.child("nickname").getValue(String.class);
                                    Long points = dataSnapshot.child("points").getValue(Long.class);
                                    if (nickname != null && !nickname.isEmpty()) {
                                        prefs.edit().putString("nickname", nickname).apply();
                                    } else {
                                        String userEmail = mAuth.getCurrentUser().getEmail();
                                        String fallbackNickname = userEmail != null ? userEmail.split("@")[0] : "User";
                                        prefs.edit().putString("nickname", fallbackNickname).apply();
                                    }
                                    if (points != null) {
                                        prefs.edit().putInt("points", points.intValue()).apply();
                                    } else {
                                        prefs.edit().putInt("points", 0).apply();
                                    }
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("navigateToProfile", true);
                                    startActivity(intent);
                                    finish();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    String userEmail = mAuth.getCurrentUser().getEmail();
                                    String fallbackNickname = userEmail != null ? userEmail.split("@")[0] : "User";
                                    prefs.edit().putString("nickname", fallbackNickname).apply();
                                    prefs.edit().putInt("points", 0).apply();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("navigateToProfile", true);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } else {
                            Toast.makeText(LoginActivity.this, "Please verify your email address", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Failed to login! Please check your credentials", Toast.LENGTH_LONG).show();
                    }
                    // No progressBar to hide
                });
    }
}
