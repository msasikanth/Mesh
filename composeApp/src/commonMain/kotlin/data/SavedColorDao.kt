package data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import model.SavedColor

@Dao
interface SavedColorDao {
    @Query("SELECT * FROM savedColor")
    fun getAll(): Flow<List<SavedColor>>

    @Insert
    suspend fun insertAll(vararg colors: SavedColor)

    @Delete
    suspend fun delete(color: SavedColor)
}