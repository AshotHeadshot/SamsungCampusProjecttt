

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.gamearena.PointManager;

public class GameView extends SurfaceView implements Runnable {
    // Session point tracking
    private int sessionPoints = 0;
    private int sessionWins = 0;
    private int sessionLosses = 0;
    private int sessionDraws = 0;
    private Thread gameThread = null;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;

    // Floating point message
    private String pointMessage = null;
    private float pointMsgX = 0, pointMsgY = 0;
    private int pointMsgTimer = 0; // frames
    private int pointMsgAlpha = 255;

    // Game objects
    private float paddleX, paddleY, paddleWidth, paddleHeight;
    private float ballX, ballY, ballRadius, ballSpeedX, ballSpeedY;
    private int screenWidth, screenHeight;
    private boolean gameOver = false;
    private boolean gameWon = false;

    // Restart button bounds
    private float restartBtnLeft = 0, restartBtnTop = 0, restartBtnRight = 0, restartBtnBottom = 0;


    // Blocks
    private int blockRows = 5;
    private int blockCols = 7;
    private boolean[][] blocks;
    private float blockWidth, blockHeight;

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        paint = new Paint();
        setFocusable(true);
        // Will initialize game objects in first draw when surface size is known
    }

    private void initGame() {
        screenWidth = getWidth();
        screenHeight = getHeight();
        // Paddle
        paddleWidth = screenWidth / 4f;
        paddleHeight = screenHeight / 30f;
        paddleX = (screenWidth - paddleWidth) / 2f;
        paddleY = screenHeight - 3 * paddleHeight;
        // Ball
        ballRadius = screenWidth / 40f;
        ballX = screenWidth / 2f;
        ballY = paddleY - ballRadius - 10;
        ballSpeedX = screenWidth / 120f;
        ballSpeedY = -screenHeight / 120f;
        // Blocks
        blocks = new boolean[blockRows][blockCols];
        for (int r = 0; r < blockRows; r++)
            for (int c = 0; c < blockCols; c++)
                blocks[r][c] = true;
        blockWidth = screenWidth / (float) blockCols;
        blockHeight = screenHeight / 18f;
        gameOver = false;
        gameWon = false;
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
            if (!gameOver && !gameWon) {
                update();
            }
            // Update floating point message timer
            if (pointMsgTimer > 0) {
                pointMsgTimer--;
                pointMsgAlpha = (int)(255 * (pointMsgTimer / 60f));
                if (pointMsgTimer == 0) {
                    pointMessage = null;
                    pointMsgAlpha = 255;
                }
            }
            Canvas canvas = holder.lockCanvas();
            drawGame(canvas);
            holder.unlockCanvasAndPost(canvas);
            try { Thread.sleep(1000/60); } catch (InterruptedException ignored) {}
        }
    }

    private void update() {
        // Move ball
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
        if (ballY - ballRadius < 0) {
            ballY = ballRadius;
            ballSpeedY = -ballSpeedY;
        }
        // Paddle collision
        if (ballY + ballRadius >= paddleY &&
            ballX >= paddleX && ballX <= paddleX + paddleWidth &&
            ballY + ballRadius <= paddleY + paddleHeight) {
            ballY = paddleY - ballRadius;
            ballSpeedY = -Math.abs(ballSpeedY);
        }
        // Block collision
        for (int r = 0; r < blockRows; r++) {
            for (int c = 0; c < blockCols; c++) {
                if (!blocks[r][c]) continue;
                float bx = c * blockWidth;
                float by = r * blockHeight + 80;
                if (ballX + ballRadius > bx && ballX - ballRadius < bx + blockWidth &&
                        ballY + ballRadius > by && ballY - ballRadius < by + blockHeight) {
                    blocks[r][c] = false;
                    ballSpeedY = -ballSpeedY;
                    // +2 points per block
                    sessionPoints += 2;
                    PointManager.getInstance().applySessionPoints(getContext(), sessionPoints, sessionWins, sessionLosses, sessionDraws);
                    // Show floating '+2 points' at block position
                    pointMessage = "+2 points";
                    pointMsgX = bx + blockWidth / 2f;
                    pointMsgY = by + blockHeight / 2f;
                    pointMsgTimer = 60; // 1 second at 60 FPS
                    pointMsgAlpha = 255;
                    break;
                }
            }
        }
        // Lose condition
        if (ballY - ballRadius > screenHeight) {
            gameOver = true;
            if (sessionPoints < 10) {
                sessionPoints -= 5;
                PointManager.getInstance().applySessionPoints(getContext(), sessionPoints, sessionWins, sessionLosses, sessionDraws);
                showShortToast("-5 penalty for low score!");
            }
        }
        // Win condition
        gameWon = true;
        for (int r = 0; r < blockRows; r++)
            for (int c = 0; c < blockCols; c++)
                if (blocks[r][c]) gameWon = false;
    }

    private void drawGame(Canvas canvas) {
        if (canvas == null) return;
        // Always clear background
        canvas.drawColor(Color.rgb(24, 27, 32));
        if (gameOver) {
            paint.setColor(Color.WHITE);
            paint.setTextSize(72);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Game Over", screenWidth / 2f, screenHeight / 2f - 60, paint);
            // Draw restart button
            paint.setColor(Color.LTGRAY);
            float btnWidth = 360, btnHeight = 100;
            float btnLeft = screenWidth / 2f - btnWidth / 2f;
            float btnTop = screenHeight / 2f + 10;
            float btnRight = btnLeft + btnWidth;
            float btnBottom = btnTop + btnHeight;
            restartBtnLeft = btnLeft;
            restartBtnTop = btnTop;
            restartBtnRight = btnRight;
            restartBtnBottom = btnBottom;
            canvas.drawRect(btnLeft, btnTop, btnRight, btnBottom, paint);
            paint.setColor(Color.DKGRAY);
            paint.setTextSize(54);
            canvas.drawText("Restart", screenWidth / 2f, btnTop + btnHeight / 2f + 20, paint);
            paint.setTextAlign(Paint.Align.LEFT);
            return;
        }
    }
    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case android.view.MotionEvent.ACTION_DOWN:
            case android.view.MotionEvent.ACTION_MOVE:
                if (gameOver) {
                    // Check if touch is inside restart button
                    if (x >= restartBtnLeft && x <= restartBtnRight && y >= restartBtnTop && y <= restartBtnBottom) {
                        // Restart game
                        initGame();
                        invalidate();
                        return true;
                    }
                } else {
                    // Paddle control
                    paddleX = x - paddleWidth / 2f;
                    if (paddleX < 0) paddleX = 0;
                    if (paddleX + paddleWidth > screenWidth) paddleX = screenWidth - paddleWidth;
                }
                break;
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

    private void showShortToast(String message) {
        final Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
        new android.os.Handler().postDelayed(toast::cancel, 500);
    }
}

