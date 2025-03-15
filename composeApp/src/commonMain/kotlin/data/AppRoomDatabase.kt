package data

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import model.MeshPoint
import model.SavedColor

@Database(entities = [SavedColor::class, MeshPoint::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppRoomDatabase : RoomDatabase() {
    abstract fun savedColorDao(): SavedColorDao
    abstract fun meshPointDao(): MeshPointDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppRoomDatabase> {
    override fun initialize(): AppRoomDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppRoomDatabase>
): AppRoomDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}