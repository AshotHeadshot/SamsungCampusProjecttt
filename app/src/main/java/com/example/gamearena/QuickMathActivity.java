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
    private TextView progressText, questionText, feedbackText, scoreText;
    private EditText answerInput;
    private Button submitBtn, finishBtn;
    private int currentQuestion = 0;
    private int correctAnswers = 0;
    private int score = 0;
    private int[] answers = new int[10];
    private String[] questions = new String[10];
    private Random random = new Random();
    private int[] userAnswers = new int[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_math);
        progressText = findViewById(R.id.progressText);
        questionText = findViewById(R.id.questionText);
        feedbackText = findViewById(R.id.feedbackText);
        scoreText = findViewById(R.id.scoreText);
        answerInput = findViewById(R.id.answerInput);
        submitBtn = findViewById(R.id.submitBtn);
        finishBtn = findViewById(R.id.finishBtn);
        generateQuestions();
        showQuestion();

        submitBtn.setOnClickListener(v -> checkAnswer());
        finishBtn.setOnClickListener(v -> finishGame());
    }

    private void generateQuestions() {
        for (int i = 0; i < 10; i++) {
            int a = random.nextInt(20) + 1;
            int b = random.nextInt(20) + 1;
            int op = random.nextInt(4);
            switch (op) {
                case 0:
                    questions[i] = a + " + " + b;
                    answers[i] = a + b;
                    break;
                case 1:
                    questions[i] = a + " - " + b;
                    answers[i] = a - b;
                    break;
                case 2:
                    questions[i] = a + " × " + b;
                    answers[i] = a * b;
                    break;
                case 3:
                    int divisor = random.nextInt(10) + 1;
                    int dividend = divisor * (random.nextInt(10) + 1);
                    questions[i] = dividend + " ÷ " + divisor;
                    answers[i] = dividend / divisor;
                    break;
            }
        }
    }

    private void showQuestion() {
        progressText.setText("Question " + (currentQuestion + 1) + "/10");
        questionText.setText(questions[currentQuestion]);
        answerInput.setText("");
        feedbackText.setText("");
    }

    private void checkAnswer() {
        String input = answerInput.getText().toString().trim();
        if (input.isEmpty()) {
            feedbackText.setText("Enter your answer");
            return;
        }
        int userAns = Integer.parseInt(input);
        userAnswers[currentQuestion] = userAns;
        boolean correct = userAns == answers[currentQuestion];
        if (correct) {
            correctAnswers++;
            score += 2;
            feedbackText.setText("Correct! +2 points");
        } else {
            feedbackText.setText("Wrong! Correct: " + answers[currentQuestion]);
        }
        scoreText.setText("Score: " + score);
        PointManager.getInstance().updateQuickMath(correct);
        updatePointsUIAndSync();
        submitBtn.setEnabled(false);
        finishBtn.setVisibility(View.VISIBLE);
        if (currentQuestion < 9) {
            finishBtn.setText("Next");
            finishBtn.setOnClickListener(v -> {
                currentQuestion++;
                submitBtn.setEnabled(true);
                finishBtn.setVisibility(View.GONE);
                showQuestion();
            });
        } else {
            finishBtn.setText("Finish");
            finishBtn.setOnClickListener(v -> finishGame());
        }
    }

    private void finishGame() {
        int bonus = 0;
        if (correctAnswers == 10) bonus = 10;
        else if (correctAnswers >= 8) bonus = 5;
        if (bonus > 0 && correctAnswers == 10) {
            // Only +10 bonus for 10 correct in a row (handled by PointManager)
            // No need to add here, PointManager already does
        } else {
            score += bonus;
        }
        String bonusMsg = bonus > 0 ? "\nBonus: +" + bonus + " points" : "";
        feedbackText.setText("You got " + correctAnswers + "/10 correct!\nTotal: " + score + " points" + bonusMsg);
        scoreText.setText("Score: " + score);
        updatePointsUIAndSync();
        submitBtn.setEnabled(false);
        finishBtn.setVisibility(View.GONE);
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
}
