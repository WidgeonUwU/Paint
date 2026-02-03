package ru.sfedu.paint.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.sfedu.paint.MainActivity
import ru.sfedu.paint.ui.testtags.TestTags

@RunWith(AndroidJUnit4::class)
class SmokeTests {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun createCanvas_drawUndoRedo() {
        /**
         * Тест: Smoke‑сценарий создания холста и undo/redo
         * Входные данные: новый холст по пресету, одна линия
         * Ожидаемый результат: Undo/Redo выполняются без ошибок, холст доступен
         */
        openNewCanvas()
        drawLine()
        composeRule.onNodeWithTag(TestTags.CANVAS_UNDO).performClick()
        composeRule.onNodeWithTag(TestTags.CANVAS_REDO).performClick()
        composeRule.onNodeWithTag(TestTags.CANVAS).assertExists()
    }

    @Test
    fun saveDrawing_andFindInGallery() {
        /**
         * Тест: Сохранение рисунка и поиск в галерее
         * Входные данные: новый холст, одна линия, уникальное имя
         * Ожидаемый результат: рисунок сохраняется и находится по поиску
         */
        val name = "Smoke ${System.currentTimeMillis()}"
        openNewCanvas()
        drawLine()
        saveDrawing(name)
        returnToGallery()
        searchByName(name)
        composeRule
            .onAllNodes(hasText(name) and hasTestTag(TestTags.GALLERY_SEARCH).not())
            .assertCountEquals(1)
    }

    @Test
    fun exportPngAndJpeg() {
        /**
         * Тест: Экспорт в PNG/JPEG
         * Входные данные: новый холст, одна линия
         * Ожидаемый результат: диалог экспорта открывается, кнопки форматов доступны
         */
        openNewCanvas()
        drawLine()
        exportWithDialog(TestTags.EXPORT_PNG)
        exportWithDialog(TestTags.EXPORT_JPEG)
    }

    private fun openNewCanvas() {
        composeRule.onNodeWithTag(TestTags.GALLERY_CREATE_BUTTON).performClick()
        composeRule.onNodeWithTag("${TestTags.TEMPLATE_PRESET_PREFIX}0").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(TestTags.TEMPLATE_CUSTOM_BUTTON, useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty()
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(TestTags.CANVAS_EXPORT, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(TestTags.CANVAS).assertExists()
    }

    private fun drawLine() {
        composeRule.onNodeWithTag(TestTags.CANVAS)
            .performTouchInput {
                down(center)
                moveTo(center.copy(x = center.x + 200))
                up()
            }
    }

    private fun saveDrawing(name: String) {
        composeRule.onNodeWithTag(TestTags.CANVAS_SAVE).performClick()
        composeRule.onNodeWithTag(TestTags.SAVE_INPUT).performTextClearance()
        composeRule.onNodeWithTag(TestTags.SAVE_INPUT).performTextInput(name)
        composeRule.onNodeWithTag(TestTags.SAVE_CONFIRM).performClick()
    }

    private fun exportWithDialog(tag: String) {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.CANVAS_EXPORT).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(TestTags.EXPORT_DIALOG, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(tag, useUnmergedTree = true).performClick()
    }

    private fun returnToGallery() {
        composeRule.onNodeWithTag(TestTags.CANVAS_BACK).performClick()
    }

    private fun searchByName(name: String) {
        composeRule.onNodeWithTag(TestTags.GALLERY_SEARCH).performTextClearance()
        composeRule.onNodeWithTag(TestTags.GALLERY_SEARCH).performTextInput(name)
    }
}
