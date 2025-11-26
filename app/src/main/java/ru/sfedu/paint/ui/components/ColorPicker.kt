package ru.sfedu.paint.ui.components

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.sfedu.paint.R
import kotlin.math.*

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var selectedColor by remember { mutableStateOf(initialColor) }
    var hue by remember { mutableStateOf(0f) }
    var saturation by remember { mutableStateOf(1f) }
    var brightness by remember { mutableStateOf(1f) }
    
    LaunchedEffect(initialColor) {
        val hsv = rgbToHsv(initialColor.red, initialColor.green, initialColor.blue)
        hue = hsv[0]
        saturation = hsv[1]
        brightness = hsv[2]
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_picker_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .aspectRatio(1f)
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val center = size.center
                                val radius = min(size.width, size.height) / 2f
                                val distance = sqrt(
                                    (offset.x - center.x).pow(2) + (offset.y - center.y).pow(2)
                                )
                                
                                if (distance <= radius) {
                                    val angle = atan2(offset.y - center.y, offset.x - center.x)
                                    hue = (Math.toDegrees(angle.toDouble()) + 360).toFloat() % 360f
                                    saturation = (distance / radius).coerceIn(0f, 1f)
                                    selectedColor = hsvToColor(hue, saturation, brightness)
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                val center = size.center
                                val radius = min(size.width, size.height) / 2f
                                val distance = sqrt(
                                    (change.position.x - center.x).pow(2) + (change.position.y - center.y).pow(2)
                                )
                                
                                if (distance <= radius) {
                                    val angle = atan2(change.position.y - center.y, change.position.x - center.x)
                                    hue = (Math.toDegrees(angle.toDouble()) + 360).toFloat() % 360f
                                    saturation = (distance / radius).coerceIn(0f, 1f)
                                    selectedColor = hsvToColor(hue, saturation, brightness)
                                }
                            }
                        }
                ) {
                    ColorWheel(
                        selectedHue = hue,
                        selectedSaturation = saturation,
                        brightness = brightness,
                        onColorChanged = { h, s ->
                            hue = h
                            saturation = s
                            selectedColor = hsvToColor(h, s, brightness)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(stringResource(R.string.color_picker_brightness))
                Slider(
                    value = brightness,
                    onValueChange = { newBrightness ->
                        brightness = newBrightness
                        selectedColor = hsvToColor(hue, saturation, newBrightness)
                    },
                    valueRange = 0f..1f
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(selectedColor, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(selectedColor) }) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun ColorWheel(
    selectedHue: Float,
    selectedSaturation: Float,
    brightness: Float,
    onColorChanged: (Float, Float) -> Unit
) {
    val wheelBitmap = rememberColorWheelBitmap()

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val center = size.center
        val radius = min(size.width, size.height) / 2f
        val circlePath = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    center.x - radius,
                    center.y - radius,
                    center.x + radius,
                    center.y + radius
                )
            )
        }

        clipPath(circlePath) {
            wheelBitmap?.let { bitmap ->
                val squareSize = (radius * 2f).roundToInt()
                val dstSize = androidx.compose.ui.unit.IntSize(squareSize, squareSize)
                drawImage(
                    image = bitmap,
                    dstOffset = IntOffset(
                        (center.x - radius).roundToInt(),
                        (center.y - radius).roundToInt()
                    ),
                    dstSize = dstSize
                )
            }

            val brightnessOverlayAlpha = 1f - brightness
            if (brightnessOverlayAlpha > 0f) {
                drawCircle(
                    color = Color.Black.copy(alpha = brightnessOverlayAlpha),
                    radius = radius,
                    center = center
                )
            }
        }

        val indicatorX = center.x + selectedSaturation * radius * cos(Math.toRadians(selectedHue.toDouble())).toFloat()
        val indicatorY = center.y + selectedSaturation * radius * sin(Math.toRadians(selectedHue.toDouble())).toFloat()

        drawCircle(
            color = Color.White,
            radius = 8.dp.toPx(),
            center = Offset(indicatorX, indicatorY),
            style = Stroke(width = 2.dp.toPx())
        )
        drawCircle(
            color = Color.Black,
            radius = 6.dp.toPx(),
            center = Offset(indicatorX, indicatorY),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

fun rgbToHsv(r: Float, g: Float, b: Float): FloatArray {
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    
    val h = when {
        delta == 0f -> 0f
        max == r -> ((g - b) / delta) % 6f * 60f
        max == g -> ((b - r) / delta + 2f) * 60f
        else -> ((r - g) / delta + 4f) * 60f
    }
    
    val s = if (max == 0f) 0f else delta / max
    val v = max
    
    return floatArrayOf(
        if (h < 0) h + 360f else h,
        s,
        v
    )
}

fun hsvToColor(h: Float, s: Float, v: Float): Color {
    val c = v * s
    val x = c * (1 - abs((h / 60f) % 2f - 1))
    val m = v - c
    
    val (r, g, b) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    
    return Color(
        red = (r + m).coerceIn(0f, 1f),
        green = (g + m).coerceIn(0f, 1f),
        blue = (b + m).coerceIn(0f, 1f)
    )
}

private fun createColorWheelBitmap(width: Int, height: Int, brightness: Float = 1f): ImageBitmap {
    val bitmap = createBitmap(width, height)
    val centerX = width / 2f
    val centerY = height / 2f
    val radius = min(width, height) / 2f

    for (y in 0 until height) {
        for (x in 0 until width) {
            val dx = x - centerX
            val dy = y - centerY
            val distance = hypot(dx, dy)
            if (distance <= radius) {
                val angle = atan2(dy, dx)
                val hue = ((Math.toDegrees(angle.toDouble()) + 360) % 360).toFloat()
                val saturation = (distance / radius).coerceIn(0f, 1f)
                val color = hsvToColor(hue, saturation, brightness)
                val argb = AndroidColor.argb(
                    (color.alpha * 255).toInt(),
                    (color.red * 255).toInt(),
                    (color.green * 255).toInt(),
                    (color.blue * 255).toInt()
                )
                bitmap[x, y] = argb
            } else {
                bitmap[x, y] = AndroidColor.TRANSPARENT
            }
        }
    }

    return bitmap.asImageBitmap()
}

@Composable
private fun rememberColorWheelBitmap(): ImageBitmap? {
    val bitmapState = remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        bitmapState.value = ColorWheelBitmapCache.getOrCreate()
    }

    return bitmapState.value
}

object ColorWheelBitmapCache {
    private const val SIZE = 512
    private var cache: ImageBitmap? = null

    suspend fun getOrCreate(): ImageBitmap = withContext(Dispatchers.Default) {
        cache ?: run {
            val bitmap = createColorWheelBitmap(SIZE, SIZE)
            cache = bitmap
            bitmap
        }
    }

    suspend fun preload() {
        getOrCreate()
    }
}

