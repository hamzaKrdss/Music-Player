package com.example.musicplayer

import Music
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MusicDataBaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "music.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "music"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DATA = "data"
        private const val COLUMN_IS_LIKED = "isLiked"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_TITLE TEXT," +
                "$COLUMN_DATA TEXT," +
                "$COLUMN_IS_LIKED INTEGER DEFAULT 0)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Get all favorite music (isLiked = 1)
    fun getFavoriteAllMusic(): List<Music> {
        val favoriteMusicList = mutableListOf<Music>()
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_TITLE, COLUMN_DATA, COLUMN_IS_LIKED),
            "$COLUMN_IS_LIKED = ?",
            arrayOf("1"),
            null, null, null
        )

        cursor.use {
            while (cursor.moveToNext()) {
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val data = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATA))
                val isLiked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LIKED))

                favoriteMusicList.add(Music(title, data, isLiked))
            }
        }

        return favoriteMusicList
    }

    // Get all music
    fun getAllMusic(): List<Music> {
        val allMusicList = mutableListOf<Music>()
        val db = this.readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_TITLE, COLUMN_DATA, COLUMN_IS_LIKED),
            null,
            null,
            null, null, null
        )

        cursor.use {
            while (cursor.moveToNext()) {
                val title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE))
                val data = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATA))
                val isLiked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_LIKED))
                allMusicList.add(Music(title, data, isLiked))
            }
        }

        return allMusicList
    }

    // Add music (isLiked default is false)
    fun addMusic(title: String, data: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DATA, data)
            put(COLUMN_IS_LIKED, 0) // Default to not liked
        }

        val cursor: Cursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_TITLE = ?",
            arrayOf(title),
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            // Şarkı zaten mevcutsa isLiked durumunu güncelle
            db.update(TABLE_NAME, values, "$COLUMN_TITLE = ?", arrayOf(title))
        } else {
            // Yeni kayıt yoksa yeni kayıt ekliyoruz
            db.insert(TABLE_NAME, null, values)
        }
        cursor.close()
    }

    // Update music like status
    fun updateMusicLikeStatus(title: String, isLiked: Boolean) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_LIKED, if (isLiked) 1 else 0)
        }
        db.update(TABLE_NAME, values, "$COLUMN_TITLE = ?", arrayOf(title))
        db.close()
    }

    // Reset database
    fun resetDatabase() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }
}
