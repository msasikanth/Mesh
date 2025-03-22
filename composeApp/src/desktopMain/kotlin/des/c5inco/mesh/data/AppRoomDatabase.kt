package des.c5inco.mesh.data

import androidx.room.Room
import androidx.room.RoomDatabase
import data.AppRoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppRoomDatabase> {
    val dbFile = File(
        System.getProperty("user.home") + File.separator +
            ".mesh" + File.separator + "appDb"
    )
    return Room.databaseBuilder<AppRoomDatabase>(
        name = dbFile.absolutePath,
    )
}