package model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DbColor(
    @SerialName("_id") val id: Long? = null,
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Float = 1f
)

@Serializable
@Entity
data class SavedColor(
    @PrimaryKey(true) val uid: Long = 0,
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Float = 1f
)