package ru.sfedu.paint.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
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
class GalleryTests {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun renameAndDeleteDrawing() {
        /**
         * Тест: Переименование и удаление рисунка в галерее
         * Входные данные: сохраненный рисунок с уникальным именем
         * Ожидаемый результат: имя меняется, рисунок удаляется из списка
         */
        val originalName = "Gallery ${System.currentTimeMillis()}"
        val renamed = "Renamed ${System.currentTimeMillis()}"

        openNewCanvas()
        drawLine()
        saveDrawing(originalName)
        returnToGallery()

        composeRule.onAllNodesWithTag(TestTags.GALLERY_RENAME).onFirst().performClick()
        composeRule.onNodeWithTag(TestTags.RENAME_INPUT).performTextClearance()
        composeRule.onNodeWithTag(TestTags.RENAME_INPUT).performTextInput(renamed)
        composeRule.onNodeWithTag(TestTags.RENAME_CONFIRM).performClick()

        searchByName(renamed)
        composeRule
            .onAllNodes(hasText(renamed) and hasTestTag(TestTags.GALLERY_SEARCH).not())
            .assertCountEquals(1)

        composeRule.onAllNodesWithTag(TestTags.GALLERY_DELETE).onFirst().performClick()
        composeRule.onNodeWithTag(TestTags.GALLERY_DELETE_CONFIRM).performClick()

        searchByName(renamed)
        composeRule
            .onAllNodes(hasText(renamed) and hasTestTag(TestTags.GALLERY_SEARCH).not())
            .assertCountEquals(0)
    }

    private fun openNewCanvas() {
        composeRule.onNodeWithTag(TestTags.GALLERY_CREATE_BUTTON).performClick()
        composeRule.onNodeWithTag("${TestTags.TEMPLATE_PRESET_PREFIX}0").performClick()
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

    private fun returnToGallery() {
        composeRule.onNodeWithTag(TestTags.CANVAS_BACK).performClick()
    }

    private fun searchByName(name: String) {
        composeRule.onNodeWithTag(TestTags.GALLERY_SEARCH).performTextClearance()
        composeRule.onNodeWithTag(TestTags.GALLERY_SEARCH).performTextInput(name)
    }
}
