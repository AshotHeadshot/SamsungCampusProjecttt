package com.example.gamearena;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.SharedPreferences;

import java.util.Random;

import com.example.gamearena.PointManager;

public class QuickMathActivity extends AppCompatActivity {
    private TextView questionText, feedbackText, scoreText, timerText; // Removed progressText, added timerText
    private Button answerBtn1, answerBtn2, answerBtn3, answerBtn4, finishBtn; // Four answer buttons
    private int score = 0;
    private int correctAnswers = 0;
    private int sessionPoints = 0;
    private int sessionWins = 0;
    private int sessionLosses = 0;
    private int sessionDraws = 0; // Not used in Quick Math, but for API consistency
    private int[] answers = new int[1];
    private String[] questions = new String[1];
    private Random random = new Random();
    private int timerSeconds = 10;
    private android.os.CountDownTimer countDownTimer; // Timer for each question

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_math);
        timerText = findViewById(R.id.timerText);
        questionText = findViewById(R.id.questionText);
        feedbackText = findViewById(R.id.feedbackText);
        scoreText = findViewById(R.id.scoreText);
        answerBtn1 = findViewById(R.id.answerBtn1);
        answerBtn2 = findViewById(R.id.answerBtn2);
        answerBtn3 = findViewById(R.id.answerBtn3);
        answerBtn4 = findViewById(R.id.answerBtn4);
        finishBtn = findViewById(R.id.finishBtn);
        generateQuestion();
        showQuestion();

        View.OnClickListener answerListener = v -> checkAnswer(((Button)v).getText().toString());
        answerBtn1.setOnClickListener(answerListener);
        answerBtn2.setOnClickListener(answerListener);
        answerBtn3.setOnClickListener(answerListener);
        answerBtn4.setOnClickListener(answerListener);
        finishBtn.setOnClickListener(v -> finishGame());
    }

    private void generateQuestion() {
        int a = random.nextInt(20) + 1;
        int b = random.nextInt(20) + 1;
        int op = random.nextInt(4);
        switch (op) {
            case 0:
                questions[0] = a + " + " + b;
                answers[0] = a + b;
                break;
            case 1:
                questions[0] = a + " - " + b;
                answers[0] = a - b;
                break;
            case 2:
                questions[0] = a + " × " + b;
                answers[0] = a * b;
                break;
            case 3:
                int divisor = random.nextInt(10) + 1;
                int dividend = divisor * (random.nextInt(10) + 1);
                questions[0] = dividend + " ÷ " + divisor;
                answers[0] = dividend / divisor;
                break;
        }
    }

    private void showQuestion() {
        questionText.setText(questions[0]);
        feedbackText.setText("");
        // Generate 3 wrong answers and shuffle
        int correct = answers[0];
        int[] options = new int[4];
        options[0] = correct;
        int count = 1;
        while (count < 4) {
            int wrong = correct + random.nextInt(11) - 5;
            if (wrong == correct || contains(options, wrong, count)) continue;
            options[count++] = wrong;
        }
        // Shuffle options
        for (int i = 3; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = options[i]; options[i] = options[j]; options[j] = tmp;
        }
        answerBtn1.setText(String.valueOf(options[0]));
        answerBtn2.setText(String.valueOf(options[1]));
        answerBtn3.setText(String.valueOf(options[2]));
        answerBtn4.setText(String.valueOf(options[3]));
        enableAnswerButtons(true);
        finishBtn.setVisibility(View.GONE);
        startTimer();
    }

    private boolean contains(int[] arr, int val, int len) {
        for (int i = 0; i < len; i++) if (arr[i] == val) return true;
        return false;
    }

    private void checkAnswer(String selected) {
        if (countDownTimer != null) countDownTimer.cancel();
        int userAns = Integer.parseInt(selected);
        boolean correct = userAns == answers[0];
        if (correct) {
            correctAnswers++;
            score += 1;
            sessionPoints += 1;
            scoreText.setText("Score: " + score);
            // Bonus for 10 correct in a row
            if (correctAnswers > 0 && correctAnswers % 10 == 0) {
                sessionPoints += 10;
                showShortToast("+10 bonus!");
            }
            PointManager.getInstance().applySessionPoints(this, sessionPoints, sessionWins, sessionLosses, sessionDraws);
            showShortToast("+1 point");
            // Check win condition
            if (score >= 10) {
                feedbackText.setText("You Win! Score: " + score);
                scoreText.setText("Score: " + score);
                enableAnswerButtons(false);
                finishBtn.setText("Restart");
                finishBtn.setVisibility(View.VISIBLE);
                finishBtn.setOnClickListener(v -> restartGame());
                return;
            }
            // Continue to next question automatically after a short delay
            enableAnswerButtons(false);
            feedbackText.setText("Correct!");
            new android.os.Handler().postDelayed(() -> {
                generateQuestion();
                showQuestion();
            }, 700);
        } else {
            sessionPoints -= 1;
            sessionLosses++;
            PointManager.getInstance().applySessionPoints(this, sessionPoints, sessionWins, sessionLosses, sessionDraws);
            showShortToast("-1 point");
            // Check lose condition
            if (score < 3) {
                feedbackText.setText("You Lose! Score: " + score);
                scoreText.setText("Score: " + score); // Update scoreText
                enableAnswerButtons(false);
                finishBtn.setText("Restart");
                finishBtn.setVisibility(View.VISIBLE);
                finishBtn.setOnClickListener(v -> restartGame());
                return;
            }
            enableAnswerButtons(false);
            feedbackText.setText("Incorrect! Game Over.");
            scoreText.setText("Score: " + score); // Update scoreText
            finishBtn.setText("Restart");
            finishBtn.setVisibility(View.VISIBLE);
            finishBtn.setOnClickListener(v -> restartGame());
        }
    }

    private void enableAnswerButtons(boolean enable) {
        answerBtn1.setEnabled(enable);
        answerBtn2.setEnabled(enable);
        answerBtn3.setEnabled(enable);
        answerBtn4.setEnabled(enable);
    }

    private void finishGame() {
        if (countDownTimer != null) countDownTimer.cancel();
        feedbackText.setText("Game Over!\nScore: " + score);
        scoreText.setText("Score: " + score);
        updatePointsUIAndSync();
        enableAnswerButtons(false);
        finishBtn.setText("Restart");
        finishBtn.setVisibility(View.VISIBLE);
        finishBtn.setOnClickListener(v -> restartGame());
    }

    private void restartGame() {
        score = 0;
        correctAnswers = 0;
        feedbackText.setText("");
        scoreText.setText("Score: 0");
        timerText.setText("Time: " + timerSeconds);
        generateQuestion();
        showQuestion();
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        timerText.setText("Time: " + timerSeconds);
        countDownTimer = new android.os.CountDownTimer(timerSeconds * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time: " + (millisUntilFinished / 1000));
            }
            public void onFinish() {
                timerText.setText("Time: 0");
                feedbackText.setText("Time's up! You lose!");
                enableAnswerButtons(false);
                finishBtn.setText("Restart");
                finishBtn.setVisibility(View.VISIBLE);
                finishBtn.setOnClickListener(v -> restartGame());
            }
        };
        countDownTimer.start();
    }

    private void updatePointsUIAndSync() {
        int points = PointManager.getInstance().getPoints();
        scoreText.setText("Points: " + points);
        PointManager.getInstance().syncPoints(this);
        // --- Update stats and achievements ---
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        int correct = prefs.getInt("quickmath_correct", 0) + correctAnswers;
        int wrong = prefs.getInt("quickmath_wrong", 0) + (10 - correctAnswers);
        int streak = Math.max(prefs.getInt("quickmath_streak", 0), correctAnswers);
        editor.putInt("quickmath_correct", correct);
        editor.putInt("quickmath_wrong", wrong);
        editor.putInt("quickmath_streak", streak);
        if (correct >= 50 && !prefs.getBoolean("ach_math_wizard", false)) {
            editor.putBoolean("ach_math_wizard", true);
        }
        editor.apply();
    }

    private void showShortToast(String message) {
        final android.widget.Toast toast = android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT);
        toast.show();
        new android.os.Handler().postDelayed(toast::cancel, 1000);
    }
}
