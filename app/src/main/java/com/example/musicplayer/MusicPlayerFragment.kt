package com.example.musicplayer

import Music
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import kotlin.random.Random

class MusicPlayerFragment : Fragment() {

    private lateinit var musicIcon: ImageView
    private lateinit var songName: TextView
    private lateinit var startSongTime: TextView
    private lateinit var endSongTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var backBtn: ImageView
    private lateinit var playPauseBtn: ImageView
    private lateinit var nextBtn: ImageView
    private lateinit var musicModBtn: ImageView
    private var modState: Int = 0 // 0: Repeat, 1: Shuffle, 2: Repeat Once

    private lateinit var allMusicList: List<Music> // Tüm müzik listesi
    private var currentSongIndex: Int = -1 // Şu anki şarkı indeksi
    private var isFromFavorites: Boolean = false

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_music_player, container, false)

        // Bileşenleri tanımla
        musicIcon = view.findViewById(R.id.music_icon)
        songName = view.findViewById(R.id.song_name)
        startSongTime = view.findViewById(R.id.start_song_time)
        endSongTime = view.findViewById(R.id.end_song_time)
        seekBar = view.findViewById(R.id.seek_bar)
        backBtn = view.findViewById(R.id.back_btn)
        playPauseBtn = view.findViewById(R.id.play_pause_btn)
        nextBtn = view.findViewById(R.id.next_btn)
        musicModBtn = view.findViewById(R.id.music_mod_btn)

        // Argümanları al
        arguments?.let {
            allMusicList = it.getParcelableArrayList("musicList") ?: emptyList()
            currentSongIndex = it.getInt("currentPosition", -1)
            isFromFavorites = it.getBoolean("isFromFavorites", false)
        }

        playPauseBtn.setImageResource(R.drawable.pause) // Başlangıçta duraklat düğmesi
        setupListeners()
        setupSeekBarListener()
        playCurrentSong()

        return view
    }

    private fun setupListeners() {
        backBtn.setOnClickListener {
            onBackButtonClicked()
        }

        playPauseBtn.setOnClickListener {
            onPlayPauseButtonClicked()
        }

        nextBtn.setOnClickListener {
            onNextButtonClicked()
        }

        musicModBtn.setOnClickListener {
            onChangeMod()
        }
    }

    private fun setupSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    startSongTime.text = formatDuration(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer?.pause()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                mediaPlayer?.start()
            }
        })
    }

    private fun onBackButtonClicked() {
        when (modState) {
            1 -> currentSongIndex = Random.nextInt(allMusicList.size) // Shuffle modunda rastgele şarkı seç
            else -> if (currentSongIndex > 0) currentSongIndex-- else currentSongIndex = allMusicList.size - 1
        }
        playCurrentSong()
    }

    private fun onNextButtonClicked() {
        when (modState) {
            1 -> currentSongIndex = Random.nextInt(allMusicList.size) // Shuffle modunda rastgele şarkı seç
            2 -> return // Repeat Once modunda şarkı sona erince başka şarkı çalma
            else -> if (currentSongIndex < allMusicList.size - 1) currentSongIndex++ else currentSongIndex = 0
        }
        playCurrentSong()
    }

    private fun onPlayPauseButtonClicked() {
        if (isPlaying) pauseMusic() else resumeMusic()
    }

    private fun onChangeMod() {
        modState = (modState + 1) % 3 // 0 -> Repeat, 1 -> Shuffle, 2 -> Repeat Once
        when (modState) {
            0 -> musicModBtn.setImageResource(R.drawable.repeat)
            1 -> musicModBtn.setImageResource(R.drawable.shuffle)
            2 -> musicModBtn.setImageResource(R.drawable.repeat_once)
        }
    }

    private fun playCurrentSong() {
        if (currentSongIndex != -1 && currentSongIndex < allMusicList.size) {
            val songUri = allMusicList[currentSongIndex].data
            playSong(songUri)
        }
    }

    private fun playSong(songUri: String) {
        if (songUri.isEmpty()) return

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(songUri)
            prepare()
            start()
        }

        isPlaying = true
        songName.text = allMusicList[currentSongIndex].title
        musicIcon.setImageResource(R.drawable.baseline_music_note_24)

        endSongTime.text = formatDuration(mediaPlayer?.duration ?: 0)
        seekBar.max = mediaPlayer?.duration ?: 0
        updateSeekBar()

        mediaPlayer?.setOnCompletionListener {
            when (modState) {
                2 -> pauseMusic() // Repeat Once modunda şarkı bitince duraklat
                else -> onNextButtonClicked() // Diğer modlarda sıradaki şarkıya geç
            }
        }
    }

    private fun updateSeekBar() {
        seekBar.postDelayed({
            mediaPlayer?.let {
                seekBar.progress = it.currentPosition
                startSongTime.text = formatDuration(it.currentPosition)
                updateSeekBar()
            }
        }, 1000)
    }

    private fun pauseMusic() {
        mediaPlayer?.pause()
        playPauseBtn.setImageResource(R.drawable.play)
        isPlaying = false
    }

    private fun resumeMusic() {
        mediaPlayer?.start()
        playPauseBtn.setImageResource(R.drawable.pause)
        isPlaying = true
    }

    private fun formatDuration(duration: Int): String {
        val minutes = duration / 1000 / 60
        val seconds = duration / 1000 % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    override fun onPause() {
        super.onPause()
        pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        if (isPlaying) resumeMusic()
    }
}
