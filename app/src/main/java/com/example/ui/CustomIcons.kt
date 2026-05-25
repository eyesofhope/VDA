package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ArrowDownwardIcon(modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Canvas(modifier = modifier.size(24.dp)) {
        val width = size.width
        val height = size.height
        // Arrow stem
        drawLine(
            color = tint,
            start = Offset(width / 2, height * 0.15f),
            end = Offset(width / 2, height * 0.75f),
            strokeWidth = 2.5f.dp.toPx()
        )
        // Arrow tip left
        drawLine(
            color = tint,
            start = Offset(width * 0.25f, height * 0.50f),
            end = Offset(width / 2, height * 0.75f),
            strokeWidth = 2.5f.dp.toPx()
        )
        // Arrow tip right
        drawLine(
            color = tint,
            start = Offset(width * 0.75f, height * 0.50f),
            end = Offset(width / 2, height * 0.75f),
            strokeWidth = 2.5f.dp.toPx()
        )
    }
}
