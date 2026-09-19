package org.koitharu.kotatsu.list.ui.adapter

import androidx.core.view.isVisible
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.koitharu.kotatsu.core.ui.list.AdapterDelegateClickListenerAdapter
import org.koitharu.kotatsu.core.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.core.util.ext.setTooltipCompat
import org.koitharu.kotatsu.core.util.ext.textAndVisible
import org.koitharu.kotatsu.databinding.ItemMangaListBinding
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.list.ui.model.MangaCompactListModel
import org.koitharu.kotatsu.list.ui.model.MangaListModel

fun mangaListItemAD(
	clickListener: OnListItemClickListener<MangaListModel>,
	titleClickListener: OnListItemClickListener<MangaListModel>? = null,
	extendedTapToRead: Boolean = false,
) = adapterDelegateViewBinding<MangaCompactListModel, ListModel, ItemMangaListBinding>(
	{ inflater, parent -> ItemMangaListBinding.inflate(inflater, parent, false) },
) {

	AdapterDelegateClickListenerAdapter(this, clickListener).attach(itemView)
	if (titleClickListener != null) {
		if (extendedTapToRead) {
			// The whole text column (title + subtitle) opens the reader; the cover
			// is left untouched so tapping it falls through to the default click (details).
			binding.layoutContent.attachClickToRead(itemView) { view ->
				titleClickListener.onItemClick(item, view)
			}
		} else {
			binding.textViewTitle.attachTitleClickToRead(itemView) { view ->
				titleClickListener.onItemClick(item, view)
			}
		}
	}

	bind {
		itemView.setTooltipCompat(item.getSummary(context))
		binding.textViewTitle.text = item.title
		val historyInfo = item.historyInfo
		if (historyInfo != null) {
			binding.textViewSubtitle.maxLines = 3
			binding.textViewSubtitle.textAndVisible = historyInfo.toLines().joinToString("\n")
		} else {
			binding.textViewSubtitle.maxLines = 1
			binding.textViewSubtitle.textAndVisible = item.subtitle
		}
		binding.imageViewPin.isVisible = item.isPinned
		binding.imageViewCover.setImageAsync(item.coverUrl, item.manga)
		binding.badge.number = item.counter
		binding.badge.isVisible = item.counter > 0
	}
}
