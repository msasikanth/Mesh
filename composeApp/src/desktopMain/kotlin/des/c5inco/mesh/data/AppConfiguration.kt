package des.c5inco.mesh.data

import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName
import des.c5inco.mesh.common.toHexStringNoHash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import model.MeshPoint
import model.SavedColor
import model.findColor
import model.toOffsetGrid
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

data class AppUiState(
    val showPoints: Boolean = false,
    val constrainEdgePoints: Boolean = true,
)

private val defaultColorPoints = listOf(
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

class AppConfiguration(
    resolution: Int = 10,
    blurLevel: Float = 0f,
    val availableColors: List<SavedColor>,
    incomingMeshPoints: List<MeshPoint> = emptyList(),
    showPoints: Boolean = false,
    val constrainEdgePoints: Boolean = true,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var canvasBackgroundColor = MutableStateFlow(-1L)
    var resolution = MutableStateFlow(resolution)
    var blurLevel = MutableStateFlow(blurLevel)
    var meshPoints = if (incomingMeshPoints.isEmpty()) {
        defaultColorPoints
    } else {
        incomingMeshPoints.toOffsetGrid()
    }.toMutableStateList()

    val uiState = MutableStateFlow(
        AppUiState(
            showPoints = showPoints,
            constrainEdgePoints = constrainEdgePoints,
        )
    )

    fun updateCanvasBackgroundColor(color: Long) {
        canvasBackgroundColor.update { color }
    }

    fun updateMeshPoint(row: Int, col: Int, point: Pair<Offset, Long>) {
        val colorPointsInRow = meshPoints[row].toMutableList()

        var newX = point.first.x
        var newY = point.first.y

        if (constrainEdgePoints) {
            newX = when (col) {
                0 -> 0f
                colorPointsInRow.size - 1 -> 1f
                else -> newX
            }
            newY = when (row) {
                0 -> 0f
                meshPoints.size - 1 -> 1f
                else -> newY
            }
        }

        val newPoint = Pair(Offset(x = newX, y = newY), point.second)
        colorPointsInRow.set(index = col, element = newPoint)

        meshPoints.set(index = row, element = colorPointsInRow.toList())
    }

    fun removeColorFromMeshPoints(colorToRemove: Long) {
        meshPoints.forEachIndexed { idx, points ->
            val newPoints = points.map { point ->
                if (point.second == colorToRemove) {
                    // Reset to transparent
                    Pair(point.first, -1L)
                } else {
                    point
                }
            }
            meshPoints.set(index = idx, element = newPoints.toList())
        }
    }

    fun exportPointsAsCode() {
        if (meshPoints.isEmpty()) return

        val offsetType = Offset::class.asClassName()
        val colorType = Color::class.asClassName()
        val pairType = Pair::class.asClassName().parameterizedBy(offsetType, colorType)
        val innerListType = List::class.asClassName().parameterizedBy(pairType)
        val outerListType = List::class.asClassName().parameterizedBy(innerListType)

        val listInitializer = CodeBlock.builder()
            .add("listOf(\n")
            .indent()

        meshPoints.forEachIndexed { _, innerList ->
            listInitializer.add("listOf(\n")
            listInitializer.indent()

            innerList.forEachIndexed { _, pair ->
                val hexString = availableColors.findColor(pair.second).toHexStringNoHash(includeAlpha = true)
                listInitializer.add(
                    "Offset(%Lf, %Lf) to Color(0x%L),\n",
                    pair.first.x,
                    pair.first.y,
                    hexString
                )
            }

            listInitializer.unindent()
            listInitializer.add("),\n")
        }

        listInitializer.unindent()
        listInitializer.add(")")

        val codeSpec = PropertySpec.builder("colorPoints", outerListType)
            .initializer(listInitializer.build())
            .build()

        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(StringSelection(codeSpec.toString()), null)
        println(codeSpec.toString())
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