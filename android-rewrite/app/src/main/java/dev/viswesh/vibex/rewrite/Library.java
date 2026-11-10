package dev.viswesh.vibex.rewrite;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Small, private local library; no account, external storage or analytics. */
final class Library {
    private final SharedPreferences prefs;
    Library(Context context) { prefs = context.getSharedPreferences("library", Context.MODE_PRIVATE); }
    private JSONObject read() {
        try { return new JSONObject(prefs.getString("lists", "{}")); }
        catch (Exception invalid) { return new JSONObject(); }
    }
    synchronized List<String> names() {
        List<String> names = new ArrayList<>(); names.add("Saved songs");
        Iterator<String> keys = read().keys();
        while (keys.hasNext()) { String key = keys.next(); if (!key.equals("Saved songs")) names.add(key); }
        return names;
    }
    synchronized List<Song> songs(String name) {
        List<Song> result = new ArrayList<>(); JSONArray rows = read().optJSONArray(name);
        if (rows != null) for (int i = 0; i < rows.length(); i++) {
            JSONObject row = rows.optJSONObject(i); if (row != null) result.add(Song.stored(row));
        }
        return result;
    }
    synchronized void create(String name) throws Exception {
        name = name.trim();
        if (name.isEmpty() || name.length() > 60) throw new IllegalArgumentException("Playlist name must be 1–60 characters");
        JSONObject lists = read();
        if (!lists.has(name) && lists.length() >= 20) throw new IllegalArgumentException("Limit: 20 playlists");
        if (!lists.has(name)) lists.put(name, new JSONArray());
        prefs.edit().putString("lists", lists.toString()).apply();
    }
    synchronized void add(String name, Song song) throws Exception {
        create(name);
        JSONObject lists = read(); JSONArray rows = lists.getJSONArray(name);
        for (int i = 0; i < rows.length(); i++) if (rows.getJSONObject(i).optString("id").equals(song.id)) return;
        if (rows.length() >= 100) throw new IllegalArgumentException("Limit: 100 songs per playlist");
        rows.put(song.json()); lists.put(name, rows); prefs.edit().putString("lists", lists.toString()).apply();
    }
    synchronized void remove(String name, String id) throws Exception {
        JSONObject lists = read(); JSONArray rows = lists.optJSONArray(name), next = new JSONArray();
        if (rows == null) return;
        for (int i = 0; i < rows.length(); i++) if (!rows.getJSONObject(i).optString("id").equals(id)) next.put(rows.get(i));
        lists.put(name, next); prefs.edit().putString("lists", lists.toString()).apply();
    }
    synchronized void clear() { prefs.edit().clear().apply(); }
}
