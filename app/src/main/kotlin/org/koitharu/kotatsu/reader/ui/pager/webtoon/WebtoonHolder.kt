package org.koitharu.kotatsu.reader.ui.pager.webtoon

import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.LifecycleOwner
import org.koitharu.kotatsu.core.exceptions.resolve.ExceptionResolver
import org.koitharu.kotatsu.core.os.NetworkState
import org.koitharu.kotatsu.databinding.ItemPageWebtoonBinding
import org.koitharu.kotatsu.reader.domain.PageLoader
import org.koitharu.kotatsu.reader.ui.config.ReaderSettings
import org.koitharu.kotatsu.reader.ui.pager.BasePageHolder
import org.koitharu.kotatsu.reader.ui.pager.ReaderPage

class WebtoonHolder(
	owner: LifecycleOwner,
	binding: ItemPageWebtoonBinding,
	loader: PageLoader,
	readerSettingsProducer: ReaderSettings.Producer,
	networkState: NetworkState,
	exceptionResolver: ExceptionResolver,
) : BasePageHolder<ItemPageWebtoonBinding>(
	binding = binding,
	loader = loader,
	readerSettingsProducer = readerSettingsProducer,
	networkState = networkState,
	exceptionResolver = exceptionResolver,
	lifecycleOwner = owner,
) {

	override val ssiv = binding.ssiv

	private var scrollToRestore = 0
	private var scrollOwnerId: Long? = null

	init {
		bindingInfo.progressBar.setVisibilityAfterHide(View.GONE)
	}

	override fun onBind(data: ReaderPage) {
		super.onBind(data)
		if (scrollOwnerId != data.id) {
			// A scroll position queued for the previous page must not be applied to this one.
			scrollOwnerId = data.id
			scrollToRestore = 0
		}
	}

	override fun onRecycled() {
		scrollToRestore = 0
		scrollOwnerId = null
		super.onRecycled()
	}

	override fun onReady() {
		binding.ssiv.colorFilter = settings.colorFilter?.toColorFilter()
		with(binding.ssiv) {
			val restore = scrollToRestore
			scrollToRestore = 0
			val toEnd = restore == 0 && itemView.top < 0
			scrollTo(
				when {
					restore != 0 -> restore
					toEnd -> getScrollRange()
					else -> 0
				},
			)
			if (toEnd && isLayoutRequested) {
				// The view is about to be resized to the page height; the range measured now is stale.
				val pageId = boundData?.id
				doOnNextLayout { view ->
					if (boundData?.id == pageId) {
						(view as WebtoonImageView).let { it.scrollTo(it.getScrollRange()) }
					}
				}
			}
		}
	}

	fun getScrollY() = binding.ssiv.getScroll()

	fun restoreScroll(scroll: Int) {
		if (binding.ssiv.isReady) {
			binding.ssiv.scrollTo(scroll)
		} else {
			scrollToRestore = scroll
		}
	}
}
