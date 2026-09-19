package org.koitharu.kotatsu.core.ui.drawable

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import org.koitharu.kotatsu.R
import org.xmlpull.v1.XmlPullParser
import kotlin.math.min

/**
 * Flat panel with all four corners cut off - the silhouette of the "Last position" pill, used as the
 * background of windows (menus, dropdowns, dialogs). XML shape drawables cannot cut corners, so this is
 * referenced as `<drawable class="...CutCornerDrawable" app:cutFillColor="?attr/..." app:cutSize="..."/>`.
 *
 * It deliberately has no constant state: the fill is a theme attribute, and a shared cached state would
 * pin the colours of whichever theme inflated it first.
 */
class CutCornerDrawable : Drawable() {

	private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
	private val path = Path()
	private var fill: ColorStateList = ColorStateList.valueOf(Color.TRANSPARENT)
	private var currentFill = Color.TRANSPARENT
	private var cutSize = 0f
	private var alphaValue = 255

	override fun inflate(r: Resources, parser: XmlPullParser, attrs: AttributeSet, theme: Resources.Theme?) {
		super.inflate(r, parser, attrs, theme)
		if (theme != null) {
			val a = theme.obtainStyledAttributes(attrs, R.styleable.CutCornerDrawable, 0, 0)
			try {
				a.getColorStateList(R.styleable.CutCornerDrawable_cutFillColor)?.let { fill = it }
				cutSize = a.getDimension(R.styleable.CutCornerDrawable_cutSize, cutSize)
			} finally {
				a.recycle()
			}
		} else {
			// No theme context available (e.g. PopupWindow on MIUI inflates drawables with theme=null).
			// Theme attributes like ?attr/colorSurfaceContainer cannot be resolved without a theme;
			// fall back to transparent so inflation succeeds — applyTheme() will correct the color later.
			val a = r.obtainAttributes(attrs, R.styleable.CutCornerDrawable)
			try {
				cutSize = a.getDimension(R.styleable.CutCornerDrawable_cutSize, cutSize)
				// Skip getColorStateList — throws UnsupportedOperationException for ?attr/ values
				// when TypedArray is obtained without a theme.
			} finally {
				a.recycle()
			}
		}
		currentFill = fill.getColorForState(state, fill.defaultColor)
		rebuildPath()
	}

	/**
	 * Apply a theme after the drawable has been inflated without one.
	 * Called by the framework when a theme becomes available.
	 */
	override fun applyTheme(theme: Resources.Theme) {
		super.applyTheme(theme)
		// Re-resolve the fill color now that we have a theme.
		val attrs = intArrayOf(android.R.attr.colorBackground)
		val a = theme.obtainStyledAttributes(attrs)
		try {
			val resolved = a.getColorStateList(0)
			if (resolved != null) fill = resolved
		} catch (_: Exception) {
			// Keep transparent fallback if resolution still fails.
		} finally {
			a.recycle()
		}
		currentFill = fill.getColorForState(state, fill.defaultColor)
		invalidateSelf()
	}

	override fun canApplyTheme(): Boolean = true

	override fun draw(canvas: Canvas) {
		if (path.isEmpty) {
			return
		}
		paint.color = currentFill
		paint.alpha = paint.alpha * alphaValue / 255
		canvas.drawPath(path, paint)
	}

	override fun onBoundsChange(bounds: Rect) {
		super.onBoundsChange(bounds)
		rebuildPath()
	}

	override fun isStateful(): Boolean = fill.isStateful

	override fun onStateChange(state: IntArray): Boolean {
		val color = fill.getColorForState(state, fill.defaultColor)
		if (color == currentFill) {
			return false
		}
		currentFill = color
		return true
	}

	override fun setAlpha(alpha: Int) {
		if (alphaValue != alpha) {
			alphaValue = alpha
			invalidateSelf()
		}
	}

	override fun setColorFilter(colorFilter: ColorFilter?) {
		paint.colorFilter = colorFilter
		invalidateSelf()
	}

	@Suppress("OVERRIDE_DEPRECATION")
	override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

	@Suppress("DEPRECATION")
	override fun getOutline(outline: Outline) {
		if (path.isEmpty) {
			outline.setEmpty()
			return
		}
		// A cut-corner rectangle is convex, so the pre-R convex-only outline is enough.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
			outline.setPath(path)
		} else {
			outline.setConvexPath(path)
		}
	}

	private fun rebuildPath() {
		path.reset()
		val b = bounds
		if (b.isEmpty) {
			return
		}
		val l = b.left.toFloat()
		val t = b.top.toFloat()
		val r = b.right.toFloat()
		val bt = b.bottom.toFloat()
		val c = min(cutSize, min(b.width(), b.height()) / 2f)
		path.moveTo(l + c, t)
		path.lineTo(r - c, t)
		path.lineTo(r, t + c)
		path.lineTo(r, bt - c)
		path.lineTo(r - c, bt)
		path.lineTo(l + c, bt)
		path.lineTo(l, bt - c)
		path.lineTo(l, t + c)
		path.close()
		invalidateSelf()
	}
}
