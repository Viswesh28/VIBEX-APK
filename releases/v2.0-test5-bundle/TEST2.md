# Vibex 2.0-test2

**Status: Historical — branding and usability.** Published in the test1–test5 archive on 8 October 2026.

## Changes

Changed the display name to **Vibex**, kept the selected icon, added **Made with ❤️ by VISWESHSARAVAN**, and removed only the top-right profile placeholder. Added recent-search history with remembered categories and per-track download badges. Reduced duplicate catalog requests, paused hidden-WebView polling, improved metadata caching, and retained immediate library-edit persistence.

## APK identity

| Field | Value |
|---|---|
| Download | [Vibex-2.0-test2.apk](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test2.apk) |
| App label | Vibex |
| Version code | 4 |
| Package | `dev.viswesh.vibex.testing` |
| Android | Minimum API 23 / Android 6.0; target API 35 |
| Size | 7,929,541 bytes (7.56 MiB) |
| Signing | Development/debug certificate; same signer across all five builds |

SHA-256: `2305fc48134b562a0b344009966eb7c3be4122697cbbb4a1a52826f87a4239e1`.

## Validation and limits

23 JavaScript, 11 browser, and 6 native/JVM tests passed at build time.

All five APK hashes, embedded package/version/minimum SDK, and signing-certificate fingerprints were rechecked during publication. No APK was rebuilt for this bundle. Development signing and a matching checksum do not certify that an app is safe or production-ready. Earlier dependency audits reported a `node-forge` advisory; security/dependency review remains open.

## Installation

Use **test5** for normal testing. Export a library backup first, then install test5 over an earlier test build **without uninstalling**. Same-package/same-signer Android update behavior is intended to retain data; the complete device upgrade matrix has not been tested.

These test APKs use `dev.viswesh.vibex.testing`, separate from the old v1.0 `dev.viswesh.vibex` app. They do not automatically replace or migrate that app’s private data. Do not downgrade from test5 to older APKs for routine use; Android normally blocks lower version codes.

See the [bundle overview](RELEASE-NOTES.md) for checksums, source-patch instructions, device checks, and all five downloads.
