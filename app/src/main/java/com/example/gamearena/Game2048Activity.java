package com.example.gamearena;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import java.util.Random;
import java.util.ArrayList;
import com.example.gamearena.PointManager;
import android.content.SharedPreferences;

public class Game2048Activity extends AppCompatActivity {
    // Bonus flags for 2048
    private boolean bonus1024Given = false;
    private boolean bonus2048Given = false;

    private static final int SIZE = 4;
    private int[][] board = new int[SIZE][SIZE];
    private TextView[][] cells = new TextView[SIZE][SIZE];
    private TextView scoreView;
    private TextView bestTileView;
    private android.widget.Button restartBtn;
    private int score = 0;
    private int bestTile = 0;
    private java.util.HashSet<Integer> notifiedMergeValues = new java.util.HashSet<>();
    private int sessionPoints = 0;
    private int sessionWins = 0;
    private int sessionLosses = 0;
    private int sessionDraws = 0;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);

        // Initialize UI elements
        scoreView = findViewById(R.id.score);
        bestTileView = findViewById(R.id.bestTile);
        restartBtn = findViewById(R.id.restartBtn);
        restartBtn.setVisibility(View.GONE);
        restartBtn.setOnClickListener(v -> {
            restartBtn.setVisibility(View.GONE);
            startNewGame();
        });
        cells[0][0] = findViewById(R.id.cell00);
        cells[0][1] = findViewById(R.id.cell01);
        cells[0][2] = findViewById(R.id.cell02);
        cells[0][3] = findViewById(R.id.cell03);
        cells[1][0] = findViewById(R.id.cell10);
        cells[1][1] = findViewById(R.id.cell11);
        cells[1][2] = findViewById(R.id.cell12);
        cells[1][3] = findViewById(R.id.cell13);
        cells[2][0] = findViewById(R.id.cell20);
        cells[2][1] = findViewById(R.id.cell21);
        cells[2][2] = findViewById(R.id.cell22);
        cells[2][3] = findViewById(R.id.cell23);
        cells[3][0] = findViewById(R.id.cell30);
        cells[3][1] = findViewById(R.id.cell31);
        cells[3][2] = findViewById(R.id.cell32);
        cells[3][3] = findViewById(R.id.cell33);

        // Set up gesture detection
        gestureDetector = new GestureDetector(this, new GestureListener());

        // Initialize game
        startNewGame();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private void startNewGame() {
        // Clear the board
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = 0;
                updateCell(i, j);
            }
        }
        bestTile = 0;
        updateBestTile();
        score = 0;
        updateScore();
        restartBtn.setVisibility(View.GONE);
        bonus1024Given = false;
        bonus2048Given = false;
        // Add two initial tiles
        addRandomTile();
        addRandomTile();
    }

    private void addRandomTile() {
        Random random = new Random();
        int value = random.nextInt(10) < 9 ? 2 : 4; // 90% chance for 2, 10% for 4

        ArrayList<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
            board[cell[0]][cell[1]] = value;
            updateCell(cell[0], cell[1]);
        }
    }

    private void updateCell(int i, int j) {
        TextView cell = cells[i][j];
        int value = board[i][j];

        if (value == 0) {
            cell.setText("");
            cell.setBackgroundResource(R.drawable.cell_empty);
        } else {
            cell.setText(String.valueOf(value));

            // Set different colors for different values
            switch (value) {
                case 2: cell.setBackgroundResource(R.drawable.cell_2); break;
                case 4: cell.setBackgroundResource(R.drawable.cell_4); break;
                case 8: cell.setBackgroundResource(R.drawable.cell_8); break;
                case 16: cell.setBackgroundResource(R.drawable.cell_16); break;
                case 32: cell.setBackgroundResource(R.drawable.cell_32); break;
                case 64: cell.setBackgroundResource(R.drawable.cell_64); break;
                case 128: cell.setBackgroundResource(R.drawable.cell_128); break;
                case 256: cell.setBackgroundResource(R.drawable.cell_256); break;
                case 512: cell.setBackgroundResource(R.drawable.cell_512); break;
                case 1024: cell.setBackgroundResource(R.drawable.cell_1024); break;
                case 2048: cell.setBackgroundResource(R.drawable.cell_2048); break;
                default: cell.setBackgroundResource(R.drawable.cell_super); break;
            }
        }
    }

    private void updateScore() {
        scoreView.setText("Score: " + score);
        updateBestTile();
        bestTileView.setText("Best tile: " + bestTile);
    }

    private boolean moveTiles(int direction) {
        boolean moved = false;
        int[][] oldBoard = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(board[i], 0, oldBoard[i], 0, SIZE);
        }

        switch (direction) {
            case 0: // UP
                moveUp();
                break;
            case 1: // RIGHT
                moveRight();
                break;
            case 2: // DOWN
                moveDown();
                break;
            case 3: // LEFT
                moveLeft();
                break;
        }

        // Check if any tile moved
        for (int i = 0; i < SIZE && !moved; i++) {
            for (int j = 0; j < SIZE && !moved; j++) {
                if (board[i][j] != oldBoard[i][j]) {
                    moved = true;
                }
            }
        }

        if (moved) {
            addRandomTile();
            updateBoard();
            updateScore();

            if (hasReached2048() || isGameOver()) {
                restartBtn.setVisibility(View.VISIBLE);
            }
        }
        return moved;
    }

    private void moveUp() {
        for (int j = 0; j < SIZE; j++) {
            int[] column = new int[SIZE];
            for (int i = 0; i < SIZE; i++) {
                column[i] = board[i][j];
            }

            column = moveAndMerge(column);

            for (int i = 0; i < SIZE; i++) {
                board[i][j] = column[i];
            }
        }
    }

    private void moveRight() {
        for (int i = 0; i < SIZE; i++) {
            int[] row = new int[SIZE];
            for (int j = 0; j < SIZE; j++) {
                row[SIZE - 1 - j] = board[i][j];
            }

            row = moveAndMerge(row);

            for (int j = 0; j < SIZE; j++) {
                board[i][j] = row[SIZE - 1 - j];
            }
        }
    }

    private void moveDown() {
        for (int j = 0; j < SIZE; j++) {
            int[] column = new int[SIZE];
            for (int i = 0; i < SIZE; i++) {
                column[SIZE - 1 - i] = board[i][j];
            }

            column = moveAndMerge(column);

            for (int i = 0; i < SIZE; i++) {
                board[i][j] = column[SIZE - 1 - i];
            }
        }
    }

    private void moveLeft() {
        for (int i = 0; i < SIZE; i++) {
            int[] row = new int[SIZE];
            System.arraycopy(board[i], 0, row, 0, SIZE);

            row = moveAndMerge(row);
            System.arraycopy(row, 0, board[i], 0, SIZE);
        }
    }

    // Restored moveAndMerge method
    private int[] moveAndMerge(int[] line) {
        int[] newLine = new int[SIZE];
        int position = 0;
        // Move non-zero elements to the front
        for (int num : line) {
            if (num != 0) {
                newLine[position++] = num;
            }
        }
        // Merge adjacent equal numbers
        for (int i = 0; i < SIZE - 1; i++) {
            if (newLine[i] != 0 && newLine[i] == newLine[i + 1]) {
                int mergedValue = newLine[i] * 2;
                newLine[i] = mergedValue;
                sessionPoints += 1; // +1 per merge
                if (!notifiedMergeValues.contains(mergedValue)) {
                    showShortToast("+1 point");
                    notifiedMergeValues.add(mergedValue);
                }
                score += mergedValue;
                PointManager.getInstance().applySessionPoints(this, 1, 0, 0, 0);
                PointManager.getInstance().syncPoints(this);
                // +10 for first 1024 tile
                if (mergedValue == 1024 && !bonus1024Given) {
                    sessionPoints += 10;
                    PointManager.getInstance().applySessionPoints(this, 10, 0, 0, 0);
                    PointManager.getInstance().syncPoints(this);
                    showShortToast("+10 bonus for 1024!");
                    bonus1024Given = true;
                }
                // +20 for first 2048 tile
                if (mergedValue == 2048 && !bonus2048Given) {
                    sessionPoints += 20;
                    PointManager.getInstance().applySessionPoints(this, 20, 0, 0, 0);
                    PointManager.getInstance().syncPoints(this);
                    showShortToast("+20 bonus for 2048!");
                    bonus2048Given = true;
                }
                // Immediately sync points when earned
                PointManager.getInstance().applySessionPoints(this, sessionPoints, sessionWins, sessionLosses, sessionDraws);
                // Shift the rest of the array left
                for (int j = i + 1; j < SIZE - 1; j++) {
                    newLine[j] = newLine[j + 1];
                }
                newLine[SIZE - 1] = 0;
            }
        }
        return newLine;
    }

    private void updateBoard() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                updateCell(i, j);
            }
        }
    }

    private void updateBestTile() {
        int newBest = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] > newBest) newBest = board[i][j];
            }
        }
        bestTile = newBest;
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        if (bestTile > prefs.getInt("2048_best_tile", 0)) {
            android.content.SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("2048_best_tile", bestTile);
            if (bestTile >= 2048 && !prefs.getBoolean("ach_2048_genius", false)) {
                editor.putBoolean("ach_2048_genius", true);
            }
            editor.apply();
        }
    }

    // Restored showEndGameSummaryDialog method
    // Dialog removed, handled by restart button UI
    

    private boolean hasReached2048() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 2048) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isGameOver() {
        // Check for empty cells
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }

        // Check for possible merges
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if ((j < SIZE - 1 && board[i][j] == board[i][j + 1]) ||
                        (i < SIZE - 1 && board[i][j] == board[i + 1][j])) {
                    return false;
                }
            }
        }

        return true;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            result = moveTiles(1); // RIGHT
                        } else {
                            result = moveTiles(3); // LEFT
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            result = moveTiles(2); // DOWN
                        } else {
                            result = moveTiles(0); // UP
                        }
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    private void showShortToast(String message) {
        final Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
        new android.os.Handler().postDelayed(toast::cancel, 1000);
    }
}
