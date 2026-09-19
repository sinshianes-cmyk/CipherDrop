package org.koitharu.kotatsu.reader.domain
import android.graphics.Rect
import android.net.Uri
import kotlinx.coroutines.*
import org.koitharu.kotatsu.core.util.progress.ProgressDeferred
import org.koitharu.kotatsu.parsers.model.MangaPage
import com.davemorrissey.labs.subscaleview.ImageSource
class PageLoader {
    val loaderScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    @Volatile var trimGate: java.util.concurrent.CountDownLatch? = null
    @Volatile var trimEntered: java.util.concurrent.CountDownLatch = java.util.concurrent.CountDownLatch(1)
    suspend fun loadPreview(page: MangaPage): ImageSource? = null
    fun loadPageAsync(page: MangaPage, force: Boolean): ProgressDeferred<Uri, Float> =
        ProgressDeferred(CompletableDeferred(Uri("page${page.id}")))
    suspend fun getTrimmedBounds(uri: Uri): Rect? {
        // Simulates an edge detector that does not honour cancellation while it runs.
        withContext(NonCancellable) {
            trimEntered.countDown()
            trimGate?.await()
        }
        return null
    }
    suspend fun convertBimap(uri: Uri): Uri = uri
}
