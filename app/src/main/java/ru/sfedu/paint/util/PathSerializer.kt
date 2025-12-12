package ru.sfedu.paint.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import org.json.JSONArray
import org.json.JSONObject
import ru.sfedu.paint.data.model.DrawingTool
import ru.sfedu.paint.data.model.PathData

object PathSerializer {
    fun serialize(paths: List<PathData>): String {
        val array = JSONArray()
        paths.forEach { data ->
            if (data.points.isEmpty()) return@forEach
            val obj = JSONObject().apply {
                put("color", data.color.toArgb())
                put("strokeWidth", data.strokeWidth.toDouble())
                put("tool", data.tool.name)
                val pointsArray = JSONArray()
                data.points.forEach { point ->
                    pointsArray.put(
                        JSONObject().apply {
                            put("x", point.x.toDouble())
                            put("y", point.y.toDouble())
                        }
                    )
                }
                put("points", pointsArray)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun deserialize(json: String): List<PathData> {
        if (json.isBlank()) return emptyList()
        val array = JSONArray(json)
        val result = mutableListOf<PathData>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val colorInt = obj.optInt("color")
            val strokeWidth = obj.optDouble("strokeWidth", 5.0).toFloat()
            val tool = runCatching { DrawingTool.valueOf(obj.optString("tool")) }
                .getOrDefault(DrawingTool.PENCIL)
            val pointsArray = obj.optJSONArray("points") ?: continue
            if (pointsArray.length() == 0) continue
            val points = mutableListOf<Offset>()
            for (j in 0 until pointsArray.length()) {
                val pointObj = pointsArray.getJSONObject(j)
                points.add(
                    Offset(
                        pointObj.optDouble("x", 0.0).toFloat(),
                        pointObj.optDouble("y", 0.0).toFloat()
                    )
                )
            }
            if (points.isEmpty()) continue
            result.add(
                PathData(
                    color = colorFromInt(colorInt),
                    strokeWidth = strokeWidth,
                    tool = tool,
                    points = points
                )
            )
        }
        return result
    }

    private fun colorFromInt(colorInt: Int): Color {
        val alpha = ((colorInt shr 24) and 0xFF) / 255f
        val red = ((colorInt shr 16) and 0xFF) / 255f
        val green = ((colorInt shr 8) and 0xFF) / 255f
        val blue = (colorInt and 0xFF) / 255f
        return Color(red, green, blue, alpha)
    }
}

