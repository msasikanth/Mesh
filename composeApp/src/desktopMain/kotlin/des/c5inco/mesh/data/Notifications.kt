package des.c5inco.mesh.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object Notifications {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _notificationFlow = MutableSharedFlow<String>()
    val notificationFlow = _notificationFlow

    fun send(message: String) {
        scope.launch {
            _notificationFlow.emit(message)
        }
    }
}