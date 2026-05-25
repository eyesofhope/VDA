package com.example.downloader

import android.content.Context
import android.util.Log
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

object YtdlpWrapper {
    private const val TAG = "YtdlpWrapper"

    fun init(context: Context) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }
    }

    suspend fun extractInfo(context: Context, url: String): String? = withContext(Dispatchers.IO) {
        try {
            init(context)
            val py = Python.getInstance()
            val pyModule = py.getModule("ytdlp_wrapper")
            val result = pyModule.callAttr("extract_info", url)
            result?.toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error in extractInfo", e)
            JSONObject().put("error", e.localizedMessage).toString()
        }
    }

    suspend fun downloadMedia(context: Context, url: String, formatId: String, outputPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            init(context)
            val py = Python.getInstance()
            val pyModule = py.getModule("ytdlp_wrapper")
            val jsonResult = pyModule.callAttr("download_media", url, formatId, outputPath).toString()
            val obj = JSONObject(jsonResult)
            obj.optBoolean("success", false)
        } catch (e: Exception) {
            Log.e(TAG, "Error in downloadMedia", e)
            false
        }
    }
}
