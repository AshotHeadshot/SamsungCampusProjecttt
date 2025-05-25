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
        holder.pointsText.setText(String.valueOf(user.points));
        holder.gamesText.setText(String.valueOf(user.games));
        holder.winRateText.setText(String.format("%.1f%%", user.winRate));
        if (user.avatarUri != null && !user.avatarUri.isEmpty()) {
            try {
                holder.avatarImage.setImageURI(Uri.parse(user.avatarUri));
            } catch (SecurityException | IllegalArgumentException e) {
                // If access is denied or the URI is invalid, show default avatar
                holder.avatarImage.setImageResource(R.drawable.ic_profile_default);
            }
        } else {
            holder.avatarImage.setImageResource(R.drawable.ic_profile_default);
        }
        // Note: For full access to external images, ensure proper permissions are requested at runtime in your Activity/Fragment.
        // Color code medals
        int color;
        switch (position) {
            case 0: color = 0xFFFFD700; break; // Gold
            case 1: color = 0xFFC0C0C0; break; // Silver
            case 2: color = 0xFFCD7F32; break; // Bronze
            default: color = 0xFFFFFFFF; break; // White
        }
        String currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        // Priority: top 3 always medal color, else neon blue if current user, else white
        boolean isCurrentUser = user.uid != null && user.uid.equals(currentUid);
        // Rank color: always medal for top 3, else white
        if (position < 3) {
            holder.positionText.setTextColor(color);
            holder.positionText.setTypeface(null, android.graphics.Typeface.BOLD);
            holder.nicknameText.setTextColor(color);
            holder.nicknameText.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            holder.positionText.setTextColor(0xFFFFFFFF); // White
            holder.positionText.setTypeface(null, android.graphics.Typeface.NORMAL);
            holder.nicknameText.setTextColor(0xFFFFFFFF); // White
            holder.nicknameText.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
        // Row highlight for current user
        if (isCurrentUser) {
            holder.itemView.setBackgroundColor(0xAA222A3A); // Subtle gray-blue highlight
        } else {
            holder.itemView.setBackgroundColor(0x00000000); // Transparent
        }



    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImage;
        TextView positionText, nicknameText, pointsText, gamesText, winRateText;
        UserViewHolder(View itemView) {
            super(itemView);
            avatarImage = itemView.findViewById(R.id.avatarImage);
            positionText = itemView.findViewById(R.id.positionText);
            nicknameText = itemView.findViewById(R.id.nicknameText);
            pointsText = itemView.findViewById(R.id.pointsText);
            gamesText = itemView.findViewById(R.id.gamesText);
            winRateText = itemView.findViewById(R.id.winRateText);
        }
    }
}
