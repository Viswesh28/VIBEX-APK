// Original VIBEX client implementation. No upstream server code is bundled.
export const DEFAULT_ENDPOINT = 'https://lossless-extension-five.vercel.app';
export class ProviderError extends Error {
  constructor(code, message) { super(message); this.name = 'ProviderError'; this.code = code; }
}
const fail = (code, message) => { throw new ProviderError(code, message); };
function httpsUrl(value) {
  let u;
  try { u = new URL(value); } catch { fail('INVALID_URL', 'Invalid provider URL'); }
  const host = u.hostname.toLowerCase();
  if (u.protocol !== 'https:' || u.username || u.password || u.port ||
      host === 'localhost' || host.endsWith('.localhost') || host.endsWith('.local') ||
      host.includes(':') || /^\d+(\.\d+){3}$/.test(host) || !host.includes('.')) {
    fail('INVALID_URL', 'A public HTTPS URL without credentials or a custom port is required');
  }
  return u;
}
const normalize = value => String(value ?? '').normalize('NFKD').replace(/\p{M}/gu, '')
  .toLowerCase().replace(/[^\p{L}\p{N}]+/gu, ' ').trim();
const tokens = value => new Set(normalize(value).split(' ').filter(Boolean));
function overlap(a, b) {
  const x = tokens(a), y = tokens(b);
  if (!x.size || !y.size) return 0;
  return [...x].filter(t => y.has(t)).length / Math.max(x.size, y.size);
}
const versions = ['live', 'remix', 'karaoke', 'instrumental', 'cover', 'sped', 'slowed', 'acoustic'];
function versionKey(title) { const t = tokens(title); return versions.filter(v => t.has(v)).join('|'); }
// Conservative cross-catalog matching. Missing identity evidence is not a match.
export function matchScore(song, candidate) {
  if (!song || !candidate || versionKey(song.title) !== versionKey(candidate.title)) return 0;
  const title = overlap(song.title, candidate.title);
  const artist = overlap(song.artist, candidate.artist);
  const duration = Number(song.duration), other = Number(candidate.duration);
  if (title < .85 || artist < .6 || !Number.isFinite(duration) || !Number.isFinite(other) ||
      duration <= 0 || other <= 0 || Math.abs(duration - other) > Math.max(3, duration * .02)) return 0;
  return title * .65 + artist * .3 + overlap(song.album, candidate.album) * .05;
}
export function pickMatch(song, tracks) {
  const ranked = tracks.map(track => ({ track, score: matchScore(song, track) }))
    .filter(x => x.score >= .9).sort((a, b) => b.score - a.score);
  if (!ranked.length) return null;
  // Duplicate results for the same signed ID do not count as ambiguity.
  const next = ranked.find(x => x.track.id !== ranked[0].track.id);
  if (next && ranked[0].score - next.score < .03) return null;
  return ranked[0].track;
}
export function validateRendition(stream) {
  if (!stream || typeof stream !== 'object') fail('INVALID_STREAM', 'Missing stream metadata');
  const url = httpsUrl(stream.url);
  const codec = String(stream.codec ?? '').toLowerCase();
  const format = String(stream.format ?? '').toLowerCase();
  const container = String(stream.container ?? '').toLowerCase();
  // Quality is not proof: upstream can default quality to the requested value.
  if (stream.encrypted === true || (stream.manifest && stream.manifest !== 'none') ||
      (codec && codec !== 'flac') || (container && container !== 'flac') ||
      (format && !['flac', 'audio/flac', 'audio/x-flac'].includes(format)) ||
      !(codec === 'flac' || ['flac', 'audio/flac', 'audio/x-flac'].includes(format))) {
    fail('NOT_FLAC', 'No supported direct, unencrypted FLAC rendition');
  }
  const positive = n => Number.isFinite(n) && n > 0 ? n : undefined;
  return Object.freeze({ url: url.href, mimeType: 'audio/flac', provider: 'lossless',
    codec: 'flac', qualityEvidence: 'provider-metadata', verifiedBytes: false,
    sampleRate: positive(stream.sampleRate), bitDepth: positive(stream.bitDepth),
    bitrate: positive(stream.bitrate), allowDownloads: false });
}
export class LosslessProvider {
  constructor({ endpoint = DEFAULT_ENDPOINT, fetchImpl = globalThis.fetch, timeoutMs = 8000 } = {}) {
    const u = httpsUrl(endpoint);
    if (u.search || u.hash) fail('INVALID_URL', 'Provider base URL cannot contain a query or fragment');
    if (!Number.isFinite(timeoutMs) || timeoutMs <= 0 || timeoutMs > 30000) fail('CONFIG', 'Invalid timeout');
    if (typeof fetchImpl !== 'function') fail('CONFIG', 'fetch implementation required');
    this.endpoint = u.href.replace(/\/+$/, ''); this.fetch = fetchImpl; this.timeoutMs = timeoutMs;
  }
  async request(path, signal) {
    const controller = new AbortController();
    const abort = () => controller.abort(signal.reason);
    if (signal?.aborted) fail('ABORTED', 'Request cancelled');
    signal?.addEventListener('abort', abort, { once: true });
    let timedOut = false;
    const timer = setTimeout(() => { timedOut = true; controller.abort(); }, this.timeoutMs);
    try {
      const response = await this.fetch(`${this.endpoint}${path}`, {
        signal: controller.signal, headers: { Accept: 'application/json' },
        credentials: 'omit', redirect: 'error', cache: 'no-store',
      });
      if (!response.ok) fail(response.status === 429 ? 'RATE_LIMITED' : 'HTTP_ERROR', `Provider HTTP ${response.status}`);
      if (!(response.headers.get('content-type') ?? '').includes('application/json')) fail('INVALID_RESPONSE', 'Expected JSON');
      // Read bounded data; do not buffer an unbounded response before checking size.
      const reader = response.body.getReader();
      const parts = []; let size = 0;
      try {
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          size += value.byteLength;
          if (size > 1024 * 1024) { await reader.cancel(); fail('INVALID_RESPONSE', 'Response exceeds 1 MiB'); }
          parts.push(value);
        }
      } finally { reader.releaseLock(); }
      const bytes = new Uint8Array(size); let offset = 0;
      for (const part of parts) { bytes.set(part, offset); offset += part.byteLength; }
      return JSON.parse(new TextDecoder().decode(bytes));
    } catch (error) {
      if (signal?.aborted) fail('ABORTED', 'Request cancelled');
      if (timedOut) fail('TIMEOUT', 'Lossless provider timed out');
      if (error instanceof ProviderError) throw error;
      fail('UNAVAILABLE', 'Lossless provider returned invalid data or is unreachable');
    } finally { clearTimeout(timer); signal?.removeEventListener('abort', abort); }
  }
  async search(query, { signal } = {}) {
    const q = String(query ?? '').trim();
    if (!q || q.length > 300) fail('QUERY', 'Search needs 1–300 characters');
    const body = await this.request(`/search?${new URLSearchParams({ q, quality: 'LOSSLESS' })}`, signal);
    if (!Array.isArray(body?.tracks)) fail('INVALID_RESPONSE', 'Missing tracks array');
    return body.tracks.slice(0, 100).filter(t => t && typeof t.title === 'string' &&
      typeof t.id === 'string' && /^qobuz_[A-Za-z0-9_-]{10,120}$/.test(t.id));
  }
  async stream(id, { signal } = {}) {
    if (typeof id !== 'string' || !/^qobuz_[A-Za-z0-9_-]{10,120}$/.test(id)) fail('TRACK_ID', 'Invalid signed track ID');
    return validateRendition(await this.request(`/stream/${encodeURIComponent(id)}?quality=LOSSLESS`, signal));
  }
  async resolve(song, { signal } = {}) {
    if (!song?.title || !song.artist || !(Number(song.duration) > 0)) fail('NO_MATCH', 'Title, performer and duration required');
    const tracks = await this.search(`${song.title} ${song.artist}`.slice(0, 300), { signal });
    const match = pickMatch(song, tracks);
    if (!match) fail('NO_MATCH', 'No unambiguous matching recording');
    // Signed IDs and expiring media URLs are deliberately never persisted or cached.
    return { ...await this.stream(match.id, { signal }), matchedTrackId: match.id };
  }
}
/** A routing seam, NOT an installed app playback engine. Inject the existing resolver. */
export async function resolvePlayback({ mode, song, lossless, existingResolve, signal, download = false }) {
  if (!['auto', 'saavn', 'youtube', 'lossless-first', 'lossless-only'].includes(mode)) fail('MODE', 'Unknown audio source');
  if (signal?.aborted) fail('ABORTED', 'Request cancelled');
  if (download || !mode.startsWith('lossless-')) return existingResolve({ mode: download && mode.startsWith('lossless-') ? 'auto' : mode, song, signal, download });
  try { return await lossless.resolve(song, { signal }); }
  catch (error) {
    if (signal?.aborted || error?.code === 'ABORTED' || mode === 'lossless-only') throw error;
    if (!(error instanceof ProviderError)) throw error;
    const fallback = await existingResolve({ mode: 'auto', song, signal, download: false });
    return { ...fallback, fallbackReason: error.code }; // retain actual source/codec; never label fallback FLAC
  }
}
