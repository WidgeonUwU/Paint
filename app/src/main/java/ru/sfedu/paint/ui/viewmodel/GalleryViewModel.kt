package ru.sfedu.paint.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.sfedu.paint.data.model.Drawing
import ru.sfedu.paint.data.repository.DrawingRepository

class GalleryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DrawingRepository
    
    private val _drawings = MutableStateFlow<List<Drawing>>(emptyList())
    val drawings: StateFlow<List<Drawing>> = _drawings.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private var searchJob: kotlinx.coroutines.Job? = null
    
    init {
        val database = ru.sfedu.paint.data.database.PaintDatabaseProvider.getDatabase(application)
        repository = DrawingRepository(database.drawingDao())
        
        loadDrawings()
    }
    
    private fun loadDrawings() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                var isFirstEmit = true
            repository.getAllDrawings().collect { drawingsList ->
                _drawings.value = drawingsList
                    if (isFirstEmit) {
                        _isLoading.value = false
                        isFirstEmit = false
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
    
    fun searchDrawings(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                var isFirstEmit = true
                val flow = if (query.isEmpty()) {
                    repository.getAllDrawings()
            } else {
                    repository.searchDrawings(query)
                }
                
                flow.collect { drawingsList ->
                    _drawings.value = drawingsList
                    if (isFirstEmit) {
                        _isLoading.value = false
                        isFirstEmit = false
                    }
                }
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteDrawing(drawing: Drawing) {
        viewModelScope.launch {
            repository.deleteDrawing(drawing)
        }
    }
    
    fun renameDrawing(id: Long, newName: String) {
        viewModelScope.launch {
            val drawing = _drawings.value.find { it.id == id } ?: return@launch
            val updated = drawing.copy(name = newName)
            repository.updateDrawing(updated)
        }
    }
}

