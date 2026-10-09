# Vibex 2.0-test7

**Testing prerelease · 9 October 2026 · supersedes test6 and test5.**

One APK, cumulative: everything from test1–test5, both test6 revisions, and test7. Version code **9**, package `dev.viswesh.vibex.testing`. Still **login-free** — no account, no API keys, anywhere.

## Downloads

| File | Purpose |
|---|---|
| [Vibex-2.0-test7.apk](Vibex-2.0-test7.apk) | The app — signed with the new permanent testing key |
| [Vibex-test7-changes.patch](Vibex-test7-changes.patch) | Binary-aware source patch vs the test5 baseline (includes the keystore) |
| [vibex-testing.keystore](vibex-testing.keystore) | Permanent testing keystore (alias/password `vibex-testing`) |
| [SHA256SUMS](SHA256SUMS) | Checksums for the three files above |
| [TEST6.md](TEST6.md) · [TEST7.md](TEST7.md) | Detailed version notes |

## What's new since test5

### test6 — three features ([details](TEST6.md))
- **JioSaavn → YouTube stream takeover**: when a Saavn stream fails (API error, dead CDN URL, mid-song death), a confidence-gated matcher silently streams the same track's audio from YouTube — songs first, video versions only as rescue, never video bytes. A "via YouTube" pill shows when active.
- **Lyrics fallback chain**: your imported LRC → LRCLIB Exact → LRCLIB Search → KuGou community synced lyrics, in your configurable order; synced beats unsynced across the whole chain.
- **Liquid Glass UI**: new Aurora glass player style toggle (Settings → Look & feel), Classic remains default.

### test6 revision 2 — the "lyrics is not loading" fix ([details](TEST6.md))
Lyrics were queried by the **composer** (JioSaavn lists the music director first), while lyric services tag by **singer** — nearly every Indian track missed. Now singer-first artist matching, title-recall search with strict local scoring, and junk "instrumental" rows no longer end the chain. Tum Hi Ho / Kesariya / Vaathi Coming all return synced lyrics.

### test7 — audio source setting ([details](TEST7.md))
Settings → Playback → **Audio source**: **Auto** (JioSaavn first, YouTube rescue — default), **JioSaavn only** (never consult YouTube), or **YouTube first** (takeover engine leads; JioSaavn rescues unmatched tracks). Streaming only — search, library and artwork stay on JioSaavn; downloads always prefer Saavn bytes at your chosen bitrate.

### Permanent signing — in-place updates from now on
Every build from test7 onward is signed with the committed testing keystore (certificate SHA-256 `e626ed74a9ba74f0ed1cb6f726547264c8a8c0ada88f57cf596404d6be94ee13`). **One final uninstall** of any older build is required once; after that every new APK installs straight over the previous one, keeping your library, settings and downloads. Builds made from the patch share the same identity. Testing key only — its password is public by design; never use it for a store release.

## Checksums

```text
805cc0fcc294afbae002befe580b19c9e99f12fbe118feebff1aeabfc9dbc549  Vibex-2.0-test7.apk
dfbde3e8c61e1b191cfcd68aadd023dc7db159a70087333675ad594737e173cf  Vibex-test7-changes.patch
cce87ff574be57fff6d72db70a521e491907d14511e6014cce162b64e100281f  vibex-testing.keystore
```

## Recorded checks

59 JavaScript unit tests · 27 Playwright browser e2e tests · JVM YtMatcher tests — all passing at build time. Lyrics fix verified live against LRCLIB/KuGou with real JioSaavn metadata. On-device behavior (takeover, KuGou reachability, OEM notification quirks) still needs phone testing.

## Building from source

```
git clone https://github.com/Viswesh28/VIBEX && cd VIBEX   # test5 baseline
git apply Vibex-test7-changes.patch                        # binary-aware; also writes the keystore
cd music-app && npm ci && (cd ../jiosaavn-api && npm install --omit=dev)
npm run build && npx cap sync android
cd android && ./gradlew assembleDebug                      # auto-signed with the testing key
```

Source project: [Viswesh28/VIBEX](https://github.com/Viswesh28/VIBEX). Vibex is not affiliated with or endorsed by JioSaavn, YouTube, LRCLIB or KuGou; it hosts no music. See [LICENSE](../../LICENSE), [NOTICE.md](../../NOTICE.md), [DMCA.md](../../DMCA.md).
