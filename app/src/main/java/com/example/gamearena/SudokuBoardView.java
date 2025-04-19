package com.example.gamearena;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SudokuBoardView extends View {
    public static final int SIZE = 9;
    private int[][] puzzle = new int[SIZE][SIZE];
    private boolean[][] isFixed = new boolean[SIZE][SIZE];
    private int selectedRow = -1, selectedCol = -1;
    private Paint thickLine, thinLine, numberPaint, selectedPaint;

    public SudokuBoardView(Context context) {
        super(context);
        init();
    }

    public SudokuBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SudokuBoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SudokuBoardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        thickLine = new Paint();
        thickLine.setColor(Color.BLACK);
        thickLine.setStrokeWidth(8);

        thinLine = new Paint();
        thinLine.setColor(Color.BLACK);
        thinLine.setStrokeWidth(2);

        numberPaint = new Paint();
        numberPaint.setColor(Color.BLACK);
        numberPaint.setTextSize(60);
        numberPaint.setTextAlign(Paint.Align.CENTER);

        selectedPaint = new Paint();
        selectedPaint.setColor(Color.parseColor("#FFEB3B"));
        selectedPaint.setAlpha(80);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        int cellSize = Math.min(w, h) / SIZE;

        // Draw background
        canvas.drawColor(Color.WHITE);

        // Highlight selected cell
        if (selectedRow >= 0 && selectedCol >= 0) {
            canvas.drawRect(selectedCol * cellSize, selectedRow * cellSize,
                    (selectedCol + 1) * cellSize, (selectedRow + 1) * cellSize, selectedPaint);
        }

        // Draw numbers
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (puzzle[r][c] != 0) {
                    float x = c * cellSize + cellSize / 2f;
                    float y = r * cellSize + cellSize / 1.5f;
                    canvas.drawText(String.valueOf(puzzle[r][c]), x, y, numberPaint);
                }
            }
        }

        // Draw grid
        for (int i = 0; i <= SIZE; i++) {
            Paint paint = (i % 3 == 0) ? thickLine : thinLine;
            // Vertical
            canvas.drawLine(i * cellSize, 0, i * cellSize, cellSize * SIZE, paint);
            // Horizontal
            canvas.drawLine(0, i * cellSize, cellSize * SIZE, i * cellSize, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int cellSize = getWidth() / SIZE;
            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);
            if (row >= 0 && row < SIZE && col >= 0 && col < SIZE) {
                selectedRow = row;
                selectedCol = col;
                invalidate();
            }
            return true;
        }
        return false;
    }

    public void setPuzzle(int[][] puzzle, boolean[][] isFixed) {
        this.puzzle = puzzle;
        this.isFixed = isFixed;
        invalidate();
    }

    public void setNumber(int number) {
        if (selectedRow >= 0 && selectedCol >= 0 && !isFixed[selectedRow][selectedCol]) {
            puzzle[selectedRow][selectedCol] = number;
            invalidate();
        }
    }
}
