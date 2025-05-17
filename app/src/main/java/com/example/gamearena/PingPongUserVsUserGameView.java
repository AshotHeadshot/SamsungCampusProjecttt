package com.example.gamearena;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PingPongUserVsUserGameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;

    private float paddle1X, paddle2X, paddleY, paddleWidth, paddleHeight;
    private float ballX, ballY, ballRadius, ballSpeedX, ballSpeedY;
    private int screenWidth, screenHeight;
    private boolean gameOver = false;
    private int score1 = 0, score2 = 0;
    private int maxScore = 5;
    private boolean touchPaddle1 = false, touchPaddle2 = false;
    private Rect restartButtonRect = new Rect();

    public PingPongUserVsUserGameView(Context context) {
        super(context);
        holder = getHolder();
        paint = new Paint();
        setFocusable(true);
    }

    private void initGame() {
        screenWidth = getWidth();
        screenHeight = getHeight();
        paddleWidth = screenWidth / 4f;
        paddleHeight = screenHeight / 40f;
        paddle1X = (screenWidth - paddleWidth) / 2f;
        paddle2X = (screenWidth - paddleWidth) / 2f;
        paddleY = 80;
        ballRadius = screenWidth / 40f;
        ballX = screenWidth / 2f;
        ballY = screenHeight / 2f;
        ballSpeedX = screenWidth / 120f;
        ballSpeedY = screenHeight / 120f;
        gameOver = false;
        score1 = 0;
        score2 = 0;
    }

    @Override
    public void run() {
        boolean initialized = false;
        while (isPlaying) {
            if (!holder.getSurface().isValid()) continue;
            if (!initialized && getWidth() > 0 && getHeight() > 0) {
                initGame();
                initialized = true;
            }
            update();
            Canvas canvas = holder.lockCanvas();
            drawGame(canvas);
            holder.unlockCanvasAndPost(canvas);
            try { Thread.sleep(1000/60); } catch (InterruptedException ignored) {}
        }
    }

    private void update() {
        if (gameOver) return;
        ballX += ballSpeedX;
        ballY += ballSpeedY;
        // Wall collision
        if (ballX - ballRadius < 0) {
            ballX = ballRadius;
            ballSpeedX = -ballSpeedX;
        } else if (ballX + ballRadius > screenWidth) {
            ballX = screenWidth - ballRadius;
            ballSpeedX = -ballSpeedX;
        }
        // Paddle collision
        // Top paddle (player 1)
        if (ballY - ballRadius <= paddleY + paddleHeight &&
                ballX >= paddle1X && ballX <= paddle1X + paddleWidth && ballSpeedY < 0) {
            ballY = paddleY + paddleHeight + ballRadius;
            ballSpeedY = -ballSpeedY;
        }
        // Bottom paddle (player 2)
        float paddle2Y = screenHeight - paddleY - paddleHeight;
        if (ballY + ballRadius >= paddle2Y &&
                ballX >= paddle2X && ballX <= paddle2X + paddleWidth && ballSpeedY > 0) {
            ballY = paddle2Y - ballRadius;
            ballSpeedY = -ballSpeedY;
        }
        // Score for player 2
        if (ballY - ballRadius < 0) {
            score2++;
            resetBall();
        }
        // Score for player 1
        if (ballY + ballRadius > screenHeight) {
            score1++;
            resetBall();
        }
        if (score1 >= maxScore || score2 >= maxScore) {
            gameOver = true;
        }
    }

    private void resetBall() {
        ballX = screenWidth / 2f;
        ballY = screenHeight / 2f;
        ballSpeedX = (Math.random() > 0.5 ? 1 : -1) * screenWidth / 120f;
        ballSpeedY = (Math.random() > 0.5 ? 1 : -1) * screenHeight / 120f;
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#181B20"));
        paint.setAntiAlias(true);
        // Draw paddles
        paint.setColor(Color.WHITE);
        float paddle2Y = screenHeight - paddleY - paddleHeight;
        canvas.drawRect(paddle1X, paddleY, paddle1X + paddleWidth, paddleY + paddleHeight, paint);
        canvas.drawRect(paddle2X, paddle2Y, paddle2X + paddleWidth, paddle2Y + paddleHeight, paint);
        // Draw ball
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(ballX, ballY, ballRadius, paint);
        // Draw scores
        paint.setColor(Color.CYAN);
        paint.setTextSize(48);
        canvas.drawText("Player 1: " + score1, 60, 60, paint);
        canvas.drawText("Player 2: " + score2, screenWidth - 320, screenHeight - 40, paint);
        if (gameOver) {
            paint.setColor(Color.GREEN);
            paint.setTextSize(60);
            String winner = score1 > score2 ? "Player 1 Wins!" : "Player 2 Wins!";
            // Move winner text higher (about 60px higher)
            canvas.drawText(winner, screenWidth / 2f - 180, screenHeight / 2f - 60, paint);
            // Draw Restart button
            int buttonWidth = 320;
            int buttonHeight = 100;
            int buttonX = screenWidth / 2 - buttonWidth / 2;
            int buttonY = (int) (screenHeight / 2f + 60);
            // Draw button with blue background and rounded corners
            paint.setColor(Color.parseColor("#5396FF"));
            float radius = 32f;
            canvas.drawRoundRect(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight, radius, radius, paint);
            // Draw white text, centered
            paint.setColor(Color.WHITE);
            paint.setTextSize(48);
            float textWidth = paint.measureText("Restart");
            float textX = screenWidth / 2f - textWidth / 2;
            float textY = buttonY + buttonHeight / 2f - (paint.descent() + paint.ascent()) / 2;
            canvas.drawText("Restart", textX, textY, paint);
            // Save button bounds for click detection
            restartButtonRect.set(buttonX, buttonY, buttonX + buttonWidth, buttonY + buttonHeight);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gameOver) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                float x = event.getX();
                float y = event.getY();
                if (restartButtonRect.contains((int)x, (int)y)) {
                    initGame();
                }
            }
            return true;
        }
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            float x = event.getX(i);
            float y = event.getY(i);
            // Top half controls player 1, bottom half controls player 2
            if (y < screenHeight / 2f) {
                paddle1X = x - paddleWidth / 2f;
                if (paddle1X < 0) paddle1X = 0;
                if (paddle1X + paddleWidth > screenWidth) paddle1X = screenWidth - paddleWidth;
            } else {
                paddle2X = x - paddleWidth / 2f;
                if (paddle2X < 0) paddle2X = 0;
                if (paddle2X + paddleWidth > screenWidth) paddle2X = screenWidth - paddleWidth;
            }
        }
        return true;
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
