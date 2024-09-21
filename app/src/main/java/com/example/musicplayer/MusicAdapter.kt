package com.example.musicplayer

import Music
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MusicAdapter(
    private val musicList: List<Music>,
    private val musicDataBaseHelper: MusicDataBaseHelper,
    private val onItemClick: (Int) -> Unit // Lambda fonksiyonu eklendi
) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate item layout
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_music, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = musicList[position]
        holder.titleTextView.text = shortenTitle(music.title)

        // Update favorite status
        holder.favoriteImageView.setImageResource(if (music.isLiked == 1) R.drawable.like_on else R.drawable.like_off)

        holder.favoriteImageView.setOnClickListener {
            // Toggle favorite status
            if (music.isLiked == 1) {
                musicDataBaseHelper.updateMusicLikeStatus(music.title, false)
                holder.favoriteImageView.setImageResource(R.drawable.like_off)
                music.isLiked = 0
            } else {
                musicDataBaseHelper.updateMusicLikeStatus(music.title, true)
                holder.favoriteImageView.setImageResource(R.drawable.like_on)
                music.isLiked = 1
            }
        }

        holder.itemView.setOnClickListener {
            onItemClick(position) // Tıklama olayını işle
        }
    }

    override fun getItemCount(): Int = musicList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.song_title)
        val favoriteImageView: ImageView = itemView.findViewById(R.id.like_button)
    }

    private fun shortenTitle(title: String, maxLength: Int = 30): String {
        return if (title.length > maxLength) {
            "${title.substring(0, maxLength)}..."
        } else {
            title
        }
    }
}
