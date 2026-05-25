package com.example.downloader

data class VideoMetadata(
    val title: String,
    val uploader: String,
    val thumbnail: String,
    val duration: Int,
    val formats: List<VideoFormat>
)

data class VideoFormat(
    val formatId: String,
    val resolution: String,
    val ext: String,
    val size: Long,
    val type: String, // "combined", "video_only", "audio_only"
    val url: String
)
