package ru.sfedu.paint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ru.sfedu.paint.data.model.Drawing
import ru.sfedu.paint.ui.screens.CanvasScreen
import ru.sfedu.paint.ui.screens.GalleryScreen
import ru.sfedu.paint.ui.theme.PaintTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaintTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PaintApp()
                }
            }
        }
    }
}

@Composable
fun PaintApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Gallery) }
    var selectedDrawing by remember { mutableStateOf<Drawing?>(null) }
    var newCanvasKey by remember { mutableStateOf(0L) }
    
    when (currentScreen) {
        is Screen.Gallery -> {
            GalleryScreen(
                onDrawingSelected = { drawing ->
                    selectedDrawing = drawing
                    currentScreen = Screen.Canvas(drawing)
                },
                onCreateNew = {
                    selectedDrawing = null
                    newCanvasKey = System.currentTimeMillis()
                    currentScreen = Screen.Canvas(null)
                }
            )
        }
        is Screen.Canvas -> {
            val canvasKey = if ((currentScreen as Screen.Canvas).drawing == null) {
                newCanvasKey
            } else {
                (currentScreen as Screen.Canvas).drawing?.id ?: 0L
            }
            key(canvasKey) {
                CanvasScreen(
                    drawing = (currentScreen as Screen.Canvas).drawing,
                    onNavigateToGallery = {
                        currentScreen = Screen.Gallery
                    }
                )
            }
        }
    }
}

sealed class Screen {
    object Gallery : Screen()
    data class Canvas(val drawing: Drawing?) : Screen()
}
