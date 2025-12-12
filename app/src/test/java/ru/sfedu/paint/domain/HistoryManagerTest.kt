package ru.sfedu.paint.domain

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ru.sfedu.paint.data.model.DrawingTool
import ru.sfedu.paint.data.model.PathData
import ru.sfedu.paint.domain.HistoryManager

class HistoryManagerTest {
    private lateinit var historyManager: HistoryManager

    @Before
    fun setUp() {
        historyManager = HistoryManager(maxHistorySize = 50)
    }

    /**
     * Тест: Сохранение состояния с пустым списком путей
     * Входные данные: Пустой список PathData
     * Ожидаемый результат: Состояние сохраняется, метод canUndo() возвращает true
     */
    @Test
    fun saveState_EmptyPaths_SavesState() {
        val paths = emptyList<PathData>()
        historyManager.saveState(paths)
        assertTrue(historyManager.canUndo())
    }

    /**
     * Тест: Сохранение состояния с непустым списком путей
     * Входные данные: Список с одним PathData (2 точки)
     * Ожидаемый результат: Состояние сохраняется, метод canUndo() возвращает true
     */
    @Test
    fun saveState_NonEmptyPaths_SavesState() {
        val paths = listOf(
            createPathData(listOf(Offset(10f, 20f), Offset(30f, 40f)))
        )
        historyManager.saveState(paths)
        assertTrue(historyManager.canUndo())
    }

    /**
     * Тест: Попытка сохранить дублирующее состояние
     * Входные данные: Одинаковый список PathData сохраняется дважды подряд
     * Ожидаемый результат: Второе сохранение игнорируется, размер стека отмены не изменяется
     */
    @Test
    fun saveState_DuplicateState_DoesNotSave() {
        val paths = listOf(createPathData(listOf(Offset(10f, 20f))))
        historyManager.saveState(paths)
        val undoCountBefore = getUndoStackSize(historyManager)
        historyManager.saveState(paths)
        val undoCountAfter = getUndoStackSize(historyManager)
        assertEquals(undoCountBefore, undoCountAfter)
    }

    /**
     * Тест: Превышение максимального размера истории
     * Входные данные: Сохранение 4 состояний при maxHistorySize = 3
     * Ожидаемый результат: Самое старое состояние удаляется, метод canUndo() возвращает true
     */
    @Test
    fun saveState_MaxHistorySize_RemovesOldest() {
        val manager = HistoryManager(maxHistorySize = 3)
        manager.saveState(listOf(createPathData(listOf(Offset(0f, 0f)))))
        manager.saveState(listOf(createPathData(listOf(Offset(1f, 1f)))))
        manager.saveState(listOf(createPathData(listOf(Offset(2f, 2f)))))
        manager.saveState(listOf(createPathData(listOf(Offset(3f, 3f)))))
        assertTrue(manager.canUndo())
    }

    /**
     * Тест: Отмена при пустом стеке
     * Входные данные: Вызов undo() при пустом стеке отмены
     * Ожидаемый результат: Метод возвращает null, canUndo() возвращает false
     */
    @Test
    fun undo_EmptyStack_ReturnsNull() {
        val result = historyManager.undo(emptyList())
        assertNull(result)
        assertFalse(historyManager.canUndo())
    }

    /**
     * Тест: Отмена с сохраненными состояниями
     * Входные данные: Два сохраненных состояния, вызов undo() с текущим состоянием
     * Ожидаемый результат: Метод возвращает предыдущее состояние, canRedo() возвращает true
     */
    @Test
    fun undo_WithSavedState_ReturnsPreviousState() {
        val initialState = listOf(createPathData(listOf(Offset(10f, 20f))))
        val newState = listOf(
            createPathData(listOf(Offset(10f, 20f))),
            createPathData(listOf(Offset(30f, 40f)))
        )
        historyManager.saveState(initialState)
        historyManager.saveState(newState)
        val result = historyManager.undo(newState)
        assertNotNull(result)
        assertEquals(initialState.size, result?.size)
        assertTrue(historyManager.canRedo())
    }

    /**
     * Тест: Множественные отмены
     * Входные данные: Три сохраненных состояния, два последовательных вызова undo()
     * Ожидаемый результат: Первый undo() возвращает второе состояние, второй undo() возвращает первое состояние
     */
    @Test
    fun undo_MultipleStates_ReturnsCorrectState() {
        val state1 = listOf(createPathData(listOf(Offset(1f, 1f))))
        val state2 = listOf(createPathData(listOf(Offset(2f, 2f))))
        val state3 = listOf(createPathData(listOf(Offset(3f, 3f))))
        historyManager.saveState(state1)
        historyManager.saveState(state2)
        historyManager.saveState(state3)
        val result1 = historyManager.undo(state3)
        val result2 = historyManager.undo(result1 ?: emptyList())
        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals(state2.size, result1?.size)
        assertEquals(state1.size, result2?.size)
    }

    /**
     * Тест: Повтор при пустом стеке
     * Входные данные: Вызов redo() при пустом стеке повтора
     * Ожидаемый результат: Метод возвращает null, canRedo() возвращает false
     */
    @Test
    fun redo_EmptyStack_ReturnsNull() {
        val result = historyManager.redo(emptyList())
        assertNull(result)
        assertFalse(historyManager.canRedo())
    }

    /**
     * Тест: Повтор после отмены
     * Входные данные: Два сохраненных состояния, undo() затем redo()
     * Ожидаемый результат: Метод redo() возвращает второе состояние (то, которое было отменено)
     */
    @Test
    fun redo_AfterUndo_ReturnsNextState() {
        val state1 = listOf(createPathData(listOf(Offset(1f, 1f))))
        val state2 = listOf(createPathData(listOf(Offset(2f, 2f))))
        historyManager.saveState(state1)
        historyManager.saveState(state2)
        val undone = historyManager.undo(state2)
        val redone = historyManager.redo(undone ?: emptyList())
        assertNotNull(redone)
        assertEquals(state2.size, redone?.size)
    }

    /**
     * Тест: Повтор после множественных отмен
     * Входные данные: Три сохраненных состояния, два undo() затем redo()
     * Ожидаемый результат: Метод redo() возвращает корректное состояние
     */
    @Test
    fun redo_AfterMultipleUndos_ReturnsCorrectState() {
        val state1 = listOf(createPathData(listOf(Offset(1f, 1f))))
        val state2 = listOf(createPathData(listOf(Offset(2f, 2f))))
        val state3 = listOf(createPathData(listOf(Offset(3f, 3f))))
        historyManager.saveState(state1)
        historyManager.saveState(state2)
        historyManager.saveState(state3)
        historyManager.undo(state3)
        historyManager.undo(listOf(createPathData(listOf(Offset(2f, 2f)))))
        val result = historyManager.redo(listOf(createPathData(listOf(Offset(1f, 1f)))))
        assertNotNull(result)
    }

    /**
     * Тест: Сохранение состояния после отмены очищает стек повтора
     * Входные данные: Два сохраненных состояния, undo(), затем сохранение нового состояния
     * Ожидаемый результат: После сохранения нового состояния canRedo() возвращает false
     */
    @Test
    fun saveState_AfterUndo_ClearsRedoStack() {
        val state1 = listOf(createPathData(listOf(Offset(1f, 1f))))
        val state2 = listOf(createPathData(listOf(Offset(2f, 2f))))
        historyManager.saveState(state1)
        historyManager.saveState(state2)
        historyManager.undo(state2)
        assertTrue(historyManager.canRedo())
        historyManager.saveState(listOf(createPathData(listOf(Offset(3f, 3f)))))
        assertFalse(historyManager.canRedo())
    }

    /**
     * Тест: Проверка возможности отмены без сохраненных состояний
     * Входные данные: Пустой HistoryManager
     * Ожидаемый результат: Метод canUndo() возвращает false
     */
    @Test
    fun canUndo_NoSavedStates_ReturnsFalse() {
        assertFalse(historyManager.canUndo())
    }

    /**
     * Тест: Проверка возможности отмены с сохраненными состояниями
     * Входные данные: HistoryManager с одним сохраненным состоянием
     * Ожидаемый результат: Метод canUndo() возвращает true
     */
    @Test
    fun canUndo_WithSavedStates_ReturnsTrue() {
        historyManager.saveState(listOf(createPathData(listOf(Offset(1f, 1f)))))
        assertTrue(historyManager.canUndo())
    }

    /**
     * Тест: Проверка возможности повтора без состояний в стеке повтора
     * Входные данные: HistoryManager без состояний в стеке повтора
     * Ожидаемый результат: Метод canRedo() возвращает false
     */
    @Test
    fun canRedo_NoRedoStates_ReturnsFalse() {
        assertFalse(historyManager.canRedo())
    }

    /**
     * Тест: Проверка возможности повтора после отмены
     * Входные данные: Два сохраненных состояния, затем undo()
     * Ожидаемый результат: Метод canRedo() возвращает true
     */
    @Test
    fun canRedo_AfterUndo_ReturnsTrue() {
        historyManager.saveState(listOf(createPathData(listOf(Offset(1f, 1f)))))
        historyManager.saveState(listOf(createPathData(listOf(Offset(2f, 2f)))))
        historyManager.undo(listOf(createPathData(listOf(Offset(2f, 2f)))))
        assertTrue(historyManager.canRedo())
    }

    /**
     * Тест: Очистка всех состояний
     * Входные данные: HistoryManager с сохраненными состояниями и состояниями в стеке повтора
     * Ожидаемый результат: После clear() методы canUndo() и canRedo() возвращают false
     */
    @Test
    fun clear_WithSavedStates_ClearsAll() {
        historyManager.saveState(listOf(createPathData(listOf(Offset(1f, 1f)))))
        historyManager.saveState(listOf(createPathData(listOf(Offset(2f, 2f)))))
        historyManager.undo(listOf(createPathData(listOf(Offset(2f, 2f)))))
        historyManager.clear()
        assertFalse(historyManager.canUndo())
        assertFalse(historyManager.canRedo())
    }

    private fun createPathData(points: List<Offset>): PathData {
        return PathData(
            points = points,
            color = Color.Black,
            strokeWidth = 5f,
            tool = DrawingTool.PENCIL
        )
    }

    private fun getUndoStackSize(manager: HistoryManager): Int {
        val testPaths = listOf(createPathData(listOf(Offset(0f, 0f))))
        var count = 0
        while (manager.canUndo()) {
            manager.undo(testPaths)
            count++
        }
        return count
    }
}


