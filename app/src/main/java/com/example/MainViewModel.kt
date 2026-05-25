package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.*
import com.example.downloader.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import java.util.UUID

sealed interface ExtractionState {
    object Idle : ExtractionState
    object Loading : ExtractionState
    data class Success(val metadata: VideoMetadata) : ExtractionState
    data class Error(val message: String) : ExtractionState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.dao()

    val history = dao.getAllHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val bookmarks = dao.getAllBookmarks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val downloads = dao.getAllDownloads().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val browserUrl = MutableStateFlow("https://www.google.com")
    val extractionState = MutableStateFlow<ExtractionState>(ExtractionState.Idle)

    // Player queue state
    val activePlayingPath = MutableStateFlow<String?>(null)
    val activePlayingTitle = MutableStateFlow<String?>(null)

    // Navigation state: "browser", "downloads", "history", "player"
    val currentView = MutableStateFlow("browser")

    fun navigateTo(view: String) {
        currentView.value = view
    }

    fun loadUrl(url: String) {
        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://www.google.com/search?q=$url"
        } else {
            url
        }
        browserUrl.value = formattedUrl
    }

    fun addHistory(title: String, url: String) {
        viewModelScope.launch {
            dao.insertHistory(HistoryItem(title = title, url = url))
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            dao.deleteHistory(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearHistory()
        }
    }

    fun addBookmark(title: String, url: String) {
        viewModelScope.launch {
            dao.insertBookmark(BookmarkItem(title = title, url = url))
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            dao.deleteBookmark(id)
        }
    }

    suspend fun isBookmarked(url: String): Boolean {
        return dao.isBookmarked(url)
    }

    fun toggleBookmark(title: String, url: String) {
        viewModelScope.launch {
            if (dao.isBookmarked(url)) {
                // Clear matching bookmark
                // Simple state retrieval for deletion
                val dbBookmarks = dao.getAllBookmarks().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList()).value
                dbBookmarks.firstOrNull { it.url == url }?.let {
                    dao.deleteBookmark(it.id)
                }
            } else {
                dao.insertBookmark(BookmarkItem(title = title, url = url))
            }
        }
    }

    fun deleteDownload(id: String, filePath: String) {
        viewModelScope.launch {
            dao.deleteDownload(id)
            if (filePath.isNotEmpty()) {
                val file = File(filePath)
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }

    fun startExtraction(url: String) {
        extractionState.value = ExtractionState.Loading
        viewModelScope.launch {
            val jsonResult = YtdlpWrapper.extractInfo(getApplication(), url)
            if (jsonResult == null) {
                extractionState.value = ExtractionState.Error("Extraction failed: Connection closed")
                return@launch
            }
            try {
                val root = JSONObject(jsonResult)
                if (root.has("error")) {
                    extractionState.value = ExtractionState.Error(root.getString("error"))
                    return@launch
                }
                val title = root.optString("title", "Unknown Video")
                val uploader = root.optString("uploader", "Unknown Artist")
                val thumbnail = root.optString("thumbnail", "")
                val duration = root.optInt("duration", 0)

                val formatsList = mutableListOf<VideoFormat>()
                val formatsArray = root.optJSONArray("formats")
                if (formatsArray != null) {
                    for (i in 0 until formatsArray.length()) {
                        val f = formatsArray.getJSONObject(i)
                        formatsList.add(
                            VideoFormat(
                                formatId = f.optString("format_id", ""),
                                resolution = f.optString("resolution", "Unknown"),
                                ext = f.optString("ext", "mp4"),
                                size = f.optLong("size", 0L),
                                type = f.optString("type", ""),
                                url = f.optString("url", "")
                            )
                        )
                    }
                }
                
                val metadata = VideoMetadata(title, uploader, thumbnail, duration, formatsList)
                extractionState.value = ExtractionState.Success(metadata)
            } catch (e: Exception) {
                extractionState.value = ExtractionState.Error(e.localizedMessage ?: "JSON Parse Exception")
            }
        }
    }

    fun startDownload(url: String, metadata: VideoMetadata, format: VideoFormat) {
        val downloadId = UUID.randomUUID().toString()
        val downloadItem = DownloadItem(
            id = downloadId,
            title = metadata.title,
            url = url,
            thumbnail = metadata.thumbnail,
            formatId = format.formatId,
            ext = format.ext,
            filePath = "",
            status = "QUEUED",
            progress = 0f
        )

        viewModelScope.launch {
            dao.insertOrUpdateDownload(downloadItem)

            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(workDataOf("DOWNLOAD_ID" to downloadId))
                .addTag("video_download")
                .build()

            WorkManager.getInstance(getApplication()).enqueue(workRequest)
            navigateTo("downloads")
        }
    }

    fun playMedia(title: String, path: String) {
        activePlayingPath.value = path
        activePlayingTitle.value = title
        navigateTo("player")
    }
}
