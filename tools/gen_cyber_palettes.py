"""Generates cyber palettes (colors + theme overlays) for DropSauce."""
import colorsys

def hx(rgb):
    return "#%02X%02X%02X" % tuple(max(0, min(255, int(round(c)))) for c in rgb)

def mix(a, b, t):
    return tuple(a[i] * (1 - t) + b[i] * t for i in range(3))

def scale(c, k):
    return tuple(min(255, v * k) for v in c)

def lum(c):
    def f(v):
        v /= 255
        return v / 12.92 if v <= 0.03928 else ((v + 0.055) / 1.055) ** 2.4
    r, g, b = (f(x) for x in c)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b

def contrast(a, b):
    la, lb = lum(a), lum(b)
    if la < lb:
        la, lb = lb, la
    return (la + 0.05) / (lb + 0.05)

# name, display, accent, tertiary
VARIANTS = [
    ("green",  "Phosphor Green", (0x00, 0xFF, 0x66), (0xFF, 0xCC, 0x00)),
    ("blue",   "Neon Blue",      (0x2E, 0xA8, 0xFF), (0xFF, 0xCC, 0x00)),
    ("cyan",   "Ice Cyan",       (0x00, 0xE5, 0xFF), (0xFF, 0x4F, 0xD8)),
    ("white",  "Ghost White",    (0xEE, 0xF3, 0xF5), (0x00, 0xE5, 0xFF)),
    ("amber",  "Amber Terminal", (0xFF, 0xB0, 0x00), (0x00, 0xE5, 0xFF)),
    ("red",    "Crimson Alert",  (0xFF, 0x3B, 0x3B), (0xFF, 0xCC, 0x00)),
    ("purple", "Violet Net",     (0xB3, 0x6B, 0xFF), (0x00, 0xFF, 0x66)),
]

ERROR = (0xFF, 0x33, 0x33)
BLACK = (0, 0, 0)

WHITE = (255, 255, 255)

def ensure(c, bg, target):
    """Lighten c toward white until it reaches the contrast target against bg."""
    t = 0.0
    while contrast(mix(c, WHITE, t), bg) < target and t < 1.0:
        t += 0.02
    return mix(c, WHITE, t)

def palette(accent, tertiary):
    bg = tuple(5 + accent[i] * 0.025 for i in range(3))
    surface = tuple(9 + accent[i] * 0.05 for i in range(3))
    def container(t):
        return tuple(surface[i] + accent[i] * t for i in range(3))
    p = {}
    p["primary"] = accent
    p["onPrimary"] = bg
    p["primaryContainer"] = scale(accent, 0.20)
    p["onPrimaryContainer"] = accent
    p["secondary"] = scale(accent, 0.62)
    p["onSecondary"] = bg
    p["secondaryContainer"] = scale(accent, 0.16)
    p["onSecondaryContainer"] = accent
    p["tertiary"] = tertiary
    p["onTertiary"] = bg
    p["tertiaryContainer"] = scale(tertiary, 0.20)
    p["onTertiaryContainer"] = tertiary
    p["error"] = ERROR
    p["onError"] = bg
    p["errorContainer"] = (0x3A, 0x0A, 0x0A)
    p["onErrorContainer"] = (0xFF, 0x9A, 0x9A)
    p["background"] = bg
    p["onBackground"] = ensure(accent, surface, 7.0)
    p["surface"] = surface
    p["onSurface"] = ensure(accent, surface, 7.0)
    p["surfaceVariant"] = scale(accent, 0.14)
    p["onSurfaceVariant"] = ensure(scale(accent, 0.72), surface, 4.8)
    p["outline"] = ensure(scale(accent, 0.56), bg, 3.2)
    p["outlineVariant"] = scale(accent, 0.20)
    p["inverseSurface"] = scale(accent, 0.92)
    p["inverseOnSurface"] = bg
    p["inversePrimary"] = scale(accent, 0.56)
    p["primaryFixed"] = accent
    p["onPrimaryFixed"] = bg
    p["primaryFixedDim"] = scale(accent, 0.72)
    p["onPrimaryFixedVariant"] = scale(accent, 0.20)
    p["secondaryFixed"] = scale(accent, 0.85)
    p["onSecondaryFixed"] = bg
    p["secondaryFixedDim"] = scale(accent, 0.62)
    p["onSecondaryFixedVariant"] = scale(accent, 0.16)
    p["tertiaryFixed"] = tertiary
    p["onTertiaryFixed"] = bg
    p["tertiaryFixedDim"] = scale(tertiary, 0.72)
    p["onTertiaryFixedVariant"] = scale(tertiary, 0.20)
    p["surfaceDim"] = mix(bg, BLACK, 0.4)
    p["surfaceBright"] = container(0.10)
    p["surfaceContainerLowest"] = mix(bg, BLACK, 0.5)
    p["surfaceContainerLow"] = bg
    p["surfaceContainer"] = surface
    p["surfaceContainerHigh"] = container(0.04)
    p["surfaceContainerHighest"] = container(0.08)
    return p

ORDER = ["primary","onPrimary","primaryContainer","onPrimaryContainer","secondary","onSecondary",
         "secondaryContainer","onSecondaryContainer","tertiary","onTertiary","tertiaryContainer",
         "onTertiaryContainer","error","onError","errorContainer","onErrorContainer","background",
         "onBackground","surface","onSurface","surfaceVariant","onSurfaceVariant","outline",
         "outlineVariant","inverseSurface","inverseOnSurface","inversePrimary","primaryFixed",
         "onPrimaryFixed","primaryFixedDim","onPrimaryFixedVariant","secondaryFixed","onSecondaryFixed",
         "secondaryFixedDim","onSecondaryFixedVariant","tertiaryFixed","onTertiaryFixed",
         "tertiaryFixedDim","onTertiaryFixedVariant","surfaceDim","surfaceBright",
         "surfaceContainerLowest","surfaceContainerLow","surfaceContainer","surfaceContainerHigh",
         "surfaceContainerHighest"]

# theme attr for each role (matches Totoro overlay attrs)
ATTR = {
    "background": "android:colorBackground",
    "inverseSurface": "colorSurfaceInverse",
    "inverseOnSurface": "colorOnSurfaceInverse",
    "inversePrimary": "colorPrimaryInverse",
}
def attr(role):
    if role in ATTR:
        return ATTR[role]
    return "color" + role[0].upper() + role[1:]

def main(out_colors, out_styles, out_strings):
    colors = ['<?xml version="1.0" encoding="utf-8"?>', '<!-- GENERATED: cyber terminal palettes. -->', '<resources>']
    styles = ['<?xml version="1.0" encoding="utf-8"?>',
              '<resources xmlns:tools="http://schemas.android.com/tools">', '']
    strings = []
    report = []
    for key, title, accent, tert in VARIANTS:
        p = palette(accent, tert)
        colors.append(f'\t<!-- {title} -->')
        for role in ORDER:
            colors.append(f'\t<color name="cyber_{key}_{role}">{hx(p[role])}</color>')
        styles.append(f'\t<!-- {title} -->')
        styles.append(f'\t<style name="ThemeOverlay.Kotatsu.Cyber.{key.capitalize()}" parent="ThemeOverlay.Kotatsu.Cyber">')
        for role in ORDER:
            styles.append(f'\t\t<item name="{attr(role)}">@color/cyber_{key}_{role}</item>')
        # window / system bars follow the deep background
        styles.append(f'\t\t<item name="android:windowBackground">@color/cyber_{key}_background</item>')
        styles.append(f'\t\t<item name="android:navigationBarColor">@android:color/transparent</item>')
        styles.append('\t</style>')
        styles.append('')
        strings.append(f'    <string name="theme_name_cyber_{key}">{title}</string>')
        report.append((key,
            contrast(p["onSurface"], p["surface"]),
            contrast(p["onSurfaceVariant"], p["surface"]),
            contrast(p["onPrimary"], p["primary"]),
            contrast(p["outline"], p["background"])))
    colors.append('</resources>')
    styles.append('</resources>')
    open(out_colors, "w").write("\n".join(colors) + "\n")
    open(out_styles, "w").write("\n".join(styles) + "\n")
    open(out_strings, "w").write("\n".join(strings) + "\n")
    print("variant  onSurface  onSurfVar  onPrimary/primary  outline/bg")
    for r in report:
        print("%-7s  %8.1f  %9.1f  %17.1f  %10.1f" % r)

if __name__ == "__main__":
    import sys
    main(*sys.argv[1:4])
