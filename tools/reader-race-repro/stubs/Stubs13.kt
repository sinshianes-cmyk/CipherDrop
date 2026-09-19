package org.koitharu.kotatsu.core.util.progress
import android.net.Uri
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
class ProgressDeferred<T, P>(private val d: Deferred<T>) {
    suspend fun await(): T = d.await()
    fun progressAsFlow(): Flow<P> = emptyFlow()
}
