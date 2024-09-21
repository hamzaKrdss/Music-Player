package com.example.musicplayer

import Music
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FavoritiesFragment : Fragment() {

    private lateinit var musicDataBaseHelper: MusicDataBaseHelper
    private lateinit var favoritiesRecyclerView: RecyclerView
    private lateinit var musicAdapter: MusicAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorities, container, false)

        // RecyclerView ve DatabaseHelper'ı tanımla
        favoritiesRecyclerView = view.findViewById(R.id.favorities_recycler_view)
        musicDataBaseHelper = MusicDataBaseHelper(requireContext())

        // Favori müzikleri getir
        val favoriteMusicList = musicDataBaseHelper.getFavoriteAllMusic()

        // RecyclerView için adapter oluştur
        musicAdapter = MusicAdapter(favoriteMusicList, musicDataBaseHelper) { position ->
            playMusic(position, favoriteMusicList)
        }

        // RecyclerView ayarlarını yap
        favoritiesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        favoritiesRecyclerView.adapter = musicAdapter

        return view
    }

    private fun playMusic(position: Int, favoriteMusicList: List<Music>) {
        val musicPlayerFragment = MusicPlayerFragment()
        val bundle = Bundle().apply {
            putParcelableArrayList("musicList", ArrayList(favoriteMusicList))
            putInt("currentPosition", position)
            putBoolean("isFromFavorites", true) // Favori fragment'tan geliniyor
        }
        musicPlayerFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, musicPlayerFragment)
            .addToBackStack(null)
            .commit()
    }
}
