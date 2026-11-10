package dev.viswesh.vibex.rewrite;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.widget.*;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.session.MediaController;
import androidx.media3.session.SessionToken;
import androidx.media3.session.SessionResult;
import com.google.common.util.concurrent.ListenableFuture;
import org.json.JSONArray;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
public final class MainActivity extends Activity {
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private SharedPreferences prefs;
    private Library library;
    private MediaController controller;
    private ListenableFuture<MediaController> connection;
    private LinearLayout root, results;
    private TextView status, now;
    private SeekBar seek;
    private EditText query;
    private final List<Song> visible = new ArrayList<>();
    private Future<?> searchJob;
    private long searchGeneration;
    private boolean destroyed;
    private String currentList;
    private String lastSessionStatus;
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (destroyed) return;
            if (controller != null) {
                long duration = controller.getDuration();
                if (!seek.isPressed()) seek.setProgress(duration > 0 ? (int) (controller.getCurrentPosition() * 1000 / duration) : 0);
                MediaMetadata metadata = controller.getMediaMetadata();
                now.setText(metadata.title == null ? "Nothing playing" : metadata.title + "\n" + metadata.artist);
                String playbackStatus = controller.getSessionExtras().getString("status");
                if (playbackStatus != null && !playbackStatus.equals(lastSessionStatus)) {
                    status.setText(playbackStatus); lastSessionStatus = playbackStatus;
                }
            }
            main.postDelayed(this, 1000);
        }
    };
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("vibex", MODE_PRIVATE); library = new Library(this);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24, 16, 24, 16);
        root.setBackgroundColor(Color.rgb(16, 21, 18));
        setContentView(root);
        if (Build.VERSION.SDK_INT >= 30) root.setOnApplyWindowInsetsListener((view, insets) -> {
            android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
            view.setPadding(24 + bars.left, 16 + bars.top, 24 + bars.right, 16 + bars.bottom); return insets;
        });
        TextView heading = label("VIBEX", 30); root.addView(heading);
        root.addView(label("Your music. Your choice of source.", 14));
        LinearLayout navigation = row();
        navigation.addView(button("Search", () -> { cancelSearch(); currentList = null; visible.clear(); results.removeAllViews(); status.setText("Search the configured JioSaavn catalog."); }));
        navigation.addView(button("Library", this::chooseList));
        navigation.addView(button("Settings", this::settings)); root.addView(navigation);
        query = new EditText(this); query.setSingleLine(true); query.setHint("Song or artist"); query.setTextColor(Color.WHITE);
        query.setContentDescription("Search songs and artists"); root.addView(query);
        root.addView(button("Find music", this::search));
        ScrollView scroll = new ScrollView(this); results = new LinearLayout(this); results.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(results); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        now = label("Nothing playing", 18); root.addView(now);
        seek = new SeekBar(this); seek.setMax(1000); seek.setContentDescription("Playback position"); root.addView(seek);
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStartTrackingTouch(SeekBar bar) {}
            public void onProgressChanged(SeekBar bar, int progress, boolean user) {}
            public void onStopTrackingTouch(SeekBar bar) {
                if (controller != null && controller.getDuration() > 0) controller.seekTo(controller.getDuration() * bar.getProgress() / 1000);
            }
        });
        LinearLayout controls = row();
        controls.addView(button("Previous", () -> command("previous")));
        controls.addView(button("Play / pause", () -> {
            if (controller == null) return;
            if (controller.isPlaying()) controller.pause(); else controller.play();
        }));
        controls.addView(button("Next", () -> command("next"))); root.addView(controls);
        LinearLayout recovery = row(); recovery.addView(button("Retry", () -> command("retry")));
        recovery.addView(button("Stop", () -> command("stop"))); root.addView(recovery);
        status = label("Source preview · configure your JioSaavn API in Settings", 13); root.addView(status);
        root.addView(label("Made with ♥ by VISWESHSARAVAN", 12));
        connection = new MediaController.Builder(this, new SessionToken(this, new ComponentName(this, PlaybackService.class))).buildAsync();
        connection.addListener(() -> {
            if (destroyed) return;
            try {
                controller = connection.get();
                controller.addListener(new Player.Listener() {
                    @Override public void onPlayerError(androidx.media3.common.PlaybackException error) {
                        status.setText("Playback failed. Retry or change the provider in Settings.");
                    }
                });
            } catch (Exception error) { status.setText("Could not connect to the playback service."); }
        }, ContextCompat.getMainExecutor(this));
        if (!prefs.getBoolean("intro", false)) new AlertDialog.Builder(this).setTitle("Vibex source preview")
                .setMessage("No accounts, ads or analytics. Search sends queries to your configured API. Playback contacts audio providers. Lossless is optional and can use substantially more data. This is unbuilt, unverified preview source—not a security-certified APK.")
                .setPositiveButton("Continue", (d, w) -> prefs.edit().putBoolean("intro", true).apply()).show();
    }
    @Override protected void onStart() { super.onStart(); main.post(tick); }
    @Override protected void onStop() { main.removeCallbacks(tick); super.onStop(); }
    private LinearLayout row() { LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); return row; }
    private TextView label(String text, int size) {
        TextView label = new TextView(this); label.setText(text); label.setTextColor(Color.rgb(229, 237, 226));
        label.setTextSize(size); label.setPadding(0, 8, 0, 8); return label;
    }
    private Button button(String text, Runnable action) {
        Button button = new Button(this); button.setText(text); button.setAllCaps(false); button.setMinHeight(48);
        button.setOnClickListener(v -> action.run()); return button;
    }
    private void cancelSearch() { searchGeneration++; if (searchJob != null) searchJob.cancel(true); }
    private void search() {
        String api = prefs.getString("api", "");
        if (api.isEmpty()) { settings(); return; }
        String term = query.getText().toString(); cancelSearch(); long token = searchGeneration;
        currentList = null; status.setText("Searching…");
        searchJob = io.submit(() -> {
            try {
                List<Song> songs = Providers.search(api, term);
                main.post(() -> { if (!destroyed && token == searchGeneration) { visible.clear(); visible.addAll(songs); render(); status.setText(songs.size() + " results"); } });
            } catch (Exception error) {
                main.post(() -> { if (!destroyed && token == searchGeneration) status.setText("Search unavailable. Check the HTTPS API URL and connection."); });
            }
        });
    }
    private void render() {
        results.removeAllViews();
        if (visible.isEmpty()) results.addView(label("No songs here yet.", 16));
        for (int i = 0; i < visible.size(); i++) {
            int selected = i; Song song = visible.get(i);
            Button item = button(song.title + "\n" + song.artist + " · " + song.duration / 60 + ":" + String.format(java.util.Locale.ROOT, "%02d", song.duration % 60), () -> play(selected));
            item.setOnLongClickListener(v -> { songActions(song); return true; }); results.addView(item);
        }
        results.addView(label("Tap to play this list. Long-press a song to save or manage it.", 13));
    }
    private void send(Bundle args) {
        if (controller == null) { status.setText("Playback service is not connected yet."); return; }
        ListenableFuture<SessionResult> result = controller.sendCustomCommand(PlaybackService.QUEUE, args);
        result.addListener(() -> {
            if (destroyed) return;
            try { if (result.get().resultCode != SessionResult.RESULT_SUCCESS) status.setText("Playback command was rejected."); }
            catch (Exception error) { status.setText("Playback service disconnected."); }
        }, ContextCompat.getMainExecutor(this));
    }
    private void command(String action) { Bundle args = new Bundle(); args.putString("action", action); send(args); }
    private void play(int selected) {
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        try {
            JSONArray rows = new JSONArray(); for (Song song : visible) rows.put(song.json());
            Bundle args = new Bundle(); args.putString("action", "play"); args.putString("songs", rows.toString()); args.putInt("index", selected); send(args);
        } catch (Exception error) { status.setText("Unable to prepare this queue."); }
    }
    private void chooseList() {
        cancelSearch(); List<String> names = library.names(); names.add("+ New playlist");
        new AlertDialog.Builder(this).setTitle("Your library").setItems(names.toArray(new String[0]), (dialog, which) -> {
            if (which == names.size() - 1) {
                EditText name = new EditText(this); name.setHint("Playlist name");
                new AlertDialog.Builder(this).setTitle("New playlist").setView(name).setPositiveButton("Create", (d, w) -> {
                    try { library.create(name.getText().toString()); chooseList(); } catch (Exception error) { status.setText(error.getMessage()); }
                }).setNegativeButton("Cancel", null).show();
            } else { currentList = names.get(which); visible.clear(); visible.addAll(library.songs(currentList)); render(); status.setText(currentList); }
        }).show();
    }
    private void songActions(Song song) {
        List<String> names = library.names(); String[] actions = currentList == null ? new String[]{"Save to playlist"} : new String[]{"Save to playlist", "Remove from this list"};
        new AlertDialog.Builder(this).setTitle(song.title).setItems(actions, (dialog, action) -> {
            if (action == 1) {
                try { library.remove(currentList, song.id); visible.clear(); visible.addAll(library.songs(currentList)); render(); }
                catch (Exception error) { status.setText("Could not update the library."); }
            } else new AlertDialog.Builder(this).setTitle("Save to…").setItems(names.toArray(new String[0]), (d, which) -> {
                try { library.add(names.get(which), song); status.setText("Saved to " + names.get(which)); }
                catch (Exception error) { status.setText(error.getMessage()); }
            }).show();
        }).show();
    }
    private void settings() {
        LinearLayout form = new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(24, 8, 24, 8);
        form.addView(label("JioSaavn API base URL (v2 schema)", 15));
        EditText api = new EditText(this); api.setSingleLine(true); api.setText(prefs.getString("api", "")); api.setHint("https://your-api.example/api"); form.addView(api);
        form.addView(label("Source for the next play/retry", 15)); Spinner source = new Spinner(this);
        String[] values = {"saavn", "lossless-first", "lossless-only"};
        source.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"JioSaavn · 320 kbps", "Lossless first · JioSaavn fallback", "Lossless only · no lossy fallback"}));
        for (int i = 0; i < values.length; i++) if (values[i].equals(prefs.getString("mode", "saavn"))) source.setSelection(i);
        form.addView(source);
        form.addView(label("Lossless sends song title and performer to " + Providers.LOSSLESS + " and its upstreams. It may use more data, fail, or return unsupported formats. No downloads. FLAC metadata is not proof of bit-perfect output.", 13));
        form.addView(button("Delete local library", () -> new AlertDialog.Builder(this).setTitle("Delete all saved songs and playlists?")
                .setPositiveButton("Delete", (d, w) -> { library.clear(); visible.clear(); render(); }).setNegativeButton("Cancel", null).show()));
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Playback & privacy").setView(form)
                .setPositiveButton("Save", null).setNegativeButton("Cancel", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String endpoint = api.getText().toString().trim().replaceAll("/+$", "");
            try {
                // Syntax check only here. Public-address/DNS checks run on the IO executor.
                URI uri = new URI(endpoint);
                if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null || uri.getPort() != -1) throw new IllegalArgumentException();
                prefs.edit().putString("api", endpoint).putString("mode", values[source.getSelectedItemPosition()]).apply();
                cancelSearch(); status.setText("Settings saved. Next play/retry uses the selected source."); dialog.dismiss();
            } catch (Exception error) { api.setError("Enter a valid HTTPS API base URL without credentials, query or fragment."); }
        })); dialog.show();
    }
    @Override protected void onDestroy() {
        destroyed = true; cancelSearch(); io.shutdownNow(); main.removeCallbacksAndMessages(null);
        if (connection != null) MediaController.releaseFuture(connection);
        super.onDestroy();
    }
}
