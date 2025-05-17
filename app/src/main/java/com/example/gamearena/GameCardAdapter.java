package com.example.gamearena;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GameCardAdapter extends RecyclerView.Adapter<GameCardAdapter.GameViewHolder> {
    private List<Game> games;

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView gameImage;
        TextView gameTitle, gameDescription;
        Button playNowButton;

        public GameViewHolder(View itemView) {
            super(itemView);
            gameImage = itemView.findViewById(R.id.gameImage);
            gameTitle = itemView.findViewById(R.id.gameTitle);
            gameDescription = itemView.findViewById(R.id.gameDescription);
            playNowButton = itemView.findViewById(R.id.playNowButton);
        }
    }

    public GameCardAdapter(List<Game> games) {
        this.games = games;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_game_card, parent, false);
        return new GameViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);
        holder.gameImage.setImageResource(game.imageResId);
        holder.gameTitle.setText(game.title);
        holder.gameDescription.setText(game.description);
        holder.playNowButton.setOnClickListener(v -> {
            if (game.onPlay != null) game.onPlay.run();
        });
    }

    @Override
    public int getItemCount() {
        return games.size();
    }
}
