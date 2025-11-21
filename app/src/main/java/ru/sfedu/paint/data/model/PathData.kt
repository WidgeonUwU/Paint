package ru.sfedu.paint.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

data class PathData(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float,
    val tool: DrawingTool
)

