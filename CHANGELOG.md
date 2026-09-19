# DropSauce 0.9.6 Cyber – Changelog

Build: `DropSauce-0_9_6-cyber-history-fix4`

> The fixes below were made without a device to test on. Please check them on your phone.

---

## New theme: Cyber Terminal

DropSauce now ships a single look, the "cyber hacker" theme ported from SQL Reader, with selectable accent colours.

### Accent colours
Seven accents, generated per colour in `res/values/colors_cyber.xml`. The theme is dark only.

| Accent | Notes |
|---|---|
| Green | Default |
| Blue | |
| Cyan | |
| White | |
| Amber | |
| Red | |
| Purple | |

A saved legacy colour scheme (Totoro, Miku, Expressive, …) falls back to Cyber Green. The old palettes can no longer be selected.

### Look and feel
- **Typography:** every text style is monospace, with bold titles and letter-spacing taken from SQL Reader. The Compose screens (`SettingsTheme.kt`) use the same scale.
- **Shapes:** cut-corner shapes and outlined cards, defined in `res/values/themes_cyber.xml`.
- **Animated background** (`CyberBackgroundView`): glow, a 40dp grid, a scan beam, corner brackets and a vignette. It is shown on every screen except the reader and translucent dialog hosts.
- **Contrast targets:** body text 7:1 or better, secondary text 4.8:1 or better, outlines 3.2:1 or better.

### Settings and onboarding
- Appearance now has the accent picker and a new **Animated grid background** switch.
- The Light / Dark / System and AMOLED options are removed, since the theme is dark only.
- The onboarding card for theme mode and AMOLED is removed. The accent picker stays.
- `composeColorSchemeFromTheme` now also maps background, error, surfaceVariant and the low containers.

### Adding an accent
Add a tuple to `VARIANTS` in `tools/gen_cyber_palettes.py`, run the script, paste the output into `colors_cyber.xml`, `themes_cyber.xml` and `strings.xml`, then add an entry to `ColorScheme`.

---

## Reader

### Fixed: pieces of a later page showing under the current page
Reading page 9 could show a part of page 13 or 16 at the bottom until you scrolled away and back.

- **Cause:** a recycled page holder could receive the result of an old load job and show the old page in its new slot.
- **Fix:** `PageViewModel` now ignores load results from an earlier bind, cancels the old job before starting a new one, and publishes results on the main thread. `BasePageHolder.bind` clears the image when the holder is given a different page.
- `tools/reader-race-repro` replays the scenario on a plain JVM. It was not run on a device.

### Fixed: chapter list hidden under the navigation bar
The reader's chapter menu ran under the phone's navigation bar, so the last chapters (for example 107–111) could not be reached. All changes are in `ChaptersPagesSheet.kt`.

- **Hidden bars:** the reader hides the system bars, and a hidden bar reports no size. The sheet now uses the navigation bar's real height, so the list gets a proper bottom margin.
- **Off-screen part of the sheet:** the sheet is always as tall as the screen, so in the half-open position its lower part hangs below the visible area. The list is now padded by exactly that hidden part.
- **Late-loading pages:** the Chapters, Pages and Bookmarks pages are created after the sheet's first layout, so each page now asks for the window insets again once it is on screen.

### Changed: the reader's chapter menu opens fully
- It opens straight to the top and skips the half-open position. Swiping down closes it directly.
- List swipes stay with the list and only the header and toolbar move the sheet, which removes the scroll fighting seen in the half-open state.
- The book details screen still opens its chapter menu half-way.

---

## Whole app

### Fixed: the app was drawn behind the status bar and navigation bar
Android forces edge-to-edge drawing, so every screen ran from the very top to the very bottom of the display. The change is in `BaseActivity.kt`.

- The content area is padded by the status bar, navigation bar and camera cutout. Screens inside get no bar insets, so they lay out as before, now inside the safe area.
- The animated Cyber background and its corner brackets are drawn inside the app area, not behind the bars.
- The reader stays fullscreen.
- While the keyboard is open the bottom padding is dropped so the keyboard height is not counted twice.
- If **Hide status bar** is on, its space stays reserved, as before.
- The book details screen also starts below the status bar now.
- Widget config activities and the crash dialog do not extend `BaseActivity` and are unchanged.

---

## Build notes

- Compiling needs the **Android 17.0 (API 37.0)** SDK platform. In Android Studio open Settings → Languages & Frameworks → Android SDK → SDK Platforms, tick "Android 17.0 (CinnamonBun) 37.0" and sync. Do not pick 37.1 or 37.2.
- Gradle 9.6.1, Android Gradle Plugin 9.2.1, Kotlin 2.3.21.
