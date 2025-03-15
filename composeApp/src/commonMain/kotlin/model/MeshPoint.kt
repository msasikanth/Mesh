package model

import androidx.compose.ui.geometry.Offset
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class MeshPoint(
    @PrimaryKey(true) val uid: Long = 0,
    val row: Int,
    val col: Int,
    val x: Float,
    val y: Float,
    val savedColorId: Long,
)

fun Pair<Offset, Long>.toMeshPoint(row: Int, col: Int): MeshPoint {
    return MeshPoint(
        row = row,
        col = col,
        x = first.x,
        y = first.y,
        savedColorId = second
    )
}

fun List<MeshPoint>.toOffsetGrid(): List<List<Pair<Offset, Long>>> {
    if (isEmpty()) return emptyList()

    // Determine the dimensions of the grid.
    val maxRow = maxOf { it.row }
    val maxCol = maxOf { it.col }

    // Initialize an empty grid.
    val grid = MutableList(maxRow + 1) { MutableList(maxCol + 1) { Pair(Offset.Zero, 0L) } }

    // Populate the grid with the MeshPoint data.
    this.forEach { meshPoint ->
        grid[meshPoint.row][meshPoint.col] = Pair(Offset(meshPoint.x, meshPoint.y), meshPoint.savedColorId)
    }

    // Convert the mutable lists to immutable lists.
    return grid.map { it.toList() }.toList()
}
