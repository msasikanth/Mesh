package des.c5inco.mesh.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

private val defaultColors = listOf(
    Color(0xFF00796B),
    Color(0xFF3F51B5),
    Color(0xFFFF00FF),
    Color.DarkGray
)

private val defaultColorPoints = listOf(
    listOf(
        Offset(0f, 0f) to 0,
        Offset(.33f, 0f) to 0,
        Offset(.67f, 0f) to 2,
        Offset(1f, 0f) to 0,
    ),
    listOf(
        Offset(0f, .4f) to 1,
        Offset(.33f, .8f) to 1,
        Offset(.67f, .8f) to 1,
        Offset(1f, .4f) to 1,
    ),
    listOf(
        Offset(0f, 1f) to 2,
        Offset(.33f, 1f) to 3,
        Offset(.67f, 1f) to 3,
        Offset(1f, 1f) to 3,
    )
)

object MainViewModel {
    var resolution by mutableStateOf(10)
    var showPoints by mutableStateOf(false)
    var constrainEdgePoints by mutableStateOf(true)
    val colors = defaultColors.toMutableStateList()
    var colorPoints = defaultColorPoints.toMutableStateList()

    fun getColor(index: Int): Color {
        return colors.getOrElse(index) { _ -> defaultColors[0] }
    }

    fun removeColor(index: Int) {
        colors.removeAt(index)
    }

    private fun resetColors() {
        colors.clear()
        colors.addAll(defaultColors)
    }

    fun updateColorPoint(col: Int, row: Int, point: Pair<Offset, Int>) {
        val colorPointsInRow = colorPoints[row].toMutableList()

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
                colorPoints.size - 1 -> 1f
                else -> newY
            }
        }

        val newPoint = Pair(Offset(x = newX, y = newY), point.second)
        colorPointsInRow.set(index = col, element = newPoint)

        colorPoints.set(index = row, element = colorPointsInRow.toList())
    }

    private fun resetColorPoints() {
        colorPoints.clear()
        colorPoints.addAll(defaultColorPoints)
    }

    fun reset() {
        resetColors()
        resetColorPoints()
    }
}