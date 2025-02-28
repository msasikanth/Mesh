package des.c5inco.mesh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import des.c5inco.mesh.common.PointCursor
import des.c5inco.mesh.common.meshGradient
import des.c5inco.mesh.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Link
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.Typography
import org.jetbrains.jewel.ui.theme.colorPalette

@Composable
fun GradientCanvas(
    onPointDrag: (Pair<Int, Int>?) -> Unit = { _ -> },
    modifier: Modifier = Modifier
) {
    val showPoints by remember { MainViewModel::showPoints }
    val resolution by remember { MainViewModel::resolution }
    val colors = remember { MainViewModel.colorPoints }
    var height by remember { mutableStateOf(0) }
    var width by remember { mutableStateOf(0) }

    fun handlePositioned(coordinates: LayoutCoordinates) {
        height = coordinates.size.height
        width = coordinates.size.width
    }

    Column(
        modifier
            .background(if (MainViewModel.canvasBackgroundColor > -1) {
                MainViewModel.getColor(MainViewModel.canvasBackgroundColor)
            } else {
                JewelTheme.colorPalette.gray(1)
            })
    ) {
        val coroutineScope = rememberCoroutineScope()
        val graphicsLayer = rememberGraphicsLayer()

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Text(
                text = "$width x $height @ $resolution.dp",
                style = Typography.editorTextStyle(),
                color = JewelTheme.globalColors.text.info
            )
            Spacer(Modifier.width(8.dp))
            Link(
                text = "Export",
                onClick = {
                    coroutineScope.launch {
                        val bitmap = graphicsLayer.toImageBitmap()
                        val awtImage = bitmap.toAwtImage()

                        MainViewModel.saveImage(awtImage)
                    }
                }
            )
        }
        BoxWithConstraints(
            modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            MainViewModel.showPoints = !showPoints
                        }
                    )
                }
                .padding(start = 32.dp, end = 32.dp, bottom = 32.dp)
        ) {
            val maxWidth = constraints.maxWidth
            val maxHeight = constraints.maxHeight

            fun handlePointDrag(coordinate: Pair<Int, Int>, offsetX: Float, offsetY: Float) {
                val colorPoints = colors[coordinate.second]
                val currentPoint = colorPoints[coordinate.first]
                val currentOffset = currentPoint.first

                val x = (currentOffset.x + (offsetX / maxWidth)).coerceIn(0f, 1f)
                val y = (currentOffset.y + (offsetY / maxHeight)).coerceIn(0f, 1f)

                MainViewModel.updateColorPoint(
                    col = coordinate.first,
                    row = coordinate.second,
                    point = Pair(Offset(x = x, y = y), currentPoint.second)
                )
            }

            Box(
                Modifier
                    .onGloballyPositioned { handlePositioned(it) }
                    .clip(RoundedCornerShape(16.dp))
                    .drawWithContent {
                        // call record to capture the content in the graphics layer
                        graphicsLayer.record {
                            // draw the contents of the composable into the graphics layer
                            this@drawWithContent.drawContent()
                        }
                        // draw the graphics layer on the visible canvas
                        drawLayer(graphicsLayer)
                    }
                    .meshGradient(
                        points = colors.map { row ->
                            row.map {
                                it.first to MainViewModel.getColor(it.second)
                            }
                        },
                        resolutionX = resolution,
                        resolutionY = resolution,
                        showPoints = showPoints
                    )
            ) {
                Spacer(Modifier.fillMaxSize())
            }
            Layout(
                content = {
                    if (showPoints) {
                        colors.forEachIndexed { rowIdx, row ->
                            row.forEachIndexed { colIdx, col ->
                                PointCursor(
                                    xIndex = colIdx,
                                    yIndex = rowIdx,
                                    color = MainViewModel.getColor(col.second),
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectDragGestures(
                                            onDragStart = {
                                                onPointDrag(Pair(rowIdx, colIdx))
                                            },
                                            onDragEnd = {
                                                onPointDrag(null)
                                            }
                                        ) { change, dragAmount ->
                                            change.consume()
                                            handlePointDrag(
                                                coordinate = Pair(colIdx, rowIdx),
                                                offsetX = dragAmount.x,
                                                offsetY = dragAmount.y
                                            )
                                        }
                                    })
                            }
                        }
                    }
                },
                measurePolicy = { measurables, constraints ->
                    val placeables = measurables.map { measurable ->
                        measurable.measure(constraints)
                    }

                    layout(constraints.maxWidth, constraints.maxHeight) {
                        if (placeables.isNotEmpty()) {
                            val cursorWidth = placeables[0].width
                            val cursorHeight = placeables[0].height
                            val rows = colors.size
                            val cols = colors[0].size

                            placeables.forEachIndexed { i, placeable ->
                                val row = i / cols
                                val col = i % cols

                                val xOffset = colors[row][col].first.x
                                val yOffset = colors[row][col].first.y

                                val x =
                                    ((xOffset * (constraints.maxWidth)) - cursorWidth / 2).toInt()
                                val y =
                                    ((yOffset * (constraints.maxHeight)) - cursorHeight / 2).toInt()
                                placeable.place(x, y)
                            }
                        }
                    }
                }
            )
        }

    }
}