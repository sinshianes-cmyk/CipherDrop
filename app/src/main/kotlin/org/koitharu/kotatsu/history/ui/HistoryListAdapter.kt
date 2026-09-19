package org.koitharu.kotatsu.history.ui

import android.content.Context
import org.koitharu.kotatsu.core.ui.list.fastscroll.FastScroller
import org.koitharu.kotatsu.list.ui.adapter.MangaListAdapter
import org.koitharu.kotatsu.list.ui.adapter.MangaListListener
import org.koitharu.kotatsu.list.ui.model.MangaListModel
import org.koitharu.kotatsu.list.ui.model.TipModel
import org.koitharu.kotatsu.list.ui.size.ItemSizeResolver

class HistoryListAdapter(
	listener: MangaListListener,
	sizeResolver: ItemSizeResolver,
	onTipClose: ((TipModel) -> Unit)? = null,
	onRemoveFromHistory: ((MangaListModel) -> Unit)? = null,
) : MangaListAdapter(
	listener = listener,
	sizeResolver = sizeResolver,
	// On the History screen, tapping the text (title/chapter/time) always opens the reader,
	// and tapping the cover opens the title's details page — regardless of the app-wide
	// "tap title to read" setting.
	titleTapToRead = true,
	extendedTapToRead = true,
	onTipClose = onTipClose,
	historyStyle = true,
	onRemoveFromHistory = onRemoveFromHistory,
), FastScroller.SectionIndexer {

	override fun getSectionText(context: Context, position: Int): CharSequence? {
		return findHeader(position)?.getText(context)
	}
}
