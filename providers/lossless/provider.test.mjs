import { test } from 'node:test';
import assert from 'node:assert/strict';
import { LosslessProvider, ProviderError, matchScore, pickMatch, validateRendition, resolvePlayback } from './provider.mjs';
const song = { title: 'Example Song', artist: 'Example Singer', duration: 240, album: 'Album' };
const track = { ...song, id: 'qobuz_abcdefghijk_0123456789abcdef' };
const flac = { url: 'https://cdn.example.org/audio?token=secret', codec: 'flac', format: 'flac', manifest: 'none', sampleRate: 44100, bitDepth: 16 };
const json = (body, status = 200) => new Response(JSON.stringify(body), { status, headers: { 'content-type': 'application/json' } });
const code = expected => error => error instanceof ProviderError && error.code === expected;
test('base URLs require public HTTPS and no credentials, query or fragment', () => {
  for (const endpoint of ['http://example.org', 'https://localhost', 'https://127.0.0.1', 'https://[::1]', 'https://u:p@example.org', 'https://example.org/?x=1', 'https://example.org/#secret']) {
    assert.throws(() => new LosslessProvider({ endpoint }), code('INVALID_URL'));
  }
  assert.equal(new LosslessProvider({ endpoint: 'https://example.org/c/' }).endpoint, 'https://example.org/c');
});
test('strict recording match accepts diacritics and rejects different versions/performers/durations', () => {
  assert.ok(matchScore(song, track) >= .9);
  for (const change of [{ title: 'Example Song Live' }, { title: 'Example Song Remix' }, { artist: 'Another Person' }, { duration: 280 }, { duration: undefined }, { title: 'Different Song' }]) {
    assert.equal(matchScore(song, { ...track, ...change }), 0);
  }
  assert.ok(matchScore({ ...song, artist: 'Beyoncé' }, { ...track, artist: 'Beyonce' }) >= .9);
});
test('ambiguous releases fail closed; duplicate IDs do not', () => {
  assert.equal(pickMatch(song, [track, { ...track, id: 'other' }]), null);
  assert.equal(pickMatch(song, [track, track]), track);
  assert.equal(pickMatch(song, []), null);
});
test('FLAC metadata retains actual quality and is not byte verification', () => {
  const result = validateRendition(flac);
  assert.equal(result.mimeType, 'audio/flac');
  assert.equal(result.sampleRate, 44100);
  assert.equal(result.verifiedBytes, false);
  assert.equal(result.allowDownloads, false);
});
test('quality=LOSSLESS or URL extension alone is never proof of FLAC', () => {
  for (const rendition of [{ url: flac.url, quality: 'LOSSLESS' }, { url: 'https://cdn.example.org/a.flac' }, { ...flac, codec: 'aac' }, { ...flac, encrypted: true }, { ...flac, manifest: 'dash' }, { ...flac, container: 'mp4' }]) {
    assert.throws(() => validateRendition(rendition), code('NOT_FLAC'));
  }
  assert.throws(() => validateRendition({ ...flac, url: 'http://cdn.example.org/a' }), code('INVALID_URL'));
});
test('search then stream preserves signed ID and sends no credentials', async () => {
  const calls = [];
  const provider = new LosslessProvider({ fetchImpl: async (url, options) => {
    calls.push({ url, options }); return json(calls.length === 1 ? { tracks: [track] } : flac);
  } });
  assert.equal((await provider.resolve(song)).provider, 'lossless');
  assert.match(calls[0].url, /quality=LOSSLESS/);
  assert.match(calls[1].url, new RegExp(`/stream/${track.id}\\?quality=LOSSLESS`));
  assert.equal(calls[0].options.credentials, 'omit');
  assert.equal(calls[0].options.redirect, 'error');
});
test('URLs are freshly resolved, never cached', async () => {
  let calls = 0;
  const provider = new LosslessProvider({ fetchImpl: async () => json(++calls % 2 ? { tracks: [track] } : flac) });
  await provider.resolve(song); await provider.resolve(song);
  assert.equal(calls, 4);
});
test('invalid signed IDs never reach the network', async () => {
  const provider = new LosslessProvider({ fetchImpl: () => assert.fail('must not fetch') });
  await assert.rejects(provider.stream('../x'), code('TRACK_ID'));
});
test('no match never calls stream endpoint', async () => {
  let calls = 0;
  const provider = new LosslessProvider({ fetchImpl: async () => { calls++; return json({ tracks: [] }); } });
  await assert.rejects(provider.resolve(song), code('NO_MATCH'));
  assert.equal(calls, 1);
});
test('HTTP errors, rate limiting, invalid JSON, missing tracks and oversized responses are bounded failures', async () => {
  for (const [response, expected] of [[json({}, 429), 'RATE_LIMITED'], [json({}, 503), 'HTTP_ERROR'], [json({}), 'INVALID_RESPONSE'], [new Response('<html>'), 'INVALID_RESPONSE'], [new Response('{', { headers: { 'content-type': 'application/json' } }), 'UNAVAILABLE'], [json({ data: 'x'.repeat(1024 * 1024) }), 'INVALID_RESPONSE']]) {
    const provider = new LosslessProvider({ fetchImpl: async () => response });
    await assert.rejects(provider.search('song'), code(expected));
  }
});
test('timeouts abort the transport', async () => {
  const provider = new LosslessProvider({ timeoutMs: 5, fetchImpl: (_, { signal }) => new Promise((_, reject) => signal.addEventListener('abort', () => reject(new Error('abort')), { once: true })) });
  await assert.rejects(provider.search('song'), code('TIMEOUT'));
});
test('cancellation is not treated as fallback', async () => {
  const controller = new AbortController(); controller.abort();
  await assert.rejects(resolvePlayback({ mode: 'lossless-first', song, signal: controller.signal, lossless: {}, existingResolve: () => assert.fail('fallback') }), code('ABORTED'));
});
test('lossless first uses honest existing fallback; strict mode does not', async () => {
  const lossless = { resolve: async () => { throw new ProviderError('NOT_FLAC', 'unavailable'); } };
  const existingResolve = async ({ mode }) => { assert.equal(mode, 'auto'); return { provider: 'saavn', codec: 'aac' }; };
  const result = await resolvePlayback({ mode: 'lossless-first', song, lossless, existingResolve });
  assert.equal(result.provider, 'saavn'); assert.equal(result.codec, 'aac'); assert.equal(result.fallbackReason, 'NOT_FLAC');
  await assert.rejects(resolvePlayback({ mode: 'lossless-only', song, lossless, existingResolve: () => assert.fail('fallback') }), code('NOT_FLAC'));
});
test('existing modes and downloads bypass the lossless provider', async () => {
  const lossless = { resolve: () => assert.fail('must not resolve lossless') };
  for (const mode of ['auto', 'saavn', 'youtube', 'lossless-first', 'lossless-only']) {
    const download = mode.startsWith('lossless-');
    const result = await resolvePlayback({ mode, song, lossless, download, existingResolve: async request => request });
    assert.equal(result.mode, download ? 'auto' : mode);
    assert.equal(result.download, download);
  }
});
test('programming errors are not hidden by fallback', async () => {
  await assert.rejects(resolvePlayback({ mode: 'lossless-first', song, lossless: { resolve: () => { throw new TypeError('bug'); } }, existingResolve: () => assert.fail('fallback') }), TypeError);
});
