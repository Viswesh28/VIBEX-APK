# Vibex 2.0-test8

**Testing prerelease · 1 November 2026 · supersedes test7 as the latest source state.**

One change over test7, cumulative: a new **Ember player style** (Settings → Look & feel → Player style), a fresh UI theme on the v2 player — warm artwork-tinted wash without heavy blur, warm cards, glowing artwork rim. Classic and Aurora glass unchanged, default stays Classic. Version code **10**, package `dev.viswesh.vibex.testing`. Still **login-free** — no account, no API keys, anywhere.

## Downloads

| File | Purpose |
|---|---|
| [Vibex-test8-changes.patch](Vibex-test8-changes.patch) | Source patch vs the test7 baseline (6 files, +97/−3) |
| [vibex-testing.keystore](vibex-testing.keystore) | Permanent testing keystore (unchanged from test7; alias/password `vibex-testing`) |
| Vibex-2.0-test8.apk | **Pending build** — not produced in the authoring environment (needs JDK 21 + Android SDK 35); see build recipe below |

## What's new since test7

- **Ember player style** — third option in the player-style picker; see [TEST8.md](TEST8.md) for the file-level change map and behavior notes.
- `versionCode 9 → 10`, `versionNameSuffix "-test8"`.
- 4 new unit tests (`tests/test8.test.mjs`); suite now **63/63 passing**.

Everything from test1–test7 carries over (audio source setting, YouTube takeover, lyrics chain with KuGou, Aurora glass, permanent signing).

## Checksums

```text
988277a6566367d6b7a2a17c7fe6d4e26df8044952a3c444ea04f8ddeed284d0  Vibex-test8-changes.patch
cce87ff574be57fff6d72db70a521e491907d14511e6014cce162b64e100281f  vibex-testing.keystore
# Vibex-2.0-test8.apk  -> added here once the APK is built (sha256 appended)
```

## Building the APK from source

```
git clone https://github.com/Viswesh28/VIBEX && cd VIBEX
git checkout ad60995                 # test5-era main baseline
git apply <test4..test7 patches>     # from the v2.0-test5-bundle release assets + releases/v2.0-test7/
git apply releases/v2.0-test8/Vibex-test8-changes.patch   # this test
cd music-app && npm ci && (cd ../jiosaavn-api && npm install --omit=dev)
npm run build && npx cap sync android
cd android && ./gradlew assembleDebug   # versionCode 10, 2.0-test8, auto-signed with the committed testing keystore
```

## Signing / updates

Same permanent testing certificate as test7 (SHA-256 `e626ed74…be94ee13`). A test8 APK installs **in place over test7** — library, settings (including player style) and downloads survive. Testing key only — its password is public by design; never use it for a store release.

## Recorded checks

- 63 JavaScript unit tests (`node --test tests/*.test.mjs`) — all passing on the reconstructed test7 tree + test8 patch.
- Changed JSX compiles cleanly (esbuild parse check).
- Playwright e2e (27) and JVM YtMatcher tests remain on record from test7, unchanged by this patch.
- On-device verification (Ember rendering on a real phone, OEM notification behavior) still needs a device — see TEST8.md.

Source project: [Viswesh28/VIBEX](https://github.com/Viswesh28/VIBEX). Vibex is not affiliated with or endorsed by JioSaavn, YouTube, LRCLIB or KuGou; it hosts no music. See [LICENSE](../../LICENSE), [NOTICE.md](../../NOTICE.md), [DMCA.md](../../DMCA.md).
