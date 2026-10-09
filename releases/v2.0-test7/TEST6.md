# Vibex 2.0-test6

**Version code 8 · package `dev.viswesh.vibex.testing` · debug-signed testing build**

Test6 builds on test5 (which fixed the missing notification player) and adds three features. All of them are **login-free**: the app still requires no account of any kind.

## Revision 2 — lyrics fix (field report: "lyrics is not loading")

The first test6 build found lyrics for almost no song on a real library. The engine and network paths were fine — the bug was **metadata**:

- **JioSaavn credits the music director first** (`Tum Hi Ho` → *Mithoon* before *Arijit Singh*), while LRCLIB and KuGou tag lyrics by the **performing singer**. Every exact and search lookup was querying by composer and missing.
- **LRCLIB carries community junk rows flagged `instrumental`** for popular songs; the chain treated instrumental as a final answer and stopped dead — e.g. *Tum Hi Ho* came back "instrumental" instead of its synced lyrics.

Fixed in this revision:

- **Singer-first artist candidates.** The app now reads JioSaavn's artist *roles* and queries with the singer first, keeping composer/primary credits as fallbacks; hits are accepted when tagged with **any** credited artist.
- **Title-recall search.** LRCLIB search now recalls by title and ranks locally with the same strict score (title overlap + any-artist match + duration drift ≤ 15 s), so wrong recordings are still refused.
- **Instrumental demoted.** Synced > plain > instrumental across the whole chain; an instrumental flag is only trusted when nothing better exists anywhere.
- **Clearer failure states.** "Lyrics sources unreachable" (network) is now distinct from "No matching lyrics yet" (no confident match) — if you see the first one, it's connectivity; the second means the recording genuinely didn't match (covers/remixes are refused by design).

Verified against live providers with real JioSaavn metadata: *Tum Hi Ho*, *Kesariya*, *Vaathi Coming*, *Why This Kolaveri Di* all return synced lyrics now (previously none). 4 regression tests added — 55 unit + 26 browser e2e tests pass.

## 1. JioSaavn → YouTube stream takeover

When a JioSaavn stream cannot be played — API failure, dead/rotated CDN URL, or a stream that dies mid-song — Vibex now **silently takes the audio from YouTube** instead of failing:

- **Songs first, video songs only as rescue.** YouTube Music is searched with the *songs* filter first (clean album audio whose duration matches Saavn metadata). Only when no confident album version exists does it fall back to *video* results — with a stricter match threshold, because film edits carry dialogues and intros. Even then, **only the audio track is streamed, never video bytes**.
- **Confidence-gated matching.** Candidates are scored on title similarity, artist overlap, and duration drift, with penalties for cover/remix/live/slowed variants. Below threshold the takeover refuses and the original error is shown — a wrong song is worse than an error.
- **Measured client cascade.** Streams are fetched via ANDROID_VR (whole-file capable, no cipher, no login) → ANDROID_VR legacy → IOS (last resort).
- **Session route memory.** Once YouTube wins a track, later plays skip the dead Saavn round-trip. When a fresh Saavn resolve succeeds again, control is handed back automatically.
- A **"via YouTube" pill** appears in the player whenever a takeover stream is playing.
- Mid-song deaths put the track under suspicion: the next resolve **probes the Saavn URL** before trusting it again.

## 2. Lyrics fallback chain with KuGou

The lyrics engine is now a provider registry tried strictly in your configured order (Settings → Lyrics):

> Your imported LRC → LRCLIB · Exact → LRCLIB · Search → **KuGou · Community synced** *(new)*

- KuGou is login-free and strong for Indian and Asian catalogs; candidates are confidence-scored (title/artist/duration) before use, and embedded credit lines are stripped.
- **Synced beats unsynced across the whole chain**: a plain-text hit from an early provider is held while later providers still get the chance to find a synced one.
- KuGou has no CORS headers, so its requests ride the app's native HTTP path (the same mechanism the embedded JioSaavn API already uses).

## 3. Liquid Glass UI

- **New "Aurora glass" player style** (Settings → Look & feel → Player style): full-bleed blurred artwork background with the controls floating on translucent glass cards — alongside the existing **Classic** style, which remains the default.
- The top bar gets a translucent glass blur.
- Both styles respect light ("Soft light") and dark ("Midnight") themes. On WebViews without `backdrop-filter` support the glass degrades gracefully to solid surfaces.

## What has actually been tested

- **51 JavaScript tests pass** (44 existing + 7 new covering the KuGou scorer, UTF-8 lyric decoding, provider registry, and player-style setting validation).
- **New JVM unit test `YtMatcherTest`** covers the takeover matcher: exact matches, noisy YouTube titles, cover/remix rejection, duration-drift penalties, and the songs-vs-video threshold ordering.
- The web bundle builds cleanly; the Android project compiles with the same toolchain as test5.

**Not verified:** on-device YouTube takeover behaviour across regions/ISPs (InnerTube responses vary), KuGou availability over time, glass rendering on every OEM WebView, or long-session battery impact of the Aurora style. The InnerTube client parameters are a known arms race — expect this path to need maintenance.

## Honest limitations

- The YouTube takeover uses unofficial InnerTube endpoints; Google may break them at any time. When that happens Vibex behaves exactly as test5 did (the original Saavn error is shown).
- Takeover audio is matched by metadata, not by recording identity. The confidence gate makes wrong-song swaps rare, not impossible — the "via YouTube" pill exists so you always know.
- IOS-client streams (deep last resort) may stop after roughly a minute; they are kept only because a partial stream beats silence.
- Same legal posture as before: Vibex hosts no music, is affiliated with neither JioSaavn nor YouTube, and these remain personal debug-signed testing builds.

## Install

- Android 6.0 / API 23+, target API 35. Version code **8** installs over test1–test5 **if signed with the same development key**. A build signed with a different key requires uninstalling the previous test APK first (library backup/restore via Settings → Your data, your control).
