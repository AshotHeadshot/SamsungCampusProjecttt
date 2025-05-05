package com.example.gamearena;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;
import com.example.gamearena.PointManager;

public class RockPaperScissorsActivity extends AppCompatActivity {
    private Button rockBtn, paperBtn, scissorsBtn, playAgainBtn;
    private TextView statusText, resultText;
    private ImageView playerHand, computerHand;
    private String[] choices = {"Rock", "Paper", "Scissors"};
    private int[] handDrawables = {R.drawable.ic_hand_rock, R.drawable.ic_hand_paper, R.drawable.ic_hand_scissors};
    private Random random = new Random();
    private int userChoice = -1;
    private int botChoice = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        View.OnClickListener listener = v -> {
            if (v == rockBtn) userChoice = 0;
            else if (v == paperBtn) userChoice = 1;
            else userChoice = 2;
            playRound();
        };
        rockBtn.setOnClickListener(listener);
        paperBtn.setOnClickListener(listener);
        scissorsBtn.setOnClickListener(listener);
        playAgainBtn.setOnClickListener(v -> resetGame());
    }

    private void playRound() {
        botChoice = random.nextInt(3);
        String userStr = choices[userChoice];
        String botStr = choices[botChoice];
        // Update hand images
        playerHand.setImageResource(handDrawables[userChoice]);
        computerHand.setImageResource(handDrawables[botChoice]);
        String result;
        String pointResult;
        String outcome;
        if (userChoice == botChoice) {
            result = "Draw! ";
            pointResult = "+2 points";
            outcome = "draw";
        } else if ((userChoice == 0 && botChoice == 2) || (userChoice == 1 && botChoice == 0) || (userChoice == 2 && botChoice == 1)) {
            result = "You win! ";
            pointResult = "+5 points";
            outcome = "win";
        } else {
            result = "You lose! ";
            pointResult = "-5 points";
            outcome = "lose";
        }
        statusText.setText("You: " + userStr + "  |  Bot: " + botStr);
        resultText.setText(result + pointResult);
        // Update points using PointManager
        PointManager.getInstance().updateRPSResult(outcome);
        updatePointsUIAndSync();
        rockBtn.setEnabled(false);
        paperBtn.setEnabled(false);
        scissorsBtn.setEnabled(false);
        playAgainBtn.setVisibility(View.VISIBLE);
    }

    private void updatePointsUIAndSync() {
        int points = PointManager.getInstance().getPoints();
        statusText.setText("Points: " + points);
        PointManager.getInstance().syncPoints(this);
    }

    private void resetGame() {
        statusText.setText("Choose Rock, Paper, or Scissors");
        resultText.setText("");
        rockBtn.setEnabled(true);
        paperBtn.setEnabled(true);
        scissorsBtn.setEnabled(true);
        playAgainBtn.setVisibility(View.GONE);
    }
}
