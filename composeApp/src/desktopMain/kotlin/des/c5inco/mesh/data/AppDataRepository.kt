package des.c5inco.mesh.data

import androidx.compose.ui.graphics.Color
import data.getRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import model.SavedColor
import model.toSavedColor

class AppDataRepository {
    private val database = getRoomDatabase(getDatabaseBuilder())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val defaultColors = listOf(
        Color(0xff7766EE),
        Color(0xff8899ff),
        Color(0xff429BED),
        Color(0xff4FC1A6),
        Color(0xffF0C03E),
        Color(0xffff5599),
        Color(0xFFFF00FF),
    )

    init {
        scope.launch {
            val savedColors = database.savedColorDao().getAll().first()
            if (savedColors.isEmpty()) {
                database.savedColorDao().insertAll(
                    *defaultColors.map { it.toSavedColor() }.toTypedArray()
                )
            }
        }
    }

    fun getColors(): Flow<List<SavedColor>> {
        return database.savedColorDao().getAll()
    }

    fun addColor(color: SavedColor) {
        scope.launch {
            database.savedColorDao().insertAll(color)
        }
    }

    fun deleteColor(color: SavedColor) {
        scope.launch {
            database.savedColorDao().delete(color)
        }
    }
}