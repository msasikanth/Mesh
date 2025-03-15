package des.c5inco.mesh.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

data class AppUiState(
    val showPoints: Boolean = false,
    val constrainEdgePoints: Boolean = true,
)

class AppConfiguration(
    resolution: Int = 10,
    blurLevel: Float = 0f,
    val showPoints: Boolean = false,
    val constrainEdgePoints: Boolean = true,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var canvasBackgroundColor = MutableStateFlow<Int?>(null)
    var resolution = MutableStateFlow(resolution)
    var blurLevel = MutableStateFlow(blurLevel)

    val uiState = MutableStateFlow(
        AppUiState(
            showPoints = showPoints,
            constrainEdgePoints = constrainEdgePoints
        )
    )

    fun toggleShowingPoints() {
        uiState.update {
            it.copy(showPoints = !it.showPoints)
        }
    }

    fun toggleConstrainingEdgePoints(constrainEdgePoints: Boolean) {
        uiState.update {
            it.copy(constrainEdgePoints = constrainEdgePoints)
        }
    }
}