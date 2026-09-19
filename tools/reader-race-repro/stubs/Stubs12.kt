package org.koitharu.kotatsu.reader.ui.config
class ReaderSettings(var crop: Boolean) {
    fun isPagesCropEnabled(isWebtoon: Boolean) = crop
    class Producer(val value: ReaderSettings)
}
