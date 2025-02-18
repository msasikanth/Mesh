package des.c5inco.mesh.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

private val Teal900 = Color(0xFF00796B)
private val Indigo700 = Color(0xFF3F51B5)
private val Magenta = Color(0xFFFF00FF)

private val defaultColorPoints = listOf(
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
    )
)

object MainViewModel {
    var resolution by mutableStateOf(10)
    var showPoints by mutableStateOf(false)
    var constrainEdgePoints by mutableStateOf(true)
    var colorPoints = defaultColorPoints.toMutableStateList()

    fun resetColorPoints() {
        colorPoints.clear()
        colorPoints.addAll(defaultColorPoints)
    }
}