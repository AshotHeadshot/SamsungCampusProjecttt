package com.example.gamearena;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GamesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);
        ImageButton ticTacToeBtn = view.findViewById(R.id.ticTacToeBtn);
        ticTacToeBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(getActivity(), TicTacToeActivity.class);
                intent.putExtra("EXTRA_PVP_MODE", true); // Enable PvP mode
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Could not open TicTacToe: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        ImageButton ticTacToeBotBtn = view.findViewById(R.id.ticTacToeBotBtn);
        ticTacToeBotBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(getActivity(), TicTacToeBotActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Could not open TicTacToe vs Bot: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        ImageButton rockPaperScissorsBtn = view.findViewById(R.id.rockPaperScissorsBtn);
        rockPaperScissorsBtn.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(getActivity(), RockPaperScissorsActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Could not open Rock-Paper-Scissors: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        ImageButton quickMathBtn = view.findViewById(R.id.quickMathBtn);
        quickMathBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), QuickMathActivity.class)));
        ImageButton snakeBtn = view.findViewById(R.id.snakeBtn);
        snakeBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), SnakeActivity.class)));
        ImageButton game2048Btn = view.findViewById(R.id.game2048Btn);
        game2048Btn.setOnClickListener(v -> startActivity(new Intent(getActivity(), Game2048Activity.class)));
        ImageButton aliasBtn = view.findViewById(R.id.aliasBtn);
        aliasBtn.setOnClickListener(v -> startActivity(new Intent(getActivity(), AliasActivity.class)));
        
        ImageButton towerBlockBtn = view.findViewById(R.id.TowerBlockBtn);
        towerBlockBtn.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Tower Block Clicked", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(getActivity(), TowerBlockActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Could not open Tower Block: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        
        ImageButton imposterColorBtn = view.findViewById(R.id.ImposterColorBtn);
        imposterColorBtn.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Imposter Color Clicked", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(getActivity(), ImposterColorActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Could not open Imposter Color: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        ImageButton wordScrambleBtn = view.findViewById(R.id.wordScrambleBtn);
        wordScrambleBtn.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Word Scramble Clicked", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(getActivity(), WordScrambleActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Could not open Word Scramble: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        
        ImageButton fastTypingBtn = view.findViewById(R.id.fasttypingBtn);
        fastTypingBtn.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Fast Typing Challenge Clicked", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(getActivity(), FastTypingActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Could not open Fast Typing Challenge: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        ImageButton pingPongUserBtn = view.findViewById(R.id.PingPong);
        pingPongUserBtn.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Ping Pong User vs User Clicked", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(getActivity(), PingPongUserVsUserActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(getActivity(), "Could not open Ping Pong User vs User: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        ImageButton pingPongVsBlocksBtn = view.findViewById(R.id.PingPongvsBlocksBtn);
        pingPongVsBlocksBtn.setOnClickListener(v -> {
    Toast.makeText(getActivity(), "Ping Pong vs Blocks Clicked", Toast.LENGTH_SHORT).show();
    try {
        Intent intent = new Intent(getActivity(), PingPongVsBlocksActivity.class);
        startActivity(intent);
    } catch (Exception e) {
        Toast.makeText(getActivity(), "Could not open Ping Pong vs Blocks: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }
});

        return view;
    }
}
