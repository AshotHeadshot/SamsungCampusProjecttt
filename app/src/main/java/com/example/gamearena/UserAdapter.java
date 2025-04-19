package com.example.gamearena;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final List<User> users;
    private final Context context;

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leaderboard_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.positionText.setText(String.valueOf(position + 1));
        holder.nicknameText.setText(user.nickname);
        holder.pointsText.setText(user.points + " pts");
        if (user.avatarUri != null && !user.avatarUri.isEmpty()) {
            holder.avatarImage.setImageURI(Uri.parse(user.avatarUri));
        } else {
            holder.avatarImage.setImageResource(android.R.drawable.sym_def_app_icon);
        }
        // Color code medals
        int color;
        switch (position) {
            case 0: color = 0xFFFFD700; break; // Gold
            case 1: color = 0xFFC0C0C0; break; // Silver
            case 2: color = 0xFFCD7F32; break; // Bronze
            default: color = 0xFF111111; break; // Black
        }
        holder.nicknameText.setTextColor(color);
        holder.positionText.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView positionText, nicknameText, pointsText;
        UserViewHolder(View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.avatarImage);
            positionText = itemView.findViewById(R.id.positionText);
            nicknameText = itemView.findViewById(R.id.nicknameText);
            pointsText = itemView.findViewById(R.id.pointsText);
        }
    }
}
