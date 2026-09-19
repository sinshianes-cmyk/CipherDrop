# Cyber Terminal theme (ported from SQL Reader)

DropSauce now ships one look: the SQL Reader "cyber hacker" theme, with selectable accent colours.

## What changed

| Area | Change |
|---|---|
| Colour schemes | `ColorScheme` now has 7 accents: `CYBER_GREEN` (default), `CYBER_BLUE`, `CYBER_CYAN`, `CYBER_WHITE`, `CYBER_AMBER`, `CYBER_RED`, `CYBER_PURPLE`. Palettes are generated per accent (dark only) in `res/values/colors_cyber.xml`. |
| Theme overlays | `res/values/themes_cyber.xml`: `ThemeOverlay.Kotatsu.Cyber` (shared base: monospace, cut-corner shapes, outlined cards) + one `ThemeOverlay.Kotatsu.Cyber.<Accent>` per palette. |
| Typography | All `TextAppearance.Kotatsu.*` styles are monospace, bold titles, sizes/letter-spacing from SQL Reader's `CyberTypography`. Compose (`SettingsTheme.kt`) uses the same scale. |
| Animated background | `core/ui/widgets/CyberBackgroundView.kt` (glow, 40dp grid, scan beam, corner brackets, vignette). Attached by `BaseActivity` on every screen except the reader and translucent dialog hosts. |
| Settings | Appearance: accent picker + new "Animated grid background" switch. The Light/Dark/System and AMOLED options are gone (the theme is dark-only; `AppSettings.theme` always returns night). |
| Onboarding | The theme-mode / AMOLED card was removed; the accent picker remains. |
| Compose bridge | `composeColorSchemeFromTheme` also maps background, error, surfaceVariant and low containers. |

Legacy palettes (Totoro, Miku, Expressive, ...) are no longer selectable; a saved legacy value falls
back to `CYBER_GREEN`. Their XML is untouched, so release builds simply shrink it away.

## Tuning

* Beam / grid / bracket opacity and speed: constants at the bottom of `CyberBackgroundView.kt`.
* Add an accent: add a tuple to `VARIANTS` in `tools/gen_cyber_palettes.py`, run
  `python3 tools/gen_cyber_palettes.py colors.xml styles.xml strings.txt`, paste the output into
  `colors_cyber.xml` / `themes_cyber.xml` / `strings.xml` (the base overlay stays in `themes_cyber.xml`),
  then add an entry to `ColorScheme`.
* Accent contrast targets: body text >= 7:1, secondary text >= 4.8:1, outlines >= 3.2:1 on the surface.
