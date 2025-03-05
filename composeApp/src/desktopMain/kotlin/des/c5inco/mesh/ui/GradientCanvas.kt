package des.c5inco.mesh.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import des.c5inco.mesh.common.PointCursor
import des.c5inco.mesh.common.meshGradient
import des.c5inco.mesh.ui.components.CanvasSnackbar
import des.c5inco.mesh.ui.data.AppState
import des.c5inco.mesh.ui.data.DimensionMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.theme.colorPalette
import org.jetbrains.jewel.ui.util.thenIf

@Composable
fun GradientCanvas(
    exportGraphicsLayer: GraphicsLayer,
    exportScale: Int,
    onPointDrag: (Pair<Int, Int>?) -> Unit = { _ -> },
    modifier: Modifier = Modifier
) {
    val showPoints by remember { AppState::showPoints }
    val resolution by remember { AppState::resolution }
    val colors = remember { AppState.colorPoints }

    val canvasWidthMode by AppState.canvasWidthMode.collectAsState()
    val canvasHeightMode by AppState.canvasHeightMode.collectAsState()
    var canvasWidth by remember { AppState::canvasWidth }
    var canvasHeight by remember { AppState::canvasHeight }

    var notificationMessage by remember { mutableStateOf<String?>(null) }

    val exportSize by derivedStateOf {
        mutableStateOf(IntSize(canvasWidth, canvasHeight))
    }

    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        launch(Dispatchers.Main) {
            AppState.notificationFlow.collectLatest {
                notificationMessage = it
            }
        }
    }

    fun handlePositioned(coordinates: LayoutCoordinates) {
        with (density) {
            val dpWidth = coordinates.size.width.toDp()
            val dpHeight = coordinates.size.height.toDp()

            canvasWidth = dpWidth.value.toInt()
            canvasHeight = dpHeight.value.toInt()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(if (AppState.canvasBackgroundColor > -1) {
                AppState.getColor(AppState.canvasBackgroundColor)
            } else {
                JewelTheme.colorPalette.gray(1)
            })
            .fillMaxSize()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            AppState.showPoints = !showPoints
                        }
                    )
                }
                .padding(32.dp)
                .then(
                    if (canvasWidthMode == DimensionMode.Fill) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier.width(canvasWidth.dp)
                    }
                )
                .then(
                    if (canvasHeightMode == DimensionMode.Fill) {
                        Modifier.fillMaxHeight()
                    } else {
                        Modifier.height(canvasHeight.dp)
                    }
                )
        ) {
            val maxWidth = constraints.maxWidth
            val maxHeight = constraints.maxHeight

            fun handlePointDrag(coordinate: Pair<Int, Int>, offsetX: Float, offsetY: Float) {
                val colorPoints = colors[coordinate.second]
                val currentPoint = colorPoints[coordinate.first]
                val currentOffset = currentPoint.first

                val x = (currentOffset.x + (offsetX / maxWidth)).coerceIn(0f, 1f)
                val y = (currentOffset.y + (offsetY / maxHeight)).coerceIn(0f, 1f)

                AppState.updateColorPoint(
                    col = coordinate.first,
                    row = coordinate.second,
                    point = Pair(Offset(x = x, y = y), currentPoint.second)
                )
            }

            val graphicsLayer = rememberGraphicsLayer()

            Box(
                Modifier
                    .thenIf(canvasWidthMode == DimensionMode.Fill || canvasHeightMode == DimensionMode.Fill) {
                        Modifier.onGloballyPositioned { handlePositioned(it) }
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .drawWithContent {
                        // Record content on visible graphics layer
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        val (width, height) = exportSize.value

                        // Scale and translate the export graphics layer accordingly
                        exportGraphicsLayer.apply {
                            scaleX = exportScale.toFloat()
                            scaleY = exportScale.toFloat()

                            when (exportScale) {
                                3 -> {
                                    translationX = width.toFloat() * 3
                                    translationY = height.toFloat() * 3
                                }
                                2 -> {
                                    translationX = width.toFloat()
                                    translationY = height.toFloat()
                                }
                                else -> {
                                    translationX = 0f
                                    translationY = 0f
                                }
                            }
                        }

                        // Record content on the export graphics layer
                        exportGraphicsLayer.record(
                            size = IntSize(width * exportScale, height * exportScale),
                        ) {
                            scale(
                                scale = 1f / density.density,
                                pivot = Offset.Zero,
                            ) {
                                this@drawWithContent.drawContent()
                            }
                        }

                        // Draw the visible graphics layer on the canvas
                        drawLayer(graphicsLayer)
                    }
                    .meshGradient(
                        points = colors.map { row ->
                            row.map {
                                it.first to AppState.getColor(it.second)
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
                                    color = AppState.getColor(col.second),
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

        if (notificationMessage != null) {
            CanvasSnackbar(
                onDismiss = {
                    notificationMessage = null
                    println("dismiss")
                },
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = notificationMessage!!,
                )
            }
        }
    }
}