package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks")
data class BookmarkItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val url: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadItem(
    @PrimaryKey val id: String, // UUID of the download work
    val title: String,
    val url: String,
    val thumbnail: String,
    val formatId: String,
    val ext: String,
    val filePath: String,
    val status: String, // "QUEUED", "DOWNLOADING", "COMPLETED", "FAILED"
    val progress: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
