package com.example.gamearena;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.MotionEvent;

public class TowerBlockGameView extends SurfaceView implements Runnable {
    private Thread gameThread = null;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;

    // Game objects
    private float blockX, blockY, blockWidth, blockHeight, blockSpeedX;
    private float towerBaseY;
    private boolean isDropping = false;
    private int screenWidth, screenHeight;
    private int score = 0;
    private boolean gameOver = false;
    // Store X and width of stacked blocks
    private static final int MAX_BLOCKS = 100;
    private float[] stackedBlockXs = new float[MAX_BLOCKS];
    private float[] stackedBlockWidths = new float[MAX_BLOCKS];

    public TowerBlockGameView(Context context) {
        super(context);
        holder = getHolder();
        paint = new Paint();
        setFocusable(true);
    }

    private void initGame() {
        screenWidth = getWidth();
        screenHeight = getHeight();
        blockWidth = screenWidth / 6f;
        blockHeight = screenHeight / 20f;
        blockX = (screenWidth - blockWidth) / 2f;
        blockY = screenHeight / 6f;
        blockSpeedX = screenWidth / 120f;
        towerBaseY = screenHeight - blockHeight;
        isDropping = false;
        score = 0;
        gameOver = false;
        stackedBlockXs[0] = blockX;
        stackedBlockWidths[0] = blockWidth;
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!holder.getSurface().isValid()) continue;
            if (screenWidth == 0 || screenHeight == 0) {
                screenWidth = getWidth();
                screenHeight = getHeight();
                if (screenWidth > 0 && screenHeight > 0) {
                    initGame();
                }
            }
            if (!gameOver) {
                update();
            }
            Canvas canvas = holder.lockCanvas();
            drawGame(canvas);
            holder.unlockCanvasAndPost(canvas);
            try { Thread.sleep(1000/60); } catch (InterruptedException ignored) {}
        }
    }

    private void update() {
        if (!isDropping) {
            blockX += blockSpeedX;
            if (blockX < 0) {
                blockX = 0;
                blockSpeedX = Math.abs(blockSpeedX);
            } else if (blockX + blockWidth > screenWidth) {
                blockX = screenWidth - blockWidth;
                blockSpeedX = -Math.abs(blockSpeedX);
            }
        } else {
            blockY += screenHeight / 90f;
            if (blockY + blockHeight >= towerBaseY - score * blockHeight) {
                // Check overlap with previous block
                float prevX = stackedBlockXs[score];
                float prevWidth = stackedBlockWidths[score];
                float overlapLeft = Math.max(blockX, prevX);
                float overlapRight = Math.min(blockX + blockWidth, prevX + prevWidth);
                float overlapWidth = overlapRight - overlapLeft;
                if (overlapWidth > 0) {
                    // Success: chop block to overlap
                    if (score + 1 < MAX_BLOCKS) {
                        stackedBlockXs[score + 1] = overlapLeft;
                        stackedBlockWidths[score + 1] = overlapWidth;
                    }
                    blockX = overlapLeft;
                    blockWidth = overlapWidth;
                    blockY = towerBaseY - (score + 1) * blockHeight;
                    isDropping = false;
                    score++;
                    // Next block starts above, same width as overlap
                    blockX = (float) (Math.random() * (screenWidth - blockWidth));
                    blockY = screenHeight / 6f;
                    // Increase speed
                    float baseSpeed = screenWidth / (120f - 6 * score);
                    blockSpeedX = (Math.random() > 0.5 ? 1 : -1) * baseSpeed;
                    // End game if block too small or tower too high
                    if (blockY < 0 || blockWidth < screenWidth / 30f) gameOver = true;
                } else {
                    // No overlap, game over
                    gameOver = true;
                }
            }
        }
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.rgb(30,30,60));
        paint.setAntiAlias(true);
        // Draw tower
        paint.setColor(Color.rgb(200,200,255));
        for (int i = 0; i < score + 1; i++) {
            float y = towerBaseY - i * blockHeight;
            float x = stackedBlockXs[i];
            float w = stackedBlockWidths[i];
            canvas.drawRect(x, y, x + w, y + blockHeight, paint);
        }
        // Draw moving/dropping block
        if (!gameOver) {
            paint.setColor(Color.rgb(100,255,200));
            canvas.drawRect(blockX, blockY, blockX + blockWidth, blockY + blockHeight, paint);
        }
        // Draw score
        paint.setColor(Color.WHITE);
        paint.setTextSize(48);
        canvas.drawText("Score: " + score, 40, 80, paint);
        if (gameOver) {
            paint.setTextSize(64);
            canvas.drawText("Game Over!", screenWidth/2f - 160, screenHeight/2f, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !gameOver) {
            if (!isDropping) {
                isDropping = true;
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
