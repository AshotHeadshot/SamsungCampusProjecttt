package com.example.gamearena;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class AliasActivity extends AppCompatActivity {
    private String[] words = {
        "happy", "car", "fast", "dog", "big", "apple", "mountain", "river", "computer", "music", "friend", "school", "family", "travel", "book", "phone", "movie", "game", "city", "country", "teacher", "student", "animal", "flower", "food", "water", "coffee", "sun", "moon", "star", "tree", "cloud", "rain", "snow", "wind", "fire", "earth", "sky", "ocean", "beach", "forest", "desert", "island", "bridge", "road", "train", "plane", "bicycle", "bus", "boat", "ship", "hotel", "restaurant", "market", "shop", "bank", "hospital", "doctor", "nurse", "police", "fireman", "actor", "singer", "artist", "writer", "painter", "dancer", "athlete", "coach", "judge", "lawyer", "pilot", "driver", "engineer", "scientist", "chef", "waiter", "farmer", "builder", "mechanic", "plumber", "electrician", "carpenter", "dentist", "pharmacist", "veterinarian", "journalist", "designer", "model", "director", "producer", "manager", "secretary", "assistant", "clerk", "guard", "soldier", "officer", "mayor", "president", "minister", "ambassador", "king", "queen", "prince", "princess", "duke", "duchess", "knight", "wizard", "witch", "giant", "dwarf", "elf", "fairy", "dragon", "monster", "ghost", "zombie", "vampire", "robot", "alien", "superhero", "villain", "detective", "spy", "pirate", "ninja", "samurai", "cowboy", "indian", "explorer", "hunter", "fisherman", "gardener", "baker", "butcher", "tailor", "shoemaker", "barber", "hairdresser", "cleaner", "janitor", "porter", "delivery", "postman", "cashier", "accountant", "consultant", "analyst", "advisor", "inspector", "controller", "planner", "organizer", "leader", "member", "guest", "host", "visitor", "tourist", "customer", "client", "partner", "neighbor", "stranger", "enemy", "rival", "champion", "winner", "loser", "player", "coach", "referee", "spectator", "fan", "supporter", "audience", "crew", "staff", "team", "group", "band", "choir", "orchestra", "company", "club", "association", "union", "community", "society", "organization", "institution", "agency", "office", "department", "division", "branch", "section", "unit", "zone", "area", "region", "district", "province", "state", "nation", "continent", "planet", "world", "universe"
    };
    private int currentIdx = 0;
    private TextView wordText, resultText, timerText, scoreText;
    private TextView team1ScoreText, team2ScoreText; // Team scores
    private Button correctBtn, skipBtn, startBtn;
    private Random random = new Random();
    private int score = 0;
    private int team1Score = 0;
    private int team2Score = 0;
    private int currentTeam = 1; // 1 or 2
    private CountDownTimer timer;
    private boolean gameActive = false;
    private int roundTime = 60; // seconds
    private int totalRounds = 2;
    private int roundCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alias);
        wordText = findViewById(R.id.wordText);
        resultText = findViewById(R.id.resultText);
        correctBtn = findViewById(R.id.correctBtn);
        skipBtn = findViewById(R.id.skipBtn);
        timerText = findViewById(R.id.timerText);
        scoreText = findViewById(R.id.scoreText);
        team1ScoreText = findViewById(R.id.team1ScoreText);
        team2ScoreText = findViewById(R.id.team2ScoreText);
        startBtn = findViewById(R.id.startBtn);
        setGameState(false);

        startBtn.setOnClickListener(v -> startGame());
        correctBtn.setOnClickListener(v -> {
            if (!gameActive) return;
            if (currentTeam == 1) {
                team1Score++;
                team1ScoreText.setText("Team 1: " + team1Score);
            } else {
                team2Score++;
                team2ScoreText.setText("Team 2: " + team2Score);
            }
            score++;
            scoreText.setText("Score: " + score);
            resultText.setText("Correct! Team " + currentTeam + " scored!");
            resultText.setTextColor(0xFF388E3C);
            showNewWord();
        });
        skipBtn.setOnClickListener(v -> {
            if (!gameActive) return;
            resultText.setText("");
            showNewWord();
        });
    }

    private void startGame() {
        score = 0;
        team1Score = 0;
        team2Score = 0;
        currentTeam = 1;
        roundCount = 1;
        if (team1ScoreText != null) team1ScoreText.setText("Team 1: 0");
        if (team2ScoreText != null) team2ScoreText.setText("Team 2: 0");
        resultText.setText("Team 1's turn!");
        startTeamRound();
    }

    private void startTeamRound() {
        setGameState(true);
        showNewWord();
        timer = new CountDownTimer(roundTime * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                timerText.setText("Time: " + millisUntilFinished / 1000);
            }
            public void onFinish() {
                setGameState(false);
                if (roundCount < totalRounds) {
                    // Switch team
                    currentTeam = (currentTeam == 1) ? 2 : 1;
                    roundCount++;
                    resultText.setText("Time's up! Now Team " + currentTeam + "'s turn!");
                    // Start next team after short delay
                    timerText.postDelayed(() -> startTeamRound(), 2000);
                } else {
                    // Game over, show both scores
                    resultText.setText("Game Over!\nTeam 1: " + team1Score + "\nTeam 2: " + team2Score);
                }
            }
        }.start();
    }

    private void setGameState(boolean active) {
        gameActive = active;
        correctBtn.setEnabled(active);
        skipBtn.setEnabled(active);
        startBtn.setEnabled(!active);
        if (!active) {
            timerText.setText("Time: 0");
        }
        scoreText.setText("Score: " + score);
    }

    private void showNewWord() {
        if (!gameActive) return;
        currentIdx = random.nextInt(words.length);
        wordText.setText(words[currentIdx]);
    }
}
