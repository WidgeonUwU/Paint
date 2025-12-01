package ru.sfedu.paint.data.model

import ru.sfedu.paint.R

data class CanvasTemplate(
    val name: String,
    val width: Int,
    val height: Int
) {
    companion object {
        val ASPECT_RATIOS = listOf(
            R.string.ratio_1_1 to Pair(1f, 1f),
            R.string.ratio_4_3 to Pair(4f, 3f),
            R.string.ratio_3_4 to Pair(3f, 4f),
            R.string.ratio_16_9 to Pair(16f, 9f),
            R.string.ratio_9_16 to Pair(9f, 16f),
            R.string.ratio_3_2 to Pair(3f, 2f),
            R.string.ratio_2_3 to Pair(2f, 3f)
        )
        
        val PRESET_TEMPLATES = listOf(
            CanvasTemplate("A4 Portrait", 2480, 3508),
            CanvasTemplate("A4 Landscape", 3508, 2480),
            CanvasTemplate("Square", 2000, 2000),
            CanvasTemplate("Instagram Post", 1080, 1080),
            CanvasTemplate("Instagram Story", 1080, 1920),
            CanvasTemplate("HD", 1920, 1080),
            CanvasTemplate("Full HD", 3840, 2160)
        )
    }
}

