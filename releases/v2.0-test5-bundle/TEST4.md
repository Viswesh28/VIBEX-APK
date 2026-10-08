# Vibex 2.0-test4

**Status: Historical — superseded; notification defect.** Published in the test1–test5 archive on 8 October 2026.

## Changes

Added a frozen top app header, direct return to the current song, notification/widget return intents, and explicit background/task-removal policy checks. Paused songs stay paused; file pickers and unfinished dialogs are protected. **Known defect:** the directly started media session was not registered with the notification manager, so music could play without a notification card. Use test5 instead.

## APK identity

| Field | Value |
|---|---|
| Download | [Vibex-2.0-test4.apk](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test4.apk) |
| App label | Vibex |
| Version code | 6 |
| Package | `dev.viswesh.vibex.testing` |
| Android | Minimum API 23 / Android 6.0; target API 35 |
| Size | 7,946,265 bytes (7.58 MiB) |
| Signing | Development/debug certificate; same signer across all five builds |

SHA-256: `db672bcc38d8744e89792c1704aec9029e1ad11dc446dc5e5c579d8f1b56bd65`.

## Validation and limits

44 JavaScript, 26 browser, and 30 native/JVM tests passed at build time. These did not test notification publication; the missing coverage was added in test5.

All five APK hashes, embedded package/version/minimum SDK, and signing-certificate fingerprints were rechecked during publication. No APK was rebuilt for this bundle. Development signing and a matching checksum do not certify that an app is safe or production-ready. Earlier dependency audits reported a `node-forge` advisory; security/dependency review remains open.

## Installation

Use **test5** for normal testing. Export a library backup first, then install test5 over an earlier test build **without uninstalling**. Same-package/same-signer Android update behavior is intended to retain data; the complete device upgrade matrix has not been tested.

These test APKs use `dev.viswesh.vibex.testing`, separate from the old v1.0 `dev.viswesh.vibex` app. They do not automatically replace or migrate that app’s private data. Do not downgrade from test5 to older APKs for routine use; Android normally blocks lower version codes.

See the [bundle overview](RELEASE-NOTES.md) for checksums, source-patch instructions, device checks, and all five downloads.
