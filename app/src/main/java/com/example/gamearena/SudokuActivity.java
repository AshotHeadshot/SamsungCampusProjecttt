package com.example.gamearena;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SudokuActivity extends AppCompatActivity {
    private static final int SIZE = 9;
    private int[][] puzzle = new int[SIZE][SIZE];
    private boolean[][] isFixed = new boolean[SIZE][SIZE];
    private SudokuBoardView sudokuBoard;
    private Button checkBtn, newGameBtn, hintBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sudoku);
        sudokuBoard = findViewById(R.id.sudokuBoard);
        checkBtn = findViewById(R.id.checkBtn);
        newGameBtn = findViewById(R.id.newGameBtn);
        hintBtn = findViewById(R.id.hintBtn);
        generatePuzzle();
        sudokuBoard.setPuzzle(puzzle, isFixed);
        checkBtn.setOnClickListener(v -> checkSolution());
        newGameBtn.setOnClickListener(v -> {
            generatePuzzle();
            sudokuBoard.setPuzzle(puzzle, isFixed);
        });
        hintBtn.setOnClickListener(v -> Toast.makeText(this, "Hint not implemented yet", Toast.LENGTH_SHORT).show());

        // Number input buttons (1-9)
        LinearLayout numberPad = new LinearLayout(this);
        numberPad.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 1; i <= 9; i++) {
            Button b = new Button(this);
            b.setText(String.valueOf(i));
            int num = i;
            b.setOnClickListener(v -> sudokuBoard.setNumber(num));
            numberPad.addView(b);
        }
        ((LinearLayout) findViewById(android.R.id.content)).addView(numberPad);
    }

    private void generatePuzzle() {
        // Demo: static puzzle, can be replaced with generator
        int[][] demo = {
                {5,3,0,0,7,0,0,0,0},
                {6,0,0,1,9,5,0,0,0},
                {0,9,8,0,0,0,0,6,0},
                {8,0,0,0,6,0,0,0,3},
                {4,0,0,8,0,3,0,0,1},
                {7,0,0,0,2,0,0,0,6},
                {0,6,0,0,0,0,2,8,0},
                {0,0,0,4,1,9,0,0,5},
                {0,0,0,0,8,0,0,7,9}
        };
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                puzzle[i][j] = demo[i][j];
                isFixed[i][j] = (demo[i][j] != 0);
            }
        }
    }

    private void checkSolution() {
        // Simple solution check: all cells filled
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (puzzle[i][j] == 0) {
                    Toast.makeText(this, "Incomplete solution!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
        Toast.makeText(this, "Congratulations, Sudoku solved!", Toast.LENGTH_LONG).show();
    }
}
