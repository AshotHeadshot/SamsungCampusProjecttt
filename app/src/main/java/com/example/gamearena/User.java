package com.example.gamearena;

public class User {
    public String email;
    public String password;
    public String nickname;
    public boolean verified;
    public int points;
    public int wins;
    public int draws;
    public int losses;
    public String avatarUri; // URI or resource name
    public String uid; // Firebase UID

    // Computed fields for leaderboard
    public int games;
    public double winRate;

    public void computeStats() {
        games = wins + losses + draws;
        winRate = games > 0 ? (wins * 100.0) / games : 0.0;
    }
}
