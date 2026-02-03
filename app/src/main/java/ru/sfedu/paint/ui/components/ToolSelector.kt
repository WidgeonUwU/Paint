package ru.sfedu.paint.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ShapeDefaults
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import ru.sfedu.paint.R
import ru.sfedu.paint.data.model.DrawingTool
import androidx.compose.ui.platform.testTag
import ru.sfedu.paint.ui.testtags.TestTags

@Composable
fun ToolSelector(
    selectedTool: DrawingTool,
    onToolSelected: (DrawingTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ToolButton(
            tool = DrawingTool.PENCIL,
            icon = R.drawable.ic_pen,
            isSelected = selectedTool == DrawingTool.PENCIL,
            onClick = { onToolSelected(DrawingTool.PENCIL) },
            tag = TestTags.toolTag(DrawingTool.PENCIL)
        )
        ToolButton(
            tool = DrawingTool.ERASER,
            icon = R.drawable.ic_eraser,
            isSelected = selectedTool == DrawingTool.ERASER,
            onClick = { onToolSelected(DrawingTool.ERASER) },
            tag = TestTags.toolTag(DrawingTool.ERASER)
        )
        ToolButton(
            tool = DrawingTool.RULER,
            icon = R.drawable.ic_ruler,
            isSelected = selectedTool == DrawingTool.RULER,
            onClick = { onToolSelected(DrawingTool.RULER) },
            tag = TestTags.toolTag(DrawingTool.RULER)
        )
    }
}

@Composable
private fun ToolButton(
    tool: DrawingTool,
    icon: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        modifier = Modifier
            .testTag(tag)
            .clickable { onClick() }
            .padding(8.dp)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .padding(12.dp)
            .size(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}











