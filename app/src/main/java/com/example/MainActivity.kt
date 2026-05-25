package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import com.example.ui.ArrowDownwardIcon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.BrowserView
import com.example.ui.DownloadsView
import com.example.ui.HistoryView
import com.example.ui.PlayerView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen()
            }
        }
    }
}

@Composable
fun MainAppScreen() {
    val viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val currentView by viewModel.currentView.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentView != "player") {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentView == "browser",
                        onClick = { viewModel.navigateTo("browser") },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Web Browser") },
                        label = { Text("Web Explorer", fontWeight = FontWeight.Medium) }
                    )
                    NavigationBarItem(
                        selected = currentView == "downloads",
                        onClick = { viewModel.navigateTo("downloads") },
                        icon = { ArrowDownwardIcon() },
                        label = { Text("Downloads", fontWeight = FontWeight.Medium) }
                    )
                    NavigationBarItem(
                        selected = currentView == "history",
                        onClick = { viewModel.navigateTo("history") },
                        icon = { Icon(Icons.Default.Search, contentDescription = "Browsing History") },
                        label = { Text("History", fontWeight = FontWeight.Medium) }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentView) {
            "browser" -> BrowserView(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            "downloads" -> DownloadsView(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            "history" -> HistoryView(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            "player" -> PlayerView(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
            else -> BrowserView(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
