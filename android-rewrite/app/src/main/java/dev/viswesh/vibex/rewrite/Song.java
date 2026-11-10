package dev.viswesh.vibex.rewrite;

import android.text.Html;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class Song {
    final String id, title, artist, album, artwork;
    final int duration;
    final JSONArray urls;
    Song(String id, String title, String artist, String album, String artwork, int duration, JSONArray urls) {
        this.id = id; this.title = title; this.artist = artist; this.album = album;
        this.artwork = artwork; this.duration = duration; this.urls = urls;
    }
    static String text(String s) { return Html.fromHtml(s).toString(); }
    static Song fromApi(JSONObject row) throws JSONException {
        JSONObject artists = row.optJSONObject("artists");
        JSONArray primary = artists == null ? null : artists.optJSONArray("primary");
        StringBuilder performer = new StringBuilder();
        if (primary != null) for (int i = 0; i < primary.length(); i++) {
            JSONObject artist = primary.optJSONObject(i);
            if (artist == null) continue;
            if (performer.length() > 0) performer.append(", ");
            performer.append(text(artist.optString("name")));
        }
        // No composer-first fallback for cross-catalog lossless matching.
        JSONObject album = row.optJSONObject("album");
        JSONArray images = row.optJSONArray("image");
        String image = "";
        if (images != null && images.length() > 0) {
            JSONObject last = images.optJSONObject(images.length() - 1);
            if (last != null) image = last.optString("url");
        }
        String id = row.getString("id"), title = text(row.getString("name"));
        if (id.isEmpty() || title.isEmpty()) throw new JSONException("Missing song identity");
        return new Song(id, title, performer.toString(), album == null ? "" : text(album.optString("name")),
                image, row.optInt("duration"), row.optJSONArray("downloadUrl"));
    }
    JSONObject json() throws JSONException {
        // Never persist expiring media URLs or signed lossless IDs.
        return new JSONObject().put("id", id).put("title", title).put("artist", artist)
                .put("album", album).put("artwork", artwork).put("duration", duration);
    }
    static Song stored(JSONObject row) {
        return new Song(row.optString("id"), row.optString("title"), row.optString("artist"),
                row.optString("album"), row.optString("artwork"), row.optInt("duration"), null);
    }
}
