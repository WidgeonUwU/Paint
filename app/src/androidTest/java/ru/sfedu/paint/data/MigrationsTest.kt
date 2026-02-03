package ru.sfedu.paint.data

import androidx.room.Database
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.sfedu.paint.data.database.PaintDatabase

@RunWith(AndroidJUnit4::class)
class MigrationsTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PaintDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun validateCurrentSchema() {
        /**
         * Тест: Валидация текущей схемы Room
         * Входные данные: версия схемы из аннотации/константы
         * Ожидаемый результат: схема корректна и проходит валидацию
         */
        val version = PaintDatabase::class.java.getAnnotation(Database::class.java)?.version
            ?: DB_VERSION
        val db = helper.createDatabase(TEST_DB, version)
        db.close()
        helper.runMigrationsAndValidate(TEST_DB, version, true)
    }

    companion object {
        private const val TEST_DB = "paint_migration_test"
        private const val DB_VERSION = 6
    }
}
