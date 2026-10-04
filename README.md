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
