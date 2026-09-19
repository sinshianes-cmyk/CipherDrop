package org.koitharu.kotatsu.list.ui.adapter

import org.koitharu.kotatsu.core.ui.BaseListAdapter
import org.koitharu.kotatsu.core.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.list.ui.model.MangaListModel
import org.koitharu.kotatsu.list.ui.model.TipModel
import org.koitharu.kotatsu.list.ui.size.ItemSizeResolver

open class MangaListAdapter(
	listener: MangaListListener,
	sizeResolver: ItemSizeResolver,
	titleTapToRead: Boolean = false,
	/**
	 * When true, tapping anywhere in the text column (title + subtitle/author/tags) opens
	 * the reader, not just the title — used by the History screen. Only takes effect when
	 * [titleTapToRead] is also true.
	 */
	extendedTapToRead: Boolean = false,
	/** Required for a tip to show its close button at all — see [tipAD]. */
	onTipClose: ((TipModel) -> Unit)? = null,
	/**
	 * When true, list / detailed-list items that carry history info are drawn with the
	 * History cards ([historyCompactItemAD] / [historyDetailedItemAD]) instead of the
	 * generic ones. Used by the History screen.
	 */
	historyStyle: Boolean = false,
	/** History cards only: called when the delete button of a card is tapped. */
	onRemoveFromHistory: ((MangaListModel) -> Unit)? = null,
) : BaseListAdapter<ListModel>() {

	init {
		val titleClickListener = if (titleTapToRead) {
			OnListItemClickListener<MangaListModel> { item, view ->
				listener.onReadClick(item.toMangaWithOverride(), view)
			}
		} else {
			null
		}
		if (historyStyle) {
			// Chosen over the generic delegates for items that carry history info (see the `on`
			// predicate); the generic ones stay registered as the fallback.
			addDelegate(
				ListItemType.MANGA_LIST_HISTORY,
				historyCompactItemAD(listener, titleClickListener, onRemoveFromHistory),
			)
			addDelegate(
				ListItemType.MANGA_LIST_DETAILED_HISTORY,
				historyDetailedItemAD(listener, titleClickListener, onRemoveFromHistory),
			)
		}
		addDelegate(ListItemType.MANGA_LIST, mangaListItemAD(listener, titleClickListener, extendedTapToRead))
		addDelegate(
			ListItemType.MANGA_LIST_DETAILED,
			mangaListDetailedItemAD(listener, titleClickListener, extendedTapToRead),
		)
		addDelegate(ListItemType.MANGA_GRID, mangaGridItemAD(sizeResolver, listener, titleClickListener))
		addDelegate(ListItemType.FOOTER_LOADING, loadingFooterAD())
		addDelegate(ListItemType.STATE_LOADING, loadingStateAD())
		addDelegate(ListItemType.STATE_ERROR, errorStateListAD(listener))
		addDelegate(ListItemType.FOOTER_ERROR, errorFooterAD(listener))
		addDelegate(ListItemType.STATE_EMPTY, emptyStateListAD(listener))
		addDelegate(ListItemType.HEADER, listHeaderAD(listener))
		addDelegate(ListItemType.QUICK_FILTER, quickFilterAD(listener))
		addDelegate(ListItemType.TIP, tipAD(listener, onTipClose))
		addDelegate(ListItemType.INFO, infoAD())
		addDelegate(ListItemType.FOOTER_BUTTON, buttonFooterAD(listener))
	}
}
