package com.example.gamearena;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;
import com.example.gamearena.PointManager;

public class RockPaperScissorsActivity extends AppCompatActivity {
    private int playerScore = 0;
    private int computerScore = 0;
    private TextView playerScoreText, computerScoreText;
    private Button rockBtn, paperBtn, scissorsBtn, playAgainBtn;
    private TextView statusText, resultText;
    private ImageView playerHand, computerHand;
    private int sessionPoints = 0;
    private int sessionWins = 0;
    private int sessionLosses = 0;
    private int sessionDraws = 0;
    private String[] choices = {"Rock", "Paper", "Scissors"};
    private int[] handDrawables = {R.drawable.ic_hand_rock, R.drawable.ic_hand_paper, R.drawable.ic_hand_scissors};
    private Random random = new Random();
    private int userChoice = -1;
    private int botChoice = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Restore scores from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("rps_scores", MODE_PRIVATE);
        playerScore = prefs.getInt("playerScore", 0);
        computerScore = prefs.getInt("computerScore", 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rock_paper_scissors);
        statusText = findViewById(R.id.statusText);
        resultText = findViewById(R.id.resultText);
        rockBtn = findViewById(R.id.rockBtn);
        paperBtn = findViewById(R.id.paperBtn);
        scissorsBtn = findViewById(R.id.scissorsBtn);
        playAgainBtn = findViewById(R.id.playAgainBtn);
        playerHand = findViewById(R.id.playerHand);
        computerHand = findViewById(R.id.computerHand);
        playerScoreText = findViewById(R.id.playerScore);
        computerScoreText = findViewById(R.id.computerScore);
        updateScoreUI();

        View.OnClickListener listener = v -> {
            if (v == rockBtn) userChoice = 0;
            else if (v == paperBtn) userChoice = 1;
            else userChoice = 2;
            playRound();
        };
        rockBtn.setOnClickListener(listener);
        paperBtn.setOnClickListener(listener);
        scissorsBtn.setOnClickListener(listener);
        playAgainBtn.setOnClickListener(v -> {
            PointManager.getInstance().applySessionPoints(this, sessionPoints, sessionWins, sessionLosses, sessionDraws);
            sessionPoints = 0;
            sessionWins = 0;
            sessionLosses = 0;
            sessionDraws = 0;
            resetGame();
        });
    }

    private void playRound() {
        // ...
        // After updating scores, save them
        saveScores();
        // Update scores
        int prevPlayerScore = playerScore;
        int prevComputerScore = computerScore;
        botChoice = random.nextInt(3);
        String userStr = choices[userChoice];
        String botStr = choices[botChoice];
        // Update hand images
        playerHand.setImageResource(handDrawables[userChoice]);
        computerHand.setImageResource(handDrawables[botChoice]);
        String result;
        String pointResult;
        String outcome;
        int deltaPoints = 0;
        if (userChoice == botChoice) {
            result = "Draw! ";
            pointResult = "+2 point";
            sessionPoints += 2;
            sessionDraws++;
            // No score change for draw
            PointManager.getInstance().applySessionPoints(this, sessionPoints, sessionWins, sessionLosses, sessionDraws);
            showShortToast("+2 point");
        } else if ((userChoice == 0 && botChoice == 2) || (userChoice == 1 && botChoice == 0) || (userChoice == 2 && botChoice == 1)) {
            result = "You win! ";
            pointResult = "+5 point";
            sessionPoints += 5;
            sessionWins++;
            playerScore++;
            PointManager.getInstance().applySessionPoints(this, sessionPoints, sessionWins, sessionLosses, sessionDraws);
            showShortToast("+5 point");
        } else {
            result = "You lose! ";
            pointResult = "-5 point";
            sessionPoints -= 5;
            sessionLosses++;
            computerScore++;
            PointManager.getInstance().applySessionPoints(this, sessionPoints, sessionWins, sessionLosses, sessionDraws);
            showShortToast("-5 point");
        }
        updateScoreUI();
        statusText.setText("You: " + userStr + "  |  Bot: " + botStr);
        resultText.setText(result + pointResult + " | Total: " + sessionPoints + " point");
        rockBtn.setEnabled(false);
        paperBtn.setEnabled(false);
        scissorsBtn.setEnabled(false);
        playAgainBtn.setVisibility(View.VISIBLE);
    }

    private void updatePointsUIAndSync() {
        PointManager.getInstance().syncPoints(this);
    }

    private void resetGame() {
        statusText.setText("Choose Rock, Paper, or Scissors");
        resultText.setText("");
        rockBtn.setEnabled(true);
        paperBtn.setEnabled(true);
        scissorsBtn.setEnabled(true);
        playAgainBtn.setVisibility(View.GONE);
        updateScoreUI();
        saveScores();
    }

    private void updateScoreUI() {
        if (playerScoreText != null) playerScoreText.setText(String.valueOf(playerScore));
        if (computerScoreText != null) computerScoreText.setText(String.valueOf(computerScore));
    }

    private void saveScores() {
        SharedPreferences prefs = getSharedPreferences("rps_scores", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("playerScore", playerScore);
        editor.putInt("computerScore", computerScore);
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveScores();
    }

    private void showShortToast(String message) {
        final Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
        new android.os.Handler().postDelayed(toast::cancel, 1000);
    }
}
