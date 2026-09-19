package org.koitharu.kotatsu.list.ui.adapter

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.koitharu.kotatsu.R
import org.koitharu.kotatsu.core.model.getTitle
import org.koitharu.kotatsu.core.ui.list.AdapterDelegateClickListenerAdapter
import org.koitharu.kotatsu.core.ui.list.OnListItemClickListener
import org.koitharu.kotatsu.core.util.ext.setTooltipCompat
import org.koitharu.kotatsu.databinding.ItemHistoryDetailsBinding
import org.koitharu.kotatsu.databinding.ItemHistoryListBinding
import org.koitharu.kotatsu.list.domain.ReadingProgress
import org.koitharu.kotatsu.list.ui.model.HistoryEntryInfo
import org.koitharu.kotatsu.list.ui.model.ListModel
import org.koitharu.kotatsu.list.ui.model.MangaCompactListModel
import org.koitharu.kotatsu.list.ui.model.MangaDetailedListModel
import org.koitharu.kotatsu.list.ui.model.MangaListModel

/**
 * History-screen cards. Same click behaviour as [mangaListItemAD] / [mangaListDetailedItemAD]
 * (cover -> details, text column -> reader) but with a real visual hierarchy:
 * title > alt title > chapter (tag + name) > tags > progress > meta (time, chapter count, source),
 * plus a delete button that removes the entry from history.
 */
fun historyCompactItemAD(
	clickListener: OnListItemClickListener<MangaListModel>,
	titleClickListener: OnListItemClickListener<MangaListModel>? = null,
	onRemoveClick: ((MangaListModel) -> Unit)? = null,
) = adapterDelegateViewBinding<MangaCompactListModel, ListModel, ItemHistoryListBinding>(
	{ inflater, parent -> ItemHistoryListBinding.inflate(inflater, parent, false) },
	on = { item, _, _ -> item is MangaCompactListModel && item.historyInfo != null },
) {

	AdapterDelegateClickListenerAdapter(this, clickListener).attach(itemView)
	if (titleClickListener != null) {
		binding.layoutContent.attachClickToRead(itemView) { view ->
			titleClickListener.onItemClick(item, view)
		}
	}
	binding.buttonRemove.isVisible = onRemoveClick != null
	binding.buttonRemove.setOnClickListener { onRemoveClick?.invoke(item) }

	bind {
		itemView.setTooltipCompat(item.getSummary(context))
		binding.textViewTitle.text = item.title
		bindHistoryInfo(
			context = context,
			info = item.historyInfo,
			source = item.source.getTitle(context),
			chapterRow = binding.layoutChapter,
			chapterTag = binding.textViewChapterTag,
			chapterName = binding.textViewChapterName,
			time = binding.textViewTime,
			sourceView = binding.textViewSource,
			percentView = binding.textViewPercent,
			progressBar = binding.progressBar,
			progressRow = null,
		)
		binding.imageViewPin.isVisible = item.isPinned
		binding.imageViewCover.setImageAsync(item.coverUrl, item.manga)
		binding.badge.number = item.counter
		binding.badge.isVisible = item.counter > 0
	}
}

fun historyDetailedItemAD(
	clickListener: MangaDetailsClickListener,
	titleClickListener: OnListItemClickListener<MangaListModel>? = null,
	onRemoveClick: ((MangaListModel) -> Unit)? = null,
) = adapterDelegateViewBinding<MangaDetailedListModel, ListModel, ItemHistoryDetailsBinding>(
	{ inflater, parent -> ItemHistoryDetailsBinding.inflate(inflater, parent, false) },
	on = { item, _, _ -> item is MangaDetailedListModel && item.historyInfo != null },
) {

	AdapterDelegateClickListenerAdapter(this, clickListener).attach(itemView)
	if (titleClickListener != null) {
		binding.layoutContent.attachClickToRead(itemView) { view ->
			titleClickListener.onItemClick(item, view)
		}
	}
	binding.buttonRemove.isVisible = onRemoveClick != null
	binding.buttonRemove.setOnClickListener { onRemoveClick?.invoke(item) }

	bind {
		binding.textViewTitle.text = item.title
		bindHistoryInfo(
			context = context,
			info = item.historyInfo,
			source = item.source.getTitle(context),
			chapterRow = binding.layoutChapter,
			chapterTag = binding.textViewChapterTag,
			chapterName = binding.textViewChapterName,
			time = binding.textViewTime,
			sourceView = binding.textViewSource,
			percentView = binding.textViewPercent,
			progressBar = binding.progressBar,
			progressRow = binding.layoutProgress,
			chaptersGroup = binding.layoutChaptersCount,
			chaptersView = binding.textViewChaptersCount,
		)
		binding.textViewSubtitle.text = item.subtitle
		binding.textViewSubtitle.isVisible = !item.subtitle.isNullOrBlank()
		val tagsText = item.tags.mapNotNull { it.title?.toString()?.takeIf(String::isNotBlank) }.take(MAX_TAGS).joinToString(", ")
		binding.textViewTags.text = tagsText
		binding.textViewTags.isVisible = tagsText.isNotEmpty()
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

private fun bindHistoryInfo(
	context: Context,
	info: HistoryEntryInfo?,
	source: String,
	chapterRow: View,
	chapterTag: TextView,
	chapterName: TextView,
	time: TextView,
	sourceView: TextView,
	percentView: TextView,
	progressBar: LinearProgressIndicator,
	progressRow: View?,
	chaptersGroup: View? = null,
	chaptersView: TextView? = null,
) {
	if (info == null) return

	// Chapter row: "CH 42" tag + chapter name; falls back to the chapter count when the
	// chapter itself could not be resolved (e.g. the source has not loaded its list yet).
	val tag = info.chapterNumber?.let { context.getString(R.string.history_chapter_short, it) }
	val name = info.chapterName ?: if (tag == null && info.chaptersCount > 0) {
		context.resources.getQuantityString(R.plurals.chapters, info.chaptersCount, info.chaptersCount)
	} else {
		null
	}
	chapterTag.text = tag
	chapterTag.isVisible = tag != null
	chapterName.text = name
	chapterName.isVisible = name != null
	chapterRow.isVisible = tag != null || name != null

	time.text = info.timeAgo
	if (chaptersGroup != null && chaptersView != null) {
		val hasCount = info.chaptersCount > 0
		chaptersGroup.isVisible = hasCount
		if (hasCount) {
			chaptersView.text = info.chaptersCount.toString()
			chaptersGroup.setTooltipCompat(
				context.resources.getQuantityString(R.plurals.chapters, info.chaptersCount, info.chaptersCount),
			)
		}
	}
	sourceView.text = source.uppercase()

	val hasProgress = ReadingProgress.isValid(info.percent)
	progressRow?.isVisible = hasProgress
	progressBar.isVisible = hasProgress
	percentView.isVisible = hasProgress
	if (hasProgress) {
		val value = ReadingProgress.percentToString(info.percent).toInt()
		progressBar.setProgressCompat(value, false)
		percentView.text = context.getString(R.string.percent_string_pattern, value.toString())
	}
}

private const val MAX_TAGS = 3
