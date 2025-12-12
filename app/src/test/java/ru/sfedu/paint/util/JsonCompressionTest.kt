package ru.sfedu.paint.util

import org.junit.Assert.*
import org.junit.Test

class JsonCompressionTest {
    /**
     * Тест: Сжатие валидной JSON-строки
     * Входные данные: Валидная JSON-строка {"test": "data", "number": 123}
     * Ожидаемый результат: Метод возвращает непустой массив байтов
     */
    @Test
    fun compress_ValidJsonString_ReturnsCompressedByteArray() {
        val json = """{"test": "data", "number": 123}"""
        val result = JsonCompression.compress(json)
        assertNotNull(result)
        assertTrue(result.size > 0)
    }

    /**
     * Тест: Сжатие пустой строки
     * Входные данные: Пустая строка ""
     * Ожидаемый результат: Метод возвращает массив байтов (размер >= 0)
     */
    @Test
    fun compress_EmptyString_ReturnsCompressedByteArray() {
        val json = ""
        val result = JsonCompression.compress(json)
        assertNotNull(result)
        assertTrue(result.size >= 0)
    }

    /**
     * Тест: Сжатие большой JSON-строки
     * Входные данные: JSON-строка с 1000 повторяющимися объектами
     * Ожидаемый результат: Метод возвращает непустой массив байтов
     */
    @Test
    fun compress_LargeJsonString_ReturnsCompressedByteArray() {
        val json = buildString {
            repeat(1000) {
                append("""{"key$it": "value$it"},""")
            }
        }
        val result = JsonCompression.compress(json)
        assertNotNull(result)
        assertTrue(result.size > 0)
    }

    /**
     * Тест: Декомпрессия валидных сжатых данных
     * Входные данные: Сжатая JSON-строка {"test": "data", "number": 123}
     * Ожидаемый результат: Метод возвращает исходную JSON-строку без изменений
     */
    @Test
    fun decompress_ValidCompressedData_ReturnsOriginalString() {
        val original = """{"test": "data", "number": 123}"""
        val compressed = JsonCompression.compress(original)
        val result = JsonCompression.decompress(compressed)
        assertEquals(original, result)
    }

    /**
     * Тест: Декомпрессия пустых сжатых данных
     * Входные данные: Сжатая пустая строка
     * Ожидаемый результат: Метод возвращает пустую строку
     */
    @Test
    fun decompress_EmptyCompressedData_ReturnsEmptyString() {
        val compressed = JsonCompression.compress("")
        val result = JsonCompression.decompress(compressed)
        assertEquals("", result)
    }

    /**
     * Тест: Декомпрессия больших сжатых данных
     * Входные данные: Сжатая JSON-строка с 1000 повторяющимися объектами
     * Ожидаемый результат: Метод возвращает исходную JSON-строку без изменений
     */
    @Test
    fun decompress_LargeCompressedData_ReturnsOriginalString() {
        val original = buildString {
            repeat(1000) {
                append("""{"key$it": "value$it"},""")
            }
        }
        val compressed = JsonCompression.compress(original)
        val result = JsonCompression.decompress(compressed)
        assertEquals(original, result)
    }

    /**
     * Тест: Декомпрессия невалидных данных
     * Входные данные: Массив байтов, не являющийся сжатыми данными [0x00, 0x01, 0x02, 0x03, 0x04]
     * Ожидаемый результат: Метод выбрасывает исключение
     */
    @Test(expected = Exception::class)
    fun decompress_InvalidData_ThrowsException() {
        val invalidData = byteArrayOf(0x00, 0x01, 0x02, 0x03, 0x04)
        JsonCompression.decompress(invalidData)
    }

    /**
     * Тест: Полный цикл сжатия и декомпрессии (round-trip)
     * Входные данные: Различные JSON-строки (простая, вложенная, массив, с Unicode)
     * Ожидаемый результат: После сжатия и декомпрессии получается исходная строка
     */
    @Test
    fun compressAndDecompress_RoundTrip_ReturnsOriginalData() {
        val testCases = listOf(
            """{"simple": "json"}""",
            """{"nested": {"object": {"value": 123}}}""",
            """{"array": [1, 2, 3, 4, 5]}""",
            """{"unicode": "тест 测试 🎨"}"""
        )
        testCases.forEach { original ->
            val compressed = JsonCompression.compress(original)
            val decompressed = JsonCompression.decompress(compressed)
            assertEquals(original, decompressed)
        }
    }
}


