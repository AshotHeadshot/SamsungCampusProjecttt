package com.example.gamearena;

public class Game {
    public int imageResId;
    public String title;
    public String description;
    public Runnable onPlay;

    public Game(int imageResId, String title, String description, Runnable onPlay) {
        this.imageResId = imageResId;
        this.title = title;
        this.description = description;
        this.onPlay = onPlay;
    }
}
