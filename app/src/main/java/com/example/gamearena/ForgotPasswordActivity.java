package com.example.gamearena;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private Button buttonResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        buttonResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = editTextEmail.getText().toString().trim();

        if (email.isEmpty()) {
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Password reset email sent. Please check your inbox",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(ForgotPasswordActivity.this,
                                "Failed to send reset email!",
                                Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.GONE);
                });
    }
}
