# Static security review — 10 November 2026

## Scope and limits

Reviewed the new Android source, manifest, resource security policy and build declarations by inspection. No build, app execution, live-provider test, dependency vulnerability scan, malware scan or Play Protect check was performed. The prior APK's Play Protect warning has not been supplied. Its cause is **unknown**. Do not attribute it to a particular permission, signing certificate, library or false positive without artifact-specific evidence.

The files are a source preview. This document is neither a passed penetration test nor a claim that an APK is safe.

## Permissions and exported components

| Permission/component | Purpose / restriction |
| --- | --- |
| INTERNET | Search, API resolution and streaming. |
| ACCESS_NETWORK_STATE | Media3 network awareness. |
| FOREGROUND_SERVICE + FOREGROUND_SERVICE_MEDIA_PLAYBACK | User-initiated background audio and media notification. |
| WAKE_LOCK | Media3 network playback wake mode, released with player. |
| POST_NOTIFICATIONS | Requested on first play on Android 13+, not at launch. Media-session notification handling remains OS-controlled. |
| MainActivity exported | Launcher only; no URL import, share, arbitrary file or deep-link handling. |
| PlaybackService exported | Necessary for media-session discovery. Both session discovery and connection callback check same UID or trusted controllers. Queue commands additionally require app UID. A manifest intent filter alone is not treated as authentication. |

No `REQUEST_INSTALL_PACKAGES`, `QUERY_ALL_PACKAGES`, accessibility, VPN, device administration, SMS, contacts, microphone, location, broad storage, boot receiver or battery-optimization exemption permission. No automatic startup after boot and no self-updater.

## Network and content handling

- HTTPS only at the app network policy level. Uses platform trust anchors; no trust-all manager, hostname verifier override, user-certificate override or cleartext exception.
- Metadata requests reject redirects, bound reads to 1 MiB and use 8-second connect/read timeouts. Read timeout is not an overall deadline; DNS and a slow-drip response can delay a serial executor. A future hardened transport should support a total call timeout and active connection cancellation.
- Checks initial URLs for public addresses. No backend proxy is exposed. DNS rebinding, re-resolution and CDN redirects are not completely controlled by these client checks; Media3 can follow HTTPS redirects. Do not reuse this code as a server-side arbitrary URL fetcher or claim complete SSRF prevention.
- No authentication/cookies or secrets deliberately added by this app. Provider URL input rejects credentials/query/fragment. Signed URLs remain sensitive and must not be logged.
- JSON is parsed as data, with malformed search rows skipped. HTML entities are converted to plain display text; there is no WebView or JavaScript bridge.
- FLAC requires explicit compatible format metadata and rejects reported encryption/manifests. This does not validate magic bytes, decoder output, reported bit depth or stream ownership.
- JioSaavn source labels say “320 kbps requested”, not measured bitrate. Fallback never retains the FLAC label. Lossless-only never silently falls back on resolution failure.
- No downloads, offline lossless storage, remote executable loading, shell invocation, installer, ad SDK, analytics SDK or obfuscated payload loader.

## State and lifecycle

- Service owns ExoPlayer and queue resolution. IO is off the application thread. Player operations are posted back to the main thread.
- Generation tokens prevent late song resolution from changing newer playback. Work is interrupted on replacement and destruction, with bounded socket timeouts; interruption cannot instantly terminate every DNS/read call.
- MediaController is released when UI is destroyed. Player/session and IO executor are released on service destruction. Audio focus and becoming-noisy handling delegated to Media3.
- Only stable song metadata is saved in app-private preferences. No expiring CDN URLs or lossless signed IDs saved in playlists. Network responses and signed URLs are not logged. Platform/media-library internals require separate runtime inspection.
- Backup is disabled to avoid accidental library transfer. Local library deletion is explicit; uninstall removes app-private data. No sync/account mechanism.
- Playlist limits: 20 lists, 100 tracks per list. Queue custom commands have size and count caps. Library writes use `SharedPreferences.apply`; low-memory/process-death durability is not equivalent to a transactional database.
- Repeat/shuffle, queue restoration, automatic recovery and schema migration are not implemented. A rewrite is not permission to silently erase/migrate the old test7 library.

## Signing and Play Protect

The repository's historic test7 keystore is public test material, **not a private publisher key**. The rewrite does not load it or commit another signing key. Release build signing is intentionally not configured; personal debug builds use local debug keys unless a dedicated private testing identity is provisioned.

Legitimate sideloaded apps can encounter warnings, but this does not prove this APK is a false positive. Conversely, a clean static review does not prove the absence of malware or vulnerable dependencies in a compiled artifact. Signing establishes identity/integrity, not Google approval.

Do not disable Play Protect, add misleading UI claiming Google approval, spoof a trusted package/certificate, or churn package names to evade a verdict. Obtain the exact warning and affected artifact, inspect permissions/signing/dependencies, and use Google's official developer false-positive/appeal process if appropriate. A Play distribution would require a separate policy/content-rights and signing review.

## Open release blockers

- Missing exact warning screenshot/text and affected APK hash.
- Uncompiled new source and unresolved dependency graph; no Java/static Android Lint output.
- No provider contract validation against the user's actual Saavn endpoint.
- No native tests for matching, service lifecycle, queue races, malformed responses or playback errors.
- No real-device permission/background/decoder/notification validation.
- No private stable signing pipeline, dependency lock/verification metadata, SBOM or reproducibility check.
- Upstream availability, access rights, service terms and licensing review remain outstanding.

These blockers must remain visible in release notes. None is resolved by renaming the APK or removing a warning message.
