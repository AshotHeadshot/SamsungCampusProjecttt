package com.example.gamearena;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TicTacToeBotActivity extends AppCompatActivity implements View.OnClickListener {

    private Button[][] buttons = new Button[3][3];
    private boolean player1Turn = true;
    private int roundCount;
    private int player1Points;
    private int player2Points;
    private TextView textViewPlayer1;
    private TextView textViewPlayer2;
    private boolean gameActive = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tictactoebot);

        textViewPlayer1 = findViewById(R.id.text_view_p1);
        textViewPlayer2 = findViewById(R.id.text_view_p2);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String buttonID = "button_" + i + j;
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                buttons[i][j] = findViewById(resID);
                buttons[i][j].setOnClickListener(this);
            }
        }

        Button buttonReset = findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetGame();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (!gameActive) {
            return;
        }

        if (!((Button) v).getText().toString().equals("")) {
            return;
        }

        if (player1Turn) {
            ((Button) v).setText("X");
        } else {
            ((Button) v).setText("O");
        }

        roundCount++;

        if (checkForWin()) {
            if (player1Turn) {
                player1Wins();
            } else {
                player2Wins();
            }
        } else if (roundCount == 9) {
            draw();
        } else {
            player1Turn = !player1Turn;
            // AI's turn if it's player 2's turn
            if (!player1Turn) {
                aiMove();
            }
        }
    }

    private boolean checkForWin() {
        String[][] field = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                field[i][j] = buttons[i][j].getText().toString();
            }
        }
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (field[i][0].equals(field[i][1]) && field[i][0].equals(field[i][2]) && !field[i][0].equals("")) {
                return true;
            }
        }
        // Check columns
        for (int i = 0; i < 3; i++) {
            if (field[0][i].equals(field[1][i]) && field[0][i].equals(field[2][i]) && !field[0][i].equals("")) {
                return true;
            }
        }
        // Check diagonals
        if (field[0][0].equals(field[1][1]) && field[0][0].equals(field[2][2]) && !field[0][0].equals("")) {
            return true;
        }
        if (field[0][2].equals(field[1][1]) && field[0][2].equals(field[2][0]) && !field[0][2].equals("")) {
            return true;
        }
        return false;
    }

    private void player1Wins() {
        player1Points++;
        Toast.makeText(this, "Player wins!", Toast.LENGTH_SHORT).show();
        updatePointsText();
        PointManager.getInstance().updateTicTacToeResult(this, "win");
        PointManager.getInstance().syncPoints(this);
        PointManager.getInstance().loadPoints(this);
        gameActive = false;
    }

    private void player2Wins() {
        player2Points++;
        Toast.makeText(this, "AI wins!", Toast.LENGTH_SHORT).show();
        updatePointsText();
        PointManager.getInstance().updateTicTacToeResult(this, "lose");
        PointManager.getInstance().syncPoints(this);
        PointManager.getInstance().loadPoints(this);
        gameActive = false;
    }

    private void draw() {
        Toast.makeText(this, "Draw!", Toast.LENGTH_SHORT).show();
        PointManager.getInstance().updateTicTacToeResult(this, "draw");
        PointManager.getInstance().syncPoints(this);
        PointManager.getInstance().loadPoints(this);
        gameActive = false;
    }

    private void updatePointsText() {
        textViewPlayer1.setText("Player: " + player1Points);
        textViewPlayer2.setText("AI: " + player2Points);
    }

    private void resetGame() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
            }
        }
        roundCount = 0;
        player1Turn = true;
        gameActive = true;
    }

    private void aiMove() {
        // Simple AI logic - first try to win, then block, then random move
        // 1. Check if AI can win
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().equals("")) {
                    buttons[i][j].setText("O");
                    roundCount++;
                    if (checkForWin()) {
                        player2Wins();
                        return;
                    } else if (roundCount == 9) {
                        draw();
                        return;
                    } else {
                        buttons[i][j].setText("");
                        roundCount--;
                    }
                }
            }
        }
        // 2. Check if player can win and block
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().equals("")) {
                    buttons[i][j].setText("X");
                    if (checkForWin()) {
                        buttons[i][j].setText("O");
                        roundCount++;
                        if (checkForWin()) {
                            player2Wins();
                            return;
                        } else if (roundCount == 9) {
                            draw();
                            return;
                        } else {
                            player1Turn = true;
                            return;
                        }
                    } else {
                        buttons[i][j].setText("");
                    }
                }
            }
        }
        // 3. Take center if available
        if (buttons[1][1].getText().toString().equals("")) {
            buttons[1][1].setText("O");
            roundCount++;
            if (checkForWin()) {
                player2Wins();
                return;
            } else if (roundCount == 9) {
                draw();
                return;
            }
            player1Turn = true;
            return;
        }
        // 4. Take a corner if available
        int[][] corners = {{0,0}, {0,2}, {2,0}, {2,2}};
        for (int[] corner : corners) {
            if (buttons[corner[0]][corner[1]].getText().toString().equals("")) {
                buttons[corner[0]][corner[1]].setText("O");
                roundCount++;
                if (checkForWin()) {
                    player2Wins();
                    return;
                } else if (roundCount == 9) {
                    draw();
                    return;
                }
                player1Turn = true;
                return;
            }
        }
        // 5. Take any available spot
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().equals("")) {
                    buttons[i][j].setText("O");
                    roundCount++;
                    if (checkForWin()) {
                        player2Wins();
                        return;
                    } else if (roundCount == 9) {
                        draw();
                        return;
                    }
                    player1Turn = true;
                    return;
                }
            }
        }
        player1Turn = true;
    }
}
