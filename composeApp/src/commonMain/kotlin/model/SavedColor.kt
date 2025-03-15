package model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class SavedColor(
    @PrimaryKey(true) val uid: Long = 0,
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Float = 1f
)

fun SavedColor.toColor(): Color {
    return Color(
        red = red / 255f,
        green = green / 255f,
        blue = blue / 255f,
        alpha = alpha
    )
}

fun Color.toSavedColor(): SavedColor {
    return SavedColor(
        red = (red * 255).toInt(),
        green = (green * 255).toInt(),
        blue = (blue * 255).toInt(),
        alpha = alpha
    )
}