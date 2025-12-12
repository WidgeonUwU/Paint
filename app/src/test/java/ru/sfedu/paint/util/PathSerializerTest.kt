package ru.sfedu.paint.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.junit.Assert.*
import org.junit.Test
import ru.sfedu.paint.data.model.DrawingTool
import ru.sfedu.paint.data.model.PathData
import ru.sfedu.paint.util.PathSerializer

class PathSerializerTest {
    /**
     * Тест: Сериализация пустого списка путей
     * Входные данные: Пустой список PathData
     * Ожидаемый результат: Метод возвращает пустой JSON-массив "[]"
     */
    @Test
    fun serialize_EmptyList_ReturnsEmptyJsonArray() {
        val paths = emptyList<PathData>()
        val result = PathSerializer.serialize(paths)
        assertEquals("[]", result)
    }

    /**
     * Тест: Сериализация одного пути
     * Входные данные: Список с одним PathData (2 точки, черный цвет, ширина 5.5, инструмент PENCIL)
     * Ожидаемый результат: Метод возвращает валидный JSON, содержащий strokeWidth и PENCIL
     */
    @Test
    fun serialize_SinglePath_ReturnsValidJson() {
        val paths = listOf(
            PathData(
                points = listOf(Offset(10f, 20f), Offset(30f, 40f)),
                color = Color.Black,
                strokeWidth = 5.5f,
                tool = DrawingTool.PENCIL
            )
        )
        val result = PathSerializer.serialize(paths)
        assertNotNull(result)
        assertTrue(result.contains("strokeWidth"))
        assertTrue(result.contains("PENCIL"))
    }

    /**
     * Тест: Сериализация нескольких путей
     * Входные данные: Список с двумя PathData (разные цвета, инструменты PENCIL и ERASER)
     * Ожидаемый результат: Метод возвращает валидный JSON, содержащий оба инструмента
     */
    @Test
    fun serialize_MultiplePaths_ReturnsValidJson() {
        val paths = listOf(
            PathData(
                points = listOf(Offset(10f, 20f)),
                color = Color.Red,
                strokeWidth = 10.25f,
                tool = DrawingTool.PENCIL
            ),
            PathData(
                points = listOf(Offset(50f, 60f), Offset(70f, 80f)),
                color = Color.Blue,
                strokeWidth = 15.75f,
                tool = DrawingTool.ERASER
            )
        )
        val result = PathSerializer.serialize(paths)
        assertNotNull(result)
        assertTrue(result.contains("PENCIL"))
        assertTrue(result.contains("ERASER"))
    }

    /**
     * Тест: Сериализация пути с пустым списком точек
     * Входные данные: PathData с пустым списком точек
     * Ожидаемый результат: Метод возвращает пустой JSON-массив "[]" (путь пропускается)
     */
    @Test
    fun serialize_PathWithEmptyPoints_SkipsPath() {
        val paths = listOf(
            PathData(
                points = emptyList(),
                color = Color.Black,
                strokeWidth = 5f,
                tool = DrawingTool.PENCIL
            )
        )
        val result = PathSerializer.serialize(paths)
        assertEquals("[]", result)
    }

    /**
     * Тест: Сохранение точности Float при сериализации ширины линии
     * Входные данные: PathData с strokeWidth = 12.345f
     * Ожидаемый результат: После сериализации и десериализации strokeWidth сохраняет точность (допуск 0.001)
     */
    @Test
    fun serialize_PathWithFloatStrokeWidth_PreservesPrecision() {
        val strokeWidth = 12.345f
        val paths = listOf(
            PathData(
                points = listOf(Offset(10f, 20f)),
                color = Color.Black,
                strokeWidth = strokeWidth,
                tool = DrawingTool.PENCIL
            )
        )
        val json = PathSerializer.serialize(paths)
        val deserialized = PathSerializer.deserialize(json)
        assertEquals(1, deserialized.size)
        assertEquals(strokeWidth, deserialized[0].strokeWidth, 0.001f)
    }

    /**
     * Тест: Десериализация пустой строки
     * Входные данные: Пустая строка ""
     * Ожидаемый результат: Метод возвращает пустой список
     */
    @Test
    fun deserialize_EmptyString_ReturnsEmptyList() {
        val result = PathSerializer.deserialize("")
        assertTrue(result.isEmpty())
    }

    /**
     * Тест: Десериализация строки с пробелами
     * Входные данные: Строка, содержащая только пробелы "   "
     * Ожидаемый результат: Метод возвращает пустой список
     */
    @Test
    fun deserialize_BlankString_ReturnsEmptyList() {
        val result = PathSerializer.deserialize("   ")
        assertTrue(result.isEmpty())
    }

    /**
     * Тест: Десериализация валидного JSON
     * Входные данные: Валидный JSON-массив с одним путем (2 точки, PENCIL, ширина 5.5)
     * Ожидаемый результат: Метод возвращает список с одним PathData, содержащим 2 точки, правильный инструмент и ширину
     */
    @Test
    fun deserialize_ValidJson_ReturnsPathList() {
        val json = """[{"color":-16777216,"strokeWidth":5.5,"tool":"PENCIL","points":[{"x":10.0,"y":20.0},{"x":30.0,"y":40.0}]}]"""
        val result = PathSerializer.deserialize(json)
        assertEquals(1, result.size)
        assertEquals(2, result[0].points.size)
        assertEquals(5.5f, result[0].strokeWidth, 0.001f)
        assertEquals(DrawingTool.PENCIL, result[0].tool)
    }

    /**
     * Тест: Десериализация нескольких путей
     * Входные данные: JSON-массив с двумя путями (PENCIL и ERASER)
     * Ожидаемый результат: Метод возвращает список с двумя PathData, каждый с правильным количеством точек и инструментом
     */
    @Test
    fun deserialize_MultiplePaths_ReturnsAllPaths() {
        val json = """[{"color":-65536,"strokeWidth":10.25,"tool":"PENCIL","points":[{"x":10.0,"y":20.0}]},{"color":-16776961,"strokeWidth":15.75,"tool":"ERASER","points":[{"x":50.0,"y":60.0},{"x":70.0,"y":80.0}]}]"""
        val result = PathSerializer.deserialize(json)
        assertEquals(2, result.size)
        assertEquals(1, result[0].points.size)
        assertEquals(2, result[1].points.size)
        assertEquals(DrawingTool.PENCIL, result[0].tool)
        assertEquals(DrawingTool.ERASER, result[1].tool)
    }

    /**
     * Тест: Десериализация с невалидным инструментом
     * Входные данные: JSON с невалидным значением tool "INVALID_TOOL"
     * Ожидаемый результат: Метод возвращает PathData с инструментом PENCIL (значение по умолчанию)
     */
    @Test
    fun deserialize_InvalidTool_DefaultsToPencil() {
        val json = """[{"color":-16777216,"strokeWidth":5.0,"tool":"INVALID_TOOL","points":[{"x":10.0,"y":20.0}]}]"""
        val result = PathSerializer.deserialize(json)
        assertEquals(1, result.size)
        assertEquals(DrawingTool.PENCIL, result[0].tool)
    }

    /**
     * Тест: Десериализация с отсутствующим strokeWidth
     * Входные данные: JSON без поля strokeWidth
     * Ожидаемый результат: Метод возвращает PathData с strokeWidth = 5.0 (значение по умолчанию)
     */
    @Test
    fun deserialize_MissingStrokeWidth_UsesDefault() {
        val json = """[{"color":-16777216,"tool":"PENCIL","points":[{"x":10.0,"y":20.0}]}]"""
        val result = PathSerializer.deserialize(json)
        assertEquals(1, result.size)
        assertEquals(5.0f, result[0].strokeWidth, 0.001f)
    }

    /**
     * Тест: Десериализация пути с пустым массивом точек
     * Входные данные: JSON с путем, содержащим пустой массив points []
     * Ожидаемый результат: Метод возвращает пустой список (путь пропускается)
     */
    @Test
    fun deserialize_PathWithEmptyPointsArray_SkipsPath() {
        val json = """[{"color":-16777216,"strokeWidth":5.0,"tool":"PENCIL","points":[]}]"""
        val result = PathSerializer.deserialize(json)
        assertTrue(result.isEmpty())
    }

    /**
     * Тест: Десериализация пути с отсутствующим полем points
     * Входные данные: JSON с путем без поля points
     * Ожидаемый результат: Метод возвращает пустой список (путь пропускается)
     */
    @Test
    fun deserialize_PathWithMissingPoints_SkipsPath() {
        val json = """[{"color":-16777216,"strokeWidth":5.0,"tool":"PENCIL"}]"""
        val result = PathSerializer.deserialize(json)
        assertTrue(result.isEmpty())
    }

    /**
     * Тест: Десериализация невалидного JSON
     * Входные данные: Невалидная JSON-строка "invalid json"
     * Ожидаемый результат: Метод выбрасывает исключение JSONException
     */
    @Test(expected = org.json.JSONException::class)
    fun deserialize_InvalidJson_ThrowsException() {
        PathSerializer.deserialize("invalid json")
    }

    /**
     * Тест: Полный цикл сериализации и десериализации (round-trip)
     * Входные данные: Список из двух PathData с разными параметрами
     * Ожидаемый результат: После сериализации и десериализации получается исходный список с сохранением всех параметров
     */
    @Test
    fun roundTrip_SerializeThenDeserialize_ReturnsOriginalPaths() {
        val originalPaths = listOf(
            PathData(
                points = listOf(Offset(10f, 20f), Offset(30f, 40f)),
                color = Color.Red,
                strokeWidth = 12.5f,
                tool = DrawingTool.PENCIL
            ),
            PathData(
                points = listOf(Offset(50f, 60f)),
                color = Color.Blue,
                strokeWidth = 8.75f,
                tool = DrawingTool.ERASER
            )
        )
        val json = PathSerializer.serialize(originalPaths)
        val deserialized = PathSerializer.deserialize(json)
        assertEquals(originalPaths.size, deserialized.size)
        for (i in originalPaths.indices) {
            assertEquals(originalPaths[i].points.size, deserialized[i].points.size)
            assertEquals(originalPaths[i].strokeWidth, deserialized[i].strokeWidth, 0.001f)
            assertEquals(originalPaths[i].tool, deserialized[i].tool)
        }
    }

    /**
     * Тест: Сериализация всех типов инструментов
     * Входные данные: PathData с каждым типом инструмента (PENCIL, ERASER, RULER)
     * Ожидаемый результат: После сериализации и десериализации инструмент сохраняется корректно
     */
    @Test
    fun serialize_AllDrawingTools_IncludesCorrectTool() {
        val tools = listOf(DrawingTool.PENCIL, DrawingTool.ERASER, DrawingTool.RULER)
        tools.forEach { tool ->
            val paths = listOf(
                PathData(
                    points = listOf(Offset(10f, 20f)),
                    color = Color.Black,
                    strokeWidth = 5f,
                    tool = tool
                )
            )
            val json = PathSerializer.serialize(paths)
            val deserialized = PathSerializer.deserialize(json)
            assertEquals(1, deserialized.size)
            assertEquals(tool, deserialized[0].tool)
        }
    }

    /**
     * Тест: Сохранение цвета при сериализации и десериализации
     * Входные данные: PathData с кастомным цветом (RGBA: 0.5, 0.3, 0.8, 0.9)
     * Ожидаемый результат: После сериализации и десериализации цвет сохраняется с точностью 0.01 для каждого компонента
     */
    @Test
    fun deserialize_ColorPreservation_PreservesColor() {
        val color = Color(0.5f, 0.3f, 0.8f, 0.9f)
        val paths = listOf(
            PathData(
                points = listOf(Offset(10f, 20f)),
                color = color,
                strokeWidth = 5f,
                tool = DrawingTool.PENCIL
            )
        )
        val json = PathSerializer.serialize(paths)
        val deserialized = PathSerializer.deserialize(json)
        assertEquals(1, deserialized.size)
        val deserializedColor = deserialized[0].color
        assertEquals(color.red, deserializedColor.red, 0.01f)
        assertEquals(color.green, deserializedColor.green, 0.01f)
        assertEquals(color.blue, deserializedColor.blue, 0.01f)
        assertEquals(color.alpha, deserializedColor.alpha, 0.01f)
    }
}


