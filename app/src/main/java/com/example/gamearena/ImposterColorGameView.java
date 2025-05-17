package com.example.gamearena;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.Random;

public class ImposterColorGameView extends SurfaceView implements Runnable {
    private Thread gameThread = null;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;
    private Random random = new Random();

    // Game state
    private int level = 1;
    private int impostorIndex = 0;
    private int numCircles = 9;
    private int gridRows = 3;
    private int gridCols = 3;
    private int[] circleColors;
    private float[] circleCentersX;
    private float[] circleCentersY;
    private float circleRadius;
    private boolean gameOver = false;
    private boolean won = false;
    private android.widget.Button playAgainBtn = null;
    private int colorDifference = 100;
    private int colorStep = 5;

    // Session points for this game session
    private int sessionPoints = 0;
    private int sessionWins = 0;
    private int sessionLosses = 0;
    private int sessionDraws = 0;

    public ImposterColorGameView(Context context) {
        super(context);
        holder = getHolder();
        paint = new Paint();
        setFocusable(true);
        // Do not call startLevel() here; wait until surface size is known.
        // Create Play Again button
        playAgainBtn = new android.widget.Button(context);
        playAgainBtn.setText("Play Again");
        playAgainBtn.setTextColor(0xFFFFFFFF);
        playAgainBtn.setBackgroundResource(R.drawable.btn_playagain_oval);
        playAgainBtn.setTextSize(20);
        playAgainBtn.setVisibility(android.view.View.GONE);
        playAgainBtn.setPadding(40, 20, 40, 20);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 60, 0, 0);
        params.gravity = android.view.Gravity.CENTER_HORIZONTAL;
        playAgainBtn.setLayoutParams(params);
        // Try to add to parent if possible
        if (context instanceof android.app.Activity) {
            android.app.Activity act = (android.app.Activity) context;
            android.view.View root = act.findViewById(android.R.id.content);
            if (root instanceof android.widget.LinearLayout) {
                ((android.widget.LinearLayout) root).addView(playAgainBtn);
            } else if (root instanceof android.view.ViewGroup) {
                ((android.view.ViewGroup) root).addView(playAgainBtn);
            }
        }
        playAgainBtn.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                playAgainBtn.setVisibility(android.view.View.GONE);
                sessionPoints = 0;
                sessionWins = 0;
                sessionLosses = 0;
                sessionDraws = 0;
                level = 1;
                startLevel();
                gameOver = false;
                won = false;
            }
        });
    }

    private void startLevel() {
        // Determine grid and number of circles based on level
        if (level <= 7) {
            numCircles = 9;
            gridRows = 3;
            gridCols = 3;
        } else if (level <= 16) {
            numCircles = 16;
            gridRows = 4;
            gridCols = 4;
        } else {
            numCircles = 25;
            gridRows = 5;
            gridCols = 5;
        }
        // Color difference gets smaller as level increases
        colorDifference = Math.max(0, 100 - (level - 1) * colorStep);
        // Prepare circles
        circleColors = new int[numCircles];
        circleCentersX = new float[numCircles];
        circleCentersY = new float[numCircles];
        // Pick base color
        int baseR = random.nextInt(156) + 50;
        int baseG = random.nextInt(156) + 50;
        int baseB = random.nextInt(156) + 50;
        int impostorR = baseR, impostorG = baseG, impostorB = baseB;
        // Randomly pick a color channel to modify for impostor
        int channel = random.nextInt(3);
        if (random.nextBoolean()) {
            if (channel == 0) impostorR = Math.max(0, Math.min(255, baseR + colorDifference));
            if (channel == 1) impostorG = Math.max(0, Math.min(255, baseG + colorDifference));
            if (channel == 2) impostorB = Math.max(0, Math.min(255, baseB + colorDifference));
        } else {
            if (channel == 0) impostorR = Math.max(0, Math.min(255, baseR - colorDifference));
            if (channel == 1) impostorG = Math.max(0, Math.min(255, baseG - colorDifference));
            if (channel == 2) impostorB = Math.max(0, Math.min(255, baseB - colorDifference));
        }
        int impostorColor = Color.rgb(impostorR, impostorG, impostorB);
        int baseColor = Color.rgb(baseR, baseG, baseB);
        impostorIndex = random.nextInt(numCircles);
        for (int i = 0; i < numCircles; i++) {
            circleColors[i] = (i == impostorIndex) ? impostorColor : baseColor;
        }
        // Calculate positions
        int w = getWidth();
        int h = getHeight();
        float cellW = w / (float) gridCols;
        float cellH = h / (float) gridRows;
        circleRadius = Math.min(cellW, cellH) * 0.4f;
        for (int r = 0; r < gridRows; r++) {
            for (int c = 0; c < gridCols; c++) {
                int idx = r * gridCols + c;
                if (idx >= numCircles) break;
                circleCentersX[idx] = c * cellW + cellW / 2f;
                circleCentersY[idx] = r * cellH + cellH / 2f;
            }
        }
        gameOver = false;
        won = false;
    }

    @Override
    public void run() {
        boolean initialized = false;
        while (isPlaying) {
            if (!holder.getSurface().isValid()) continue;
            if (!initialized && getWidth() > 0 && getHeight() > 0) {
                startLevel();
                initialized = true;
            }
            Canvas canvas = holder.lockCanvas();
            drawGame(canvas);
            holder.unlockCanvasAndPost(canvas);
            try { Thread.sleep(1000/60); } catch (InterruptedException ignored) {}
        }
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.rgb(30,30,60));
        paint.setAntiAlias(true);
        // Draw circles
        for (int i = 0; i < numCircles; i++) {
            paint.setColor(circleColors[i]);
            canvas.drawCircle(circleCentersX[i], circleCentersY[i], circleRadius, paint);
        }
        // Draw text
        paint.setColor(Color.WHITE);
        paint.setTextSize(48);
        if (gameOver) {
            if (won) {
                canvas.drawText("Correct! Next Level", 60, 100, paint);
            } else {
                canvas.drawText("Game Over!", 60, 100, paint);
            }
        } else {
            canvas.drawText("Level: " + level, 60, 100, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && !gameOver) {
            float x = event.getX();
            float y = event.getY();
            for (int i = 0; i < numCircles; i++) {
                float dx = x - circleCentersX[i];
                float dy = y - circleCentersY[i];
                if (dx * dx + dy * dy <= circleRadius * circleRadius) {
                    if (i == impostorIndex) {
                        won = true;
                        gameOver = false;
                        sessionPoints += 2;
                        PointManager.getInstance().applySessionPoints(getContext(), sessionPoints, sessionWins, sessionLosses, sessionDraws);
                        // +10 bonus after 10 correct
                        if ((level-1) > 0 && (level-1) % 10 == 0) {
                            sessionPoints += 10;
                            PointManager.getInstance().applySessionPoints(getContext(), sessionPoints, sessionWins, sessionLosses, sessionDraws);
                            showShortToast("+10 bonus!");
                        }
                        sessionWins++;
                        showShortToast("+2 point");
                        level++;
                        startLevel();
                    } else {
                        won = false;
                        gameOver = true;
                        sessionPoints -= 3;
                        sessionLosses++;
                        showShortToast("-3 point");
                        Context context = getContext();
                        if (playAgainBtn == null) {
                            playAgainBtn = new android.widget.Button(context);
                            playAgainBtn.setText("Play Again");
                            playAgainBtn.setTextColor(0xFFFFFFFF);
                            playAgainBtn.setBackgroundResource(R.drawable.btn_playagain_oval);
                            playAgainBtn.setTextSize(20);
                            playAgainBtn.setVisibility(android.view.View.GONE);
                            playAgainBtn.setPadding(40, 20, 40, 20);
                            android.widget.FrameLayout.LayoutParams params = new android.widget.FrameLayout.LayoutParams(
                                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                                    android.widget.FrameLayout.LayoutParams.WRAP_CONTENT);
                            params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
                            params.setMargins(0, 0, 0, 80); // 80px bottom margin for spacing
                            playAgainBtn.setLayoutParams(params);
                            playAgainBtn.setOnClickListener(new android.view.View.OnClickListener() {
                                @Override
                                public void onClick(android.view.View v) {
                                    playAgainBtn.setVisibility(android.view.View.GONE);
                                    sessionPoints = 0;
                                    sessionWins = 0;
                                    sessionLosses = 0;
                                    sessionDraws = 0;
                                    level = 1;
                                    startLevel();
                                    gameOver = false;
                                    won = false;
                                }
                            });
                        }
                        // Remove from old parent if needed
                        if (playAgainBtn.getParent() != null) {
                            ((android.view.ViewGroup) playAgainBtn.getParent()).removeView(playAgainBtn);
                        }
                        if (context instanceof android.app.Activity) {
                            android.app.Activity act = (android.app.Activity) context;
                        }
                        playAgainBtn.setVisibility(android.view.View.VISIBLE);
                        playAgainBtn.bringToFront();
                    }
                    break;
                }
            }
        }
        return true;
    }

    private void nextLevel() {
        // Award points for correct level
        sessionPoints += 2; // Example: +2 per correct level
        PointManager.getInstance().applySessionPoints(getContext(), sessionPoints, sessionWins, sessionLosses, sessionDraws);
        level++;
        startLevel();
    }

    private void restart() {
        // Not used anymore; replaced by Play Again button.
    }

    // Removed summary dialog; replaced by Play Again button and Toast only.

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
        final android.widget.Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.show();
        new android.os.Handler().postDelayed(toast::cancel, 500);
    }
}
