package harness
import kotlinx.coroutines.*
import java.util.concurrent.Executors
object MainThread {
    val executor = Executors.newSingleThreadExecutor { r -> Thread(r, "fake-main").apply { isDaemon = true } }
    val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()
}
