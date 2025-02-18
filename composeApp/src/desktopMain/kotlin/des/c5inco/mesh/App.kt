package des.c5inco.mesh

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import des.c5inco.mesh.common.PointCursor
import des.c5inco.mesh.common.meshGradient
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.jewel.foundation.theme.JewelTheme

@Composable
@Preview
fun App() {
    var showPoints by remember { mutableStateOf(false) }
    var resolution by remember { mutableStateOf(10) }
    var constrainEdgePoints by remember { mutableStateOf(true) }

    val Teal900 = Color(0xFF00796B)
    val Indigo700 = Color(0xFF3F51B5)
    val Magenta = Color(0xFFFF00FF)

    val colors = remember { mutableStateListOf(
        listOf(
            Offset(0f, 0f) to Magenta,
            Offset(.33f, 0f) to Magenta,
            Offset(.67f, 0f) to Teal900,
            Offset(1f, 0f) to Magenta,
        ),
        listOf(
            Offset(0f, .4f) to Indigo700,
            Offset(.33f, .8f) to Indigo700,
            Offset(.67f, .8f) to Indigo700,
            Offset(1f, .4f) to Indigo700,
        ),
        listOf(
            Offset(0f, 1f) to Teal900,
            Offset(.33f, 1f) to Color.DarkGray,
            Offset(.67f, 1f) to Color.DarkGray,
            Offset(1f, 1f) to Color.DarkGray,
        ),
    )}

    BoxWithConstraints(
        Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        showPoints = !showPoints
                    }
                )
            }
            .background(JewelTheme.globalColors.panelBackground)
            .padding(32.dp)
    ) {
        fun handlePointDrag(coordinate: Pair<Int, Int>, offsetX: Float, offsetY: Float) {
            val colorPoints = colors[coordinate.second]
            val currentPoint = colorPoints[coordinate.first]
            val currentOffset = currentPoint.first

            var newX = (currentOffset.x + (offsetX / constraints.maxWidth)).coerceIn(0f, 1f)
            var newY = (currentOffset.y + (offsetY / constraints.maxHeight)).coerceIn(0f, 1f)

            if (constrainEdgePoints) {
                newX = when (coordinate.first) {
                    0 -> 0f
                    colorPoints.size - 1 -> 1f
                    else -> newX
                }
                newY = when (coordinate.second) {
                    0 -> 0f
                    colors.size - 1 -> 1f
                    else -> newY
                }
            }

            val newPoint = Pair(Offset(x = newX, y = newY), currentPoint.second)
            val newColorPoints = colorPoints.toMutableList()
            newColorPoints.set(index = coordinate.first, element = newPoint)

            colors.set(index = coordinate.second, element = newColorPoints.toList())
        }

        Layout(
            modifier = Modifier
                .meshGradient(
                    points = colors,
                    resolutionX = resolution,
                    resolutionY = resolution,
                    showPoints = showPoints
                ),
            content = {
                if (showPoints) {
                    colors.forEachIndexed { rowIdx, row ->
                        row.forEachIndexed { colIdx, col ->
                            PointCursor(
                                xIndex = colIdx,
                                yIndex = rowIdx,
                                color = col.second,
                                modifier = Modifier.pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
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