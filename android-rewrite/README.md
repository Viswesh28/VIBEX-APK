# Vibex native rewrite — source preview

**Status: implementation written and statically inspected only. Not compiled, linted, installed or run. No APK produced. Not a security certification or a finished replacement for test7.** The existing test7 artifacts are unchanged.

This is an independent native Android rewrite for the agreed scope: the test7-inspired core player plus an optional lossless provider, for personal APK testing. It does not depend on the unavailable historical test4/test5 source patches. It does not substitute the unrelated current v3 source tree.

## Implemented source

- Native Java UI: search, saved songs, named playlists, playback position, previous/next, retry/stop, provider settings and a first-use privacy explanation.
- Native Media3 ExoPlayer/MediaSessionService: service-owned queue resolution, automatic next-track resolution, background audio, media-session notification, focus/noisy handling and wake mode.
- JioSaavn v2-schema API client: fresh ID-based playback resolution requesting the API's 320 kbps rendition.
- Optional lossless-first with JioSaavn fallback, or strict lossless-only. No claim of FLAC based solely on a quality preference. Direct unencrypted FLAC metadata required; conservative recording matching.
- Local app-private playlists; signed lossless IDs and expiring stream URLs are not persisted in the library.
- Source/generation isolation: superseded service jobs cannot replace a newer track. Search jobs are invalidated on screen/list changes. Service outlives UI while playing; UI releases its controller.
- HTTPS-only networking, default platform TLS validation, bounded JSON responses, per-connection/read timeouts, no metadata redirects, and private-address checks on initial API/media URLs.

The Android provider is a separate implementation of the same design as `../providers/lossless/provider.mjs`. Those JavaScript tests do **not** test the Java implementation. It needs its own JVM/instrumentation coverage before release.

## Intentionally not claimed / not implemented

Exact test7 UI/feature parity, YouTube fallback, downloads, lyrics, local-audio import, artwork rendering, EQ/DSP, cloud sync, export/import, native notification next/previous queue actions, repeat/shuffle, and process-death queue restoration. The notification relies on Media3 for play/pause/seek; previous/next are in the app and queue advancement is service-owned. No automatic retry/fallback after a decoder or mid-stream failure: errors are shown, and retry is explicit. Source changes apply on next play/retry, not during the current track.

No bitrate/codec byte verification, bit-perfect promise, hi-res guarantee, live endpoint verification or Play Protect verdict. No APK signing credentials are included.

## Configuration needed

In Settings, supply the **existing JioSaavn API base URL**. It must return the sumitkolhe v2-style schema:

- `GET <base>/search/songs?query=...&limit=20` → `{ "data": { "results": [song] } }`
- `GET <base>/songs/<id>` → `{ "data": [song] }`
- Song fields: `id`, `name`, `duration` in seconds, `artists.primary[].name`, `album.name`, `image[].url`, `downloadUrl[].{quality,url}`.
- Playback requires `quality: "320kbps"` and an HTTPS `url`.

No unverified public fallback API is hard-coded. No service credential belongs in an APK. If your API has a different schema or requires authentication, its contract needs an explicit adaptation. A bare user-entered HTTPS URL is not evidence of an authorized music service.

Lossless endpoint is `https://lossless-extension-five.vercel.app`. Its source contract was reviewed, but sandbox network restrictions prevented live checks. Native clients do not depend on browser CORS. Permission to access the music catalog remains separate from endpoint accessibility. Use only with applicable rights and upstream terms.

## Build prerequisites (instructions only; not executed)

- JDK 17, Android SDK platform 35 and platform build tools.
- Gradle **8.11.1** for Android Gradle Plugin 8.9.2. No unverified wrapper binary is committed. Provision a trusted Gradle distribution and generate/verify its wrapper before CI adoption.
- Google Maven, Maven Central and Android SDK network access, not available in this sandbox.
- Set `ANDROID_HOME`, or supply an ignored `local.properties` containing `sdk.dir`.

From this directory in a properly provisioned environment:

```sh
gradle lintRelease assembleRelease
```

This creates an **unsigned** source-preview release if compilation succeeds. Do not distribute it as a security-reviewed build. Use a privately held stable testing identity to sign only after reviewing build/lint results. Do not copy the public test7 testing keystore into this project: it provides no private publisher identity.

For installation testing, `gradle assembleDebug` uses the workstation's debug key. That is convenient locally, but different workstations' debug keys cannot update one another. For repeated personal testing, create a dedicated private testing keystore outside Git and protect/retain it. Configure signing locally or sign the unsigned APK with trusted Android SDK tooling. Never put passwords in chat, Git, build logs or a public artifact.

Inspect signatures using `apksigner verify --verbose --print-certs`, inspect the manifest/permissions with Android SDK tools, and publish a SHA-256 checksum alongside any artifact. Checksums identify bytes; they do not certify safety.

## Identity and upgrade safety

The source-preview package is `dev.viswesh.vibex.rewrite`, version code 1, label `Vibex Preview`. This intentionally installs beside `dev.viswesh.vibex.testing` rather than impersonating an in-place test7 upgrade with a different signing identity. Existing test7 data is not imported. If true in-place migration is later required, explicitly design its signing, package, database and settings migration and test it before changing this identity. Changing a package name is **not** a Play Protect workaround or guarantee.

## Static security review

See [SECURITY-REVIEW.md](SECURITY-REVIEW.md). No application code, Gradle build, dependency audit, automated test, emulator or APK was executed for this rewrite, per the user's request. File inspection and whitespace checks are not substitutes for compilation or security analysis of an APK.

## Dependencies and attribution

This rewrite's original code follows the repository license. AndroidX Media3 and Core, Android Gradle Plugin, and Guava are separately licensed under Apache-2.0; retain their license/NOTICE material in distribution and generate a dependency/license inventory from the resolved build. No EchoMusic GPL server code or Saavn API-wrapper implementation is bundled; the rewrite is an HTTP client to configured services. External music, artwork and catalog data are not licensed by this source code.

## Required before calling this usable/release-ready

1. Confirm the actual JioSaavn API URL and response schema.
2. Capture the exact Play Protect warning from the existing APK (screenshot, package, APK SHA-256, phone/Android version and acquisition URL).
3. Compile, run Android Lint, resolve dependency metadata and audit the actual dependency graph; generate an SBOM/license inventory. Pinned versions alone are not proof of current vulnerability status.
4. Add Java provider/schema/matcher and lifecycle tests. Verify Media3 service/controller API use against the resolved dependencies.
5. Verify catalog identity and actual FLAC decoding, seeking, signed-URL expiry and truthful fallback UI using authorized streams.
6. Verify Android 6+ compatibility, Android 13+ notification denial, Android 15 edge-to-edge, rotation, loss of connectivity, rapid next/previous, navigation during pending searches and source changes.
7. Verify background queue advancement, lock-screen/BT controls, phone-call interruption, foreground-service transitions and stop/task removal on real devices.
8. Decide whether the intentionally omitted test7 features are needed before replacing its release.
9. Sign with a private stable identity, verify the final APK and submit any false-positive report through official Play Protect channels if the actual signed artifact is flagged. Do not ask users to disable protection.
