# Vibex 2.0-test7

**Version code 9 · package `dev.viswesh.vibex.testing` · debug-signed testing build**

Test7 builds on test6 revision 2 (singer-first lyrics fix) and adds one feature: a user-facing choice of **audio source service**. The app remains fully login-free.

## Audio source setting (Settings → Playback → Audio source)

Pick which service the **sound itself** streams from. Search, library, artwork and metadata always come from JioSaavn — this setting only changes where the audio bytes originate.

| Mode | Behavior |
|---|---|
| **Auto** *(default)* | Exactly test6's behavior: JioSaavn first; when a stream fails (API error, dead CDN URL, mid-song death) YouTube silently takes over that track. |
| **JioSaavn only** | YouTube is never consulted. Failures surface truthfully as errors instead of switching services. |
| **YouTube first** | Every track is resolved through the confidence-gated YouTube takeover engine first (songs filter → video rescue, audio-only, same scoring as test6). When no confident YouTube match exists, **JioSaavn rescues the track** — the mirror of what Auto does. |

Details worth knowing:

- **Switching bites immediately.** Changing the mode clears the resolved-URL cache, so the next play (not just the next session) uses the new source. Already-buffering audio finishes from wherever it started.
- **The "via YouTube" pill** in the player keeps telling you the truth: in YouTube-first mode you'll see it on most tracks; on a JioSaavn rescue it stays hidden.
- **Downloads always prefer JioSaavn bytes** at your chosen download bitrate, in every mode. YouTube's stream URLs expire within hours and ignore bitrate preferences — wrong properties for offline copies. YouTube still rescues a download if JioSaavn genuinely fails.
- **Session route memory still applies**: once YouTube takes a track (any mode but JioSaavn-only), later plays skip the dead round-trip until the source heals.
- The setting syncs to the native player instantly and survives restarts; legacy settings without it default to Auto.
- On the web build the setting is stored but has no effect — the YouTube engine is native-only.

## Carried over from test6 (both revisions)

- JioSaavn → YouTube stream takeover (songs-first, confidence-gated, audio-only)
- Lyrics chain: your LRC → LRCLIB Exact → LRCLIB Search → KuGou, with the revision-2 fix (singer-first artist matching, junk-instrumental demotion, clearer failure states)
- Liquid Glass UI: Aurora glass player style toggle
- No login, no API keys, anywhere

## Testing

- 59 JS unit tests (4 new for the setting) — pass
- 27 Playwright browser e2e tests (1 new: the setting offers all three services, persists, survives reload) — pass
- Java unit tests (YtMatcher) — pass
- APK verified: versionCode 9, `2.0-test7`, package `dev.viswesh.vibex.testing`, both the new setting and the lyrics fix present in the bundled web assets

## Permanent signing — in-place updates from now on

Starting with this build, every Vibex testing APK is signed with one **permanent testing keystore** (`vibex-testing.keystore`, certificate SHA-256 `e626ed74…be94ee13`) instead of a throwaway machine debug key. The keystore is committed to the project and wired into the debug build, so:

- **Every future APK installs straight over the previous one** — library, settings, downloads and stats survive updates. No more uninstall/reinstall.
- Builds you make from the patch and builds delivered to you are **mutually updatable**: the patch carries the keystore (apply with `git apply` — it is a binary-aware patch) and Gradle signs with it automatically.
- **One last transition**: whatever is installed right now (test5/test6/earlier test7) still carries an old signature, so this particular APK needs one final uninstall first. Every build after that updates in place.
- Testing identity only — its password is in the repo by design. Never use it for a Play Store release.

## Files

| File | SHA-256 |
|---|---|
| `Vibex-2.0-test7.apk` (signed with the permanent testing key) | `805cc0fcc294afbae002befe580b19c9e99f12fbe118feebff1aeabfc9dbc549` |
| `Vibex-test7-changes.patch` (binary-aware, cumulative vs test5 baseline: test6 + rev2 + test7 + signing; includes the keystore) | `dfbde3e8c61e1b191cfcd68aadd023dc7db159a70087333675ad594737e173cf` |
| `vibex-testing.keystore` (also embedded in the patch; alias `vibex-testing`, password `vibex-testing`) | `cce87ff574be57fff6d72db70a521e491907d14511e6014cce162b64e100281f` |

## Installing

```
git apply Vibex-test7-changes.patch       # binary-aware: also writes android/vibex-testing.keystore
cd music-app && npm ci && (cd ../jiosaavn-api && npm install --omit=dev)
npm run build && npx cap sync android
cd android && ./gradlew assembleDebug     # automatically signed with the permanent testing key
```

## On-device checks worth doing

1. Settings → Playback → **Audio source** shows the three options and remembers your pick.
2. **YouTube first**: play a popular track — the "via YouTube" pill should appear and audio should start within a few seconds.
3. **YouTube first** with an obscure regional track that YouTube can't confidently match — it should still play (JioSaavn rescue, no pill).
4. **JioSaavn only**: everything plays as it did in test5; a dead stream shows an error instead of switching.
5. Lyrics (rev-2 fix): Tum Hi Ho / Kesariya / Vaathi Coming should all show synced lyrics.
