package des.c5inco.mesh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import des.c5inco.mesh.common.toColor
import des.c5inco.mesh.common.toHexStringNoHash
import des.c5inco.mesh.ui.viewmodel.MainViewModel
import des.c5inco.mesh.ui.views.ColorSwatch
import des.c5inco.mesh.ui.views.OffsetInputField
import kotlinx.coroutines.flow.collectLatest
import mesh.composeapp.generated.resources.Res
import mesh.composeapp.generated.resources.add_dark
import mesh.composeapp.generated.resources.closeSmall_dark
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.CheckboxRow
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Link
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.Typography

@Composable
fun SidePanel(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxHeight()
        .background(JewelTheme.globalColors.panelBackground),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            var showColorInput by remember { mutableStateOf(false) }

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Colors",
                    style = Typography.h4TextStyle(),
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = { showColorInput = true }
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.add_dark),
                        contentDescription = "Add color"
                    )
                }
            }

            if (showColorInput) {
                ColorInput(
                    onCancel = { showColorInput = false },
                    onSubmit = {
                        showColorInput = false
                        MainViewModel.colors.add(it)
                    }
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(MainViewModel.colors) { index, color ->
                    ColorSwatch(
                        color = color,
                        modifier = Modifier.clickable { MainViewModel.removeColor(index) }
                    )
                }
            }
        }

        Divider(orientation = Orientation.Horizontal, thickness = 1.dp, modifier = Modifier.fillMaxWidth())

        Column(
            Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "Points",
                    style = Typography.h4TextStyle(),
                    fontWeight = FontWeight.SemiBold
                )
                Link(
                    text = "Reset",
                    onClick = MainViewModel::reset,
                )
            }
            Spacer(Modifier.height(12.dp))
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
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            itemsIndexed(MainViewModel.colorPoints) { rowIdx, colorPoints ->
                Text(
                    text =  "Row ${rowIdx + 1}",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    colorPoints.forEachIndexed { colIdx, point ->
                        ColorPointRow(
                            x = point.first.x,
                            y = point.first.y,
                            constrainX = MainViewModel.constrainEdgePoints && (colIdx == 0 || colIdx == colorPoints.size - 1),
                            constrainY = MainViewModel.constrainEdgePoints && (rowIdx == 0 || rowIdx == MainViewModel.colorPoints.size - 1),
                            colorInt = point.second,
                            onUpdatePoint = { (nextOffset, nextColor) ->
                                MainViewModel.updateColorPoint(
                                    col = colIdx,
                                    row = rowIdx,
                                    point = Pair(Offset(x = nextOffset.x, y = nextOffset.y), nextColor)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorInput(
    onSubmit: (Color) -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var color: Color by remember { mutableStateOf(Color.LightGray) }
    val textFieldState = remember { TextFieldState() }
    var isColorValid by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        snapshotFlow { textFieldState.text }
            .collectLatest {
                var text = it.toString()
                if (text.startsWith(("#"))) {
                    text = text.substring(1, 7)
                    textFieldState.edit { replace(0, it.length, text) }
                }

                try {
                    color = text.toColor()
                    println("converted: $color")
                } catch (e: Exception) {
                    println("fail")
                }
            }
    }

    fun reset() {
        textFieldState.edit { replace(0, textFieldState.text.length, color.toHexStringNoHash(false)) }
    }

    fun validate(): Boolean {
        val trimmed = textFieldState.text.toString().replace(Regex("[^A-Fa-f0-9]"), "")
        val formattedText = if (trimmed.length > 6) {
            trimmed.substring(0, 6)
        } else {
            trimmed
        }

        try {
            color = formattedText.toColor()
            textFieldState.edit { replace(0, originalText.length, formattedText) }
            println("converted: $color")
            return true
        } catch (e: Exception) {
            reset()
            println("fail")
        }
        return false
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        TextField(
            state = textFieldState,
            leadingIcon = {
                ColorSwatch(
                    color = color,
                    modifier = Modifier.padding(end = 8.dp)
                )
            },
            outline = if (!isColorValid) Outline.Error else Outline.None,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier
                .onFocusChanged { validate() }
                .onKeyEvent {
                    when (it.key) {
                        Key.Enter -> {
                            isColorValid = validate()
                            if (isColorValid) onSubmit(color)
                        }
                        Key.Tab -> {
                            isColorValid = validate()
                            return@onKeyEvent false
                        }
                        Key.Escape -> {
                            onCancel()
                        }
                    }
                    return@onKeyEvent true
                }
                .weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = onCancel
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.closeSmall_dark),
                contentDescription = "Cancel"
            )
        }
    }
}

@Composable
private fun ColorPointRow(
    x: Float,
    y: Float,
    constrainX: Boolean,
    constrainY: Boolean,
    colorInt: Int,
    onUpdatePoint: (Pair<Offset, Int>) -> Unit = { _ -> },
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        ColorSwatch(color = MainViewModel.getColor(colorInt))
        OffsetInputField(
            value = x,
            enabled = !constrainX,
            paramName = "X",
            onUpdate = { onUpdatePoint(Pair(Offset(x = it, y = y), colorInt)) }
        )
        OffsetInputField(
            value = y,
            enabled = !constrainY,
            paramName = "Y",
            onUpdate = { onUpdatePoint(Pair(Offset(x = x, y = it), colorInt)) }
        )
    }
}