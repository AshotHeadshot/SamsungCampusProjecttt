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

        return view;
    }
}
