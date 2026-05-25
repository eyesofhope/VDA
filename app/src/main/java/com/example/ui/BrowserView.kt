package com.example.ui

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import com.example.ui.ArrowDownwardIcon
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun BrowserView(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val browserUrl by viewModel.browserUrl.collectAsState()
    val scope = rememberCoroutineScope()
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var inputUrl by remember { mutableStateOf(browserUrl) }
    val currentUrl = browserUrl

    LaunchedEffect(browserUrl) {
        inputUrl = browserUrl
    }

    var isBookmarkedPage by remember { mutableStateOf(false) }

    ExtractionDialog(viewModel = viewModel)

    LaunchedEffect(currentUrl) {
        isBookmarkedPage = viewModel.isBookmarked(currentUrl)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (currentUrl.isNotEmpty() && currentUrl != "about:blank" && !currentUrl.contains("google.com/search")) {
                FloatingActionButton(
                    onClick = {
                        viewModel.startExtraction(currentUrl)
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    ArrowDownwardIcon(tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Address Bar
            Row(
                modifier = Modifier
                    .fillCastWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    webViewInstance?.goBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                IconButton(onClick = {
                    webViewInstance?.goForward()
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Forward")
                }

                TextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    placeholder = { Text("Search or type URL") },
                    trailingIcon = {
                        IconButton(onClick = {
                            viewModel.loadUrl(inputUrl)
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )

                IconButton(onClick = {
                    webViewInstance?.reload()
                }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload")
                }

                IconButton(onClick = {
                    viewModel.toggleBookmark(
                        webViewInstance?.title ?: currentUrl,
                        currentUrl
                    )
                    scope.launch {
                        isBookmarkedPage = viewModel.isBookmarked(currentUrl)
                    }
                }) {
                    Icon(
                        imageVector = if (isBookmarkedPage) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarkedPage) Color.Red else LocalContentColor.current
                    )
                }
            }

            // Quick Platform Shortcuts
            if (currentUrl.contains("google.com") || currentUrl == "https://www.google.com" || currentUrl == "about:blank") {
                ShortcutsGrid(onShortcutClick = { url ->
                    viewModel.loadUrl(url)
                })
            }

            // Embedded Android WebView Wrapper
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                url?.let {
                                    viewModel.browserUrl.value = it
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                url?.let {
                                    viewModel.browserUrl.value = it
                                    val title = view?.title ?: "Web Page"
                                    if (url != "about:blank" && !url.contains("google.com/search") && !url.contains("google.com/")) {
                                        viewModel.addHistory(title, url)
                                    }
                                }
                            }
                        }
                        webViewInstance = this
                        loadUrl(currentUrl)
                    }
                },
                update = { webView ->
                    if (webView.url != currentUrl) {
                        webView.loadUrl(currentUrl)
                    }
                }
            )
        }
    }
}

// Custom extension wrapper to avoid any platform padding stretch
private fun Modifier.fillCastWidth() = this.fillMaxWidth()

@Composable
fun ShortcutsGrid(onShortcutClick: (String) -> Unit) {
    val shortcuts = listOf(
        ShortcutItem("YouTube", "https://m.youtube.com", Color(0xFFFF0000)),
        ShortcutItem("Instagram", "https://www.instagram.com", Color(0xFFE1306C)),
        ShortcutItem("Facebook", "https://m.facebook.com", Color(0xFF1877F2)),
        ShortcutItem("TikTok", "https://www.tiktok.com", Color(0xFF000000)),
        ShortcutItem("Twitter/X", "https://mobile.twitter.com", Color(0xFF1DA1F2)),
        ShortcutItem("Pinterest", "https://www.pinterest.com", Color(0xFFBD081C))
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Video Downloading Portal",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(180.dp)
        ) {
            items(shortcuts) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = item.color.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShortcutClick(item.url) }
                        .padding(4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = item.name,
                            fontWeight = FontWeight.SemiBold,
                            color = item.color,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }
        }
    }
}

data class ShortcutItem(
    val name: String,
    val url: String,
    val color: Color
)
