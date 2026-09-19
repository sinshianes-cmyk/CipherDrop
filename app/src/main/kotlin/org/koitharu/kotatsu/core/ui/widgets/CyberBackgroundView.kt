package org.koitharu.kotatsu.core.ui.widgets

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.appcompat.R as appcompatR
import androidx.core.graphics.ColorUtils
import com.google.android.material.color.MaterialColors
import kotlin.math.max

/**
 * Cyberpunk HUD ambience drawn over the app content — a port of SQL Reader's
 * `CyberBackgroundEffect`: a soft phosphor glow at the top, a faint structural grid, a slow scan
 * beam drifting down the screen, HUD corner brackets and a light vignette.
 *
 * Every layer is drawn in the theme's accent colour (`colorPrimary`) at low opacity, so it works
 * for all Cyber palettes. The view never handles touches and pauses itself when the window is
 * hidden or animations are disabled system-wide (battery saver / "remove animations").
 */
class CyberBackgroundView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

	private val density = resources.displayMetrics.density
	private val accent = MaterialColors.getColor(context, appcompatR.attr.colorPrimary, Color.GREEN)

	private val glowPaint = Paint()
	private val beamPaint = Paint()
	private val vignettePaint = Paint()
	private val gridPaint = Paint().apply {
		strokeWidth = 1f
		color = withAlpha(GRID_ALPHA)
	}
	private val bracketPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
		strokeWidth = 2f * density
		strokeCap = Paint.Cap.SQUARE
		color = withAlpha(BRACKET_ALPHA)
	}

	private var gridLines = FloatArray(0)
	private var brackets = FloatArray(0)
	private var glowHeight = 0f
	private var beamHalf = 0f
	private var progress = BEAM_START
	private var lastBandTop = 0
	private var lastBandBottom = 0

	private val animator by lazy {
		ValueAnimator.ofFloat(BEAM_START, BEAM_END).apply {
			duration = BEAM_DURATION_MS
			repeatCount = ValueAnimator.INFINITE
			repeatMode = ValueAnimator.RESTART
			interpolator = LinearInterpolator()
			addUpdateListener {
				progress = it.animatedValue as Float
				invalidateBeamBand()
			}
		}
	}

	init {
		setWillNotDraw(false)
		isClickable = false
		isFocusable = false
		importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		val fw = w.toFloat()
		val fh = h.toFloat()

		// 1) Soft phosphor bloom from the top edge.
		glowHeight = fh * GLOW_HEIGHT_FRACTION
		glowPaint.shader = LinearGradient(
			0f, 0f, 0f, glowHeight,
			withAlpha(GLOW_ALPHA), withAlpha(0),
			Shader.TileMode.CLAMP,
		)

		// 2) Faint structural grid, 40dp cells.
		val step = GRID_STEP_DP * density
		val lines = ArrayList<Float>()
		var x = step
		while (x < fw) {
			lines.add(x); lines.add(0f); lines.add(x); lines.add(fh)
			x += step
		}
		var y = step
		while (y < fh) {
			lines.add(0f); lines.add(y); lines.add(fw); lines.add(y)
			y += step
		}
		gridLines = lines.toFloatArray()

		// 3) Scan beam: a soft gradient band, positioned per frame by translating the canvas.
		beamHalf = BEAM_HALF_DP * density
		beamPaint.shader = LinearGradient(
			0f, 0f, 0f, beamHalf * 2f,
			intArrayOf(withAlpha(0), withAlpha(BEAM_ALPHA), withAlpha(0)),
			null,
			Shader.TileMode.CLAMP,
		)

		// 4) HUD corner brackets.
		buildBrackets(fw, fh)

		// 5) Vignette to focus attention toward the centre.
		vignettePaint.shader = RadialGradient(
			fw / 2f, fh / 2f, max(fw, fh) * VIGNETTE_RADIUS_FRACTION,
			intArrayOf(Color.TRANSPARENT, VIGNETTE_COLOR),
			null,
			Shader.TileMode.CLAMP,
		)
	}

	/**
	 * Height (px) of the system bar this view extends behind. The glow, grid and beam start at the very
	 * top of the screen, while the corner brackets stay where they were: below the status bar.
	 */
	var topInset: Int = 0
		set(value) {
			if (field != value) {
				field = value
				if (width > 0 && height > 0) {
					buildBrackets(width.toFloat(), height.toFloat())
					invalidate()
				}
			}
		}

	private fun buildBrackets(fw: Float, fh: Float) {
		val len = BRACKET_LENGTH_DP * density
		val inset = BRACKET_INSET_DP * density
		val top = topInset + inset
		brackets = floatArrayOf(
			// top-left
			inset, top, inset + len, top,
			inset, top, inset, top + len,
			// top-right
			fw - inset, top, fw - inset - len, top,
			fw - inset, top, fw - inset, top + len,
			// bottom-left
			inset, fh - inset, inset + len, fh - inset,
			inset, fh - inset, inset, fh - inset - len,
			// bottom-right
			fw - inset, fh - inset, fw - inset - len, fh - inset,
			fw - inset, fh - inset, fw - inset, fh - inset - len,
		)
	}

	override fun onDraw(canvas: Canvas) {
		val w = width.toFloat()
		val h = height.toFloat()
		canvas.drawRect(0f, 0f, w, glowHeight, glowPaint)
		canvas.drawLines(gridLines, gridPaint)

		val save = canvas.save()
		canvas.translate(0f, h * progress - beamHalf)
		canvas.drawRect(0f, 0f, w, beamHalf * 2f, beamPaint)
		canvas.restoreToCount(save)

		canvas.drawLines(brackets, bracketPaint)
		canvas.drawRect(0f, 0f, w, h, vignettePaint)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		updateAnimation()
	}

	override fun onDetachedFromWindow() {
		animator.cancel()
		super.onDetachedFromWindow()
	}

	override fun onWindowVisibilityChanged(visibility: Int) {
		super.onWindowVisibilityChanged(visibility)
		updateAnimation()
	}

	override fun onVisibilityChanged(changedView: View, visibility: Int) {
		super.onVisibilityChanged(changedView, visibility)
		updateAnimation()
	}

	private fun updateAnimation() {
		val shouldRun = isAttachedToWindow &&
			windowVisibility == VISIBLE &&
			visibility == VISIBLE &&
			ValueAnimator.areAnimatorsEnabled()
		if (shouldRun) {
			if (!animator.isStarted) {
				animator.start()
			}
		} else {
			animator.cancel()
		}
	}

	/**
	 * Only the strip the beam moved through needs repainting each frame; everything else in this
	 * view is static, so invalidate just the union of the previous and the new band.
	 */
	private fun invalidateBeamBand() {
		val h = height
		if (h == 0) {
			return
		}
		val center = h * progress
		val top = (center - beamHalf).toInt().coerceAtLeast(0)
		val bottom = (center + beamHalf).toInt().coerceAtMost(h)
		val invalidateTop = minOf(top, lastBandTop)
		val invalidateBottom = maxOf(bottom, lastBandBottom)
		lastBandTop = top
		lastBandBottom = bottom
		if (invalidateBottom > invalidateTop) {
			invalidate(0, invalidateTop, width, invalidateBottom)
		}
	}

	private fun withAlpha(alpha: Int): Int = ColorUtils.setAlphaComponent(accent, alpha)

	private companion object {

		const val BEAM_START = -0.15f
		const val BEAM_END = 1.15f
		const val BEAM_DURATION_MS = 6000L
		const val BEAM_HALF_DP = 90f
		const val GRID_STEP_DP = 40f
		const val BRACKET_LENGTH_DP = 22f
		const val BRACKET_INSET_DP = 10f
		const val GLOW_HEIGHT_FRACTION = 0.55f
		const val VIGNETTE_RADIUS_FRACTION = 0.85f

		// 0..255 alphas, matching SQL Reader's CyberGridLine / CyberGlowTop / CyberScanBeam / CyberCornerBracket
		const val GRID_ALPHA = 0x0F
		const val GLOW_ALPHA = 0x26
		const val BEAM_ALPHA = 0x33
		const val BRACKET_ALPHA = 0x80

		// Softer than SQL Reader's 0xE6 so toolbar / bottom-bar icons in the corners stay legible.
		const val VIGNETTE_COLOR = 0x66000000
	}
}
