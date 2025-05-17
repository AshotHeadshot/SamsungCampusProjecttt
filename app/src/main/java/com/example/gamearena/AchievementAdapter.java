package com.example.gamearena;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AchievementAdapter extends RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder> {
    private final List<Achievement> achievements;
    private final Context context;

    public AchievementAdapter(Context context, List<Achievement> achievements) {
        this.context = context;
        this.achievements = achievements;
    }

    @NonNull
    @Override
    public AchievementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_achievement, parent, false);
        return new AchievementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AchievementViewHolder holder, int position) {
        Achievement achievement = achievements.get(position);
        holder.title.setText(achievement.title);
        // Date removed as per user request
        holder.icon.setImageResource(achievement.iconResId);
    }

    @Override
    public int getItemCount() {
        return achievements.size();
    }

    static class AchievementViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView title;
        AchievementViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.achievementIcon);
            title = itemView.findViewById(R.id.achievementTitle);
            // date removed
        }
    }
}
