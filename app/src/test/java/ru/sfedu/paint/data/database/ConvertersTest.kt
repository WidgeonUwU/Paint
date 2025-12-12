package ru.sfedu.paint.data.database

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date
import ru.sfedu.paint.data.database.Converters

class ConvertersTest {
    private lateinit var converters: Converters

    @Before
    fun setUp() {
        converters = Converters()
    }

    /**
     * Тест: Преобразование валидного timestamp в Date
     * Входные данные: Timestamp 1609459200000L (1 января 2021)
     * Ожидаемый результат: Метод возвращает Date с временем, равным переданному timestamp
     */
    @Test
    fun fromTimestamp_ValidTimestamp_ReturnsDate() {
        val timestamp = 1609459200000L
        val result = converters.fromTimestamp(timestamp)
        assertNotNull(result)
        assertEquals(timestamp, result?.time)
    }

    /**
     * Тест: Преобразование null timestamp в Date
     * Входные данные: null
     * Ожидаемый результат: Метод возвращает null
     */
    @Test
    fun fromTimestamp_NullTimestamp_ReturnsNull() {
        val result = converters.fromTimestamp(null)
        assertNull(result)
    }

    /**
     * Тест: Преобразование нулевого timestamp в Date
     * Входные данные: Timestamp 0L (эпоха Unix)
     * Ожидаемый результат: Метод возвращает Date(0)
     */
    @Test
    fun fromTimestamp_ZeroTimestamp_ReturnsEpochDate() {
        val timestamp = 0L
        val result = converters.fromTimestamp(timestamp)
        assertNotNull(result)
        assertEquals(Date(0), result)
    }

    /**
     * Тест: Преобразование большого timestamp в Date
     * Входные данные: Timestamp Long.MAX_VALUE
     * Ожидаемый результат: Метод возвращает Date с временем, равным переданному timestamp
     */
    @Test
    fun fromTimestamp_LargeTimestamp_ReturnsCorrectDate() {
        val timestamp = Long.MAX_VALUE
        val result = converters.fromTimestamp(timestamp)
        assertNotNull(result)
        assertEquals(timestamp, result?.time)
    }

    /**
     * Тест: Преобразование отрицательного timestamp в Date
     * Входные данные: Timestamp -1000L
     * Ожидаемый результат: Метод возвращает Date с временем, равным переданному timestamp
     */
    @Test
    fun fromTimestamp_NegativeTimestamp_ReturnsCorrectDate() {
        val timestamp = -1000L
        val result = converters.fromTimestamp(timestamp)
        assertNotNull(result)
        assertEquals(timestamp, result?.time)
    }

    /**
     * Тест: Преобразование валидной Date в timestamp
     * Входные данные: Date(1609459200000L)
     * Ожидаемый результат: Метод возвращает timestamp 1609459200000L
     */
    @Test
    fun dateToTimestamp_ValidDate_ReturnsTimestamp() {
        val date = Date(1609459200000L)
        val result = converters.dateToTimestamp(date)
        assertNotNull(result)
        assertEquals(1609459200000L, result)
    }

    /**
     * Тест: Преобразование null Date в timestamp
     * Входные данные: null
     * Ожидаемый результат: Метод возвращает null
     */
    @Test
    fun dateToTimestamp_NullDate_ReturnsNull() {
        val result = converters.dateToTimestamp(null)
        assertNull(result)
    }

    /**
     * Тест: Преобразование Date эпохи в timestamp
     * Входные данные: Date(0)
     * Ожидаемый результат: Метод возвращает timestamp 0L
     */
    @Test
    fun dateToTimestamp_EpochDate_ReturnsZero() {
        val date = Date(0)
        val result = converters.dateToTimestamp(date)
        assertNotNull(result)
        assertEquals(0L, result)
    }

    /**
     * Тест: Преобразование текущей Date в timestamp
     * Входные данные: Текущая дата Date()
     * Ожидаемый результат: Метод возвращает timestamp, равный времени переданной даты
     */
    @Test
    fun dateToTimestamp_CurrentDate_ReturnsCurrentTimestamp() {
        val date = Date()
        val result = converters.dateToTimestamp(date)
        assertNotNull(result)
        assertEquals(date.time, result)
    }

    /**
     * Тест: Преобразование будущей Date в timestamp
     * Входные данные: Дата на 24 часа в будущем
     * Ожидаемый результат: Метод возвращает timestamp, равный времени переданной даты
     */
    @Test
    fun dateToTimestamp_FutureDate_ReturnsCorrectTimestamp() {
        val futureTime = System.currentTimeMillis() + 86400000L
        val date = Date(futureTime)
        val result = converters.dateToTimestamp(date)
        assertNotNull(result)
        assertEquals(futureTime, result)
    }

    /**
     * Тест: Полный цикл преобразования Date -> timestamp -> Date (round-trip)
     * Входные данные: Date(1609459200000L)
     * Ожидаемый результат: После преобразования Date -> timestamp -> Date получается исходная дата
     */
    @Test
    fun roundTrip_DateToTimestampToDate_ReturnsOriginalDate() {
        val originalDate = Date(1609459200000L)
        val timestamp = converters.dateToTimestamp(originalDate)
        val convertedDate = converters.fromTimestamp(timestamp)
        assertNotNull(convertedDate)
        assertEquals(originalDate.time, convertedDate?.time)
    }

    /**
     * Тест: Полный цикл преобразования timestamp -> Date -> timestamp (round-trip)
     * Входные данные: Timestamp 1609459200000L
     * Ожидаемый результат: После преобразования timestamp -> Date -> timestamp получается исходный timestamp
     */
    @Test
    fun roundTrip_TimestampToDateToTimestamp_ReturnsOriginalTimestamp() {
        val originalTimestamp = 1609459200000L
        val date = converters.fromTimestamp(originalTimestamp)
        val convertedTimestamp = converters.dateToTimestamp(date)
        assertNotNull(convertedTimestamp)
        assertEquals(originalTimestamp, convertedTimestamp)
    }
}


