package org.koitharu.kotatsu.core.prefs

import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import org.koitharu.kotatsu.R

/**
 * Accent palettes of the Cyber Terminal theme. Every entry shares the same monospace / HUD look
 * (see ThemeOverlay.Kotatsu.Cyber) and only swaps the accent colour.
 *
 * Previously saved values from the removed legacy palettes no longer match an entry, so
 * [AppSettings.colorScheme] silently falls back to [default].
 */
@Keep
enum class ColorScheme(
	@StyleRes val styleResId: Int,
	@StringRes val titleResId: Int,
) {

	CYBER_GREEN(R.style.ThemeOverlay_Kotatsu_Cyber_Green, R.string.theme_name_cyber_green),
	CYBER_BLUE(R.style.ThemeOverlay_Kotatsu_Cyber_Blue, R.string.theme_name_cyber_blue),
	CYBER_CYAN(R.style.ThemeOverlay_Kotatsu_Cyber_Cyan, R.string.theme_name_cyber_cyan),
	CYBER_WHITE(R.style.ThemeOverlay_Kotatsu_Cyber_White, R.string.theme_name_cyber_white),
	CYBER_AMBER(R.style.ThemeOverlay_Kotatsu_Cyber_Amber, R.string.theme_name_cyber_amber),
	CYBER_RED(R.style.ThemeOverlay_Kotatsu_Cyber_Red, R.string.theme_name_cyber_red),
	CYBER_PURPLE(R.style.ThemeOverlay_Kotatsu_Cyber_Purple, R.string.theme_name_cyber_purple),
	;

	companion object {

		val default: ColorScheme
			get() = CYBER_GREEN

		fun getAvailableList(): List<ColorScheme> = entries.toList()
	}
}
