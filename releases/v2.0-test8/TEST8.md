# Vibex 2.0-test8

**Version code 10 · package `dev.viswesh.vibex.testing` · same permanent testing signature as test7**

Test8 builds on test7 and adds one UI change: a third **player style**, **Ember**, selectable at Settings → Look & feel → Player style next to Classic and Aurora glass. The app remains fully login-free.

## What changed (UI)

| File | Change |
|---|---|
| `music-app/src/v2/SettingsView.jsx` | Player-style picker gains `Ember (test8)` option |
| `music-app/src/v2/Player.jsx` | Renders an `.ember-bg` artwork wash when `playerStyle === "ember"` (same artwork source as Aurora) |
| `music-app/src/v2/theme.css` | New `[data-style="ember"]` block: warm artwork-tinted gradient wash (no heavy blur — cheap on low-end GPUs), warm solid cards, glowing artwork rim |
| `music-app/src/v2/core.js` | `cleanSettings` accepts `playerStyle: "ember"`; default stays `classic` (no surprise retheme for existing users) |
| `music-app/android/app/build.gradle` | `versionCode 9 → 10`, `versionNameSuffix "-test7" → "-test8"` |
| `music-app/tests/test8.test.mjs` | 4 new `node:test` cases: default is classic, all three styles round-trip, unknown styles fall back to classic, legacy settings default to classic |

Behavior notes:

- Classic and Aurora glass are untouched; Ember is purely additive.
- Ember uses the track's 500x500 artwork as a tinted background with a warm gradient overlay; in dark theme the wash is dimmed further. Unlike Aurora it does **not** use `backdrop-filter` blur, so it stays smooth on budget devices.
- The setting persists in local settings and survives restarts; invalid/legacy values fall back to Classic.

## Carried over from test7

- Audio source setting (Auto / JioSaavn only / YouTube first with Saavn rescue)
- JioSaavn → YouTube confidence-gated stream takeover ("via YouTube" pill)
- Lyrics chain: local LRC → LRCLIB Exact → LRCLIB Search → KuGou, singer-first matching
- Aurora glass player style, Classic default
- Permanent testing keystore — installs in place over test7 (and any build since test7)
- No login, no API keys, anywhere

## Testing

- 63 JS unit tests (`node --test tests/*.test.mjs`), including the 4 new test8 cases — **63/63 pass** (run on the reconstructed test7 tree + test8 patch)
- Changed JSX (`Player.jsx`, `SettingsView.jsx`) compiles cleanly via esbuild
- Playwright e2e and JVM YtMatcher tests unchanged from test7 (all passing on record there)
- **APK binary not built in this environment** (needs JDK 21 + Android SDK 35) — build with the recipe in RELEASE-NOTES.md; it is auto-signed with the committed `vibex-testing.keystore`
- On-device check once built: play a song → open full player → Settings → Look & feel → Player style → Ember; the player page should show the warm artwork wash

## Signing

Same permanent testing keystore as test7 (`vibex-testing.keystore`, certificate SHA-256 `e626ed74a9ba74f0ed1cb6f726547264c8a8c0ada88f57cf596404d6be94ee13`, alias/password `vibex-testing`). A test8 APK installs **straight over test7** — no uninstall. Testing key only; its password is public by design; never use it for a store release.
