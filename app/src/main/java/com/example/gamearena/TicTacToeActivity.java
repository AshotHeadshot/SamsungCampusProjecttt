package com.example.gamearena;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamearena.PointManager;

public class TicTacToeActivity extends AppCompatActivity {
    private Button[][] buttons = new Button[3][3];
    private boolean xTurn = true;
    private int moves = 0;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tictactoe);
        statusText = findViewById(R.id.statusText);
        GridLayout grid = findViewById(R.id.ticGrid);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int idx = i * 3 + j;
                buttons[i][j] = (Button) grid.getChildAt(idx);
                int finalI = i, finalJ = j;
                buttons[i][j].setOnClickListener(v -> onPlayerMove(finalI, finalJ));
            }
        }
        resetBoard();
        // --- Manual Points Test Button ---
        statusText.setOnClickListener(v -> {
            PointManager.getInstance().updateTicTacToeResult(this, "win");
            PointManager.getInstance().syncPoints(this);
            PointManager.getInstance().loadPoints(this);
            int points = PointManager.getInstance().getPoints();
            Toast.makeText(this, "Manual points: " + points, Toast.LENGTH_LONG).show();
        });
    }

    private void onPlayerMove(int i, int j) {
        if (!buttons[i][j].getText().toString().isEmpty()) return;
        buttons[i][j].setText(xTurn ? "X" : "O");
        moves++;
        if (checkWin(xTurn ? "X" : "O")) {
            endGame((xTurn ? "X" : "O") + " wins!");
            return;
        } else if (moves == 9) {
            endGame("Draw!");
            return;
        } else {
            xTurn = !xTurn;
            statusText.setText((xTurn ? "X" : "O") + "'s turn");
        }
        // Bot move if it's O's turn
        if (!xTurn) {
            botMove();
        }
    }

    // Simple bot: pick first available cell
    private void botMove() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().isEmpty()) {
                    buttons[i][j].setText("O");
                    moves++;
                    if (checkWin("O")) {
                        endGame("O wins!");
                        return;
                    } else if (moves == 9) {
                        endGame("Draw!");
                        return;
                    } else {
                        xTurn = true;
                        statusText.setText("X's turn");
                    }
                    return;
                }
            }
        }
    }

    private boolean checkWin(String symbol) {
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().toString().equals(symbol) &&
                buttons[i][1].getText().toString().equals(symbol) &&
                buttons[i][2].getText().toString().equals(symbol))
                return true;
            if (buttons[0][i].getText().toString().equals(symbol) &&
                buttons[1][i].getText().toString().equals(symbol) &&
                buttons[2][i].getText().toString().equals(symbol))
                return true;
        }
        if (buttons[0][0].getText().toString().equals(symbol) &&
            buttons[1][1].getText().toString().equals(symbol) &&
            buttons[2][2].getText().toString().equals(symbol))
            return true;
        if (buttons[0][2].getText().toString().equals(symbol) &&
            buttons[1][1].getText().toString().equals(symbol) &&
            buttons[2][0].getText().toString().equals(symbol))
            return true;
        return false;
    }

    private void endGame(String result) {
        Toast.makeText(this, "[TicTacToe] endGame called: " + result, Toast.LENGTH_SHORT).show();
        String outcome;
        String lowerResult = result.toLowerCase().trim();
        if (lowerResult.startsWith("x") && lowerResult.contains("win")) {
            outcome = "win";
        } else if (lowerResult.startsWith("o") && lowerResult.contains("win")) {
            outcome = "lose";
        } else if (lowerResult.contains("draw")) {
            outcome = "draw";
        } else {
            outcome = "lose";
        }
        Toast.makeText(this, "Outcome for points: " + outcome, Toast.LENGTH_SHORT).show();
        PointManager.getInstance().updateTicTacToeResult(this, outcome);
        // --- Update stats and achievements ---
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (outcome.equals("win")) {
            int wins = prefs.getInt("tictactoe_wins", 0) + 1;
            editor.putInt("tictactoe_wins", wins);
            if (!prefs.getBoolean("ach_first_win", false)) {
                editor.putBoolean("ach_first_win", true);
            }
        } else if (outcome.equals("lose")) {
            int losses = prefs.getInt("tictactoe_losses", 0) + 1;
            editor.putInt("tictactoe_losses", losses);
        } else if (outcome.equals("draw")) {
            int draws = prefs.getInt("tictactoe_draws", 0) + 1;
            editor.putInt("tictactoe_draws", draws);
        }
        editor.apply();
        updatePointsUIAndSync();
        statusText.setText(result);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                buttons[i][j].setEnabled(false);
        Button resetBtn = findViewById(R.id.resetBtn);
        resetBtn.setVisibility(View.VISIBLE);
    }

    private void updatePointsUIAndSync() {
        PointManager.getInstance().syncPoints(this);
        PointManager.getInstance().loadPoints(this);
        int points = PointManager.getInstance().getPoints();
        statusText.setText("Points: " + points);
        Toast.makeText(this, "[TicTacToe] Syncing points: " + points, Toast.LENGTH_SHORT).show();
    }

    public void onReset(View v) {
        resetBoard();
        updatePointsUIAndSync();
    }

    private void resetBoard() {
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                buttons[i][j].setEnabled(true);
            }
        moves = 0;
        xTurn = true;
        statusText.setText("X's turn");
        Button resetBtn = findViewById(R.id.resetBtn);
        resetBtn.setVisibility(View.GONE);
    }
}
