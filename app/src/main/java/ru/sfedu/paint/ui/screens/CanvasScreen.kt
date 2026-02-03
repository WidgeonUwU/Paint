package ru.sfedu.paint.ui.screens

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sfedu.paint.data.model.DrawingTool
import ru.sfedu.paint.data.model.PathData
import ru.sfedu.paint.ui.components.ColorPalette
import ru.sfedu.paint.ui.components.ColorWheelBitmapCache
import ru.sfedu.paint.ui.components.DrawingCanvas
import ru.sfedu.paint.ui.components.ToolSelector
import ru.sfedu.paint.ui.viewmodel.PaintViewModel
import ru.sfedu.paint.util.ImageExporter
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.sfedu.paint.R
import ru.sfedu.paint.ui.testtags.TestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(
    drawing: ru.sfedu.paint.data.model.Drawing? = null,
    viewModel: PaintViewModel = viewModel(),
    onNavigateToGallery: () -> Unit
) {
    val drawingState by viewModel.drawingState.collectAsState()
    val currentDrawing by viewModel.currentDrawing.collectAsState()
    val paletteColors by viewModel.paletteColors.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        ColorWheelBitmapCache.preload()
    }
    
    var showTemplateDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(drawing?.id) {
        if (drawing != null) {
            if (currentDrawing?.id != drawing.id) {
                viewModel.loadDrawing(drawing)
            }
        } else {
            viewModel.createNewCanvas(1920, 1080)
            showTemplateDialog = true
        }
    }
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.forceSaveCurrentState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showStrokeWidthDialog by remember { mutableStateOf(false) }
    var showOpacityDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var drawingName by remember { mutableStateOf("") }
    
    BackHandler {
        when {
            showTemplateDialog -> {
                if (currentDrawing == null && drawingState.paths.isEmpty()) {
                    viewModel.createNewCanvas(1920, 1080)
                }
                showTemplateDialog = false
            }
            showSaveDialog -> {
                showSaveDialog = false
            }
            showExportDialog -> {
                showExportDialog = false
            }
            showStrokeWidthDialog -> {
                showStrokeWidthDialog = false
            }
            showOpacityDialog -> {
                showOpacityDialog = false
            }
            showRenameDialog -> {
                showRenameDialog = false
            }
            else -> {
                viewModel.forceSaveCurrentState()
                onNavigateToGallery()
            }
        }
    }
    
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .width(48.dp)
                    .fillMaxHeight()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            viewModel.forceSaveCurrentState()
                            onNavigateToGallery()
                        },
                        modifier = Modifier.testTag(TestTags.CANVAS_BACK)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.canvas_nav_back))
                    }
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (currentDrawing != null) {
                                        showRenameDialog = true
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentDrawing?.name ?: stringResource(R.string.canvas_new_drawing),
                        modifier = Modifier
                            .graphicsLayer { rotationZ = -90f }
                            .testTag(TestTags.CANVAS_TITLE),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = { viewModel.undo() },
                        modifier = Modifier.testTag(TestTags.CANVAS_UNDO)
                    ) {
                        Icon(painterResource(R.drawable.ic_undo), stringResource(R.string.canvas_action_undo))
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        modifier = Modifier.testTag(TestTags.CANVAS_REDO)
                    ) {
                        Icon(painterResource(R.drawable.ic_redo), stringResource(R.string.canvas_action_redo))
                    }
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag(TestTags.CANVAS_EXPORT)
                    ) {
                        Icon(painterResource(R.drawable.ic_save), stringResource(R.string.canvas_action_export))
                    }
                    IconButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.testTag(TestTags.CANVAS_SAVE)
                    ) {
                        Icon(painterResource(R.drawable.ic_done), stringResource(R.string.canvas_action_save_db))
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                DrawingCanvas(
                    modifier = Modifier.fillMaxSize().testTag(TestTags.CANVAS),
                    paths = drawingState.paths,
                    currentTool = drawingState.currentTool,
                    currentColor = drawingState.currentColor,
                    strokeWidth = drawingState.strokeWidth,
                    backgroundColor = drawingState.backgroundColor,
                    canvasWidth = drawingState.canvasWidth,
                    canvasHeight = drawingState.canvasHeight,
                    onPathDrawn = { pathData ->
                        viewModel.addPath(pathData)
                    }
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ToolSelectorVertical(
                        selectedTool = drawingState.currentTool,
                        onToolSelected = { viewModel.setTool(it) }
                    )
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    ColorPaletteVertical(
                        colors = paletteColors,
                        selectedColor = drawingState.currentColor,
                        onColorSelected = { viewModel.setColor(it) },
                        onColorReplace = { index, color ->
                            viewModel.replaceColorInPalette(index, color)
                        }
                    )
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { viewModel.clearCanvas() },
                        modifier = Modifier.testTag(TestTags.CANVAS_CLEAR)
                    ) {
                        Icon(painterResource(R.drawable.ic_clear), stringResource(R.string.canvas_button_clear))
                    }
                    IconButton(
                        onClick = { showOpacityDialog = true },
                        modifier = Modifier.testTag(TestTags.CANVAS_OPACITY)
                    ) {
                        Icon(painterResource(R.drawable.ic_trans), stringResource(R.string.opacity_dialog_title))
                    }
                    IconButton(
                        onClick = { showStrokeWidthDialog = true },
                        modifier = Modifier.testTag(TestTags.CANVAS_STROKE)
                    ) {
                        Icon(painterResource(R.drawable.ic_tick), stringResource(R.string.stroke_dialog_title))
                    }
                }
            }
        }
    } else {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onDoubleTap = {
                                        if (currentDrawing != null) {
                                            showRenameDialog = true
                                        }
                                    }
                                )
                            }
                    ) {
                        Text(
                            currentDrawing?.name ?: stringResource(R.string.canvas_new_drawing),
                            modifier = Modifier.testTag(TestTags.CANVAS_TITLE)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.forceSaveCurrentState()
                            onNavigateToGallery()
                        },
                        modifier = Modifier.testTag(TestTags.CANVAS_BACK)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.canvas_nav_back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.undo() },
                        modifier = Modifier.testTag(TestTags.CANVAS_UNDO)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_undo),
                            stringResource(R.string.canvas_action_undo)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        modifier = Modifier.testTag(TestTags.CANVAS_REDO)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_redo),
                            stringResource(R.string.canvas_action_redo)
                        )
                    }
                    IconButton(
                        onClick = { 
                            showExportDialog = true
                        },
                        modifier = Modifier.testTag(TestTags.CANVAS_EXPORT)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_save),
                            stringResource(R.string.canvas_action_export),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { showSaveDialog = true },
                        modifier = Modifier.testTag(TestTags.CANVAS_SAVE)
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_done),
                            stringResource(R.string.canvas_action_save_db),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column {
                ToolSelector(
                    selectedTool = drawingState.currentTool,
                    onToolSelected = { viewModel.setTool(it) }
                )
                ColorPalette(
                    colors = paletteColors,
                    selectedColor = drawingState.currentColor,
                    onColorSelected = { viewModel.setColor(it) },
                    onColorReplace = { index, color ->
                        viewModel.replaceColorInPalette(index, color)
                    }
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showStrokeWidthDialog = true },
                        modifier = Modifier
                            .padding(4.dp)
                            .testTag(TestTags.CANVAS_STROKE)
                    ) {
                        Icon(painterResource(R.drawable.ic_tick),
                            stringResource(R.string.stroke_dialog_title),
                            modifier = Modifier.padding(end = 2.dp))
                        Text(stringResource(R.string.canvas_stroke_value, drawingState.strokeWidth.toInt()))
                    }
                    Button(
                        onClick = { showOpacityDialog = true },
                        modifier = Modifier
                            .padding(4.dp)
                            .testTag(TestTags.CANVAS_OPACITY)
                    ) {
                        Icon(painterResource(R.drawable.ic_trans),
                            stringResource(R.string.opacity_dialog_title),
                            modifier = Modifier.padding(end = 2.dp))
                        Text(
                            stringResource(
                                R.string.canvas_opacity_value,
                                (drawingState.currentAlpha * 100).toInt()
                            )
                        )
                    }
                    Button(
                        onClick = { viewModel.clearCanvas() },
                        modifier = Modifier
                            .padding(4.dp)
                            .testTag(TestTags.CANVAS_CLEAR)
                    ) {
                        Icon(painterResource(R.drawable.ic_clear),
                            stringResource(R.string.canvas_button_clear))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DrawingCanvas(
                modifier = Modifier.fillMaxSize().testTag(TestTags.CANVAS),
                paths = drawingState.paths,
                currentTool = drawingState.currentTool,
                currentColor = drawingState.currentColor,
                strokeWidth = drawingState.strokeWidth,
                backgroundColor = drawingState.backgroundColor,
                canvasWidth = drawingState.canvasWidth,
                canvasHeight = drawingState.canvasHeight,
                onPathDrawn = { pathData ->
                    viewModel.addPath(pathData)
                }
            )
            }
        }
    }
    
    if (showTemplateDialog) {
        TemplateDialog(
            onDismiss = { 
                if (currentDrawing == null && drawingState.paths.isEmpty()) {
                    viewModel.createNewCanvas(1920, 1080)
                }
                showTemplateDialog = false 
            },
            onTemplateSelected = { template ->
                viewModel.createNewCanvas(template.width, template.height)
                showTemplateDialog = false
            }
        )
    }
    
    val defaultDrawingName = stringResource(R.string.canvas_default_name, System.currentTimeMillis())

    suspend fun showToast(message: String) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    if (showSaveDialog) {
        SaveDialog(
            initialName = currentDrawing?.name ?: defaultDrawingName,
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                scope.launch {
                    if (currentDrawing == null) {
                        viewModel.saveDrawing(name, drawingState.canvasWidth, drawingState.canvasHeight)
                    } else {
                        viewModel.updateDrawing(name)
                    }
                    showSaveDialog = false
                    showToast(context.getString(R.string.canvas_toast_saved))
                }
            }
        )
    }
    
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { formatString ->
                scope.launch {
                    try {
                        val format = when (formatString) {
                            "PNG" -> ru.sfedu.paint.util.ImageExporter.ImageFormat.PNG
                            "JPEG" -> ru.sfedu.paint.util.ImageExporter.ImageFormat.JPEG
                            else -> ru.sfedu.paint.util.ImageExporter.ImageFormat.PNG
                        }
                        
                        val bitmap = ru.sfedu.paint.util.CanvasCapture.captureCanvasAsBitmap(
                            paths = drawingState.paths,
                            backgroundColor = drawingState.backgroundColor,
                            canvasWidth = drawingState.canvasWidth,
                            canvasHeight = drawingState.canvasHeight
                        )
                        
                        val uri = viewModel.exportToGallery(bitmap, format)
                        if (uri != null) {
                            showToast(context.getString(R.string.canvas_toast_export_success))
                        } else {
                            showToast(context.getString(R.string.canvas_toast_export_error))
                        }
                        bitmap.recycle()
                    } catch (e: Exception) {
                        val message = e.message ?: ""
                        showToast(context.getString(R.string.canvas_toast_error_with_message, message))
                    }
                    showExportDialog = false
                }
            }
        )
    }
    
    if (showStrokeWidthDialog) {
        StrokeWidthDialog(
            currentWidth = drawingState.strokeWidth,
            onDismiss = { showStrokeWidthDialog = false },
            onWidthSelected = { width ->
                viewModel.setStrokeWidth(width)
                showStrokeWidthDialog = false
            }
        )
    }
    
    if (showOpacityDialog) {
        OpacityDialog(
            currentAlpha = drawingState.currentAlpha,
            onDismiss = { showOpacityDialog = false },
            onAlphaSelected = { alpha ->
                viewModel.setColorAlpha(alpha)
                showOpacityDialog = false
            }
        )
    }
    
    if (showRenameDialog) {
        val drawingToRename = currentDrawing
        if (drawingToRename != null) {
            RenameDialog(
                currentName = drawingToRename.name,
                onDismiss = { showRenameDialog = false },
                onRename = { newName: String ->
                    scope.launch {
                        viewModel.updateDrawing(newName)
                        showRenameDialog = false
                        showToast(context.getString(R.string.canvas_toast_renamed))
                    }
                }
            )
        }
    }
}

@Composable
fun TemplateDialog(
    onDismiss: () -> Unit,
    onTemplateSelected: (ru.sfedu.paint.data.model.CanvasTemplate) -> Unit
) {
    var showCustomSize by remember { mutableStateOf(false) }
    var selectedAspectRatio by remember { mutableStateOf<Pair<String, Pair<Float, Float>>?>(null) }
    var customWidth by remember { mutableStateOf("1920") }
    var customHeight by remember { mutableStateOf("1080") }
    
    val aspectRatios = ru.sfedu.paint.data.model.CanvasTemplate.ASPECT_RATIOS
    val baseWidth = 1080f
    
    if (showCustomSize) {
        CustomSizeDialog(
            initialWidth = customWidth,
            initialHeight = customHeight,
            onDismiss = { showCustomSize = false },
            onConfirm = { width, height ->
                val template = ru.sfedu.paint.data.model.CanvasTemplate(
                    "Custom",
                    width.toInt(),
                    height.toInt()
                )
                onTemplateSelected(template)
                showCustomSize = false
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.template_dialog_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        stringResource(R.string.template_dialog_presets),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    aspectRatios.forEachIndexed { index, (nameRes, ratio) ->
                        val name = stringResource(nameRes)
                        val aspectRatio = ratio.first / ratio.second
                        val width = baseWidth.toInt()
                        val height = (baseWidth / aspectRatio).toInt()
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                                .testTag("${TestTags.TEMPLATE_PRESET_PREFIX}$index")
                                .clickable {
                                    val template = ru.sfedu.paint.data.model.CanvasTemplate(
                                        name,
                                        width,
                                        height
                                    )
                                    onTemplateSelected(template)
                                }
                        ) {
                            Text(
                                text = stringResource(R.string.template_dialog_item, name, width, height),
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { showCustomSize = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(TestTags.TEMPLATE_CUSTOM_BUTTON)
                    ) {
                        Text(stringResource(R.string.template_dialog_custom_button))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
fun CustomSizeDialog(
    initialWidth: String,
    initialHeight: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var width by remember { mutableStateOf(initialWidth) }
    var height by remember { mutableStateOf(initialHeight) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_size_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = width,
                    onValueChange = { width = it },
                    label = { Text(stringResource(R.string.custom_size_width_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.CUSTOM_WIDTH_INPUT)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text(stringResource(R.string.custom_size_height_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(TestTags.CUSTOM_HEIGHT_INPUT)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val w = width.toIntOrNull() ?: 1920
                    val h = height.toIntOrNull() ?: 1080
                    onConfirm(w.toString(), h.toString())
                },
                modifier = Modifier.testTag(TestTags.CUSTOM_CONFIRM)
            ) {
                Text(stringResource(R.string.common_ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(TestTags.CUSTOM_CANCEL)
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun SaveDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.save_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.save_dialog_name_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.SAVE_INPUT)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name) },
                modifier = Modifier.testTag(TestTags.SAVE_CONFIRM)
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(TestTags.SAVE_CANCEL)
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (String) -> Unit
) {
    AlertDialog(
        modifier = Modifier.testTag(TestTags.EXPORT_DIALOG),
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_dialog_title)) },
        text = {
            Column {
                TextButton(
                    onClick = { onExport("PNG") },
                    modifier = Modifier.testTag(TestTags.EXPORT_PNG)
                ) {
                    Text(stringResource(R.string.export_dialog_format_png))
                }
                TextButton(
                    onClick = { onExport("JPEG") },
                    modifier = Modifier.testTag(TestTags.EXPORT_JPEG)
                ) {
                    Text(stringResource(R.string.export_dialog_format_jpeg))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(TestTags.EXPORT_CANCEL)
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun StrokeWidthDialog(
    currentWidth: Float,
    onDismiss: () -> Unit,
    onWidthSelected: (Float) -> Unit
) {
    var width by remember { mutableStateOf(currentWidth) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.stroke_dialog_title)) },
        text = {
            Column {
                Slider(
                    value = width,
                    onValueChange = { width = it },
                    valueRange = 1f..1000f,
                    steps = 998
                )
                Text(stringResource(R.string.canvas_stroke_value, width.toInt()))
            }
        },
        confirmButton = {
            TextButton(onClick = { onWidthSelected(width) }) {
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
fun OpacityDialog(
    currentAlpha: Float,
    onDismiss: () -> Unit,
    onAlphaSelected: (Float) -> Unit
) {
    var alpha by remember { mutableStateOf(currentAlpha) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.opacity_dialog_title)) },
        text = {
            Column {
                Slider(
                    value = alpha,
                    onValueChange = { alpha = it },
                    valueRange = 0f..1f,
                    steps = 100
                )
                Text(stringResource(R.string.canvas_opacity_value, (alpha * 100).toInt()))
            }
        },
        confirmButton = {
            TextButton(onClick = { onAlphaSelected(alpha) }) {
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
fun ToolSelectorVertical(
    selectedTool: DrawingTool,
    onToolSelected: (DrawingTool) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DrawingTool.entries.reversed().forEach { tool ->
            val icon = when (tool) {
                DrawingTool.PENCIL -> R.drawable.ic_pen
                DrawingTool.ERASER -> R.drawable.ic_eraser
                DrawingTool.RULER -> R.drawable.ic_ruler
            }
            val baseModifier = if (selectedTool == tool) {
                Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                )
            } else Modifier
            IconButton(
                onClick = { onToolSelected(tool) },
                modifier = baseModifier.then(Modifier.testTag(TestTags.toolTag(tool)))
            ) {
                Icon(
                    painterResource(icon),
                    contentDescription = null,
                    tint = if (selectedTool == tool) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ColorPaletteVertical(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    onColorReplace: (Int, Color) -> Unit
) {
    var showColorPickerForIndex by remember { mutableStateOf<Int?>(null) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colors.indices.reversed().forEach { index ->
            val color = colors[index]
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color, androidx.compose.foundation.shape.CircleShape)
                    .then(
                        if (color == selectedColor) {
                            Modifier.border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                        } else Modifier
                    )
                    .testTag(TestTags.paletteColorTag(index))
                    .pointerInput(index, color) {
                        detectTapGestures(
                            onDoubleTap = { showColorPickerForIndex = index },
                            onTap = { onColorSelected(color) }
                        )
                    }
            )
        }
    }
    
    showColorPickerForIndex?.let { index ->
        val initialColor = colors.getOrNull(index) ?: Color.White
        ru.sfedu.paint.ui.components.ColorPickerDialog(
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

