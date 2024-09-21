package com.example.musicplayer

import android.os.Bundle
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var frameLayout: FrameLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var musicDataBaseHelper: MusicDataBaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        musicDataBaseHelper = MusicDataBaseHelper(this)
        musicDataBaseHelper.resetDatabase() // Veritabanını her açıldığında sıfırla

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        frameLayout = findViewById(R.id.frame_layout)

        bottomNavigationView = findViewById(R.id.bottom_bar)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_bar -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.music_player_bar -> {
                    loadFragment(MusicPlayerFragment())
                    true
                }
                R.id.favorities_bar -> {
                    loadFragment(FavoritiesFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .commit()
    }
}
