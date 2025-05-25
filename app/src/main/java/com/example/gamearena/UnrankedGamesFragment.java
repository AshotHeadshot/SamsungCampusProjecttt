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

public class UnrankedGamesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_unranked_games, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.gamesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2)); // 2 columns

        List<Game> games = new ArrayList<>();
        games.add(new Game(R.drawable.tictactoe_pvp_logo, "Tic Tac Toe vs Player", "Play Tic Tac Toe with a friend.", () -> {
            Intent intent = new Intent(getActivity(), TicTacToeActivity.class);
            intent.putExtra("EXTRA_PVP_MODE", true); // Enable PvP mode
            startActivity(intent);
        }));
        games.add(new Game(R.drawable.word_scramble_logo, "Word Scramble", "Unscramble words as fast as you can.", () -> {
            startActivity(new Intent(getActivity(), WordScrambleActivity.class));
        }));

        games.add(new Game(R.drawable.fast_typing_logo, "Fast Typing Challenge", "Type words quickly and accurately.", () -> {
            startActivity(new Intent(getActivity(), FastTypingActivity.class));
        }));
        games.add(new Game(R.drawable.ping_pong_logo, "Ping Pong", "Classic ping pong game.", () -> {
            startActivity(new Intent(getActivity(), PingPongUserVsUserActivity.class));
        }));

        recyclerView.setAdapter(new GameCardAdapter(games));
        return view;
    }
}
