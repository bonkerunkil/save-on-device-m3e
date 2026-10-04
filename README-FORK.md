# Save On Device — Material 3 Expressive fork

A fork of **Save On Device**, rebuilt around **Material 3 Expressive** with a modern save
composer, pinned folders and a credits screen.

---

## Credits

### Original app

**Save On Device** — created by **Landan Jackson** (GitHub: [lmj0011](https://github.com/lmj0011/save-on-device)).

The original app is a small share-target utility that saves files from other apps to local storage,
plus copied text. Licensed under the **Apache License 2.0** (© 2023 Landan Jackson).

- Source: https://github.com/lmj0011/save-on-device
- F-Droid: https://f-droid.org/packages/name.lmj001.savetodevice/
- Google Play: https://play.google.com/store/apps/details?id=name.lmj001.savetodevice

### This fork

**Fork developer: BonkerUnkil (Bonki)**

- Telegram: https://t.me/BonkerUnkilBonki
- GitHub: https://github.com/BonkerUnkilBonki

Maintained independently. The fork keeps the original Apache 2.0 licence and credits above.

---

## What this fork adds

- **Material 3 Expressive design** — `Theme.Material3Expressive.*` with runtime dynamic colour,
  replacing the original plain `Activity` with hard-coded colours.
- **A Save file composer** — a dark, rounded quick-capture panel that shows the file name (editable),
  the exact destination folder, a *Change location* button and quick-location buttons, so the
  destination is obvious *before* the file is saved.
- **Pinned folders** — pin any folder from inside the app and it appears as a one-tap button in the
  composer, next to Downloads, Documents, Pictures, Movies and Music. Pin and unpin from the Save tab.
- **Bottom navigation** — Save, Locations and About tabs.
- **Opens over the source app** — sharing shows the composer over the app you shared from (a
  transparent, animation-less share activity) instead of switching you into a full-screen app.
- **Saved-location history** — every folder a file was saved into, with a file count and last-used
  date, plus per-item delete and clear-all.
- **Credits screen** — the About tab credits both the original developer and this fork.
- **Compact, one-hand-friendly layout** — smaller controls and tighter spacing than the first build.

### How saving works

- Quick buttons and the default destination write through **MediaStore** on Android 10+, so no
  storage permission is needed and files land in the real public folders.
- *Change location* and pinned folders write through the **Storage Access Framework** with a
  persisted tree grant, so they work on every supported version.

---

## Building

Requires JDK 17 and Android SDK 35.

```bash
./gradlew assembleDebug        # APK at app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease      # unsigned release build (add your own signing config)
```

If you are behind a proxy, add `systemProp.http.proxyHost` / `systemProp.https.proxyHost` entries to
`gradle.properties` (Java does not read `http_proxy` environment variables).

## Project layout

| Path | Purpose |
| --- | --- |
| `MainActivity` | Launcher: bottom navigation (Save / Locations / About) |
| `ShareActivity` | Share target: transparent window hosting the composer |
| `SaveFileSheet` | The Save file composer bottom sheet |
| `HomeFragment` | Saving behaviour + pinned folders |
| `LocationsFragment` | Saved-location history |
| `AboutFragment` | Credits |
| `Destination` / `DestinationStore` | Destination model, current + pinned persistence |
| `FileSaver` | MediaStore and SAF writers |
| `DocumentUtils` | Turns SAF URIs into friendly folder / storage names |

## Licence

Apache License 2.0, inherited from upstream. Original work © 2023 Landan Jackson.
Fork © BonkerUnkil (Bonki).

## Changes in 1.3.0

- **Google Sans Flex applied app-wide** — the supplied `Regular.ttf` (Google Sans Flex, a variable
  font) was instanced to a static font and wired in as the app typeface for every text role, so
  Material components (buttons, chips, text fields, navigation labels) all pick it up too.
- **Telegram and GitHub logos** on the credit buttons in the About tab.
- **About page text no longer clipped** — the credit buttons now size to their content instead of a
  fixed height, so long labels wrap instead of overflowing the button.
- **Quick-location buttons** stay in a single sideways-scrolling row, so the composer keeps its
  short height and the Save button stays within one-handed reach.
- **Long-press to reorder pinned folders** — drag a pinned folder up or down in the Save tab to
  change the order; the order is saved and used for the quick buttons in the composer.

### Font note

The bundled typeface is Google Sans Flex. It is Google's brand typeface — check its licence terms
before publishing this build publicly, and swap in a licensed font if you need to.

### Reordering

Pinned folders are listed on the Save tab. Long-press a row (or drag its handle) to move it, and the
new order is persisted immediately on drop.

## Changes in 1.4.0

- **Drag to reorder the quick buttons in the Save file panel.** Long-press any quick-location
  button and drag it left or right to move it. This covers the built-in folders (Downloads,
  Documents, Pictures, Movies, Music) as well as pinned folders, so the row can be arranged however
  you like. The order is saved immediately and reused for every future save.

The quick buttons live in a horizontal list; the built-in folders and pinned folders share one
order, stored under `quick_order` in the app's preferences. Newly pinned folders are appended to the
end until you move them.

## Changes in 1.4.1

- **Panel reordered for one-handed use.** The destination controls — *Change location* and the
  quick-location buttons — now sit at the bottom of the panel, directly above Cancel / Save, so
  everything you reach for with your thumb is in one place. The current destination stays at the top
  as the headline, with the storage line tucked under it.

## Changes in 1.5.0

- **Change location moved into the bottom row**, in the space to the left of Cancel, as a compact
  text button with a small folder icon. The bottom band is now: *Change location* · *Cancel* ·
  *Save*, with the quick-location buttons sitting directly above it — everything the thumb needs in
  one place.

## Changes in 1.6.0

- **Many files at once in a single panel.** Sharing several files no longer opens one popup per
  file. The composer shows a summary of the first file plus a count — `icon.png  +49` — and tapping
  it expands the full list inside the same panel.
- **The file list scrolls inside a bounded area** (capped at 150dp), so the panel never grows
  however many files are shared.
- **Select and deselect freely.** Every row has a checkbox, with *Select all* / *Clear all* and an
  "n of m selected" counter. Save writes only the selected files, all to the one destination.
- The old "save multiple files individually" switch was removed, since it existed only to produce
  the one-popup-per-file behaviour.

## Changes in 1.7.0

A new **Appearance** tab (fourth tab, palette icon) with three controls:

- **Theme** — System, Light, Dark or **Pitch black**. Pitch black forces a true `#000000` background
  (AMOLED-friendly) on the screens, cards, toolbar and bottom bar, on top of the dark theme.
- **Accent colour** — eight presets (blue, green, teal, purple, pink, orange, red, yellow) plus a
  **custom hex** field (`#RRGGBB`). The accent is applied to the save button, folder icons, folder
  buttons, switches, chips, checkboxes, the bottom bar and section headings.
- **Rounder corners** — a toggle that turns buttons and folder buttons into pills and softens cards
  from squarish to fully rounded.

### How theming works

Layouts opt into theming with `android:tag` values (`surface_root`, `surface_card`, `accent_bg`,
`accent_fg`, `accent_text`, `accent_icon`, `accent_chip`, `accent_switch`, `accent_nav`,
`accent_check`). `Themer` walks the inflated view tree and applies the saved settings, so nothing is
guessed from view types. Changing any appearance setting recreates the screen immediately.

System dynamic colour is still used as the default accent until you pick one; choosing an accent
overrides it.

## Changes in 1.7.1

- **Fixed: pitch black no longer hides the app you shared from.** The 1.7.0 build tinted the bottom
  sheet's *container*, which painted the whole dialog window black and covered the app behind it.
  Pitch black now styles only the panel itself — the sheet's content view carries a
  `surface_sheet` tag and gets a rounded, pure-black background of its own, so the rounded top
  corners are preserved and everything behind the panel stays visible.
- The share window also explicitly forces a transparent background, so no theme mode can make it
  opaque.
