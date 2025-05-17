package com.example.gamearena;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import java.util.Random;

public class FastTypingGameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;
    private EditText inputBox;
    private String currentText = "";
    private String userInput = "";
    private int score = 0;
    private long startTime = 0;
    private long timeLimit = 10000; // 10 seconds
    private boolean gameOver = false;
    private boolean inputActive = false;
    private Random random = new Random();
    private String[] textPool = {
            "android", "challenge", "keyboard", "surface", "fragment", "activity", "scramble", "imposter", "canvas", "variable",
            "project", "package", "import", "return", "static", "public", "private", "object", "method", "widget"
    };

    public FastTypingGameView(Context context) {
        super(context);
        holder = getHolder();
        paint = new Paint();
        setFocusable(true);
        nextText();
    }

    private void nextText() {
        currentText = textPool[random.nextInt(textPool.length)];
        userInput = "";
        startTime = System.currentTimeMillis();
        gameOver = false;
        inputActive = true;
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!holder.getSurface().isValid()) continue;
            Canvas canvas = holder.lockCanvas();
            drawGame(canvas);
            holder.unlockCanvasAndPost(canvas);
            if (inputActive && System.currentTimeMillis() - startTime > timeLimit) {
                gameOver = true;
                inputActive = false;
            }
            try { Thread.sleep(1000/60); } catch (InterruptedException ignored) {}
        }
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.rgb(30, 30, 60));
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        canvas.drawText("Type this:", centerX, centerY - 120, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawText(currentText, centerX, centerY - 40, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Your input: " + userInput, centerX, centerY + 40, paint);
        paint.setTextSize(40);
        canvas.drawText("Score: " + score, centerX, 80, paint);
        if (gameOver) {
            paint.setColor(Color.RED);
            paint.setTextSize(60);
            canvas.drawText("Time's up!", centerX, centerY + 140, paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(40);
            canvas.drawText("Tap to try again", centerX, centerY + 200, paint);
        } else {
            long timeLeft = Math.max(0, timeLimit - (System.currentTimeMillis() - startTime));
            paint.setColor(Color.CYAN);
            paint.setTextSize(40);
            canvas.drawText("Time left: " + (timeLeft / 1000.0) + "s", centerX, centerY + 100, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (gameOver) {
                nextText();
                if (inputBox != null) {
                    inputBox.setText("");
                    inputBox.setVisibility(View.VISIBLE);
                    inputBox.requestFocus();
                }
                return true;
            }
            if (inputBox != null) {
                inputBox.setText("");
                inputBox.setVisibility(View.VISIBLE);
                inputBox.requestFocus();
            }
        }
        return true;
    }

    public void setInputBox(EditText inputBox) {
        this.inputBox = inputBox;
        this.inputBox.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String guess = inputBox.getText().toString();
                submitInput(guess);
                inputBox.setText("");
                inputBox.setVisibility(View.INVISIBLE);
                return true;
            }
            return false;
        });
    }

    public void submitInput(String input) {
        if (!gameOver && inputActive) {
            userInput = input;
            if (userInput.equals(currentText)) {
                score++;
                nextText();
                if (inputBox != null) {
                    inputBox.setText("");
                    inputBox.setVisibility(View.VISIBLE);
                    inputBox.requestFocus();
                }
            } else {
                // Incorrect but allow retry until time runs out
            }
        }
    }

    public void pause() {
        isPlaying = false;
        try {
            if (gameThread != null)
                gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}
