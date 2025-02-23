package des.c5inco.mesh.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import des.c5inco.mesh.common.toColor
import des.c5inco.mesh.common.toHexStringNoHash
import des.c5inco.mesh.ui.viewmodel.MainViewModel
import des.c5inco.mesh.ui.views.ColorSwatch
import des.c5inco.mesh.ui.views.OffsetInputField
import mesh.composeapp.generated.resources.Res
import mesh.composeapp.generated.resources.closeSmall
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.jewel.foundation.modifier.onHover
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
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
        .width(250.dp)
        .fillMaxHeight()
        .background(JewelTheme.globalColors.panelBackground),
    ) {
        Column(
            Modifier.padding(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "Configuration",
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

        Divider(orientation = Orientation.Horizontal, thickness = 1.dp, modifier = Modifier.fillMaxWidth())

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Colors",
                style = Typography.h4TextStyle(),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            MainViewModel.colors.forEachIndexed { index, color ->
                ColorInputRow(index, color)
            }
        }

        Divider(orientation = Orientation.Horizontal, thickness = 1.dp, modifier = Modifier.fillMaxWidth())

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            itemsIndexed(MainViewModel.colorPoints) { rowIdx, colorPoints ->
                Text(
                    text =  "Row ${rowIdx + 1}",
                    style = Typography.h4TextStyle(),
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
private fun ColorInputRow(
    index: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val textFieldState = remember(color) { TextFieldState(color.toHexStringNoHash(false)) }
    var hovered by remember { mutableStateOf(false) }

    LaunchedEffect(color) {
        snapshotFlow { textFieldState.text }
            .collect {
                try {
                    val int = it.toString().toColor()
                    MainViewModel.updateColor(index, int)
                    println("converted: $int")
                } catch (e: Exception) {
                    println("fail")
                    textFieldState.edit { replace(0, textFieldState.text.length, color.toHexStringNoHash(false)) }
                }
            }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .onHover { hovered = !hovered }
            .fillMaxWidth()
    ) {
        TextField(
            state = textFieldState,
            leadingIcon = {
                ColorSwatch(color = color, modifier = Modifier.padding(end = 8.dp))
            },
            outputTransformation = HexColorVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
            modifier = Modifier.width(120.dp)
        )

        if (hovered) {
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = { MainViewModel.removeColor(index) }
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.closeSmall),
                    contentDescription = "Remove"
                )
            }
        }
    }
}

class HexColorVisualTransformation : OutputTransformation {
    override fun TextFieldBuffer.transformOutput() {
        val trimmed = originalText.toString().replace("#", "")
        val formattedText = if (trimmed.length > 6) {
            trimmed.substring(0, 6)
        } else {
            trimmed
        }
        replace(0, formattedText.length, formattedText)
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