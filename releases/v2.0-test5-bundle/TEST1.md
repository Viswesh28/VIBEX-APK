# Vibex 2.0-test1

**Status: Historical — initial v2 testing build.** Published in the test1–test5 archive on 8 October 2026.

## Changes

Initial native Android v2 testing build with the selected AI-generated launcher icon. Covers the native playback architecture, durable library/backups, queue controls, local audio, managed downloads, lyrics, playlists, EQ, listening statistics, and widget. Its launcher label is **VIBEX Test**, not the later **Vibex** label.

## APK identity

| Field | Value |
|---|---|
| Download | [VIBEX-2.0-test1.apk](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/VIBEX-2.0-test1.apk) |
| App label | VIBEX Test |
| Version code | 3 |
| Package | `dev.viswesh.vibex.testing` |
| Android | Minimum API 23 / Android 6.0; target API 35 |
| Size | 7,927,344 bytes (7.56 MiB) |
| Signing | Development/debug certificate; same signer across all five builds |

SHA-256: `16d5df08764884c5890a3ac96a6fc33efeef2abc1ed2e8ffabb5480712fad4f2`.

## Validation and limits

16 JavaScript tests; Android assembly, native unit task and lint passed. These are build-time records, not fresh physical-phone validation.

All five APK hashes, embedded package/version/minimum SDK, and signing-certificate fingerprints were rechecked during publication. No APK was rebuilt for this bundle. Development signing and a matching checksum do not certify that an app is safe or production-ready. Earlier dependency audits reported a `node-forge` advisory; security/dependency review remains open.

## Installation

Use **test5** for normal testing. Export a library backup first, then install test5 over an earlier test build **without uninstalling**. Same-package/same-signer Android update behavior is intended to retain data; the complete device upgrade matrix has not been tested.

These test APKs use `dev.viswesh.vibex.testing`, separate from the old v1.0 `dev.viswesh.vibex` app. They do not automatically replace or migrate that app’s private data. Do not downgrade from test5 to older APKs for routine use; Android normally blocks lower version codes.

See the [bundle overview](RELEASE-NOTES.md) for checksums, source-patch instructions, device checks, and all five downloads.
