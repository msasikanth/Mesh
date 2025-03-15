package data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import model.MeshPoint

@Dao
interface MeshPointDao {
    @Query("SELECT * FROM meshPoint")
    fun getAll(): Flow<List<MeshPoint>>

    @Insert
    suspend fun insertAll(vararg points: MeshPoint)

    @Query("DELETE FROM meshPoint")
    suspend fun deleteAll()
}