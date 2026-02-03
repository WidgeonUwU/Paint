package ru.sfedu.paint.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import ru.sfedu.paint.ui.testtags.TestTags

@Composable
fun ColorPalette(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onColorReplace: (Int, Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorPickerForIndex by remember { mutableStateOf<Int?>(null) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEachIndexed { index, color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, CircleShape)
                    .border(
                        width = if (color == selectedColor) 3.dp else 0.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                    .testTag(TestTags.paletteColorTag(index))
                    .pointerInput(index, color) {
                        detectTapGestures(
                            onDoubleTap = {
                                showColorPickerForIndex = index
                            },
                            onTap = {
                                onColorSelected(color)
                            }
                        )
                    }
            )
        }
    }
    
    showColorPickerForIndex?.let { index ->
        val initialColor = colors.getOrNull(index) ?: Color.White
        ColorPickerDialog(
            initialColor = initialColor,
            onDismiss = { showColorPickerForIndex = null },
            onColorSelected = { newColor ->
                onColorReplace(index, newColor)
                onColorSelected(newColor)
                showColorPickerForIndex = null
            }
        )
    }
}

