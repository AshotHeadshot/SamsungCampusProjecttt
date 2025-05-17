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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailInput, nicknameInput, passwordInput, confirmPasswordInput;
    private Button registerButton;
    private TextView loginLink;
    private FirebaseAuth mAuth; // ProgressBar removed, not in new layout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.email_input);
        nicknameInput = findViewById(R.id.nickname_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        registerButton = findViewById(R.id.register_button);
        loginLink = findViewById(R.id.login_link);

        registerButton.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String nickname = nicknameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }
        if (nickname.isEmpty()) {
            nicknameInput.setError("Nickname is required");
            nicknameInput.requestFocus();
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
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        // No progressBar in new layout

        // Check if nickname is already used
        FirebaseDatabase.getInstance().getReference("users").orderByChild("nickname").equalTo(nickname)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        nicknameInput.setError("This nickname is already taken!");
                        nicknameInput.requestFocus();
                        return;
                    } else {
                        // Continue with registration as before
                        registerUserAfterNicknameCheck(email, nickname, password, confirmPassword);
                    }
                }
                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(RegisterActivity.this, "Error checking nickname", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void registerUserAfterNicknameCheck(String email, String nickname, String password, String confirmPassword) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Save nickname to Realtime Database
                        if (user != null) {
                            FirebaseDatabase.getInstance().getReference("users")
                                .child(user.getUid())
                                .child("nickname").setValue(nickname);
                            FirebaseDatabase.getInstance().getReference("users")
                                .child(user.getUid())
                                .child("email").setValue(email);
                            FirebaseDatabase.getInstance().getReference("users")
                                .child(user.getUid())
                                .child("points").setValue(0);
                        }
                        // Save nickname locally for immediate use
                        getSharedPreferences("user_prefs", MODE_PRIVATE)
                                .edit().putString("nickname", nickname).apply();
                        sendEmailVerification(user);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed! Please try again", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendEmailVerification(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this,
                                "Registration successful. Please check your email for verification",
                                Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("fromRegister", true);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                "Failed to send verification email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
    
                });
    }
}
