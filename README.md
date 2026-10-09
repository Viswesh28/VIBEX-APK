# Vibex — Android testing downloads

Search and play music, manage a library and playlists, use lyrics and local audio, and keep playback in a native Android service.

## Recommended download

### [Download Vibex 2.0-test7](https://github.com/Viswesh28/VIBEX-APK/raw/main/releases/v2.0-test7/Vibex-2.0-test7.apk)

**[Full test7 release notes](releases/v2.0-test7/RELEASE-NOTES.md)** · [Checksums](releases/v2.0-test7/SHA256SUMS) · [TEST7 details](releases/v2.0-test7/TEST7.md) · [TEST6 details](releases/v2.0-test7/TEST6.md)

Test7 is cumulative: JioSaavn→YouTube stream takeover, the lyrics fallback chain with the singer-first fix, the Aurora glass player style, and a new **Audio source** setting (Auto / JioSaavn only / YouTube first). From test7 onward all builds share a **permanent testing signature**: uninstall your older build once, and every future APK installs straight over the previous one. These are testing builds, not production releases.

| Build | Purpose | APK |
|---|---|---|
| **test7 — recommended** | Audio source setting, lyrics fix, YouTube takeover, Aurora UI, permanent signing | [Download](https://github.com/Viswesh28/VIBEX-APK/raw/main/releases/v2.0-test7/Vibex-2.0-test7.apk) |
| test5 — historical | Notification-session registration fix and Android-framework regression tests | [Download](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test5.apk) |
| test4 — historical | Fixed header/current-song return; known missing-notification defect | [Download](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test4.apk) |
| test3 — historical | Battery Saver, Home cache, playlist pins/sort, lyric offsets, optional autoplay | [Download](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test3.apk) |
| test2 — historical | Vibex branding, creator credit, recent searches, download badges, performance | [Download](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test2.apk) |
| test1 — historical | Initial native v2 test build and selected AI-generated icon; label VIBEX Test | [Download](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/VIBEX-2.0-test1.apk) |

## Installation

- Requires **Android 6.0 / API 23+**. Target API 35.
- Package: **`dev.viswesh.vibex.testing`**; test7 version code **9**.
- **Signature change at test7:** test1–test6 used throwaway debug keys; test7 introduces a permanent testing certificate. Back up your library, **uninstall the older test build once**, install test7 — and from then on every newer APK installs over the previous one without uninstalling.
- The original v1.0 app uses the separate package `dev.viswesh.vibex`. It is not automatically replaced and its private data does not automatically migrate into the test app.
- Only install if you trust the source. Review Android security warnings; do not blindly bypass them or disable device protection. A checksum checks file integrity, not app safety.

Test7 SHA-256:

```text
805cc0fcc294afbae002befe580b19c9e99f12fbe118feebff1aeabfc9dbc549
```

Play a song, press Home, and expand the notification shade to check the music card and controls. Android decides the system slider's layout and availability. Actual phone/OEM notification behavior still needs device testing; a successful framework simulation is not a phone test.

## Source and verification

Test7's recorded checks: **59 JavaScript + 27 browser + JVM YtMatcher tests**, all passing at build time; the lyrics fix was verified live against LRCLIB/KuGou with real JioSaavn metadata. Test5's earlier checks (44 JS + 26 browser + 32 JVM, incl. Robolectric notification simulations) remain on record. Existing dependency/security review remains open; earlier audits reported a `node-forge` advisory. No phone battery-life percentage or production-readiness claim is made.

Source project: [Viswesh28/VIBEX](https://github.com/Viswesh28/VIBEX). This is the **distribution repository**. Test7's cumulative source patch (binary-aware, includes the testing keystore) ships in [releases/v2.0-test7](releases/v2.0-test7/); older patches are attached to the [test5 bundle release](https://github.com/Viswesh28/VIBEX-APK/releases/tag/v2.0-test5-bundle). See each bundle's release notes for the patch map and build caveats.

**Made with ❤️ by VISWESHSARAVAN**

## Historical v1.0

The root [`VIBEX.apk`](VIBEX.apk) and [v1.0 release](https://github.com/Viswesh28/VIBEX-APK/releases/tag/v1.0) are retained as historical artifacts. **They are not the recommended test5 download.** The existing `screenshots/` folder shows the older interface, not a verified test5 notification screenshot.

## Legal

Vibex is not affiliated with or endorsed by JioSaavn or Saavn Media Pvt. Ltd. It does not host music. Respect artists' rights, applicable law, and the services' terms. See [LICENSE](LICENSE), [NOTICE.md](NOTICE.md), and [DMCA.md](DMCA.md).
