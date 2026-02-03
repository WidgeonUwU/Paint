package ru.sfedu.paint.ui

import android.content.pm.ActivityInfo
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.sfedu.paint.MainActivity
import ru.sfedu.paint.ui.testtags.TestTags

@RunWith(AndroidJUnit4::class)
class CanvasStateTests {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun orientationChange_preservesCanvas() {
        /**
         * Тест: Смена ориентации сохраняет состояние
         * Входные данные: холст с нарисованной линией
         * Ожидаемый результат: холст доступен после поворота экрана
         */
        openNewCanvas()
        drawLine()
        composeRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.CANVAS).assertExists()
        composeRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.CANVAS).assertExists()
    }

    @Test
    fun backgroundForeground_preservesCanvas() {
        /**
         * Тест: Переход в фон/возврат не ломает холст
         * Входные данные: холст с нарисованной линией
         * Ожидаемый результат: холст доступен после возврата
         */
        openNewCanvas()
        drawLine()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.onNodeWithTag(TestTags.CANVAS).assertExists()
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
}
