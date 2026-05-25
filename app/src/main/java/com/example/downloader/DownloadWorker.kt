package com.example.downloader

import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.work.*
import com.example.data.AppDatabase
import com.example.data.DownloadItem
import java.io.File

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val downloadId = inputData.getString("DOWNLOAD_ID") ?: return Result.failure()
        val db = AppDatabase.getDatabase(context)
        val dao = db.dao()

        var downloadItem = dao.getDownloadById(downloadId) ?: return Result.failure()

        try {
            // Update status to DOWNLOADING
            downloadItem = downloadItem.copy(status = "DOWNLOADING", progress = 0.2f)
            dao.insertOrUpdateDownload(downloadItem)
            setProgress(workDataOf("progress" to 20))

            // Create target file path in app public downloads
            val ext = downloadItem.ext.ifEmpty { "mp4" }
            val sanitizedTitle = downloadItem.title.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
            val targetDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
            val targetFile = File(targetDir, "${downloadId}_$sanitizedTitle.$ext")

            Log.d("DownloadWorker", "Downloading URL: ${downloadItem.url} with Format: ${downloadItem.formatId} into path: ${targetFile.absolutePath}")

            // Run download via yt-dlp wrapper in Chaquopy
            val success = YtdlpWrapper.downloadMedia(
                context,
                downloadItem.url,
                downloadItem.formatId,
                targetFile.absolutePath
            )

            if (success && targetFile.exists()) {
                downloadItem = downloadItem.copy(
                    status = "COMPLETED",
                    progress = 1.0f,
                    filePath = targetFile.absolutePath
                )
                dao.insertOrUpdateDownload(downloadItem)
                setProgress(workDataOf("progress" to 100))
                return Result.success()
            } else {
                downloadItem = downloadItem.copy(status = "FAILED", progress = 0f)
                dao.insertOrUpdateDownload(downloadItem)
                return Result.failure()
            }
        } catch (e: Exception) {
            Log.e("DownloadWorker", "Download job failed", e)
            downloadItem = downloadItem.copy(status = "FAILED", progress = 0f)
            dao.insertOrUpdateDownload(downloadItem)
            return Result.failure()
        }
    }
}
