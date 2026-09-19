package org.koitharu.kotatsu.core.util.ext
import android.content.Context
import kotlinx.coroutines.flow.Flow
fun Context.copyToClipboard(label: String, text: String) {}
fun Throwable.printStackTraceDebug() {}
fun <T> Flow<T>.throttle(ms: Long): Flow<T> = this
