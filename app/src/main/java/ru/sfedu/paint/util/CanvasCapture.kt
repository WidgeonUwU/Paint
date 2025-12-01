package ru.sfedu.paint.util

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import ru.sfedu.paint.data.model.PathData

object CanvasCapture {
    fun captureCanvasAsBitmap(
        paths: List<PathData>,
        backgroundColor: Color,
        canvasWidth: Int,
        canvasHeight: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        
        canvas.drawColor(android.graphics.Color.argb(
            (backgroundColor.alpha * 255).toInt(),
            (backgroundColor.red * 255).toInt(),
            (backgroundColor.green * 255).toInt(),
            (backgroundColor.blue * 255).toInt()
        ))
        
        paths.forEach { pathData ->
            val androidPath = android.graphics.Path()
            
            if (pathData.points.isNotEmpty()) {
                androidPath.moveTo(pathData.points[0].x, pathData.points[0].y)
                for (i in 1 until pathData.points.size) {
                    androidPath.lineTo(pathData.points[i].x, pathData.points[i].y)
                }
            }
            
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = pathData.strokeWidth
                strokeCap = android.graphics.Paint.Cap.ROUND
                strokeJoin = android.graphics.Paint.Join.ROUND
                color = android.graphics.Color.argb(
                    (pathData.color.alpha * 255).toInt(),
                    (pathData.color.red * 255).toInt(),
                    (pathData.color.green * 255).toInt(),
                    (pathData.color.blue * 255).toInt()
                )
            }
            
            canvas.drawPath(androidPath, paint)
        }
        
        return bitmap
    }
}

