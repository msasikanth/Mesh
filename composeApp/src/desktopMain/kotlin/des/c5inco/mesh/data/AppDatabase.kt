package des.c5inco.mesh.data.des.c5inco.mesh.ui.data

import com.github.lamba92.kotlin.document.store.core.KotlinDocumentStore
import com.github.lamba92.kotlin.document.store.core.getObjectCollection
import com.github.lamba92.kotlin.document.store.stores.leveldb.LevelDBStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

class AppDatabase {
    private val store = LevelDBStore.open(System.getProperty("user.home") + File.separator + ".mesh_db")
    private val db = KotlinDocumentStore(store)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val colors = MutableStateFlow<List<DbColor>>(emptyList())

    init {
        scope.launch {
            val colorCollection = db.getObjectCollection<DbColor>("colors")
            if (colorCollection.size() < 1) {
                colorCollection.insert(DbColor(red = 0, green = 0, blue = 0))
                colorCollection.insert(DbColor(red = 255, green = 0, blue = 255))
                colorCollection.insert(DbColor(red = 255, green = 0, blue = 0))
            }
        }
    }

    fun getColors() = flow {
        val colorCollection = withContext(Dispatchers.IO) {
            db.getObjectCollection<DbColor>("colors")
        }

        emit(colorCollection.iterateAll().toList())
    }

    fun addColor(color: DbColor) {
        scope.launch {
            val colorCollection = db.getObjectCollection<DbColor>("colors")
            colorCollection.insert(color)
        }
    }
}

@Serializable
data class DbColor(
    @SerialName("_id") val id: Long? = null,
    val red: Int,
    val green: Int,
    val blue: Int,
    val alpha: Float = 1f
)