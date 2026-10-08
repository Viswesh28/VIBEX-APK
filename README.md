# Vibex — Android testing downloads

Search and play music, manage a library and playlists, use lyrics and local audio, and keep playback in a native Android service.

## Recommended download

### [Download Vibex 2.0-test5](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test5.apk)

**[Open the test1–test5 testing release](https://github.com/Viswesh28/VIBEX-APK/releases/tag/v2.0-test5-bundle)** · [Full release notes](releases/v2.0-test5-bundle/RELEASE-NOTES.md) · [Checksums](releases/v2.0-test5-bundle/SHA256SUMS)

Test5 fixes the missing notification player while retaining the fixed top header, direct return to the current song, manual Battery Saver, playlists, lyrics, downloads, and selected icon. These are **debug-signed testing builds**, not production releases.

| Build | Purpose | APK |
|---|---|---|
| **test5 — recommended** | Notification-session registration fix and Android-framework regression tests | [Download](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test5.apk) |
| test4 — historical | Fixed header/current-song return; known missing-notification defect | [Download](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test4.apk) |
| test3 — historical | Battery Saver, Home cache, playlist pins/sort, lyric offsets, optional autoplay | [Download](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test3.apk) |
| test2 — historical | Vibex branding, creator credit, recent searches, download badges, performance | [Download](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test2.apk) |
| test1 — historical | Initial native v2 test build and selected AI-generated icon; label VIBEX Test | [Download](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/VIBEX-2.0-test1.apk) |

## Installation

- Requires **Android 6.0 / API 23+**. Target API 35.
- Package: **`dev.viswesh.vibex.testing`**; test5 version code **7**.
- Back up the test-app library, then install test5 over an earlier test APK **without uninstalling**. All five have the same development signing certificate. Device-specific upgrade behavior has not been comprehensively tested.
- The original v1.0 app uses the separate package `dev.viswesh.vibex`. It is not automatically replaced and its private data does not automatically migrate into the test app.
- Only install if you trust the source. Review Android security warnings; do not blindly bypass them or disable device protection. A checksum checks file integrity, not app safety.

Test5 SHA-256:

```text
0c4f49c5072c476a3f0f344e4dcf51262e5185b431d4dc07ed48d22cc22795c2
```

Play a song, press Home, and expand the notification shade to check the music card and controls. Android decides the system slider's layout and availability. Actual phone/OEM notification behavior still needs device testing; a successful framework simulation is not a phone test.

## Source and verification

Test5's recorded checks: **44 JavaScript + 26 browser + 32 JVM tests**, including Robolectric notification/control simulations for API 28 and 33. Existing dependency/security review remains open; earlier audits reported a `node-forge` advisory. No phone battery-life percentage or production-readiness claim is made.

Source project: [Viswesh28/VIBEX](https://github.com/Viswesh28/VIBEX). This is the **distribution repository**. Available source patches are attached to the release; the source project's branches were not updated by this publication. See the [patch map and build caveats](releases/v2.0-test5-bundle/RELEASE-NOTES.md#available-source-patches).

**Made with ❤️ by VISWESHSARAVAN**

## Historical v1.0

The root [`VIBEX.apk`](VIBEX.apk) and [v1.0 release](https://github.com/Viswesh28/VIBEX-APK/releases/tag/v1.0) are retained as historical artifacts. **They are not the recommended test5 download.** The existing `screenshots/` folder shows the older interface, not a verified test5 notification screenshot.

## Legal

Vibex is not affiliated with or endorsed by JioSaavn or Saavn Media Pvt. Ltd. It does not host music. Respect artists' rights, applicable law, and the services' terms. See [LICENSE](LICENSE), [NOTICE.md](NOTICE.md), and [DMCA.md](DMCA.md).
