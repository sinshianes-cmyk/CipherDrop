package org.koitharu.kotatsu.core.exceptions.resolve
class ExceptionResolver {
    companion object { fun canResolve(e: Throwable) = false }
    suspend fun resolve(e: Throwable): Boolean = false
}
