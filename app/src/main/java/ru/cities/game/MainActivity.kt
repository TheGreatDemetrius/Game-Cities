package ru.cities.game

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.cities.game.fragment.MainFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_GameCities)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction().add(R.id.container, MainFragment()).commit()
    }
}
