package org.koitharu.kotatsu.list.ui.model

/**
 * Extra info shown on the History screen cards:
 * chapter name (if any), chapter number, read progress and when it was last read.
 */
data class HistoryEntryInfo(
	val chapterName: String?,
	val chapterLabel: String?,
	val timeAgo: String,
	/** Raw chapter number ("42", "42.5") or null when the chapter is unknown. */
	val chapterNumber: String? = null,
	/** Read progress in 0..1, or a negative value when unknown. */
	val percent: Float = -1f,
	val chaptersCount: Int = 0,
) {

	/** Ordered lines: chapter name, then chapter number, then last-read time. */
	fun toLines(): List<String> = listOfNotNull(chapterName, chapterLabel, timeAgo)
}
