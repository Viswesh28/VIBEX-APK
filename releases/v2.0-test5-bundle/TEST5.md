# Vibex 2.0-test5

**Status: Recommended testing build.** Published in the test1–test5 archive on 8 October 2026.

## Changes

Fixes the missing notification player by explicitly registering the directly started session with Media3 using `addSession(session)`. This attaches the notification controller and foreground-service management without relying on a UI/controller connection. Retains the fixed header, current-song return, all test3 features, library, icon, and creator credit. Does not add a custom notification, overlay permission, or a progress-polling loop.

## APK identity

| Field | Value |
|---|---|
| Download | [Vibex-2.0-test5.apk](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test5.apk) |
| App label | Vibex |
| Version code | 7 |
| Package | `dev.viswesh.vibex.testing` |
| Android | Minimum API 23 / Android 6.0; target API 35 |
| Size | 7,946,282 bytes (7.58 MiB) |
| Signing | Development/debug certificate; same signer across all five builds |

SHA-256: `0c4f49c5072c476a3f0f344e4dcf51262e5185b431d4dc07ed48d22cc22795c2`.

## Validation and limits

44 JavaScript, 26 browser, and 32 JVM tests passed. The JVM suite includes two Robolectric Android-framework simulations (API 28 and 33) exercising notification publication, foreground promotion, session token/title, advancing position, pause, seek, resume, and next-track metadata using a deterministic test player. The pre-fix direct-start regression failed with zero registered sessions, then passed after the fix. These simulations do not render or verify a physical phone’s notification shade.

All five APK hashes, embedded package/version/minimum SDK, and signing-certificate fingerprints were rechecked during publication. No APK was rebuilt for this bundle. Development signing and a matching checksum do not certify that an app is safe or production-ready. Earlier dependency audits reported a `node-forge` advisory; security/dependency review remains open.

## Installation

Use **test5** for normal testing. Export a library backup first, then install test5 over an earlier test build **without uninstalling**. Same-package/same-signer Android update behavior is intended to retain data; the complete device upgrade matrix has not been tested.

These test APKs use `dev.viswesh.vibex.testing`, separate from the old v1.0 `dev.viswesh.vibex` app. They do not automatically replace or migrate that app’s private data. Do not downgrade from test5 to older APKs for routine use; Android normally blocks lower version codes.

See the [bundle overview](RELEASE-NOTES.md) for checksums, source-patch instructions, device checks, and all five downloads.
