package com.example.musicplayer

import Music
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var musicDataBaseHelper: MusicDataBaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.all_music_list_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        musicDataBaseHelper = MusicDataBaseHelper(requireContext())

        checkSelfPermission()
        return view
    }

    private fun loadMusic() {
        val musicList = mutableListOf<Music>()
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA)
        val cursor = requireActivity().contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            val titlesSeen = mutableSetOf<String>()

            while (cursor.moveToNext()) {
                val title = cursor.getString(titleColumn)
                val data = cursor.getString(dataColumn)

                if (!titlesSeen.contains(title)) {
                    titlesSeen.add(title)

                    if (!musicDataBaseHelper.getAllMusic().any { it.title == title }) {
                        musicDataBaseHelper.addMusic(title, data)
                    }

                    val isLiked = musicDataBaseHelper.getAllMusic().any { it.title == title && it.isLiked == 1 }
                    val music = Music(title, data, if (isLiked) 1 else 0)
                    musicList.add(music)
                }
            }
        }

        // Adapter'ı müzik listesi ile oluştur
        recyclerView.adapter = MusicAdapter(musicList, musicDataBaseHelper) { position ->
            playMusic(position, musicList)
        }
    }

    private fun playMusic(position: Int, musicList: List<Music>) {
        val musicPlayerFragment = MusicPlayerFragment()
        val bundle = Bundle().apply {
            putParcelableArrayList("musicList", ArrayList(musicList))
            putInt("currentPosition", position)
            putString("songUri", musicList[position].data) // Şarkı URI'sini ekle
            putBoolean("isFromFavorites", false) // HomeFragment'tan gelindi
        }
        musicPlayerFragment.arguments = bundle

        // BottomNavigationView'de music_player_bar item'ını seçili yap
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_bar)
        bottomNavigationView.menu.findItem(R.id.music_player_bar).isChecked = true

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, musicPlayerFragment)
            .addToBackStack(null)
            .commit()
    }



    private fun checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        } else {
            loadMusic()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadMusic()
            } else {
                Toast.makeText(requireContext(), "İzin verilmedi, müzik dosyalarına erişim sağlanamayacak.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
