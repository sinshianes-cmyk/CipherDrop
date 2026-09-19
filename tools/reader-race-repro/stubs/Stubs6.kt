package com.davemorrissey.labs.subscaleview
import android.graphics.Rect
sealed class ImageSource {
    class Uri(val uri: android.net.Uri) : ImageSource()
    fun region(r: Rect): ImageSource = this
    companion object { fun uri(u: android.net.Uri): ImageSource = Uri(u) }
}
interface DefaultOnImageEventListener {
    fun onImageLoaded() {}
    fun onImageLoadError(e: Throwable) {}
}
