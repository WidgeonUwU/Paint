package ru.sfedu.paint.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.min
import ru.sfedu.paint.data.model.DrawingTool
import ru.sfedu.paint.data.model.PathData

@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    paths: List<PathData>,
    currentTool: DrawingTool,
    currentColor: Color,
    strokeWidth: Float,
    backgroundColor: Color,
    canvasWidth: Int,
    canvasHeight: Int,
    onPathDrawn: (PathData) -> Unit
) {
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var currentPathStart by remember { mutableStateOf<Offset?>(null) }
    var currentPathPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var pathUpdateKey by remember { mutableStateOf(0) }
    
    val frameColor = MaterialTheme.colorScheme.background

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(currentTool, currentColor, strokeWidth, backgroundColor, canvasWidth, canvasHeight) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            when (currentTool) {
                                DrawingTool.PENCIL, DrawingTool.ERASER -> {
                                    val path = Path()
                                    path.moveTo(offset.x, offset.y)
                                    currentPath = path
                                    currentPathPoints = listOf(offset)
                                    pathUpdateKey++
                                }
                                DrawingTool.RULER -> {
                                    currentPathStart = offset
                                    val path = Path()
                                    path.moveTo(offset.x, offset.y)
                                    currentPath = path
                                    currentPathPoints = listOf(offset)
                                    pathUpdateKey++
                                }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            when (currentTool) {
                                DrawingTool.PENCIL -> {
                                    currentPath?.let { path ->
                                        path.lineTo(change.position.x, change.position.y)
                                        currentPathPoints = currentPathPoints + change.position
                                        pathUpdateKey++
                                    }
                                }
                                DrawingTool.ERASER -> {
                                    currentPath?.let { path ->
                                        path.lineTo(change.position.x, change.position.y)
                                        currentPathPoints = currentPathPoints + change.position
                                        pathUpdateKey++
                                    }
                                }
                                DrawingTool.RULER -> {
                                    currentPathStart?.let { start ->
                                        val path = Path()
                                        path.moveTo(start.x, start.y)
                                        path.lineTo(change.position.x, change.position.y)
                                        currentPath = path
                                        currentPathPoints = listOf(start, change.position)
                                        pathUpdateKey++
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            currentPath?.let { path ->
                                val pathData = PathData(
                                    points = currentPathPoints,
                                    color = if (currentTool == DrawingTool.ERASER) backgroundColor else currentColor,
                                    strokeWidth = strokeWidth,
                                    tool = currentTool
                                )
                                onPathDrawn(pathData)
                                currentPath = null
                                currentPathPoints = emptyList()
                                pathUpdateKey++
                            }
                            currentPathStart = null
                        }
                    )
                }
        ) {

        drawRect(
            color = frameColor,
            size = Size(width = size.width, height = size.height)
        )

        val clipWidth = min(canvasWidth.toFloat(), size.width)
        val clipHeight = min(canvasHeight.toFloat(), size.height)

        clipRect(
            left = 0f,
            top = 0f,
            right = clipWidth,
            bottom = clipHeight
        ) {
            drawRect(backgroundColor, size = Size(clipWidth, clipHeight))

        fun buildPath(points: List<Offset>): Path? {
            if (points.isEmpty()) return null
            val path = Path()
            val first = points.first()
            path.moveTo(first.x, first.y)
            points.drop(1).forEach { point ->
                path.lineTo(point.x, point.y)
            }
            return path
        }

        paths.forEach { pathData ->
            val path = buildPath(pathData.points)
            if (path != null) {
                drawPath(
                    path = path,
                    color = pathData.color,
                    style = Stroke(
                        width = pathData.strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }

            val _aa = pathUpdateKey
            currentPath?.let { path ->
                val color = if (currentTool == DrawingTool.ERASER) backgroundColor else currentColor
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
        }
    }
}

