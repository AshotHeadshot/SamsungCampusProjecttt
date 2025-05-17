package com.example.gamearena;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FastTypingActivity extends AppCompatActivity {
    private FastTypingGameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_fast_typing);
        // Prepare for logic usage (findViewById)
        TextView errorsText = findViewById(R.id.errorsText);
        TextView timeText = findViewById(R.id.timeText);
        TextView accuracyText = findViewById(R.id.accuracyText);
        EditText typingEditText = findViewById(R.id.typingEditText);
        TextView titleText = findViewById(R.id.titleText);
        Button startBtn = findViewById(R.id.startBtn);
        Button finishBtn = findViewById(R.id.finishBtn);

        String[] prompts = {
                "The quick brown fox jumps over the lazy dog.",
                "Android development is fun and rewarding.",
                "Practice makes perfect in typing games.",
                "Java is a versatile programming language.",
                "Stay focused and keep improving your skills."
        };

        final int[] totalErrors = {0};
        final int[] totalTyped = {0};
        final int[] totalCorrect = {0};
        final boolean[] gameActive = {false};
        final CountDownTimer[] timer = {null};
        final boolean[] finished = {false};

        startBtn.setOnClickListener(v -> {
            finished[0] = false;
            finishBtn.setEnabled(true);
            // Reset state
            String prompt = prompts[(int) (Math.random() * prompts.length)];
            titleText.setText(prompt);
            typingEditText.setText("");
            typingEditText.setEnabled(true);
            typingEditText.setFocusable(true);
            typingEditText.setFocusableInTouchMode(true);
            typingEditText.requestFocus();
            errorsText.setText("0");
            accuracyText.setText("100");
            timeText.setText("60s");
            totalErrors[0] = 0;
            totalTyped[0] = 0;
            totalCorrect[0] = 0;
            gameActive[0] = true;
            startBtn.setEnabled(false);
            finishBtn.setEnabled(true);

            // Timer
            if (timer[0] != null) timer[0].cancel();
            timer[0] = new android.os.CountDownTimer(60000, 1000) {
                int secondsLeft = 60;

                public void onTick(long ms) {
                    secondsLeft--;
                    timeText.setText(secondsLeft + "s");
                }

                public void onFinish() {
                    if (finished[0]) return;
                    finished[0] = true;
                    gameActive[0] = false;
                    typingEditText.setEnabled(false);
                    startBtn.setEnabled(true);
                    finishBtn.setEnabled(false);
                    // Calculate accuracy
                    int correct = totalCorrect[0];
                    int typed = totalTyped[0];
                    int accuracy = (typed > 0) ? (int) (((double) correct / typed) * 100) : 100;
                    accuracyText.setText(String.valueOf(accuracy));
                }
            }.start();
        });

        finishBtn.setEnabled(false);
        finishBtn.setOnClickListener(v -> {
            if (finished[0]) return;
            finished[0] = true;
            if (timer[0] != null) timer[0].cancel();
            gameActive[0] = false;
            typingEditText.setEnabled(false);
            startBtn.setEnabled(true);
            finishBtn.setEnabled(false);
            // Calculate accuracy
            int correct = totalCorrect[0];
            int typed = totalTyped[0];
            int accuracy = (typed > 0) ? (int) (((double) correct / typed) * 100) : 100;
            accuracyText.setText(String.valueOf(accuracy));
        });

        typingEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!gameActive[0]) return;
                String prompt = titleText.getText().toString();
                String typed = s.toString();
                int errors = 0, correct = 0, total = typed.length();
                for (int i = 0; i < typed.length(); i++) {
                    if (i < prompt.length() && typed.charAt(i) == prompt.charAt(i)) {
                        correct++;
                    } else {
                        errors++;
                    }
                }
                totalErrors[0] = errors;
                totalTyped[0] = total;
                totalCorrect[0] = correct;
                errorsText.setText(String.valueOf(errors));
                int accuracy = (total > 0) ? (int) (((double) correct / total) * 100) : 100;
                accuracyText.setText(String.valueOf(accuracy));
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });


    }
}
