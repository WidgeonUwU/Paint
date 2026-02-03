package ru.sfedu.paint.ui.testtags

import ru.sfedu.paint.data.model.DrawingTool

object TestTags {
    const val CANVAS = "canvas_drawing"
    const val CANVAS_BACK = "canvas_back"
    const val CANVAS_UNDO = "canvas_undo"
    const val CANVAS_REDO = "canvas_redo"
    const val CANVAS_EXPORT = "canvas_export"
    const val CANVAS_SAVE = "canvas_save"
    const val CANVAS_CLEAR = "canvas_clear"
    const val CANVAS_OPACITY = "canvas_opacity"
    const val CANVAS_STROKE = "canvas_stroke"
    const val CANVAS_TITLE = "canvas_title"

    const val TEMPLATE_CUSTOM_BUTTON = "template_custom_button"
    const val TEMPLATE_PRESET_PREFIX = "template_preset_"
    const val CUSTOM_WIDTH_INPUT = "custom_width_input"
    const val CUSTOM_HEIGHT_INPUT = "custom_height_input"
    const val CUSTOM_CONFIRM = "custom_confirm"
    const val CUSTOM_CANCEL = "custom_cancel"

    const val SAVE_INPUT = "save_input"
    const val SAVE_CONFIRM = "save_confirm"
    const val SAVE_CANCEL = "save_cancel"

    const val EXPORT_PNG = "export_png"
    const val EXPORT_JPEG = "export_jpeg"
    const val EXPORT_CANCEL = "export_cancel"
    const val EXPORT_DIALOG = "export_dialog"

    const val RENAME_INPUT = "rename_input"
    const val RENAME_CONFIRM = "rename_confirm"
    const val RENAME_CANCEL = "rename_cancel"

    const val GALLERY_CREATE_BUTTON = "gallery_create"
    const val GALLERY_EMPTY_CREATE_BUTTON = "gallery_empty_create"
    const val GALLERY_SEARCH = "gallery_search"
    const val GALLERY_CLEAR_SEARCH = "gallery_search_clear"
    const val GALLERY_GRID = "gallery_grid"
    const val GALLERY_CARD_PREFIX = "gallery_card_"
    const val GALLERY_RENAME = "gallery_rename"
    const val GALLERY_DELETE = "gallery_delete"
    const val GALLERY_DELETE_CONFIRM = "gallery_delete_confirm"
    const val GALLERY_DELETE_CANCEL = "gallery_delete_cancel"

    fun toolTag(tool: DrawingTool): String {
        return when (tool) {
            DrawingTool.PENCIL -> "tool_pencil"
            DrawingTool.ERASER -> "tool_eraser"
            DrawingTool.RULER -> "tool_ruler"
        }
    }

    fun paletteColorTag(index: Int): String {
        return "palette_color_$index"
    }
}
