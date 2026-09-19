package org.koitharu.kotatsu.settings.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Per-position HUD corners — 12dp cut on the outer edges, square internal seams. */
fun groupItemShape(index: Int, total: Int): Shape = when {
	total <= 1 -> CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp)
	index == 0 -> CutCornerShape(topStart = 12.dp)
	index == total - 1 -> CutCornerShape(bottomEnd = 12.dp)
	else -> CutCornerShape(0.dp)
}

data class GroupItemPosition(val index: Int, val total: Int) {
	val shape: Shape get() = groupItemShape(index, total)
}

class SettingsGroupScope {
	internal val items = mutableListOf<@Composable (GroupItemPosition) -> Unit>()
	fun item(content: @Composable (GroupItemPosition) -> Unit) {
		items += content
	}
}

/**
 * Visual container for a stack of settings rows. Children render via [SettingsGroupScope.item]
 * and receive their position so they can pick the right shape. 2dp gaps between items keep the
 * grouped feel without breaking the visual block.
 */
@Composable
fun SettingsGroup(
	modifier: Modifier = Modifier,
	title: String? = null,
	content: SettingsGroupScope.() -> Unit,
) {
	val scope = SettingsGroupScope()
	scope.content()
	Column(modifier = modifier) {
		if (title != null) {
			Text(
				text = title.uppercase(),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.primary,
				// Aligned with the title text of icon-less setting rows (12dp card padding)
				modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 8.dp),
			)
		}
		val total = scope.items.size
		scope.items.forEachIndexed { i, render ->
			render(GroupItemPosition(index = i, total = total))
			if (i < total - 1) Spacer(Modifier.height(2.dp))
		}
	}
}
