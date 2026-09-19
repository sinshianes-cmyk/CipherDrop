package org.koitharu.kotatsu.reader.ui.pager.vm

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import androidx.annotation.WorkerThread
import com.davemorrissey.labs.subscaleview.DefaultOnImageEventListener
import com.davemorrissey.labs.subscaleview.ImageSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import okio.IOException
import java.util.concurrent.atomic.AtomicInteger
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.exceptions.resolve.ExceptionResolver
import org.koitharu.kotatsu.core.os.NetworkState
import org.koitharu.kotatsu.core.util.ext.copyToClipboard
import org.koitharu.kotatsu.core.util.ext.printStackTraceDebug
import org.koitharu.kotatsu.core.util.ext.throttle
import org.koitharu.kotatsu.parsers.model.MangaPage
import org.koitharu.kotatsu.reader.domain.PageLoader
import org.koitharu.kotatsu.reader.ui.config.ReaderSettings

class PageViewModel(
	private val loader: PageLoader,
	val settingsProducer: ReaderSettings.Producer,
	private val networkState: NetworkState,
	private val exceptionResolver: ExceptionResolver,
	private val isWebtoon: Boolean,
) : DefaultOnImageEventListener {

	private val scope = loader.loaderScope + Dispatchers.Main.immediate
	private var job: Job? = null
	private var cachedBounds: Rect? = null

	val state = MutableStateFlow<PageState>(PageState.Empty)

	/**
	 * Bumped every time this view model is bound to a page, retried or recycled. A load job only
	 * publishes its result while its own generation is still the current one, so a job that lost
	 * the race against a recycle/rebind can never push the previous page's image into the holder
	 * (which showed up as pieces of another page below the one being read).
	 */
	private val generation = AtomicInteger(0)

	/** Id of the page this view model is currently bound to; stamped on every published state. */
	@Volatile
	private var pageId: Long = PageState.NO_PAGE

	fun isLoading() = job?.isActive == true

	fun onBind(page: MangaPage) {
		val gen = generation.incrementAndGet()
		pageId = page.id
		val prevJob = job
		// Cancel right away (not from inside the new job) so the old load can no longer publish.
		prevJob?.cancel()
		// Drop whatever the previous page left behind until the new page reports its own state.
		state.value = PageState.Empty
		job = scope.launch(Dispatchers.Default) {
			prevJob?.join()
			doLoad(page, force = false, gen = gen)
		}
	}

	fun retry(page: MangaPage, isFromUser: Boolean) {
		val gen = generation.incrementAndGet()
		pageId = page.id
		val prevJob = job
		prevJob?.cancel()
		job = scope.launch {
			prevJob?.join()
			val e = (state.value as? PageState.Error)?.error
			if (e != null && ExceptionResolver.canResolve(e)) {
				if (isFromUser) {
					exceptionResolver.resolve(e)
				}
			}
			withContext(Dispatchers.Default) {
				doLoad(page, force = true, gen = gen)
			}
		}
	}

	fun copyErrorToClipboard(context: Context) {
		val e = (state.value as? PageState.Error)?.error ?: return
		context.copyToClipboard(context.getString(R.string.error), e.stackTraceToString())
	}

	fun onRecycle() {
		generation.incrementAndGet()
		pageId = PageState.NO_PAGE
		job?.cancel()
		state.value = PageState.Empty
		cachedBounds = null
	}

	override fun onImageLoaded() {
		state.update { currentState ->
			if (currentState is PageState.Loaded) {
				PageState.Shown(currentState.source, currentState.isConverted, currentState.pageId)
			} else {
				currentState
			}
		}
	}

	override fun onImageLoadError(e: Throwable) {
		e.printStackTraceDebug()

		state.update { currentState ->
			if (currentState is PageState.Loaded) {
				val uri = (currentState.source as? ImageSource.Uri)?.uri
				if (!currentState.isConverted && uri != null && e is IOException) {
					tryConvert(uri, e)
					PageState.Converting()
				} else {
					PageState.Error(e)
				}
			} else {
				currentState
			}
		}
	}

	private fun tryConvert(uri: Uri, e: Exception) {
		val gen = generation.get()
		val ownerId = pageId
		val prevJob = job
		job = scope.launch(Dispatchers.Default) {
			prevJob?.join()
			publish(gen) { PageState.Converting() }
			try {
				val newUri = loader.convertBimap(uri)
				cachedBounds = if (settingsProducer.value.isPagesCropEnabled(isWebtoon)) {
					loader.getTrimmedBounds(newUri)
				} else {
					null
				}
				publish(gen) { PageState.Loaded(newUri.toImageSource(cachedBounds), isConverted = true, pageId = ownerId) }
			} catch (ce: CancellationException) {
				throw ce
			} catch (e2: Throwable) {
				e2.printStackTrace()
				e.addSuppressed(e2)
				publish(gen) { PageState.Error(e) }
			}
		}
	}

	/**
	 * Publishes a state change on the main thread, and only if this view model is still on the
	 * generation the calling job was started for. Running the check on the main thread makes it
	 * atomic with [onBind] / [onRecycle], which run there as well.
	 */
	private suspend fun publish(gen: Int, transform: (PageState) -> PageState) {
		withContext(Dispatchers.Main.immediate) {
			ensureActive()
			if (generation.get() == gen) {
				state.update(transform)
			}
		}
	}

	@WorkerThread
	private suspend fun doLoad(data: MangaPage, force: Boolean, gen: Int) = coroutineScope {
		publish(gen) { PageState.Loading(null, -1, data.id) }
		val previewJob = launch {
			val preview = loader.loadPreview(data) ?: return@launch
			publish(gen) {
				if (it is PageState.Loading) it.copy(preview = preview) else it
			}
		}
		try {
			val task = loader.loadPageAsync(data, force)
			val progressObserver = observeProgress(this, task.progressAsFlow(), gen)
			val uri = task.await()
			progressObserver.cancelAndJoin()
			previewJob.cancel()
			cachedBounds = if (settingsProducer.value.isPagesCropEnabled(isWebtoon)) {
				loader.getTrimmedBounds(uri)
			} else {
				null
			}
			val source = uri.toImageSource(cachedBounds)
			publish(gen) { PageState.Loaded(source, isConverted = false, pageId = data.id) }
		} catch (e: CancellationException) {
			throw e
		} catch (e: Throwable) {
			e.printStackTraceDebug()
			publish(gen) { PageState.Error(e) }
			if (e is IOException && !networkState.value) {
				networkState.awaitForConnection()
				// Only retry if the holder still shows this very page.
				if (generation.get() == gen) {
					retry(data, isFromUser = false)
				}
			}
		}
	}

	private fun observeProgress(scope: CoroutineScope, progress: Flow<Float>, gen: Int) = progress
		.throttle(250)
		.onEach {
			val progressValue = (100 * it).toInt()
			publish(gen) { currentState ->
				if (currentState is PageState.Loading) {
					currentState.copy(progress = progressValue)
				} else {
					currentState
				}
			}
		}.launchIn(scope)

	private fun Uri.toImageSource(bounds: Rect?): ImageSource {
		val source = ImageSource.uri(this)
		return if (bounds != null) {
			source.region(bounds)
		} else {
			source
		}
	}
}
