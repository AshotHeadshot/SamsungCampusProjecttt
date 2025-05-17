package com.example.gamearena;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PingPongVsBlocksView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    // Game state
    private static final int BLOCK_ROWS = 5;
    private static final int BLOCK_COLS = 8;
    private static final int TARGET_SCORE = 50;
    private boolean[][] blocks = new boolean[BLOCK_ROWS][BLOCK_COLS];
    private float blockWidth, blockHeight;
    private int level = 1;
    private int points = 0;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private boolean showRestart = false;
    private RectF restartBtnRect = new RectF();

    private Thread gameThread;
    private boolean isPlaying = false;
    private SurfaceHolder holder;
    private Paint paint;

    // Paddle
    private float paddleX, paddleY, paddleWidth, paddleHeight;
    // Ball
    private float ballX, ballY, ballRadius, ballSpeedX, ballSpeedY;

    public PingPongVsBlocksView(Context context) {
        super(context);
        init();
    }
    public PingPongVsBlocksView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        holder = getHolder();
        holder.addCallback(this);
        paint = new Paint();
        setFocusable(true);
    }

    private void initGame() {
        int w = getWidth();
        int h = getHeight();
        paddleWidth = w / 4f;
        paddleHeight = h / 30f;
        paddleX = (w - paddleWidth) / 2f;
        paddleY = h - 3 * paddleHeight;
        ballRadius = w / 40f;
        ballX = w / 2f;
        ballY = paddleY - ballRadius - 10;
        ballSpeedX = w / 120f;
        ballSpeedY = -h / 120f;

        // Block setup
        blockWidth = w / (float) BLOCK_COLS;
        blockHeight = h / 20f;
        java.util.Random rand = new java.util.Random();
        for (int r = 0; r < BLOCK_ROWS; r++) {
            for (int c = 0; c < BLOCK_COLS; c++) {
                // 70% chance to place a block (for unsymmetry)
                blocks[r][c] = rand.nextFloat() < 0.7f;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initGame();
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isPlaying = false;
        try {
            if (gameThread != null) gameThread.join();
        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!holder.getSurface().isValid()) continue;
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
        if (ballX - ballRadius < 0) { ballX = ballRadius; ballSpeedX = -ballSpeedX; }
        else if (ballX + ballRadius > getWidth()) { ballX = getWidth() - ballRadius; ballSpeedX = -ballSpeedX; }
        if (ballY - ballRadius < 0) { ballY = ballRadius; ballSpeedY = -ballSpeedY; }
        // Block collision
        blockLoop:
        for (int r = 0; r < BLOCK_ROWS; r++) {
            for (int c = 0; c < BLOCK_COLS; c++) {
                if (!blocks[r][c]) continue;
                float left = c * blockWidth;
                float top = r * blockHeight + 40;
                float right = left + blockWidth;
                float bottom = top + blockHeight;
                // Ball-block collision (simple AABB)
                if (ballX + ballRadius > left && ballX - ballRadius < right &&
                    ballY + ballRadius > top && ballY - ballRadius < bottom) {
                    blocks[r][c] = false;
                    points += 2; 
                    showShortToast("+2 points");
                    // Increase ball speed
                    ballSpeedX *= 1.07f;
                    ballSpeedY *= 1.07f;
                    // Bounce ball
                    ballSpeedY = -ballSpeedY;
                    break blockLoop;
                }
            }
        }
        // Paddle collision
        if (ballY + ballRadius >= paddleY && ballX >= paddleX && ballX <= paddleX + paddleWidth) {
            ballY = paddleY - ballRadius;
            ballSpeedY = -ballSpeedY;
        }
        // Bottom (lose)
        if (ballY - ballRadius > getHeight()) {
            if (points < 10) {
                points -= 5;
                showShortToast("-5 points");
            }
            gameOver = true;
            gameWon = false;
            showRestart = true;
        }
        // Check for win condition
        boolean allBlocksCleared = true;
        for (int r = 0; r < BLOCK_ROWS; r++) {
            for (int c = 0; c < BLOCK_COLS; c++) {
                if (blocks[r][c]) {
                    allBlocksCleared = false;
                    break;
                }
            }
        }
        if (allBlocksCleared || points >= TARGET_SCORE) {
            points += 10; // Award points for level completion
            showShortToast("+10 points");
            gameOver = true;
            gameWon = true;
            showRestart = true;
        }
    }

    private void drawGame(Canvas canvas) {
        if (canvas == null) return;
        canvas.drawColor(Color.rgb(24, 27, 32));
        // Draw blocks
        paint.setColor(Color.parseColor("#FF8C00"));
        for (int r = 0; r < BLOCK_ROWS; r++) {
            for (int c = 0; c < BLOCK_COLS; c++) {
                if (blocks[r][c]) {
                    float left = c * blockWidth;
                    float top = r * blockHeight + 40;
                    float right = left + blockWidth - 6;
                    float bottom = top + blockHeight - 6;
                    canvas.drawRect(left, top, right, bottom, paint);
                }
            }
        }
        // Draw paddle
        paint.setColor(Color.WHITE);
        canvas.drawRect(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight, paint);
        // Draw ball
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(ballX, ballY, ballRadius, paint);

        // Game over/win UI
        if (gameOver) {
            paint.setTextSize(60);
            paint.setColor(gameWon ? Color.GREEN : Color.RED);
            String msg = gameWon ? "You Win!" : "Game Over";
            float textW = paint.measureText(msg);
            canvas.drawText(msg, (getWidth() - textW) / 2, getHeight() / 2f - 40, paint);
            // Restart button
            paint.setColor(Color.parseColor("#5396FF"));
            float btnW = 400, btnH = 100;
            float btnX = (getWidth() - btnW) / 2;
            float btnY = getHeight() / 2f + 20;
            restartBtnRect.set(btnX, btnY, btnX + btnW, btnY + btnH);
            canvas.drawRoundRect(restartBtnRect, 30, 30, paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(44);
            String btnText = "Play Again";
            float btnTextW = paint.measureText(btnText);
            canvas.drawText(btnText, btnX + (btnW - btnTextW) / 2, btnY + btnH / 2 + 18, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (gameOver && showRestart) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (restartBtnRect.contains(x, y)) {
                    // Restart everything
                    level = 1;
                    points = 0;
                    gameOver = false;
                    gameWon = false;
                    showRestart = false;
                    initGame();
                    invalidate();
                    return true;
                }
            }
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                paddleX = x - paddleWidth / 2f;
                if (paddleX < 0) paddleX = 0;
                if (paddleX + paddleWidth > getWidth()) paddleX = getWidth() - paddleWidth;
                break;
        }
        return true;
    }

    // Helper to show Toast from game thread
    private void showShortToast(final String msg) {
        post(new Runnable() {
            @Override
            public void run() {
                android.widget.Toast.makeText(getContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
