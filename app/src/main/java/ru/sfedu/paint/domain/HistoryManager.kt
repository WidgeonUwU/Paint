package ru.sfedu.paint.domain

import ru.sfedu.paint.data.model.PathData

class HistoryManager(private val maxHistorySize: Int = 50) {
    private val undoStack = mutableListOf<List<PathData>>()
    private val redoStack = mutableListOf<List<PathData>>()
    
    fun saveState(paths: List<PathData>) {
        val pathsCopy = paths.toList()
        if (undoStack.isNotEmpty() && undoStack.last() == pathsCopy) {
            return
        }
        undoStack.add(pathsCopy)
        if (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
        redoStack.clear()
    }
    
    fun undo(currentPaths: List<PathData>): List<PathData>? {
        if (undoStack.isEmpty()) return null
        
        redoStack.add(currentPaths.toList())
        if (undoStack.size == 1) {
        return undoStack.removeLastOrNull()
        }
        undoStack.removeLastOrNull()
        return undoStack.lastOrNull()
    }
    
    fun redo(currentPaths: List<PathData>): List<PathData>? {
        if (redoStack.isEmpty()) return null
        
        undoStack.add(currentPaths.toList())
        return redoStack.removeLastOrNull()
    }
    
    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()
    
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}

