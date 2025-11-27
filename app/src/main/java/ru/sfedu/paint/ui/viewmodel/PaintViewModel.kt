package ru.sfedu.paint.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.sfedu.paint.R
import ru.sfedu.paint.data.model.Drawing
import ru.sfedu.paint.data.model.DrawingTool
import ru.sfedu.paint.data.model.PathData
import ru.sfedu.paint.data.repository.DrawingRepository
import ru.sfedu.paint.domain.DrawingState
import ru.sfedu.paint.domain.HistoryManager
import ru.sfedu.paint.util.CanvasCapture
import ru.sfedu.paint.util.ImageExporter

class PaintViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DrawingRepository
    private val historyManager = HistoryManager(maxHistorySize = 50)
    private val autoSaveNameFormatter = SimpleDateFormat(
        getApplication<Application>().getString(R.string.auto_save_time_pattern),
        Locale.getDefault()
    )
    private val defaultPalette = listOf(
        Color.Black,
        Color.White,
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color.Magenta,
        Color.Cyan,
        Color.Gray
    )
    
    private val _drawingState = MutableStateFlow(DrawingState())
    val drawingState: StateFlow<DrawingState> = _drawingState.asStateFlow()
    
    private val _currentDrawing = MutableStateFlow<Drawing?>(null)
    val currentDrawing: StateFlow<Drawing?> = _currentDrawing.asStateFlow()

    private val _paletteColors = MutableStateFlow(defaultPalette)
    val paletteColors: StateFlow<List<Color>> = _paletteColors.asStateFlow()
    
    private var autoSaveJob: Job? = null
    private var loadDrawingJob: Job? = null
    private val persistMutex = Mutex()
    
    fun replaceColorInPalette(index: Int, newColor: Color) {
        val current = _paletteColors.value.toMutableList()
        if (index in current.indices) {
            current[index] = newColor
            _paletteColors.value = current
        }
        setColor(newColor)
    }
    
    init {
        val database = ru.sfedu.paint.data.database.PaintDatabaseProvider.getDatabase(application)
        repository = DrawingRepository(database.drawingDao())
    }
    
    fun setTool(tool: DrawingTool) {
        _drawingState.value = _drawingState.value.copy(currentTool = tool)
    }
    
    fun setColor(color: Color) {
        val alpha = _drawingState.value.currentAlpha
        _drawingState.value = _drawingState.value.copy(currentColor = color.copy(alpha = alpha))
    }
    
    fun setColorAlpha(alpha: Float) {
        val normalized = alpha.coerceIn(0f, 1f)
        val currentColor = _drawingState.value.currentColor
        _drawingState.value = _drawingState.value.copy(
            currentAlpha = normalized,
            currentColor = currentColor.copy(alpha = normalized)
        )
    }
    
    fun setStrokeWidth(width: Float) {
        _drawingState.value = _drawingState.value.copy(strokeWidth = width)
    }
    
    fun addPath(pathData: PathData) {
        val currentPaths = _drawingState.value.paths
        historyManager.saveState(currentPaths)
        val newPaths = currentPaths + pathData
        _drawingState.value = _drawingState.value.copy(paths = newPaths)
        startAutoSave()
    }
    
    fun undo() {
        val previousState = historyManager.undo(_drawingState.value.paths)
        if (previousState != null) {
            _drawingState.value = _drawingState.value.copy(paths = previousState)
            startAutoSave()
        }
    }
    
    fun redo() {
        val nextState = historyManager.redo(_drawingState.value.paths)
        if (nextState != null) {
            _drawingState.value = _drawingState.value.copy(paths = nextState)
            startAutoSave()
        }
    }
    
    fun canUndo(): Boolean = historyManager.canUndo()
    fun canRedo(): Boolean = historyManager.canRedo()
    
    fun clearCanvas() {
        val currentPaths = _drawingState.value.paths
        historyManager.saveState(currentPaths)
        _drawingState.value = _drawingState.value.copy(paths = emptyList())
        autoSaveJob?.cancel()
    }
    
    fun createNewCanvas(width: Int, height: Int, backgroundColor: Color = Color.White) {
        loadDrawingJob?.cancel()
        _currentDrawing.value = null
        autoSaveJob?.cancel()
        val initialState = DrawingState(
            backgroundColor = backgroundColor,
            currentColor = Color.Black,
            currentTool = DrawingTool.PENCIL,
            strokeWidth = 5f,
            currentAlpha = 1f,
            canvasWidth = width,
            canvasHeight = height
        )
        _drawingState.value = initialState
        historyManager.clear()
        historyManager.saveState(emptyList())
    }
    
    fun loadDrawing(drawing: Drawing) {
        loadDrawingJob?.cancel()
        loadDrawingJob = viewModelScope.launch {
            val targetDrawingId = drawing.id
            _currentDrawing.value = drawing
            
            val loadedPaths = loadPathsFromStorage(targetDrawingId)
            
            if (!coroutineContext.isActive || _currentDrawing.value?.id != targetDrawingId) {
                return@launch
            }
            
            val initialState = DrawingState(
                backgroundColor = Color(drawing.backgroundColor),
                currentColor = Color.Black,
                currentTool = DrawingTool.PENCIL,
                strokeWidth = 5f,
                currentAlpha = 1f,
                canvasWidth = drawing.width,
                canvasHeight = drawing.height
            ).copy(paths = loadedPaths)
            
            if (!coroutineContext.isActive || _currentDrawing.value?.id != targetDrawingId) {
                return@launch
            }
            
            _drawingState.value = initialState
            historyManager.clear()
            historyManager.saveState(loadedPaths)
            autoSaveJob?.cancel()
        }
    }
    
    fun saveDrawing(name: String, width: Int, height: Int) {
        viewModelScope.launch {
            persistDrawingState(nameOverride = name)
        }
    }
    
    fun updateDrawing(name: String) {
        viewModelScope.launch {
            persistDrawingState(nameOverride = name)
        }
    }
    
    suspend fun exportToGallery(bitmap: Bitmap, format: ImageExporter.ImageFormat): Uri? {
        return ImageExporter.saveBitmapToGallery(
            getApplication(),
            bitmap,
            format
        )
    }
    
    private fun startAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(30000)
            persistDrawingState()
        }
    }

    fun forceSaveCurrentState() {
        viewModelScope.launch {
            persistDrawingState()
        }
    }

    private suspend fun persistDrawingState(nameOverride: String? = null) {
        persistMutex.withLock {
            val state = _drawingState.value
            val existing = _currentDrawing.value
            val paths = state.paths
            val pathsJson = serializePaths(paths)
            val previewBytes = generatePreviewBytes(state)

            if (existing == null) {
                val now = Date()
                val baseName = getApplication<Application>().getString(R.string.canvas_new_drawing)
                val generatedName = "$baseName ${autoSaveNameFormatter.format(now)}"
                val name = nameOverride ?: generatedName
                val drawing = Drawing(
                    name = name,
                    createdAt = now,
                    width = state.canvasWidth,
                    height = state.canvasHeight,
                    backgroundColor = state.backgroundColor.toArgb().toLong(),
                    preview = previewBytes
                )
                val id = repository.insertDrawing(drawing)
                repository.upsertContent(id, pathsJson)
                val savedDrawing = drawing.copy(id = id)
                _currentDrawing.value = savedDrawing
            } else {
                repository.upsertContent(existing.id, pathsJson)
                val updated = existing.copy(
                    name = nameOverride ?: existing.name,
                    width = state.canvasWidth,
                    height = state.canvasHeight,
                    backgroundColor = state.backgroundColor.toArgb().toLong(),
                    preview = previewBytes ?: existing.preview
                )
                repository.updateDrawing(updated)
                _currentDrawing.value = updated
            }
        }
    }

    private suspend fun loadPathsFromStorage(id: Long): List<PathData> {
        return withContext(Dispatchers.IO) {
            val pathsJson = repository.getPathsJson(id) ?: return@withContext emptyList()
            deserializePaths(pathsJson)
        }
    }

    private suspend fun generatePreviewBytes(state: DrawingState): ByteArray? {
        return withContext(Dispatchers.Default) {
            runCatching {
                val bitmap = CanvasCapture.captureCanvasAsBitmap(
                    paths = state.paths,
                    backgroundColor = state.backgroundColor,
                    canvasWidth = state.canvasWidth,
                    canvasHeight = state.canvasHeight
                )
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, stream)
                bitmap.recycle()
                stream.toByteArray()
            }.getOrNull()
        }
    }

    private fun serializePaths(paths: List<PathData>): String {
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

    private fun deserializePaths(json: String): List<PathData> {
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

