package des.c5inco.mesh.data

import androidx.compose.ui.geometry.Offset
import des.c5inco.mesh.data.AppState.constrainEdgePoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import model.MeshPoint
import model.toOffsetGrid

data class AppUiState(
    val showPoints: Boolean = false,
    val constrainEdgePoints: Boolean = true,
    val meshPoints: List<MeshPoint> = emptyList()
)

class AppConfiguration(
    resolution: Int = 10,
    blurLevel: Float = 0f,
    meshPoints: List<MeshPoint> = emptyList(),
    showPoints: Boolean = false,
    constrainEdgePoints: Boolean = true,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var canvasBackgroundColor = MutableStateFlow(-1L)
    var resolution = MutableStateFlow(resolution)
    var blurLevel = MutableStateFlow(blurLevel)
    var meshPoints = MutableStateFlow<List<List<Pair<Offset, Long>>>>(emptyList())

    init {
        this.meshPoints.update {
            val points = meshPoints.toOffsetGrid()

            points.ifEmpty {
                listOf(
                    listOf(
                        Offset(0f, 0f) to 1L,
                        Offset(.33f, 0f) to 1L,
                        Offset(.67f, 0f) to 1L,
                        Offset(1f, 0f) to 1L,
                    ),
                    listOf(
                        Offset(0f, .4f) to 2L,
                        Offset(.33f, .8f) to 2L,
                        Offset(.67f, .8f) to 2L,
                        Offset(1f, .4f) to 2L,
                    ),
                    listOf(
                        Offset(0f, 1f) to 3L,
                        Offset(.33f, 1f) to 3L,
                        Offset(.67f, 1f) to 3L,
                        Offset(1f, 1f) to 3L,
                    )
                )
            }
        }
    }

    val uiState = MutableStateFlow(
        AppUiState(
            showPoints = showPoints,
            constrainEdgePoints = constrainEdgePoints,
        )
    )

    fun updateMeshPoint(incomingRow: Int, incomingCol: Int, incomingPoint: Pair<Offset, Long>) {
        meshPoints.update {
            it.mapIndexed { row, colPoints ->
                if (row == incomingRow) {
                    colPoints.mapIndexed { col, point ->
                        if (col == incomingCol) {
                            var newX = incomingPoint.first.x
                            var newY = incomingPoint.first.y

                            if (constrainEdgePoints) {
                                newX = when (col) {
                                    0 -> 0f
                                    colPoints.size - 1 -> 1f
                                    else -> newX
                                }
                                newY = when (row) {
                                    0 -> 0f
                                    it.size - 1 -> 1f
                                    else -> newY
                                }
                            }

                            Pair(Offset(x = newX, y = newY), incomingPoint.second)
                        } else {
                            point
                        }
                    }
                } else {
                    colPoints
                }
            }
        }
    }

    fun toggleShowingPoints() {
        uiState.update {
            it.copy(showPoints = !it.showPoints)
        }
    }

    fun toggleConstrainingEdgePoints() {
        uiState.update {
            it.copy(constrainEdgePoints = !it.constrainEdgePoints)
        }
    }
}