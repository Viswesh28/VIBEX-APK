# Vibex 2.0-test3

**Status: Historical — battery and feature update.** Published in the test1–test5 archive on 8 October 2026.

## Changes

Added manual **Battery Saver** while improving normal-mode scheduling. Added persistent Home metadata caching, pinned/sorted playlists, per-song lyric timing offsets, and optional bounded autoplay. Saver and autoplay default off. User-controlled downloads and the core player remain available. No measured phone battery-life percentage is claimed.

## APK identity

| Field | Value |
|---|---|
| Download | [Vibex-2.0-test3.apk](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test3.apk) |
| App label | Vibex |
| Version code | 5 |
| Package | `dev.viswesh.vibex.testing` |
| Android | Minimum API 23 / Android 6.0; target API 35 |
| Size | 8,060,006 bytes (7.69 MiB) |
| Signing | Development/debug certificate; same signer across all five builds |

SHA-256: `481c0b144f4d37c77f18fea72d98e8e4eabc2f12b1541057eac6d436b68818f3`.

## Validation and limits

36 JavaScript, 19 browser, and 20 native/JVM tests passed at build time.

All five APK hashes, embedded package/version/minimum SDK, and signing-certificate fingerprints were rechecked during publication. No APK was rebuilt for this bundle. Development signing and a matching checksum do not certify that an app is safe or production-ready. Earlier dependency audits reported a `node-forge` advisory; security/dependency review remains open.

## Installation

Use **test5** for normal testing. Export a library backup first, then install test5 over an earlier test build **without uninstalling**. Same-package/same-signer Android update behavior is intended to retain data; the complete device upgrade matrix has not been tested.

These test APKs use `dev.viswesh.vibex.testing`, separate from the old v1.0 `dev.viswesh.vibex` app. They do not automatically replace or migrate that app’s private data. Do not downgrade from test5 to older APKs for routine use; Android normally blocks lower version codes.

See the [bundle overview](RELEASE-NOTES.md) for checksums, source-patch instructions, device checks, and all five downloads.
