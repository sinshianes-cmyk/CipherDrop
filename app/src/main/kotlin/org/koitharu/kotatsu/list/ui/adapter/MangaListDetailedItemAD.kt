package org.koitharu.kotatsu.list.ui.adapter

import androidx.core.view.isVisible
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.ui.list.AdapterDelegateClickListenerAdapter
import org.koitharu.kotatsu.core.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.list.ui.model.MangaListModel
import org.koitharu.kotatsu.core.util.ext.textAndVisible
import org.koitharu.kotatsu.databinding.ItemMangaListDetailsBinding
import org.koitharu.kotatsu.list.ui.ListModelDiffCallback
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.list.ui.model.MangaDetailedListModel

fun mangaListDetailedItemAD(
	clickListener: MangaDetailsClickListener,
	titleClickListener: OnListItemClickListener<MangaListModel>? = null,
	extendedTapToRead: Boolean = false,
) = adapterDelegateViewBinding<MangaDetailedListModel, ListModel, ItemMangaListDetailsBinding>(
	{ inflater, parent -> ItemMangaListDetailsBinding.inflate(inflater, parent, false) },
) {

	AdapterDelegateClickListenerAdapter(this, clickListener)
		.attach(itemView)
	if (titleClickListener != null) {
		if (extendedTapToRead) {
			// The whole text column (title + author/tags) opens the reader; the cover
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

	bind { payloads ->
		binding.textViewTitle.text = item.title
		val historyInfo = item.historyInfo
		if (historyInfo != null) {
			// Order: chapter name (if found) on the author line, then chapter number
			// and last-read time stacked on the tags line.
			val lines = historyInfo.toLines()
			binding.textViewAuthor.textAndVisible = lines.firstOrNull().orEmpty()
			binding.textViewTags.text = lines.drop(1).joinToString("\n")
		} else {
			binding.textViewAuthor.textAndVisible = item.manga.authors.joinToString(", ")
			binding.textViewTags.text = item.tags.joinToString(separator = ", ") { it.title ?: "" }
		}
		binding.progressView.setProgress(
			value = item.progress,
			animate = ListModelDiffCallback.PAYLOAD_PROGRESS_CHANGED in payloads,
		)
		with(binding.iconsView) {
			clearIcons()
			if (item.isSaved) addIcon(R.drawable.ic_storage)
			if (item.isFavorite) addIcon(R.drawable.ic_heart_outline)
			isVisible = iconsCount > 0
		}
		binding.imageViewPin.isVisible = item.isPinned
		binding.imageViewCover.setImageAsync(item.coverUrl, item.manga)
		binding.badge.number = item.counter
		binding.badge.isVisible = item.counter > 0
	}
}
