package ru.sfedu.paint.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Date
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.sfedu.paint.data.database.PaintDatabase
import ru.sfedu.paint.data.model.Drawing
import ru.sfedu.paint.data.model.DrawingTool
import ru.sfedu.paint.data.model.PathData
import ru.sfedu.paint.data.repository.DrawingRepository
import ru.sfedu.paint.util.PathSerializer

@RunWith(AndroidJUnit4::class)
class RoomIntegrationTest {

    private lateinit var db: PaintDatabase
    private lateinit var repository: DrawingRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PaintDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = DrawingRepository(db.drawingDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertLoadAndContentRoundTrip() = runBlocking {
        /**
         * Тест: Интеграция Room (вставка + контент + чтение)
         * Входные данные: Drawing + сериализованные PathData
         * Ожидаемый результат: данные читаются и совпадают с сохраненными
         */
        val drawing = Drawing(
            name = "Room Test",
            createdAt = Date(),
            width = 1920,
            height = 1080,
            backgroundColor = 0xFFFFFFFF,
            preview = null
        )
        val id = repository.insertDrawing(drawing)
        assertNotNull(id)

        val paths = listOf(
            PathData(
                points = listOf(Offset(10f, 20f), Offset(30f, 40f)),
                color = Color.Black,
                strokeWidth = 5f,
                tool = DrawingTool.PENCIL
            )
        )
        val json = PathSerializer.serialize(paths)
        repository.upsertContent(id, json)

        val storedJson = repository.getPathsJson(id)
        assertEquals(json, storedJson)

        val withContent = repository.getDrawingWithContentById(id)
        assertNotNull(withContent)
        assertEquals(id, withContent?.drawing?.id)
    }
}
