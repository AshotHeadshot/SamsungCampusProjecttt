package com.example.gamearena;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class GamesPagerAdapter extends FragmentStateAdapter {
    public GamesPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new RankedGamesFragment();
        } else {
            return new UnrankedGamesFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
