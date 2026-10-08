# Vibex 2.0 — test1–test5 bundle

**Testing prerelease · 8 October 2026 · test5 is recommended.**

All five previously delivered Android APKs are archived here unchanged, together with checksums, version notes, selected icon artwork, and available development source patches. These are debug-signed testing builds—not a production/stable release.

## Downloads

| Build | Version code | APK | Size | Notes |
|---|---:|---|---:|---|
| **test5** — recommended | 7 | [Vibex-2.0-test5.apk](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test5.apk) | 7.58 MiB | [Details](TEST5.md) |
| **test4** — historical | 6 | [Vibex-2.0-test4.apk](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test4.apk) | 7.58 MiB | [Details](TEST4.md) |
| **test3** — historical | 5 | [Vibex-2.0-test3.apk](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test3.apk) | 7.69 MiB | [Details](TEST3.md) |
| **test2** — historical | 4 | [Vibex-2.0-test2.apk](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test2.apk) | 7.56 MiB | [Details](TEST2.md) |
| **test1** — historical | 3 | [VIBEX-2.0-test1.apk](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/VIBEX-2.0-test1.apk) | 7.56 MiB | [Details](TEST1.md) |

## What changed in each version?

- **test1:** initial native Android v2 testing build with the selected AI-generated launcher icon; launcher label **VIBEX Test**.
- **test2:** **Vibex** branding and creator credit, removal of the top-right profile placeholder, recent searches, download badges, caching/polling improvements.
- **test3:** normal-mode power work, manual Battery Saver, persistent Home picks, playlist pins/sorting, per-song lyric timing offsets, optional bounded autoplay.
- **test4:** fixed top header and current-song return behavior. **Historical notification defect:** audio could continue without a notification card because the direct-start session was not registered with the notification manager.
- **test5:** fixes that registration with `addSession(session)` and adds Android-framework notification regression coverage. **Choose this APK unless you specifically need an older build for comparison.**

All later builds retain the earlier features. The selected launcher icon is preserved; test2 onward use the name **Vibex** and the credit **Made with ❤️ by VISWESHSARAVAN**.

## Install safely

1. Download **[Vibex-2.0-test5.apk](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/Vibex-2.0-test5.apk)** and verify its SHA-256 below.
2. Back up your existing test-app library, then install over test1/test2/test3/test4 **without uninstalling**. All five use package `dev.viswesh.vibex.testing` and the same development signing certificate.
3. If Android requests permission to install from your browser or file manager, grant it only if you trust this download. Do not blindly bypass security warnings or disable device protection.
4. Open Vibex and play a song. Allow notifications if prompted. Press Home and expand the notification shade to check the music card, playback controls, and—where your Android version supports it—the seek slider.

**Compatibility:** Android 6.0 / API 23 or newer; target API 35. Test5 is version code **7**. The test package is separate from the original v1.0 package `dev.viswesh.vibex`; private data is not automatically migrated between them. Downgrading to an older APK is not recommended and normally fails Android's version-code check. If you see a signature conflict, report it rather than immediately uninstalling.

## APK checksums

```text
16d5df08764884c5890a3ac96a6fc33efeef2abc1ed2e8ffabb5480712fad4f2  VIBEX-2.0-test1.apk
2305fc48134b562a0b344009966eb7c3be4122697cbbb4a1a52826f87a4239e1  Vibex-2.0-test2.apk
481c0b144f4d37c77f18fea72d98e8e4eabc2f12b1541057eac6d436b68818f3  Vibex-2.0-test3.apk
db672bcc38d8744e89792c1704aec9029e1ad11dc446dc5e5c579d8f1b56bd65  Vibex-2.0-test4.apk
0c4f49c5072c476a3f0f344e4dcf51262e5185b431d4dc07ed48d22cc22795c2  Vibex-2.0-test5.apk
```

Download [SHA256SUMS](https://github.com/Viswesh28/VIBEX-APK/releases/download/v2.0-test5-bundle/SHA256SUMS) alongside the assets, then run `sha256sum --ignore-missing -c SHA256SUMS` on Linux. To verify one file on Windows: `Get-FileHash .\Vibex-2.0-test5.apk -Algorithm SHA256`.

Shared signing-certificate SHA-256: `995adfd729de7af19e69d1780d4fcf6b21a81b6b89b4390aeb1e76df016e6553`.

## What has actually been tested?

Test5 passed **44 JavaScript tests, 26 browser tests, and 32 JVM tests**. Its JVM suite includes Robolectric simulations of Android API 28 and 33 that exercise the real service and Media3 notification code with a deterministic test player. They check notification publication/foreground promotion, title/session token, advancing position, pause, seek, resume, and next-track metadata. The original direct-start registration failure was reproduced before fixing it.

**Not verified:** live notification rendering on your phone, all OEM battery restrictions, a full physical-device upgrade matrix, Bluetooth/call behavior on every device, or measured battery-life improvements. Framework simulations are not physical-device tests. Android controls the notification/seek-slider layout; its appearance varies by version and manufacturer. Android Force stop, device shutdown, and OS process termination can still stop playback.

These remain personal/testing builds. Earlier dependency audits reported a `node-forge` advisory; no blanket security clearance or production-readiness claim is made. Use third-party music services only in accordance with their terms and applicable rights.

## Available source patches

The public source project is [Viswesh28/VIBEX](https://github.com/Viswesh28/VIBEX). **This publication updates the APK distribution repo, not the source repo's branches.** The release includes development patches so the new source changes are not silently assumed to be on the source repo's main branch.

Base source commit for the cumulative patches: [`ad609952515902e6701cfb049f95938d625b279d`](https://github.com/Viswesh28/VIBEX/tree/ad609952515902e6701cfb049f95938d625b279d).

- `VIBEX-implementation.patch`: early v2 implementation reference; it predates the final test1 icon packaging and is **not an exact complete test1 source snapshot**.
- `Vibex-test2-changes.patch`, `Vibex-test3-changes.patch`, and `Vibex-test4-changes.patch`: independent **cumulative patches against the original source HEAD**. Do not stack them on each other.
- `Vibex-test5-changes.patch`: **incremental patch on top of test4**.

To reconstruct the supplied test5 working-source changes in a clean source checkout:

```bash
git checkout -b vibex-test5-source ad609952515902e6701cfb049f95938d625b279d
git apply /path/to/Vibex-test4-changes.patch
git apply /path/to/Vibex-test5-changes.patch
```

Follow the source project's build instructions. No byte-for-byte reproducible-build guarantee is made. The original development signing key is intentionally **not distributed**; a build signed with a different key will not update the installed testing APK without a conflict. No access tokens, signing keys, caches, or private user data are included.

## Reporting a problem

Include your phone model, Android version, Vibex version, exact steps, and a screenshot/short recording if useful. For a missing notification, state whether music continues after pressing Home. Prefer test5; test4's known notification defect is fixed there.

[License](../../LICENSE) · [Notice](../../NOTICE.md) · [DMCA](../../DMCA.md)
