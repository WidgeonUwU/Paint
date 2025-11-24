package ru.sfedu.paint.domain

import androidx.compose.ui.graphics.Color
import ru.sfedu.paint.data.model.DrawingTool

data class DrawingState(
    val paths: List<ru.sfedu.paint.data.model.PathData> = emptyList(),
    val currentTool: DrawingTool = DrawingTool.PENCIL,
    val currentColor: Color = Color.Black,
    val strokeWidth: Float = 5f,
    val currentAlpha: Float = 1f,
    val backgroundColor: Color = Color.White,
    val canvasWidth: Int = 1920,
    val canvasHeight: Int = 1080
)

