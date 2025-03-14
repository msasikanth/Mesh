package des.c5inco.mesh.data

import data.getRoomDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import model.SavedColor

class AppDataRepository {
    private val database = getRoomDatabase(getDatabaseBuilder())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            val savedColors = database.savedColorDao().getAll().first()
            if (savedColors.isEmpty()) {
                database.savedColorDao().insertAll(
                    SavedColor(red = 0, green = 0, blue = 0),
                    SavedColor(red = 255, green = 0, blue = 255),
                    SavedColor(red = 255, green = 0, blue = 0)
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
}