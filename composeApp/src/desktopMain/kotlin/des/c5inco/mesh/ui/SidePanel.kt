package des.c5inco.mesh.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import des.c5inco.mesh.common.toColor
import des.c5inco.mesh.common.toHexStringNoHash
import des.c5inco.mesh.ui.components.ColorDropdown
import des.c5inco.mesh.ui.components.ColorSwatch
import des.c5inco.mesh.ui.components.DimensionInputField
import des.c5inco.mesh.ui.components.OffsetInputField
import des.c5inco.mesh.ui.data.AppState
import des.c5inco.mesh.ui.data.DimensionMode
import kotlinx.coroutines.flow.collectLatest
import mesh.composeapp.generated.resources.Res
import mesh.composeapp.generated.resources.distributeEvenly_dark
import mesh.composeapp.generated.resources.featureCodeBlock_dark
import mesh.composeapp.generated.resources.modeFilled_dark
import mesh.composeapp.generated.resources.modeFixed_dark
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.Orientation
import org.jetbrains.jewel.ui.Outline
import org.jetbrains.jewel.ui.component.CheckboxRow
import org.jetbrains.jewel.ui.component.Divider
import org.jetbrains.jewel.ui.component.DropdownLink
import org.jetbrains.jewel.ui.component.Icon
import org.jetbrains.jewel.ui.component.IconButton
import org.jetbrains.jewel.ui.component.Link
import org.jetbrains.jewel.ui.component.Slider
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.component.Tooltip
import org.jetbrains.jewel.ui.component.Typography
import org.jetbrains.jewel.ui.component.VerticallyScrollableContainer
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import org.jetbrains.jewel.ui.theme.colorPalette
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun SidePanel(
    exportScale: Int,
    onExportScaleChange: (Int) -> Unit,
    onExport: () -> Unit = {},
    onAddColor: (Color) -> Unit = { _ -> },
    selectedColorPoint: Pair<Int, Int>? = null,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    VerticallyScrollableContainer(
        modifier = modifier
            .fillMaxHeight()
            .background(JewelTheme.globalColors.panelBackground),
    ) {
        Column {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp),
            ) {
                var showColorInput by remember { mutableStateOf(false) }

                SectionHeader(
                    title = "Colors",
                    actions = {
                        Tooltip(tooltip = { Text("Add color") }) {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    showColorInput = true
                                }
                            ) {
                                Icon(
                                    key = AllIconsKeys.General.Add,
                                    iconClass = AllIconsKeys::class.java,
                                    contentDescription = "Add color"
                                )
                            }
                        }
                    }
                )

                if (showColorInput) {
                    ColorInput(
                        onCancel = { showColorInput = false },
                        onSubmit = {
                            showColorInput = false
                            AppState.colors.add(it)
                            onAddColor(it)
                        }
                    )
                }
                FlowRow(
                    maxItemsInEachRow = 10,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AppState.colors.forEachIndexed { index, color ->
                        ColorSwatch(
                            color = color,
                            modifier = Modifier.clickable { AppState.removeColor(index) }
                        )
                    }
                }
            }

            Divider(
                orientation = Orientation.Horizontal,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            CanvasSection(
                exportScale = exportScale,
                onExportScaleChange = onExportScaleChange,
                onExport = onExport
            )

            Divider(
                orientation = Orientation.Horizontal,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            ) {
                SectionHeader(
                    title = "Points",
                    actions = {
                        Tooltip(tooltip = { Text("Export points as code") }) {
                            IconButton(onClick = {
                                val export = AppState.exportPointsAsCode()
                                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                clipboard.setContents(StringSelection(export), null)
                                println(export)
                                focusManager.clearFocus()

                                AppState.sendNotification("📋 Points copied to the clipboard!")
                            }) {
                                Icon(
                                    painter = painterResource(resource = Res.drawable.featureCodeBlock_dark),
                                    contentDescription = "Export points as code"
                                )
                            }
                        }
                        Spacer(Modifier.width(2.dp))
                        Tooltip(tooltip = { Text("Distribute points evenly") }) {
                            IconButton(
                                onClick = {
                                    AppState.distributeOffsetsEvenly()
                                    focusManager.clearFocus()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(resource = Res.drawable.distributeEvenly_dark),
                                    contentDescription = "Distribute points evenly"
                                )
                            }
                        }
                    }
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    DimensionInputField(
                        value = AppState.colorPointsRows,
                        enabled = true,
                        paramName = "Rows",
                        min = 2,
                        max = 10,
                        onUpdate = { AppState.updatePointsRows(it.coerceIn(2, 10)) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    DimensionInputField(
                        value = AppState.colorPointsCols,
                        enabled = true,
                        paramName = "Cols",
                        min = 2,
                        max = 10,
                        onUpdate = { AppState.updatePointsCols(it.coerceIn(2, 10)) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(12.dp))
                CheckboxRow(
                    text = "Show points",
                    checked = AppState.showPoints,
                    onCheckedChange = { AppState.showPoints = it },
                )
                CheckboxRow(
                    text = "Constrain edge points",
                    checked = AppState.constrainEdgePoints,
                    onCheckedChange = { AppState.constrainEdgePoints = it },
                )
            }

            val selectedPointColor = JewelTheme.colorPalette.blue(6)
            val selectedHighlightHeight = 24.dp

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                AppState.colorPoints.forEachIndexed() { rowIdx, colorPoints ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "Row ${rowIdx + 1}",
                            fontWeight = FontWeight.SemiBold
                        )
                        colorPoints.forEachIndexed { colIdx, point ->
                            Box(
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                ColorPointRow(
                                    x = point.first.x,
                                    y = point.first.y,
                                    constrainX = AppState.constrainEdgePoints && (colIdx == 0 || colIdx == colorPoints.size - 1),
                                    constrainY = AppState.constrainEdgePoints && (rowIdx == 0 || rowIdx == AppState.colorPoints.size - 1),
                                    colorInt = point.second,
                                    onUpdatePoint = { (nextOffset, nextColor) ->
                                        AppState.updateColorPoint(
                                            col = colIdx,
                                            row = rowIdx,
                                            point = Pair(
                                                Offset(x = nextOffset.x, y = nextOffset.y),
                                                nextColor
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (selectedColorPoint == Pair(rowIdx, colIdx)) {
                                    Spacer(
                                        Modifier
                                            .offset(x = -16.dp)
                                            .background(selectedPointColor)
                                            .width(4.dp)
                                            .height(selectedHighlightHeight)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = Typography.h4TextStyle(),
            fontWeight = FontWeight.SemiBold
        )
        Row {
            actions()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ColorInput(
    onSubmit: (Color) -> Unit = {},
    onCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var color: Color by remember { mutableStateOf(Color.LightGray) }
    val textFieldState = remember { TextFieldState() }
    var isColorValid by remember { mutableStateOf(true) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
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
                .focusRequester(focusRequester)
                .onFocusChanged { validate() }
                .onKeyEvent {
                    when (it.key) {
                        Key.Enter, Key.NumPadEnter -> {
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
        Tooltip(tooltip = { Text("Cancel") }) {
            IconButton(
                onClick = onCancel
            ) {
                Icon(
                    key = AllIconsKeys.General.CloseSmall,
                    iconClass = AllIconsKeys::class.java,
                    contentDescription = "Cancel"
                )
            }
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
        ColorDropdown(
            selectedColor = colorInt,
            colors = AppState.colors,
            onSelected = { onUpdatePoint(Pair(Offset(x = x, y = y), it)) }
        )
        OffsetInputField(
            value = x,
            enabled = !constrainX,
            paramName = "X",
            onUpdate = { onUpdatePoint(Pair(Offset(x = it, y = y), colorInt)) },
            modifier = Modifier.weight(1f)
        )
        OffsetInputField(
            value = y,
            enabled = !constrainY,
            paramName = "Y",
            onUpdate = { onUpdatePoint(Pair(Offset(x = x, y = it), colorInt)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CanvasSection(
    exportScale: Int,
    onExportScaleChange: (Int) -> Unit,
    onExport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val canvasWidthMode by AppState.canvasWidthMode.collectAsState()
    val canvasHeightMode by AppState.canvasHeightMode.collectAsState()
    val canvasWidth by remember { AppState::canvasWidth }
    val canvasHeight by remember { AppState::canvasHeight }
    val blurLevel by remember { AppState::blurLevel }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier.padding(16.dp),
    ) {
        SectionHeader(title = "Canvas")
        Spacer(Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ColorDropdown(
                selectedColor = AppState.canvasBackgroundColor,
                colors = AppState.colors,
                allowTransparency = true,
                onSelected = { AppState.canvasBackgroundColor = it }
            )
            DimensionInputField(
                value = canvasWidth,
                enabled = canvasWidthMode == DimensionMode.Fixed,
                paramName = "W",
                min = 100,
                trailingIcon = {
                    Tooltip(tooltip = {
                        Text(text = "Toggle to ${if (canvasWidthMode == DimensionMode.Fixed) "Filled" else "Fixed"}")
                    }) {
                        IconButton(
                            onClick = {
                                AppState.updateCanvasWidthMode()
                                focusManager.clearFocus()
                            }
                        ) {
                            Icon(
                                painter = painterResource(getModeIcon(canvasWidthMode)),
                                contentDescription = null
                            )
                        }
                    }
                },
                onUpdate = { AppState.canvasWidth = it },
                modifier = Modifier.weight(1f)
            )
            DimensionInputField(
                value = canvasHeight,
                enabled = canvasHeightMode == DimensionMode.Fixed,
                paramName = "H",
                min = 100,
                trailingIcon = {
                    Tooltip(tooltip = {
                        Text(text = "Toggle to ${if (canvasHeightMode == DimensionMode.Fixed) "Filled" else "Fixed"}")
                    }) {
                        IconButton(
                            onClick = {
                                AppState.updateCanvasHeightMode()
                                focusManager.clearFocus()
                            }
                        ) {
                            Icon(
                                painter = painterResource(getModeIcon(canvasHeightMode)),
                                contentDescription = null
                            )
                        }
                    }
                },
                onUpdate = { AppState.canvasHeight = it },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Link(
                text = "Export",
                onClick = onExport
            )
            Spacer(Modifier.width(4.dp))
            DropdownLink(
                text = "@${exportScale}x",
            ) {
                repeat(3) {
                    selectableItem(
                        selected = exportScale == it + 1,
                        onClick = {
                            onExportScaleChange(it + 1)
                        },
                    ) {
                        Text("${it + 1}x")
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Blur")
            Spacer(Modifier.width(16.dp))
            Slider(
                value = blurLevel,
                onValueChange = {
                    AppState.blurLevel = it
                }
            )
        }
    }
}

private fun getModeIcon(mode: DimensionMode): DrawableResource {
    return if (mode == DimensionMode.Fixed) {
        Res.drawable.modeFixed_dark
    } else {
        Res.drawable.modeFilled_dark
    }
}