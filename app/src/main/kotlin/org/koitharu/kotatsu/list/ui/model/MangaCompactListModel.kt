package org.koitharu.kotatsu.list.ui.model

import org.koitharu.kotatsu.core.ui.model.MangaOverride
import org.koitharu.kotatsu.parsers.model.Manga

data class MangaCompactListModel(
	override val manga: Manga,
	override val override: MangaOverride?,
	val subtitle: String,
	override val counter: Int,
	val isPinned: Boolean = false,
	val historyInfo: HistoryEntryInfo? = null,
) : MangaListModel() {

	override fun getChangePayload(previousState: ListModel): Any? = when {
		previousState !is MangaCompactListModel || previousState.manga != manga -> null
		previousState.historyInfo != historyInfo -> null
		else -> super.getChangePayload(previousState)
	}
}
