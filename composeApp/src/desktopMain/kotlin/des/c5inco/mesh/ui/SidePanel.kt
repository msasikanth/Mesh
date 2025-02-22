package des.c5inco.mesh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import des.c5inco.mesh.common.formatFloat
import des.c5inco.mesh.common.toHexString
import des.c5inco.mesh.ui.viewmodel.MainViewModel
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.CheckboxRow
import org.jetbrains.jewel.ui.component.Link
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.Typography

@Composable
fun SidePanel(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .width(250.dp)
        .fillMaxHeight()
        .background(JewelTheme.globalColors.panelBackground)
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Configuration",
                style = Typography.h2TextStyle(),
            )
            Link(
                text = "Reset",
                onClick = MainViewModel::resetColorPoints,
            )
        }
        CheckboxRow(
            text = "Show points",
            checked = MainViewModel.showPoints,
            onCheckedChange = { MainViewModel.showPoints = it },
        )
        CheckboxRow(
            text = "Constrain edge points",
            checked = MainViewModel.constrainEdgePoints,
            onCheckedChange = { MainViewModel.constrainEdgePoints = it },
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(MainViewModel.colorPoints) { rowIdx, colorPoints ->
                Text("Row $rowIdx", style = Typography.h3TextStyle())
                Spacer(Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    colorPoints.forEachIndexed { colIdx, point ->
                        ColorPointRow(
                            row = rowIdx,
                            col = colIdx,
                            x = point.first.x,
                            y = point.first.y,
                            color = point.second,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ColorPointRow(
    row: Int,
    col: Int,
    x: Float,
    y: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    fun updatePoint(nextX: Float, nextY: Float) {
        MainViewModel.updateColorPoint(
            col = col,
            row = row,
            point = Pair(Offset(x = nextX, y = nextY), color)
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        OffsetInputField(
            value = x,
            onUpdate = { updatePoint(it, y) }
        )
        OffsetInputField(
            value = y,
            onUpdate = { updatePoint(x, it) }
        )
        Text(
            color.toHexString(),
            style = JewelTheme.editorTextStyle
        )
    }
}

@Composable
fun OffsetInputField(
    value: Float,
    onUpdate: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val textFieldState = remember(value) { TextFieldState(formatFloat(value)) }

    fun reset() {
        textFieldState.edit { replace(0, textFieldState.text.length, formatFloat(value)) }
    }

    fun validate() {
        textFieldState.text.toString().toFloatOrNull()?.let { next ->
            onUpdate(next)
        } ?: run {
            reset()
        }
    }

    TextField(
        state = textFieldState,
        modifier = modifier
            .onFocusChanged {
                validate()
            }
            .onKeyEvent {
                when (it.key) {
                    Key.Enter,
                    Key.Tab -> {
                        validate()
                        if (it.key == Key.Enter) {
                            focusManager.clearFocus()
                        } else {
                            return@onKeyEvent false
                        }
                    }
                    Key.Escape -> {
                        reset()
                        focusManager.clearFocus()
                    }
                }
                return@onKeyEvent true
            }
            .width(64.dp)
    )
}