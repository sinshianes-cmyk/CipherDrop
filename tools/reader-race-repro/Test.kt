import android.net.Uri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koitharu.kotatsu.core.exceptions.resolve.ExceptionResolver
import org.koitharu.kotatsu.core.os.NetworkState
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.reader.domain.PageLoader
import org.koitharu.kotatsu.reader.ui.config.ReaderSettings
import org.koitharu.kotatsu.reader.ui.pager.vm.PageState
import org.koitharu.kotatsu.reader.ui.pager.vm.PageViewModel
import com.davemorrissey.labs.subscaleview.ImageSource
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

fun label(s: PageState): String = when (s) {
    is PageState.Loaded -> "Loaded(" + ((s.source as ImageSource.Uri).uri) + ")"
    is PageState.Shown -> "Shown"
    is PageState.Loading -> "Loading"
    is PageState.Empty -> "Empty"
    is PageState.Error -> "Error"
    else -> "Converting"
}

fun main() = runBlocking {
    val loader = PageLoader()
    val vm = PageViewModel(loader, ReaderSettings.Producer(ReaderSettings(crop = true)), NetworkState(), ExceptionResolver(), true)
    val seen = java.util.Collections.synchronizedList(ArrayList<String>())
    val collector = launch(Dispatchers.Unconfined) { vm.state.collect { seen.add(label(it)) } }

    // Holder is bound to page 13; its load is in the slow, non-cancellable step (crop detection).
    loader.trimGate = CountDownLatch(1)
    vm.onBind(MangaPage(13))
    loader.trimEntered.await(5, TimeUnit.SECONDS)

    // User scrolls back: holder is recycled and rebound to page 10 while page 13 is still finishing.
    seen.add("--- recycle + rebind to page 10 ---")
    vm.onRecycle()
    loader.trimEntered = CountDownLatch(1)
    vm.onBind(MangaPage(10))

    // Page 13's slow step finishes now.
    loader.trimGate!!.countDown()
    delay(400)
    collector.cancel()
    println(seen.joinToString(" -> "))
    val stale = seen.dropWhile { !it.startsWith("---") }.any { it == "Loaded(page13)" }
    println(if (stale) "RESULT: STALE page13 image was published to the holder now showing page 10" else "RESULT: OK, page13 never reached the holder")
    exitProcess0()
}
fun exitProcess0() { System.out.flush(); Runtime.getRuntime().halt(0) }
