package des.c5inco.mesh

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.unit.dp
import des.c5inco.mesh.data.AppConfiguration
import des.c5inco.mesh.data.AppDataRepository
import des.c5inco.mesh.data.AppState
import des.c5inco.mesh.ui.GradientCanvas
import des.c5inco.mesh.ui.SidePanel
import kotlinx.coroutines.launch
import model.SavedColor

@Composable
fun App(
    repository: AppDataRepository,
    configuration: AppConfiguration
) {
    val presetColors by repository.getPresetColors().collectAsState(initial = emptyList())
    val customColors by repository.getCustomColors().collectAsState(initial = emptyList())
    val canvasBackgroundColor by configuration.canvasBackgroundColor.collectAsState()
    val uiState by configuration.uiState.collectAsState()
    val resolution by configuration.resolution.collectAsState()

    Row(
        Modifier.fillMaxSize()
    ) {
        var selectedColorPoint: Pair<Int, Int>? by remember { mutableStateOf(null) }
        var exportScale by remember { mutableStateOf(1) }
        val exportGraphicsLayer = rememberGraphicsLayer()
        val coroutineScope = rememberCoroutineScope()

        GradientCanvas(
            exportGraphicsLayer = exportGraphicsLayer,
            exportScale = exportScale,
            resolution = resolution,
            availableColors = presetColors + customColors,
            canvasBackgroundColor = canvasBackgroundColor,
            meshPoints = configuration.meshPoints,
            showPoints = uiState.showPoints,
            onTogglePoints = { configuration.toggleShowingPoints() },
            onPointDragStartEnd = { selectedColorPoint = it },
            onPointDrag = { row, col, point ->
                configuration.updateMeshPoint(row, col, point)
            },
            modifier = Modifier.weight(1f)
        )
        SidePanel(
            exportScale = exportScale,
            presetColors = presetColors,
            customColors = customColors,
            canvasBackgroundColor = canvasBackgroundColor,
            meshPoints = configuration.meshPoints,
            showPoints = uiState.showPoints,
            constrainEdgePoints = uiState.constrainEdgePoints,
            onUpdateMeshPoint = { row, col, point ->
                configuration.updateMeshPoint(row, col, point)
            },
            onTogglePoints = { configuration.toggleShowingPoints() },
            onToggleConstrainingEdgePoints = { configuration.toggleConstrainingEdgePoints() },
            onExportScaleChange = { exportScale = it },
            onExport = {
                coroutineScope.launch {
                    val bitmap = exportGraphicsLayer.toImageBitmap()
                    val awtImage = bitmap.toAwtImage()

                    AppState.saveImage(image = awtImage, scale = exportScale)
                }
            },
            onCanvasBackgroundColorChange = configuration::updateCanvasBackgroundColor,
            onAddColor = {
                repository.addColor(
                    SavedColor(
                        red = (255 * it.red).toInt(),
                        green = (255 * it.green).toInt(),
                        blue = (255 * it.blue).toInt(),
                        alpha = it.alpha
                    )
                )
            },
            onRemoveColor = { repository.deleteColor(it) },
            selectedColorPoint = selectedColorPoint,
            modifier = Modifier.width(280.dp)
        )
    }
}