package hu.evolver.uhc.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rroman on 6/3/16.
 */
public class KodiPlaylist {
    List<KodiFile> storageRoot = new ArrayList<>(); // note: later might want to separate playlist & storage handling?
    List<KodiFile> playlist = new ArrayList<>();

    private int audioPlaylistId = 0;

    public int getAudioPlaylistId() {
        return audioPlaylistId;
    }

    public void addSources(final JSONArray sources) {
        // TODO

    }

    public void gotPlaylists(final JSONArray playlists) {
        for (int i = 0; i < playlists.length(); ++i) {
            final String type = playlists.optString(i);
            if ("audio".equals(type)) {
                final int audioPlaylistId = playlists.optInt(i);
                this.audioPlaylistId = audioPlaylistId;
                Log.d("KodiPlaylist", "Got audio playlist id: " + audioPlaylistId);
            }
        }
    }

    public void clear() {
        storageRoot.clear();
        playlist.clear();
    }

}
