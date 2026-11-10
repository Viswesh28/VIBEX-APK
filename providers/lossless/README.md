# Lossless provider — integration foundation (not an APK release)

## Current status

Original, dependency-free JavaScript adapter and routing seam with 15 offline contract tests. **Not wired into the Android app or a settings screen. No new APK has been built.** Test7 and its download recommendation remain unchanged.

Run with Node 20+:

```sh
node --test providers/lossless/provider.test.mjs
```

The tests use fixtures and injected fetch, not live upstreams. A passing test suite is not evidence of catalog availability, accurate upstream quality claims, browser CORS support, or device playback.

## Research findings

Reviewed 10 November 2026, upstream commit [`456831d79b4bc81dced2cedaad751290bad6b5ec`](https://github.com/EchoMusicApp/lossless-extension/tree/456831d79b4bc81dced2cedaad751290bad6b5ec).

| Finding | Source / consequence |
| --- | --- |
| Search is `GET /search?q=...&quality=LOSSLESS`, returning `{ tracks: [...] }`. | [server.ts](https://github.com/EchoMusicApp/lossless-extension/blob/456831d79b4bc81dced2cedaad751290bad6b5ec/src/server.ts). Preserve title, artist, album and duration for matching. |
| Streams are metadata JSON from `GET /stream/:signedId?quality=LOSSLESS`, not audio bytes or necessarily a redirect. | Same server source. Hand the returned media URL to the player, not the `/stream/` endpoint. |
| Public IDs are HMAC-signed, deployment-specific opaque identifiers. | [normalize.ts](https://github.com/EchoMusicApp/lossless-extension/blob/456831d79b4bc81dced2cedaad751290bad6b5ec/src/normalize.ts). Never generate IDs, translate Saavn IDs directly, or reuse IDs across deployments. |
| `quality` can default to the requested quality even when absent in the upstream result. | `streamResponse` in normalize.ts. Do not infer FLAC from `quality=LOSSLESS`, a URL extension, or a user preference. |
| Root routing actually races Qobuz-DL, LastBabe and Monochrome in this revision. Other providers have isolated prefixes. | [providers/index.ts](https://github.com/EchoMusicApp/lossless-extension/blob/456831d79b4bc81dced2cedaad751290bad6b5ec/src/providers/index.ts). README's broad “all providers” wording is not the exact implementation. |
| Manifest advertises `allowDownloads: 0`. | server.ts. No lossless downloads or offline storage in this adapter. Existing downloads bypass lossless. |
| Returned stream metadata may include codec, container, encryption, manifest, sample rate and bit depth. | [types.ts](https://github.com/EchoMusicApp/lossless-extension/blob/456831d79b4bc81dced2cedaad751290bad6b5ec/src/types.ts). Initial scope accepts direct unencrypted FLAC only, not DASH/HLS or DRM. |
| Server code is GPL-3.0. | [LICENSE](https://github.com/EchoMusicApp/lossless-extension/blob/456831d79b4bc81dced2cedaad751290bad6b5ec/LICENSE). This is a separately written HTTP client; no upstream implementation is copied or bundled. Do not silently copy GPL server code into MIT app code. Legal review is still appropriate before distribution. |

Supplied endpoint: `https://lossless-extension-five.vercel.app`. This sandbox could not reach that host (outbound access restriction). Its availability, deployed revision, response headers and upstream rights have **not** been independently verified. Public accessibility does not establish permission to stream a catalog; deploy/use only with applicable rights and service terms. No account-secret extraction, decryption or access-control bypass is implemented here.

## Provider behavior implemented

- Existing modes `auto`, `saavn`, `youtube` pass unchanged to the injected existing resolver.
- `lossless-first` searches for the same recording, requests direct FLAC, then falls back to existing Auto on a typed provider failure. The returned fallback keeps its actual provider/codec and a failure reason.
- `lossless-only` reports failure rather than quietly playing lossy audio.
- Downloads never call this provider, even when a lossless mode is selected.
- Cross-catalog matching requires close title, performer and duration agreement. Live/remix/cover and other version markers must agree. Ambiguous candidates fail closed. Missing durations also fail closed.
- Search and media URLs are not persisted. Every play resolves fresh signed IDs and URLs.
- HTTPS public-host syntactic validation, omitted cookies, no HTTP redirects for metadata, bounded response size, timeout and caller cancellation.
- This is a client URL validator, **not a complete SSRF defense**. A future backend proxy needs DNS/IP validation on every hop, allowlists, size limits and redirect controls. Do not expose an arbitrary URL proxy.
- `verifiedBytes: false` is explicit: FLAC acceptance is based on provider metadata, not decoder inspection. Never advertise bit-perfect playback, hi-res, or FLAC verification based on this alone.

## Proposed app design (not yet installed)

Keep JioSaavn as the catalog/search/library identity and retain 320 kbps unchanged. Add these options to Settings → Playback → Audio source:

1. Auto (existing default)
2. JioSaavn only
3. YouTube first
4. Lossless first — “Try a matching FLAC stream; use Auto if unavailable. More mobile data.”
5. Lossless only — “No lossy fallback. Some songs may be unavailable.”

Keep bitrate selection separate: “JioSaavn/download bitrate”, not a universal quality control. Never change users' existing preference during migration. Initially use the supplied deployment as an opt-in service; explain that song metadata is sent to it and third-party upstreams. Endpoint editing, if exposed, must invalidate provider routes and any signed IDs.

Player provenance should distinguish “Resolving lossless”, “Lossless provider · FLAC reported”, verified decoder format if available, and “JioSaavn · fallback” / “YouTube · fallback”. Show sample rate/bit depth only when known. Retain the original song's library ID and artwork through provider switches; do not write lossless signed IDs into Saavn catalog records.

## Integration seam

```js
import { LosslessProvider, resolvePlayback } from './provider.mjs';

const lossless = new LosslessProvider();
const result = await resolvePlayback({
  mode: 'lossless-first',
  song: { title: 'Track title', artist: 'Primary performer', duration: 240, album: 'Album' },
  lossless,
  signal: trackAbortController.signal,
  existingResolve, // supplied by the app; returns real provider/codec/url metadata
});
```

The caller must map the actual test7 song schema into this explicit contract. Use performers, not composer-first display credits; require duration in seconds. Multi-artist/local-script equivalence needs fixture coverage before relaxing conservative matching.

### Android integration required

Test7's patch shows resolution in `VibeMedia.resolve(id, quality, download)`, not merely in React. Adding this JavaScript module to a web bundle **will not change native playback**. Integrate an equivalent resolver on the native service's IO executor, or an explicit supported bridge, before claiming this feature works on Android.

- Insert lossless routing before the lossy URL/YouTube route-cache lookups. Cache keys must include provider mode, endpoint, track and stream/download purpose, not just Saavn ID and bitrate.
- Respect `dl=1` and preserve the existing downloader path. Do not place lossless media bytes in persistent download caches without authorization.
- Do not keep `audio/mp4` as the MIME hint for FLAC. Use `audio/flac` and verify Media3 extractor/decoder support against the exact pinned Android dependencies.
- Cancel superseded work and guard by a playback generation token. A late response must not replace the next track or a newer source selection.
- Persist source settings through the native store; validate new enum values and default legacy/unknown values to Auto. Clear routes on source changes.
- Propagate actual provider and format through service state, notification metadata and UI. A `viaYoutube` boolean alone is insufficient.
- On mid-stream failure, invalidate the rendition, re-resolve at most once, then apply explicit fallback policy. Preserve position only on a confident recording match. Strict lossless must not fall back. Handle decoder failures separately from resolution failures.
- Verify seek/Range requests, URL expiry, background playback, audio focus, headset/BT controls, notification next/previous, and upgrades with the existing permanent testing key.

## Why app integration is blocked here

This checkout is the distribution repository. Public `Viswesh28/VIBEX/main` is now a different v3.2-era architecture and is not the test7 app being retained here. Test7 reconstruction requires:

1. Source commit `ad609952515902e6701cfb049f95938d625b279d`.
2. Cumulative `Vibex-test4-changes.patch` from the test5 GitHub release.
3. Incremental `Vibex-test5-changes.patch` from that release.
4. The local `releases/v2.0-test7/Vibex-test7-changes.patch`.

The source commit is accessible, but release-asset downloads redirect to `release-assets.githubusercontent.com`, which this sandbox cannot reach. The required test4/test5 patches are not checked into this distribution repository. Consequently a complete test7 source tree, integration build and signed APK cannot honestly be produced from the available files.

**To unblock:** supply the reconstructed test7 source archive (preferred) or both missing patch files. Then wire the native resolver/settings/UI, run regressions, verify live streams and sign a new testing APK. Do not substitute the unrelated current source main or overwrite test7 artifacts.

## Release gate

- [x] Offline adapter contract tests (15 passing)
- [x] No third-party dependencies or copied GPL implementation
- [ ] Matching complete test7 source available
- [ ] Settings and native service wired and regression-tested
- [ ] Supplied live deployment manifest/search/stream checked
- [ ] Real FLAC decoder output, MIME, seeking and expiry checked
- [ ] 320 kbps, YouTube, downloads, notifications and cancellation regressions pass
- [ ] Physical Android playback and in-place signed upgrade verified
- [ ] Upstream terms/rights reviewed
- [ ] New APK built, signature/package/version checked, checksums published
