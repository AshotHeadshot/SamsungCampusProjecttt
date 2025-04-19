package com.example.gamearena;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LeaderboardFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        RecyclerView recycler = view.findViewById(R.id.leaderboardRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        List<User> users = new ArrayList<>();
        UserAdapter adapter = new UserAdapter(getContext(), users);
        recycler.setAdapter(adapter);

        // Fetch leaderboard from Firebase
        FirebaseDatabase.getInstance().getReference("users").orderByChild("points").limitToLast(50)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    users.clear();
                    for (DataSnapshot userSnap : snapshot.getChildren()) {
                        User user = userSnap.getValue(User.class);
                        if (user != null) {
                            users.add(user);
                        }
                    }
                    // Sort descending
                    Collections.sort(users, (a, b) -> b.points - a.points);
                    adapter.notifyDataSetChanged();
                }
                @Override
                public void onCancelled(DatabaseError error) {}
            });
        return view;
    }
}
