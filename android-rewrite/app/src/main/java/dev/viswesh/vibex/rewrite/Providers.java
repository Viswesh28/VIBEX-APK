package dev.viswesh.vibex.rewrite;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Blocking IO; callers must use an executor, never the UI or Media3 application thread. */
final class Providers {
    static final String LOSSLESS = "https://lossless-extension-five.vercel.app";
    static final class Rendition {
        final String url, mime, source;
        Rendition(String url, String mime, String source) { this.url = url; this.mime = mime; this.source = source; }
    }
    static final class Unavailable extends IOException {
        Unavailable(String message) { super(message); }
    }
    static URI safeUrl(String value) throws IOException {
        try {
            URI u = new URI(value);
            String host = u.getHost();
            if (!"https".equalsIgnoreCase(u.getScheme()) || host == null || u.getRawUserInfo() != null ||
                    u.getPort() != -1 || u.getFragment() != null || !host.contains(".") || host.endsWith(".local"))
                throw new IOException("A public HTTPS endpoint is required");
            for (InetAddress address : InetAddress.getAllByName(host)) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress() ||
                        address.isSiteLocalAddress() || address.isMulticastAddress() ||
                        (address.getAddress().length == 16 && (address.getAddress()[0] & 0xfe) == 0xfc))
                    throw new IOException("Private network URLs are not supported");
            }
            return u;
        } catch (java.net.URISyntaxException e) { throw new IOException("Invalid HTTPS URL", e); }
    }
    static String base(String value) throws IOException {
        URI u = safeUrl(value.trim());
        if (u.getQuery() != null) throw new IOException("Endpoint must not contain a query");
        return u.toString().replaceAll("/+$", "");
    }
    static String encoded(String value) {
        try { return URLEncoder.encode(value, "UTF-8"); }
        catch (java.io.UnsupportedEncodingException impossible) { throw new AssertionError(impossible); }
    }
    static JSONObject get(String url) throws Exception {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
        HttpURLConnection c = (HttpURLConnection) safeUrl(url).toURL().openConnection();
        c.setConnectTimeout(8000); c.setReadTimeout(8000); c.setInstanceFollowRedirects(false);
        c.setRequestProperty("Accept", "application/json");
        try {
            int status = c.getResponseCode();
            if (status < 200 || status >= 300) throw new Unavailable("Provider HTTP " + status);
            String contentType = c.getContentType();
            if (contentType == null || !contentType.toLowerCase(Locale.ROOT).contains("application/json"))
                throw new Unavailable("Provider did not return JSON");
            try (InputStream in = c.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192]; int count;
                while ((count = in.read(buffer)) != -1) {
                    if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
                    if (out.size() + count > 1024 * 1024) throw new Unavailable("Provider response too large");
                    out.write(buffer, 0, count);
                }
                return new JSONObject(new String(out.toByteArray(), StandardCharsets.UTF_8));
            }
        } finally { c.disconnect(); }
    }
    static List<Song> search(String api, String query) throws Exception {
        String q = query.trim();
        if (q.isEmpty() || q.length() > 200) throw new Unavailable("Enter 1–200 search characters");
        JSONObject root = get(base(api) + "/search/songs?query=" + encoded(q) + "&limit=20");
        JSONObject data = root.optJSONObject("data");
        JSONArray rows = data == null ? null : data.optJSONArray("results");
        if (rows == null) throw new Unavailable("Unsupported JioSaavn API response (v2 schema required)");
        List<Song> songs = new ArrayList<>();
        for (int i = 0; i < Math.min(rows.length(), 50); i++) {
            try { songs.add(Song.fromApi(rows.getJSONObject(i))); } catch (org.json.JSONException ignored) { /* malformed row only */ }
        }
        return songs;
    }
    static Rendition saavn(String api, Song song) throws Exception {
        // Resolve by stable ID on every play, not by an expired stored CDN URL.
        JSONObject root = get(base(api) + "/songs/" + encoded(song.id));
        JSONArray rows = root.optJSONArray("data");
        if (rows == null || rows.length() == 0) throw new Unavailable("Song unavailable on JioSaavn");
        Song fresh = Song.fromApi(rows.getJSONObject(0));
        if (!fresh.id.equals(song.id)) throw new Unavailable("Provider returned a different song");
        if (fresh.urls != null) for (int i = 0; i < fresh.urls.length(); i++) {
            JSONObject row = fresh.urls.optJSONObject(i);
            if (row != null && "320kbps".equals(row.optString("quality"))) {
                String url = row.optString("url"); safeUrl(url);
                return new Rendition(url, "audio/mp4", "JioSaavn · 320 kbps requested");
            }
        }
        throw new Unavailable("No 320 kbps rendition is available");
    }
    static String norm(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFKD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }
    static Set<String> words(String s) {
        Set<String> result = new HashSet<>(Arrays.asList(norm(s).split(" +"))); result.remove(""); return result;
    }
    static double overlap(String a, String b) {
        Set<String> x = words(a), y = words(b);
        int size = Math.max(x.size(), y.size()); if (size == 0) return 0;
        x.retainAll(y); return (double) x.size() / size;
    }
    static Set<String> version(String title) {
        Set<String> result = words(title);
        result.retainAll(Arrays.asList("live", "remix", "karaoke", "instrumental", "cover", "sped", "slowed", "acoustic"));
        return result;
    }
    static double score(Song song, JSONObject row) {
        String title = row.optString("title"), artist = row.optString("artist");
        double titleScore = overlap(song.title, title), artistScore = overlap(song.artist, artist);
        double duration = row.optDouble("duration", 0);
        if ((Double.isNaN(duration) || Double.isInfinite(duration)) || duration <= 0 || song.duration <= 0 || titleScore < .85 || artistScore < .6 ||
                Math.abs(song.duration - duration) > Math.max(3, song.duration * .02) || !version(song.title).equals(version(title))) return 0;
        return titleScore * .65 + artistScore * .3 + overlap(song.album, row.optString("album")) * .05;
    }
    static Rendition lossless(Song song) throws Exception {
        if (song.artist.isEmpty() || song.duration <= 0) throw new Unavailable("Missing recording identity for lossless matching");
        JSONObject root = get(LOSSLESS + "/search?q=" + encoded(song.title + " " + song.artist) + "&quality=LOSSLESS");
        JSONArray rows = root.optJSONArray("tracks");
        if (rows == null) throw new Unavailable("Invalid lossless search response");
        double best = 0, second = 0; String id = "";
        for (int i = 0; i < Math.min(rows.length(), 100); i++) {
            JSONObject row = rows.optJSONObject(i); if (row == null) continue;
            String candidate = row.optString("id");
            if (!candidate.matches("qobuz_[A-Za-z0-9_-]{10,120}") || candidate.equals(id)) continue;
            double s = score(song, row);
            if (s > best) { second = best; best = s; id = candidate; }
            else second = Math.max(second, s);
        }
        if (best < .9 || best - second < .03) throw new Unavailable("No unambiguous lossless recording match");
        JSONObject stream = get(LOSSLESS + "/stream/" + encoded(id) + "?quality=LOSSLESS");
        String codec = stream.optString("codec").toLowerCase(Locale.ROOT);
        String format = stream.optString("format").toLowerCase(Locale.ROOT);
        String container = stream.optString("container").toLowerCase(Locale.ROOT);
        String manifest = stream.optString("manifest");
        boolean flac = "flac".equals(codec) || Arrays.asList("flac", "audio/flac", "audio/x-flac").contains(format);
        if (!flac || (!codec.isEmpty() && !codec.equals("flac")) ||
                (!format.isEmpty() && !Arrays.asList("flac", "audio/flac", "audio/x-flac").contains(format)) ||
                (!container.isEmpty() && !container.equals("flac")) || stream.optBoolean("encrypted") ||
                (!manifest.isEmpty() && !manifest.equals("none"))) throw new Unavailable("No direct unencrypted FLAC rendition");
        String url = stream.optString("url"); safeUrl(url);
        return new Rendition(url, "audio/flac", "Lossless provider · FLAC reported (not byte-verified)");
    }
    static Rendition resolve(String api, String mode, Song song) throws Exception {
        if (!Arrays.asList("saavn", "lossless-first", "lossless-only").contains(mode)) throw new Unavailable("Unknown provider mode");
        if (!mode.equals("saavn")) {
            try { return lossless(song); }
            catch (InterruptedException cancelled) { throw cancelled; }
            catch (IOException | org.json.JSONException error) {
                if (mode.equals("lossless-only") || Thread.currentThread().isInterrupted()) throw error;
                Rendition fallback = saavn(api, song);
                return new Rendition(fallback.url, fallback.mime, fallback.source + " · lossless unavailable");
            }
        }
        return saavn(api, song);
    }
}
