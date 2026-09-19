package org.koitharu.kotatsu.settings.compose

import android.content.res.Configuration
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koitharu.kotatsu.main.ui.nav.composeColorSchemeFromTheme

/**
 * Cyber Terminal typography: monospace everywhere, bold titles, sizes and letter spacing taken
 * from SQL Reader's `CyberTypography`. Keeps the XML `TextAppearance.Kotatsu.*` styles and the
 * Compose screens visually identical.
 */
private fun cyberTypography(): Typography {
	val mono = FontFamily.Monospace
	val noPadding = PlatformTextStyle(includeFontPadding = false)
	fun style(weight: FontWeight, size: Int, line: Int, spacing: Double) = TextStyle(
		fontFamily = mono,
		fontWeight = weight,
		fontSize = size.sp,
		lineHeight = line.sp,
		letterSpacing = spacing.sp,
		platformStyle = noPadding,
	)
	return Typography(
		displayLarge = style(FontWeight.Bold, 34, 42, 1.0),
		displayMedium = style(FontWeight.Bold, 30, 38, 1.0),
		displaySmall = style(FontWeight.Bold, 26, 32, 1.0),
		headlineLarge = style(FontWeight.Bold, 26, 32, 0.8),
		headlineMedium = style(FontWeight.Bold, 22, 28, 0.7),
		headlineSmall = style(FontWeight.Bold, 20, 26, 0.6),
		titleLarge = style(FontWeight.Bold, 18, 24, 0.5),
		titleMedium = style(FontWeight.SemiBold, 15, 20, 0.5),
		titleSmall = style(FontWeight.SemiBold, 13, 18, 0.4),
		bodyLarge = style(FontWeight.Normal, 14, 20, 0.25),
		bodyMedium = style(FontWeight.Normal, 12, 16, 0.25),
		bodySmall = style(FontWeight.Normal, 11, 15, 0.25),
		labelLarge = style(FontWeight.Bold, 13, 18, 0.6),
		labelMedium = style(FontWeight.Bold, 11, 15, 0.75),
		labelSmall = style(FontWeight.Bold, 10, 14, 1.0),
	)
}

/** HUD silhouette shared with the XML theme: top-start and bottom-end corners are cut. */
private fun cyberShapes(): Shapes = Shapes(
	extraSmall = CutCornerShape(2.dp),
	small = CutCornerShape(4.dp),
	medium = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
	large = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp),
	// Windows (dialogs, sheets): all four corners cut, like the "Last position" pill.
	extraLarge = CutCornerShape(20.dp),
)

/**
 * MaterialTheme wrapper that pulls colors from the host Android theme (the active Cyber accent
 * palette) and applies the cyber typography and shapes. Use this at the top of any Compose
 * subtree we host inside an existing Fragment/Activity.
 */
@Composable
fun DropSauceTheme(content: @Composable () -> Unit) {
	val ctx = LocalContext.current
	val isDark = (LocalConfiguration.current.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
		Configuration.UI_MODE_NIGHT_YES
	val scheme = remember(ctx, isDark) { composeColorSchemeFromTheme(ctx, isDark) }
	val typography = remember { cyberTypography() }
	val shapes = remember { cyberShapes() }
	MaterialTheme(colorScheme = scheme, typography = typography, shapes = shapes, content = content)
}
