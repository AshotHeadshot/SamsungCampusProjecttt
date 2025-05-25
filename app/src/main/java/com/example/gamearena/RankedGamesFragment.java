package com.example.gamearena;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class RankedGamesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranked_games, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.gamesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns

        List<Game> games = new ArrayList<>();
        games.add(new Game(R.drawable.rock_paper_scissors_vs_bot, "Rock Paper Scissors", "Play classic Rock Paper Scissors against AI.", () -> {
            startActivity(new Intent(getActivity(), RockPaperScissorsActivity.class));
        }));
        games.add(new Game(R.drawable.quick_math_challenge_logo, "Quick Math Challenge", "Solve math problems quickly for points.", () -> {
            startActivity(new Intent(getActivity(), QuickMathActivity.class));
        }));
        games.add(new Game(R.drawable.snake_logo, "Snake", "Classic snake game. Eat, grow, and avoid walls.", () -> {
            startActivity(new Intent(getActivity(), SnakeActivity.class));
        }));
        games.add(new Game(R.drawable.game_2048_logo, "2048", "Combine numbers to reach 2048.", () -> {
            startActivity(new Intent(getActivity(), Game2048Activity.class));
        }));
        games.add(new Game(R.drawable.ping_pong_vs_blocks_logo, "Ping Pong vs Blocks", "Break blocks with your paddle.", () -> {
            startActivity(new Intent(getActivity(), PingPongVsBlocksActivity.class));
        }));
        games.add(new Game(R.drawable.tictactoe_vs_bot_logo, "TicTacToe vs Bot", "Play TicTacToe against the bot.", () -> {
            startActivity(new Intent(getActivity(), TicTacToeBotActivity.class));
        }));


        recyclerView.setAdapter(new GameCardAdapter(games));
        return view;
    }
}
