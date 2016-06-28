package hu.evolver.uhc.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import hu.evolver.uhc.comm.KodiTcpSender;
import hu.evolver.uhc.comm.KodiUpdateListener;

/**
 * Created by rroman on 6/3/16.
 */
public class KodiPlaylist {
    private KodiUpdateListener updateListener;
    private KodiTcpSender kodiTcpSender;

    // TODO populate with sources / root dir files;
    List<KodiItem> storageRoot = new ArrayList<>(); // note: later might want to separate playlist & storage handling?

    private int audioPlaylistId = 0;

    public KodiPlaylist(KodiUpdateListener updateListener, KodiTcpSender kodiTcpSender) {
        this.updateListener = updateListener;
        this.kodiTcpSender = kodiTcpSender;
    }

    public int getAudioPlaylistId() {
        return audioPlaylistId;
    }

    public void addSources(final JSONArray sources) {
        // TODO

    }

    public void onGetPlaylists(final JSONArray playlists) {
        if (playlists == null)
            return;

        for (int i = 0; i < playlists.length(); ++i) {
            JSONObject playlist = playlists.optJSONObject(i);
            if (playlist == null)
                return;

            final String type = playlist.optString("type");
            if ("audio".equals(type)) {
                final int audioPlaylistId = playlist.optInt("playlistid");
                this.audioPlaylistId = audioPlaylistId;
                Log.d("KodiPlaylist", "Got audio playlist id: " + audioPlaylistId);
                kodiTcpSender.sendPlaylistGetItems(audioPlaylistId);
            }
        }
    }

    public void onGetItems(final JSONArray items) {
        if (items == null)
            return;
        Log.d("KodiPlaylist", "onGetItems");

        ArrayList<KodiItem> itemArrayList = new ArrayList<>(items.length());

        for (int i = 0; i < items.length(); ++i) {
            JSONObject itemJSON = items.optJSONObject(i);
            if (itemJSON == null)
                return;

            KodiItem item = new KodiItem(itemJSON);
            itemArrayList.add(item);
        }

        updateListener.kodiPlaylistUpdate(itemArrayList);
    }

    public void clear() {
        storageRoot.clear();
    }

}
