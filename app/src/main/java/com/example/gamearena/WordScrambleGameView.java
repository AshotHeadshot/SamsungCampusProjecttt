package com.example.gamearena;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class WordScrambleGameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private SurfaceHolder holder;
    private Paint paint;
    private TextPaint textPaint;
    private Random random = new Random();
    private android.widget.EditText inputBox;

    private String[] wordList = {
            // Words
            "ANDROID", "KOTLIN", "JAVA", "SURFACE", "BUTTON", "ACTIVITY", "FRAGMENT", "LAYOUT", "WIDGET", "CANVAS",
            "OBJECT", "STRING", "METHOD", "VARIABLE", "PROJECT", "PACKAGE", "IMPORT", "RETURN", "STATIC", "PUBLIC",
            "MOBILE", "DEVELOPER", "APPLICATION", "RESOURCE", "MANIFEST", "GRADLE", "DEBUG", "RELEASE", "EMULATOR", "DEVICE",
            // Sentences
            "Hello World", "Android is awesome", "I love coding", "Java is powerful", "Kotlin is modern",
            "SurfaceView is cool", "Fragments manage UI", "Layouts define structure", "Debug your code", "Import statements matter"
    };
    private String currentWord;
    private String scrambledWord;
    private boolean showResult = false;
    private boolean isCorrect = false;
    private String userGuess = "";


    public WordScrambleGameView(Context context) {
        super(context);
        holder = getHolder();
        paint = new Paint();
        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80);
        setFocusable(true);
        nextWord();
    }

    public void setInputBox(android.widget.EditText inputBox) {
        this.inputBox = inputBox;
    }

    public void submitGuess(String guess) {
        if (!showResult) {
            userGuess = guess;
            checkGuess();
            invalidate();
        }
    }

    private void nextWord() {
        currentWord = wordList[random.nextInt(wordList.length)];
        if (currentWord.contains(" ")) {
            scrambledWord = scrambleSentence(currentWord);
        } else {
            scrambledWord = scrambleWord(currentWord);
        }
        showResult = false;
        isCorrect = false;
        userGuess = "";
    }

    // Scramble a sentence by shuffling the words and also shuffling the letters within each word
    private String scrambleSentence(String sentence) {
        String[] words = sentence.split(" ");
        // Shuffle words
        List<String> wordList = Arrays.asList(words);
        Collections.shuffle(wordList);
        // Shuffle letters in each word
        for (int i = 0; i < wordList.size(); i++) {
            String word = wordList.get(i);
            List<String> letters = Arrays.asList(word.split(""));
            Collections.shuffle(letters);
            wordList.set(i, String.join("", letters));
        }
        return String.join(" ", wordList);
    }

    private String scrambleWord(String word) {
        List<String> letters = Arrays.asList(word.split(""));
        do {
            Collections.shuffle(letters);
        } while (String.join("", letters).equals(word));
        return String.join("", letters);
    }

    @Override
    public void run() {
        while (isPlaying) {
            if (!holder.getSurface().isValid()) continue;
            Canvas canvas = holder.lockCanvas();
            drawGame(canvas);
            holder.unlockCanvasAndPost(canvas);
            try { Thread.sleep(1000/60); } catch (InterruptedException ignored) {}
        }
    }

    private void drawGame(Canvas canvas) {
        canvas.drawColor(Color.rgb(24, 27, 32));
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Scrambled:", centerX, centerY - 160, textPaint);
        textPaint.setColor(Color.rgb(255, 140, 0)); // Orange
        if (currentWord != null && currentWord.contains(" ")) {
            // It's a sentence, center it
            canvas.drawText(scrambledWord, centerX, centerY - 60, textPaint);
        } else {
            // Single word
            canvas.drawText(scrambledWord, centerX, centerY - 60, textPaint);
        }
        textPaint.setColor(Color.WHITE);
        if (showResult) {
            if (isCorrect) {
                textPaint.setColor(Color.GREEN);
                canvas.drawText("Correct!", centerX, centerY + 100, textPaint);
            } else {
                textPaint.setColor(Color.RED);
                String wrongMsg = "Wrong! The answer was: " + currentWord;
                float maxWidth = getWidth() * 0.9f;
                float textSize = 80f;
                textPaint.setTextSize(textSize);
                while (textPaint.measureText(wrongMsg) > maxWidth && textSize > 36f) {
                    textSize -= 4f;
                    textPaint.setTextSize(textSize);
                }
                canvas.drawText(wrongMsg, centerX, centerY + 100, textPaint);
                textPaint.setTextSize(80f); // reset for other text
            }
            textPaint.setColor(Color.WHITE);
            canvas.drawText("Tap to continue", centerX, centerY + 200, textPaint);
        }
        textPaint.setTextSize(80);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (showResult) {
                nextWord();
                if (inputBox != null) {
                    inputBox.setText("");
                    inputBox.setVisibility(View.VISIBLE);
                    inputBox.requestFocus();
                }
                return true;
            }
            // Show EditText for input
            if (inputBox != null) {
                inputBox.setText("");
                inputBox.setVisibility(View.VISIBLE);
                inputBox.requestFocus();
            }
        }
        return true;
    }

    // onKeyPreIme is no longer needed; input is handled by EditText
    // Removed custom keyboard logic

    private void checkGuess() {
        if (userGuess.equalsIgnoreCase(currentWord)) {
            isCorrect = true;
        } else {
            isCorrect = false;
        }
        showResult = true;
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
