package com.example.gamearena;

import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WordScrambleActivity extends AppCompatActivity {
    private WordScrambleGameView gameView;
    private EditText inputBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setBackgroundColor(android.graphics.Color.rgb(24, 27, 32));
        int verticalPadding = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics());
        layout.setPadding(0, verticalPadding, 0, verticalPadding);

        gameView = new WordScrambleGameView(this);
        inputBox = new EditText(this);
        inputBox.setSingleLine();
        inputBox.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        inputBox.setImeOptions(EditorInfo.IME_ACTION_DONE);
        inputBox.setTextSize(24);
        inputBox.setGravity(Gravity.CENTER);
        // Layout params for game view (take most space)
        LinearLayout.LayoutParams gameParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            (int) android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 320, getResources().getDisplayMetrics())
        );
        // Layout params for input (fixed width, margin top)
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
            (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 400, getResources().getDisplayMetrics()),
            LinearLayout.LayoutParams.WRAP_CONTENT);
        inputParams.topMargin = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        inputBox.setLayoutParams(inputParams);
        // Always enabled and ready for input
        inputBox.setVisibility(View.VISIBLE);
        inputBox.setEnabled(true);
        inputBox.setFocusable(true);
        inputBox.setFocusableInTouchMode(true);
        inputBox.setAlpha(1.0f);
        inputBox.setHint("Type your answer here...");
        inputBox.setText("");
        inputBox.setBackgroundColor(0xFFFF8C00); // Orange
        inputBox.setTextColor(0xFFFFFFFF); // White
        inputBox.setTypeface(inputBox.getTypeface(), android.graphics.Typeface.BOLD);
        inputBox.setPadding(40, 32, 40, 32);
        inputBox.setElevation(16f);
        inputBox.setShadowLayer(8f, 0f, 4f, 0xAA000000);
        android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
        border.setColor(0xFFFF8C00); // Orange
        border.setCornerRadius(24f);
        border.setStroke(6, 0xFFFFFFFF); // White border
        inputBox.setBackground(border);
        layout.addView(gameView, gameParams);
        layout.addView(inputBox, inputParams);
        setContentView(layout);



        // Listen for input submission
        inputBox.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String guess = inputBox.getText().toString();
                gameView.submitGuess(guess);
                inputBox.setText("");
                // Always keep input enabled and ready
                inputBox.setEnabled(true);
                inputBox.setFocusable(true);
                inputBox.setFocusableInTouchMode(true);
                inputBox.setAlpha(1.0f);
                inputBox.setHint("Type your answer here...");
                inputBox.clearFocus();
                return true;
            }
            return false;
        });

        gameView.setInputBox(inputBox);
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
}
