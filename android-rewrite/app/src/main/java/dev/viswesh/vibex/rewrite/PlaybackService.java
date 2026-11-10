package dev.viswesh.vibex.rewrite;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.PlaybackException;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import androidx.media3.session.SessionCommand;
import androidx.media3.session.SessionResult;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import android.os.Handler;
import android.os.Looper;

/** Owns playback AND queue resolution, including when the activity is gone. */
@androidx.annotation.OptIn(markerClass = androidx.media3.common.util.UnstableApi.class)
public final class PlaybackService extends MediaSessionService {
    static final SessionCommand QUEUE = new SessionCommand("dev.viswesh.vibex.QUEUE", Bundle.EMPTY);
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final List<Song> queue = new ArrayList<>();
    private ExoPlayer player;
    private MediaSession session;
    private Future<?> job;
    private long generation;
    private int index;
    private boolean destroyed;

    @Override public void onCreate() {
        super.onCreate();
        DefaultHttpDataSource.Factory http = new DefaultHttpDataSource.Factory()
                .setUserAgent("VibexNative/0.1").setConnectTimeoutMs(10000).setReadTimeoutMs(15000)
                .setAllowCrossProtocolRedirects(false);
        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(this).setDataSourceFactory(http)).build();
        player.setAudioAttributes(new AudioAttributes.Builder().setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true);
        player.setHandleAudioBecomingNoisy(true);
        player.setWakeMode(C.WAKE_MODE_NETWORK);
        player.addListener(new Player.Listener() {
            @Override public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED && index + 1 < queue.size()) play(index + 1);
            }
            @Override public void onPlayerError(PlaybackException error) {
                // Do not silently loop or claim lossy audio is FLAC. User may explicitly retry.
                publish("Playback failed. Check connection or retry; no automatic decoder fallback.");
            }
        });
        PendingIntent activity = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        session = new MediaSession.Builder(this, player).setSessionActivity(activity)
                .setCallback(new MediaSession.Callback() {
                    @Override public MediaSession.ConnectionResult onConnect(MediaSession s, MediaSession.ControllerInfo controller) {
                        if (controller.getUid() != Process.myUid() && !controller.isTrusted())
                            return MediaSession.ConnectionResult.reject();
                        MediaSession.ConnectionResult.AcceptedResultBuilder result = new MediaSession.ConnectionResult.AcceptedResultBuilder(s);
                        if (controller.getUid() == Process.myUid()) result.setAvailableSessionCommands(
                                MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon().add(QUEUE).build());
                        return result.build();
                    }
                    @Override public ListenableFuture<SessionResult> onCustomCommand(MediaSession s,
                            MediaSession.ControllerInfo controller, SessionCommand command, Bundle args) {
                        if (controller.getUid() != Process.myUid() || !command.customAction.equals(QUEUE.customAction))
                            return Futures.immediateFuture(new SessionResult(SessionResult.RESULT_ERROR_PERMISSION_DENIED));
                        try {
                            String action = args.getString("action", "play");
                            if (action.equals("stop")) {
                                generation++; if (job != null) job.cancel(true);
                                player.stop(); player.clearMediaItems(); queue.clear(); stopSelf();
                            } else if (action.equals("next")) play(index + 1);
                            else if (action.equals("previous")) play(index - 1);
                            else if (action.equals("retry")) play(index);
                            else if (action.equals("play")) {
                                String raw = args.getString("songs", "[]");
                                if (raw.length() > 200000) throw new IllegalArgumentException();
                                JSONArray rows = new JSONArray(raw);
                                if (rows.length() == 0 || rows.length() > 100) throw new IllegalArgumentException();
                                List<Song> next = new ArrayList<>();
                                for (int i = 0; i < rows.length(); i++) {
                                    Song song = Song.stored(rows.getJSONObject(i));
                                    if (song.id.isEmpty() || song.title.isEmpty()) throw new IllegalArgumentException();
                                    next.add(song);
                                }
                                int selected = args.getInt("index", 0);
                                if (selected < 0 || selected >= next.size()) throw new IllegalArgumentException();
                                queue.clear(); queue.addAll(next); play(selected);
                            } else throw new IllegalArgumentException();
                            return Futures.immediateFuture(new SessionResult(SessionResult.RESULT_SUCCESS));
                        } catch (Exception invalid) {
                            return Futures.immediateFuture(new SessionResult(SessionResult.RESULT_ERROR_BAD_VALUE));
                        }
                    }
                }).build();
    }
    private void publish(String message) {
        Bundle extras = new Bundle(); extras.putString("status", message);
        session.setSessionExtras(extras);
    }
    private void play(int selected) {
        if (selected < 0 || selected >= queue.size() || destroyed) return;
        index = selected;
        long token = ++generation;
        if (job != null) job.cancel(true);
        player.stop(); player.clearMediaItems();
        Song song = queue.get(index);
        android.content.SharedPreferences prefs = getSharedPreferences("vibex", MODE_PRIVATE);
        String api = prefs.getString("api", ""), mode = prefs.getString("mode", "saavn");
        publish("Resolving “" + song.title + "”…");
        job = io.submit(() -> {
            try {
                Providers.Rendition result = Providers.resolve(api, mode, song);
                if (Thread.currentThread().isInterrupted()) return;
                main.post(() -> {
                    if (destroyed || token != generation) return;
                    MediaMetadata metadata = new MediaMetadata.Builder().setTitle(song.title).setArtist(song.artist)
                            .setAlbumTitle(song.album).setSubtitle(result.source).build();
                    player.setMediaItem(new MediaItem.Builder().setMediaId(song.id).setUri(result.url)
                            .setMimeType(result.mime).setMediaMetadata(metadata).build());
                    player.prepare(); player.play(); publish(result.source);
                });
            } catch (Exception error) {
                if (Thread.currentThread().isInterrupted()) return;
                // No signed URLs, response bodies, query strings or credentials in UI/logging.
                main.post(() -> { if (!destroyed && token == generation) publish("Unable to resolve this recording. Check provider settings, connection or try JioSaavn."); });
            }
        });
    }
    @Override public MediaSession onGetSession(MediaSession.ControllerInfo controller) {
        return controller.getUid() == Process.myUid() || controller.isTrusted() ? session : null;
    }
    @Override public void onTaskRemoved(Intent rootIntent) {
        if (player == null || (!player.getPlayWhenReady() && (job == null || job.isDone()))) stopSelf();
    }
    @Override public void onDestroy() {
        destroyed = true; generation++;
        if (job != null) job.cancel(true);
        io.shutdownNow(); main.removeCallbacksAndMessages(null);
        if (session != null) { session.release(); session = null; }
        if (player != null) { player.release(); player = null; }
        super.onDestroy();
    }
}
