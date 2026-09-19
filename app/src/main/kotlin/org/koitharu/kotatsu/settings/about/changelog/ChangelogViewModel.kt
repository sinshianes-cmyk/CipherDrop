package org.koitharu.kotatsu.settings.about.changelog

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import org.jsoup.internal.StringUtil
import org.koitharu.kotatsu.core.github.AppUpdateRepository
import org.koitharu.kotatsu.core.github.AppVersion
import org.koitharu.kotatsu.core.ui.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ChangelogViewModel @Inject constructor(
	private val appUpdateRepository: AppUpdateRepository,
) : BaseViewModel() {

	val changelog = MutableStateFlow<String?>(null)

	init {
		launchLoadingJob(Dispatchers.Default) {
			val remoteVersions = appUpdateRepository.getAvailableVersions()
			// Prepend the local 0.9.7 entry so it always appears even before
			// a GitHub release is published.
			val allVersions = buildList {
				if (remoteVersions.none { it.name == LOCAL_097.name }) {
					add(LOCAL_097)
				}
				addAll(remoteVersions)
			}
			val stringJoiner = StringUtil.StringJoiner("\n\n\n")
			for (version in allVersions) {
				stringJoiner.add(version.description.formatChangelogDescription())
			}
			changelog.value = stringJoiner.complete()
		}
	}

	private fun String.formatChangelogDescription(): String {
		var text = replace(Regex("(?<!\\n)\\nIf this is your first"), "\n\nIf this is your first")
		// installation instructions belong after the changelog, not before it
		installationSection.find(text)?.let { match ->
			text = text.removeRange(match.range).trimEnd() + "\n\n\n" + match.value.trim()
		}
		return text.tidyInstructions()
	}

	private fun String.tidyInstructions(): String = replace(listItem) { match ->
		match.groupValues[1] + match.groupValues[2].uppercase()
	}.replace(Regex("\"\\s*$", RegexOption.MULTILINE), "")

	private companion object {

		val installationSection = Regex(
			"^#{1,6}\\s*Installation Instructions\\b.*?(?=^#{1,6}\\s|\\z)",
			setOf(RegexOption.MULTILINE, RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE),
		)

		val listItem = Regex("^(\\s*(?:\\d+[.)]|[-*+])\\s+)([a-z])", RegexOption.MULTILINE)

		/** Hard-coded 0.9.7 changelog entry — always shown at the top of the list. */
		val LOCAL_097 = AppVersion(
			id = 97000,
			name = "0.9.7",
			url = "",
			apkSize = 0L,
			apkUrl = "",
			description = """
## CipherDrop 0.9.7

### Bug Fixes
- Fixed crash on MIUI devices when opening overflow menus — popup windows with theme attributes now inflate safely without a theme context
- Resolved `CutCornerDrawable` `UnsupportedOperationException` when `theme=null` is passed during drawable inflation

### Changes
- App rebranded to **CipherDrop** with a new cyber-themed icon
- New **CD** lettermark icon with neon cyan glyph and purple glitch shadow on dark grid background
- Application ID updated to `org.haziffe.dropsauce2` to allow side-by-side installation with the previous version
- Version code bumped to 73
			""".trimIndent(),
		)
	}
}
