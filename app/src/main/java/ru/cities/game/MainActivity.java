package ru.cities.game;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import ru.cities.game.fragment.MainFragment;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_GameCities);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.container, MainFragment.class, null).commit();
    }
}
