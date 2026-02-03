package ru.sfedu.paint.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.sfedu.paint.data.model.Drawing
import ru.sfedu.paint.ui.viewmodel.GalleryViewModel
import java.text.SimpleDateFormat
import java.util.*
import ru.sfedu.paint.R
import ru.sfedu.paint.ui.testtags.TestTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel = viewModel(),
    onDrawingSelected: (Drawing) -> Unit,
    onCreateNew: () -> Unit
) {
    val drawings by viewModel.drawings.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<Drawing?>(null) }
    
    LaunchedEffect(searchQuery) {
        viewModel.searchDrawings(searchQuery)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.gallery_title)) },
                actions = {
                    IconButton(
                        onClick = onCreateNew,
                        modifier = Modifier.testTag(TestTags.GALLERY_CREATE_BUTTON)
                    ) {
                        Icon(Icons.Default.Add, stringResource(R.string.gallery_create_new))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text(stringResource(R.string.gallery_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.testTag(TestTags.GALLERY_CLEAR_SEARCH)
                        ) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag(TestTags.GALLERY_SEARCH),
                singleLine = true
            )
            
            if (isLoading && drawings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (drawings.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painterResource(R.drawable.ic_image),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.gallery_empty_message),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCreateNew,
                            modifier = Modifier.testTag(TestTags.GALLERY_EMPTY_CREATE_BUTTON)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.gallery_create_new))
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag(TestTags.GALLERY_GRID)
                ) {
                    items(drawings) { drawing ->
                        DrawingCard(
                            drawing = drawing,
                            onClick = { onDrawingSelected(drawing) },
                            onDelete = { showDeleteDialog = drawing },
                            onRename = { newName ->
                                viewModel.renameDrawing(drawing.id, newName)
                            }
                        )
                    }
                }
            }
        }
    }
    
    showDeleteDialog?.let { drawing ->
        DeleteDialog(
            drawing = drawing,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteDrawing(drawing)
                showDeleteDialog = null
            }
        )
    }
}

@Composable
fun DrawingCard(
    drawing: Drawing,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    val previewBitmap = remember(drawing.preview) {
        drawing.preview?.let { bytes ->
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .testTag("${TestTags.GALLERY_CARD_PREFIX}${drawing.id}")
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (previewBitmap != null) {
                    Image(
                        bitmap = previewBitmap,
                        contentDescription = drawing.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painterResource(R.drawable.ic_image),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                showRenameDialog = true
                            }
                        )
                    }
            ) {
                Text(
                    text = drawing.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val datePattern = stringResource(R.string.gallery_date_format)
            Text(
                text = remember(drawing.createdAt, datePattern) {
                    SimpleDateFormat(datePattern, Locale.getDefault()).format(drawing.createdAt)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = { showRenameDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag(TestTags.GALLERY_RENAME)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag(TestTags.GALLERY_DELETE)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
    
    if (showRenameDialog) {
        RenameDialog(
            currentName = drawing.name,
            onDismiss = { showRenameDialog = false },
            onRename = { newName ->
                onRename(newName)
                showRenameDialog = false
            }
        )
    }
}

@Composable
fun DeleteDialog(
    drawing: Drawing,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_dialog_title)) },
        text = { Text(stringResource(R.string.delete_dialog_message, drawing.name)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(TestTags.GALLERY_DELETE_CONFIRM)
            ) {
                Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(TestTags.GALLERY_DELETE_CANCEL)
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.save_dialog_name_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.RENAME_INPUT)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onRename(name) },
                modifier = Modifier.testTag(TestTags.RENAME_CONFIRM)
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(TestTags.RENAME_CANCEL)
            ) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

