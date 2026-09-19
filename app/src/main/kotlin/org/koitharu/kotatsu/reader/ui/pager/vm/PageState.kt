package org.koitharu.kotatsu.reader.ui.pager.vm

import com.davemorrissey.labs.subscaleview.ImageSource

sealed class PageState {

	data object Empty : PageState()

	data class Loading(
		val preview: ImageSource?,
		val progress: Int,
		val pageId: Long = NO_PAGE,
	) : PageState()

	data class Loaded(
		val source: ImageSource,
		val isConverted: Boolean,
		val pageId: Long = NO_PAGE,
	) : PageState()

	class Converting() : PageState()

	data class Shown(
		val source: ImageSource,
		val isConverted: Boolean,
		val pageId: Long = NO_PAGE,
	) : PageState()

	data class Error(
		val error: Throwable,
	) : PageState()

	fun isFinalState(): Boolean = this is Error || this is Shown

	/**
	 * The page this state was produced for, or [NO_PAGE] when the state is not tied to a page.
	 * The holder compares it with the page it currently shows and drops states of any other page.
	 */
	fun ownerPageId(): Long = when (this) {
		is Loading -> pageId
		is Loaded -> pageId
		is Shown -> pageId
		else -> NO_PAGE
	}

	companion object {

		const val NO_PAGE = Long.MIN_VALUE
	}
}
