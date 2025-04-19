package com.example.gamearena;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.GridLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.ViewGroup;
import android.view.View;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import java.util.LinkedList;
import java.util.Random;
import java.util.ArrayList;
import com.example.gamearena.PointManager;
import android.widget.Toast;

public class SnakeActivity extends AppCompatActivity {
    private static final int GRID_SIZE = 15;
    private static final int INITIAL_SNAKE_LENGTH = 3;
    private static final int CELL_SIZE_DP = 20;
    private static final int UPDATE_DELAY = 200; // ms
    private static final int FOOD_COUNT = 3; // Number of red squares

    private enum Direction { UP, DOWN, LEFT, RIGHT }

    private GridLayout snakeGrid;
    private TextView scoreText;
    private Handler handler = new Handler();
    private GestureDetector gestureDetector;
    private LinkedList<int[]> snake = new LinkedList<>();
    private ArrayList<int[]> foodList = new ArrayList<>();
    private Direction currentDirection = Direction.RIGHT;
    private boolean isGameOver = false;
    private int score = 0;
    private View[][] cellViews = new View[GRID_SIZE][GRID_SIZE];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake);
        scoreText = findViewById(R.id.scoreText);
        snakeGrid = findViewById(R.id.snakeGrid);
        gestureDetector = new GestureDetector(this, new GestureListener());
        setupGrid();
        startNewGame();
    }

    private void setupGrid() {
        snakeGrid.removeAllViews();
        int cellSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CELL_SIZE_DP, getResources().getDisplayMetrics());
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                View cell = new View(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSizePx;
                params.height = cellSizePx;
                cell.setLayoutParams(params);
                cell.setBackgroundColor(Color.parseColor("#E0E0E0"));
                snakeGrid.addView(cell);
                cellViews[i][j] = cell;
            }
        }
    }

    private void startNewGame() {
        snake.clear();
        foodList.clear();
        isGameOver = false;
        score = 0;
        currentDirection = Direction.RIGHT;
        int center = GRID_SIZE / 2;
        for (int i = 0; i < INITIAL_SNAKE_LENGTH; i++) {
            snake.add(new int[]{center, center - i});
        }
        for (int i = 0; i < FOOD_COUNT; i++) {
            placeFood();
        }
        updateGrid();
        updateScore();
        handler.removeCallbacks(gameRunnable);
        handler.postDelayed(gameRunnable, UPDATE_DELAY);
    }

    private void placeFood() {
        Random rand = new Random();
        while (true) {
            int x = rand.nextInt(GRID_SIZE);
            int y = rand.nextInt(GRID_SIZE);
            boolean onSnake = false;
            for (int[] pos : snake) {
                if (pos[0] == x && pos[1] == y) {
                    onSnake = true;
                    break;
                }
            }
            boolean onFood = false;
            for (int[] f : foodList) {
                if (f[0] == x && f[1] == y) {
                    onFood = true;
                    break;
                }
            }
            if (!onSnake && !onFood) {
                foodList.add(new int[]{x, y});
                break;
            }
        }
    }

    private void updateGrid() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                cellViews[i][j].setBackgroundColor(Color.parseColor("#E0E0E0"));
            }
        }
        for (int k = 0; k < snake.size(); k++) {
            int[] pos = snake.get(k);
            if (k == 0) {
                cellViews[pos[0]][pos[1]].setBackgroundColor(Color.parseColor("#388E3C"));
            } else {
                cellViews[pos[0]][pos[1]].setBackgroundColor(Color.parseColor("#66BB6A"));
            }
        }
        for (int[] f : foodList) {
            cellViews[f[0]][f[1]].setBackgroundColor(Color.parseColor("#FF5252"));
        }
    }

    private void updateScore() {
        scoreText.setText("Score: " + score);
    }

    private void updatePointsUIAndSync() {
        int points = PointManager.getInstance().getPoints();
        scoreText.setText("Points: " + points);
        PointManager.getInstance().syncPoints(this);
    }

    private final Runnable gameRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isGameOver) {
                moveSnake();
                handler.postDelayed(this, UPDATE_DELAY);
            }
        }
    };

    private void moveSnake() {
        int[] head = snake.getFirst();
        int newX = head[0], newY = head[1];
        switch (currentDirection) {
            case UP: newX--; break;
            case DOWN: newX++; break;
            case LEFT: newY--; break;
            case RIGHT: newY++; break;
        }
        if (newX < 0 || newX >= GRID_SIZE || newY < 0 || newY >= GRID_SIZE) {
            gameOver();
            return;
        }
        for (int[] pos : snake) {
            if (pos[0] == newX && pos[1] == newY) {
                gameOver();
                return;
            }
        }
        snake.addFirst(new int[]{newX, newY});
        boolean ate = false;
        for (int i = 0; i < foodList.size(); i++) {
            int[] f = foodList.get(i);
            if (newX == f[0] && newY == f[1]) {
                score++;
                PointManager.getInstance().addSnakeFood();
                updatePointsUIAndSync();
                foodList.remove(i);
                placeFood();
                updateScore();
                ate = true;
                break;
            }
        }
        if (!ate) {
            snake.removeLast();
        }
        updateGrid();
    }

    private void gameOver() {
        isGameOver = true;
        handler.removeCallbacks(gameRunnable);
        int bestScore = Math.max(score, getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("snake_best", 0));
        int totalFood = getSharedPreferences("user_prefs", MODE_PRIVATE).getInt("snake_food", 0) + score;
        SharedPreferences.Editor editor = getSharedPreferences("user_prefs", MODE_PRIVATE).edit();
        editor.putInt("snake_best", bestScore);
        editor.putInt("snake_food", totalFood);
        if (bestScore >= 50 && !getSharedPreferences("user_prefs", MODE_PRIVATE).getBoolean("ach_snake_master", false)) {
            editor.putBoolean("ach_snake_master", true);
        }
        editor.apply();
        Toast.makeText(this, "Game Over! Score: " + score, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 60;
        private static final int SWIPE_VELOCITY_THRESHOLD = 60;
        @Override
        public boolean onDown(MotionEvent e) { return true; }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0 && currentDirection != Direction.LEFT) {
                        currentDirection = Direction.RIGHT;
                    } else if (diffX < 0 && currentDirection != Direction.RIGHT) {
                        currentDirection = Direction.LEFT;
                    }
                    return true;
                }
            } else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0 && currentDirection != Direction.UP) {
                        currentDirection = Direction.DOWN;
                    } else if (diffY < 0 && currentDirection != Direction.DOWN) {
                        currentDirection = Direction.UP;
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
