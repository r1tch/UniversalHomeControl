package hu.evolver.uhc.comm;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import hu.evolver.uhc.model.KodiItem;
import hu.evolver.uhc.model.KodiPlayers;
import hu.evolver.uhc.model.KodiPlaylist;

/**
 * Created by rroman on 6/2/16.
 */
public class KodiConnection {
    private KodiUpdateListener updateListener = null;
    private KodiTcpSender kodiTcpSender = null;
    private KodiPlaylist kodiPlaylist = null;
    private KodiPlayers kodiPlayers = null;
    private KodiRoot kodiRoot = new KodiRoot();

    private double volumePercent = 0;
    private boolean isMuted = false;

    public KodiConnection(KodiUpdateListener updateListener, SimpleTcpClient simpleTcpClient) {
        this.updateListener = updateListener;
        this.kodiTcpSender = new KodiTcpSender(simpleTcpClient);
        this.kodiPlaylist = new KodiPlaylist(updateListener, kodiTcpSender);
        this.kodiPlayers = new KodiPlayers(updateListener, kodiTcpSender, kodiPlaylist);
    }

    public boolean isMuted() {
        return isMuted;
    }

    public double getVolumePercent() {
        return volumePercent;
    }

    public void onTcpConnected() {
        kodiTcpSender.sendApplicationGetProperties();
        kodiTcpSender.sendGetSourcesMusic();
        kodiPlayers.onTcpConnected();
        kodiRoot.clear();
        kodiTcpSender.sendGetPlaylists();

        // for sources, logic could be: 1) get sources; if single returned, get directory too & populate "root" with that
        //     (ie if dir list received & root empty -> make this the root)
        // later when selecting files: remember last dir requested, wait for it...
    }

    public boolean isAudioPlayerShuffleOn() {
        return kodiPlayers.isAudioPlayerShuffleOn();
    }

    public String getAudioPlayerRepeat() {
        return kodiPlayers.getAudioPlayerRepeat();
    }

    public void setAudioRepeat(String repeat) {
        kodiPlayers.setAudioRepeat(repeat);
    }

    public void setAudioShuffle(boolean shuffle) {
        kodiPlayers.setAudioShuffle(shuffle);
    }

    public void newUpdate(JSONObject jsonObject) {
        final String fromService = jsonObject.optString("fromService");
        if (!"kodi".equals(fromService))
            return;

        final String msg = jsonObject.optString("msg");
        if ("kodiReconnected".equals(msg)) {        // happens e.g upon Kodi reboot
            onTcpConnected();
            return;
        }

        final String id = jsonObject.optString("id");
        String idMethod = "";
        String extraId = "";
        if (id != null) {
            String[] methodAndExtraId = id.split(",", 2);
            if (methodAndExtraId.length > 1) {
                idMethod = methodAndExtraId[0];
                extraId = methodAndExtraId[1];
            }
        }

        final JSONObject errorObject = jsonObject.optJSONObject("error");
        if (errorObject != null) {
            parseErrorJSON(jsonObject, idMethod, extraId);
            return;
        }

        final JSONObject resultObject = jsonObject.optJSONObject("result");
        final JSONArray resultArray = jsonObject.optJSONArray("result");

        if ("Player.GetProperties".equals(idMethod)) {
            kodiPlayers.playerPropertiesUpdate(jsonObject.optJSONObject("result"), extraId);
            return;
        } else if ("Player.GetItem".equals(idMethod)) {
            parsePlayerItem(resultObject);
            return;
        } else if ("Playlist.GetItems".equals(idMethod)) {
            kodiPlaylist.onGetItems(resultObject.optJSONArray("items"));
            return;
        } else if ("Player.GetActivePlayers".equals(idMethod)) {
            kodiPlayers.updateActivePlayers(resultArray);
            return;
        } else if ("Files.GetSources".equals(idMethod)) {
            gotSources(resultObject);
            return;
        } else if ("Files.GetDirectory".equals(idMethod)) {
            parseGetDirectory(resultObject);
        }

        if (resultObject != null) {
            parseResultObject(resultObject);
            return;
        }

        if (resultArray != null) {
            parseResultArray(resultArray);
            return;
        }


        final String method = jsonObject.optString("method");
        if ("Player.OnPlay".equals(method)) {
            parseOnPlay(jsonObject.optJSONObject("params"));
        } else if ("Player.OnPause".equals(method)) {
            parseOnPause(jsonObject.optJSONObject("params"));
        } else if ("Player.OnStop".equals(method)) {
            parseOnStop();
        } else if ("Player.OnSeek".equals(method)) {
            parseOnSeek(jsonObject.optJSONObject("params"));
        } else if ("Playlist.OnClear".equals(method)) {
            updateListener.kodiClearAudioPlaylist();
        } else if ("Application.OnVolumeChanged".equals(method)) {
            parseOnVolumeChanged(jsonObject);
            return;
        } else if ("Playlist.OnAdd".equals(method)) {
            parseOnAdd(jsonObject.optJSONObject("params"));
            return;
        } else if ("Playlist.OnRemove".equals(method)) {
            parseOnRemove(jsonObject.optJSONObject("params"));
            return;
        }
    }

    private void parsePlayerItem(JSONObject resultObject) {
        final JSONObject item = resultObject.optJSONObject("item");
        if (item == null)
            return;

        String label = item.optString("label");
        String title = item.optString("title");
        String file = item.optString("file");

        if (label == null)
            label = title;

        if (label == null) {
            if (file == null)
                label = "";
            else {
                label = file.substring(file.lastIndexOf('/'));
            }
        }

        updateListener.kodiPlayingItem(label);
    }

    private void parseOnPlay(JSONObject params) {
        if (params == null)
            return;

        kodiPlayers.getFullUpdate();
/*
        try {
            int playerid = params.getJSONObject("data").getJSONObject("player").getInt("playerid");
            kodiTcpSender.sendPlayerGetProperties(playerid);    // we need this for proper playlist position update
        } catch (JSONException e) {
            Log.e("KodiConnection", e.toString());
        }
        */
    }

    private void parseOnPause(JSONObject params) {
        if (params == null)
            return;

        kodiPlayers.getFullUpdate();
/*
        try {
            int playerid = params.getJSONObject("data").getJSONObject("player").getInt("playerid");
            kodiPlayers.onPause(playerid);
        } catch (JSONException e) {
            Log.e("KodiConnection", e.toString());
        }
        */
    }

    private void parseOnStop() {
        updateListener.kodiOnStop();
        kodiPlayers.getFullUpdate();
    }

    private void parseOnSeek(JSONObject params) {
        if (params == null)
            return;

        try {
            // no, other info is not usable (outdated!)
            int playerid = params.getJSONObject("data").getJSONObject("player").getInt("playerid");
            kodiPlayers.onSeek(playerid);
        } catch (JSONException e) {
            Log.e("KodiConnection", "parseOnSeek - " + e.toString());
        }
    }

    private void parseOnAdd(JSONObject params) {
        if (params == null)
            return;

        try {
            int playlistid = params.getJSONObject("data").getInt("playlistid");
            if (playlistid != kodiPlaylist.getAudioPlaylistId())
                return;

            // note: id might be missing if item unknown!
            int itemid = params.getJSONObject("data").getJSONObject("item").optInt("id", -1);
            int position = params.getJSONObject("data").getInt("position");
            updateListener.kodiAddAudioPlaylistItem(position, itemid);
        } catch (JSONException e) {
            Log.e("KodiConnection", "parseOnAdd - " + e.toString());
        }
    }

    private void parseOnRemove(JSONObject params) {
        if (params == null)
            return;

        try {
            int playlistid = params.getJSONObject("data").getInt("playlistid");
            if (playlistid != kodiPlaylist.getAudioPlaylistId())
                return;

            int position = params.getJSONObject("data").getInt("position");
            updateListener.kodiRemoveAudioPlaylistItem(position);
        } catch (JSONException e) {
            Log.e("KodiConnection", "parseOnRemove - " + e.toString());
        }
    }

    private void parseErrorJSON(JSONObject jsonObject, String idMethod, String extraId) {
        if ("Files.GetDirectory".equals(idMethod))
            updateListener.kodiDirectoryUpdate(null);
        /*
        try {
            JSONObject data = jsonObject.getJSONObject("error").getJSONObject("data");
            String method = data.getString("method");
            String message = data.getJSONObject("stack").getString("message");
            if ("Player.GetProperties".equals(method) && message.startsWith("Value between 0")) {
                if (!kodiPlayers.isGettingPlayers()) {
                    Log.e("KodiConnection", "Error of invalid player received but not getting players");
                    return;
                }

                kodiPlayers.doneGettingPlayers();
            }
        } catch (JSONException e) {
        }
        */
    }

    private void parseResultArray(JSONArray resultArray) {
        if (resultArray.length() == 0)
            return;

        final JSONObject firstObject = resultArray.optJSONObject(0);
        if (firstObject == null)
            return;     // perfectly legit if empty result; eg list of active players empty if all stopped
        // {..., result: [{playlist1}, {playlist2}] }
        if (firstObject.has("playlistid")) {
            kodiPlaylist.onGetPlaylists(resultArray);
        }
    }

    private void parseResultObject(JSONObject resultObject) {

        if (resultObject.has("muted") && resultObject.has("volume")) {
            isMuted = resultObject.optBoolean("muted");
            volumePercent = resultObject.optDouble("volume");
            updateListener.kodiVolumeChanged(isMuted, volumePercent);
        }
    }

    private void parseOnVolumeChanged(JSONObject jsonObject) {
        final JSONObject paramsObject = jsonObject.optJSONObject("params");
        if (paramsObject == null || !paramsObject.has("data"))   // error msg?
            return;

        final JSONObject dataObject = paramsObject.optJSONObject("data");
        if (dataObject == null || !dataObject.has("muted") || !dataObject.has("volume"))
            return;

        isMuted = dataObject.optBoolean("muted");
        volumePercent = dataObject.optDouble("volume");
        updateListener.kodiVolumeChanged(isMuted, volumePercent);
        return;
    }

    private void gotSources(final JSONObject resultObject) {
        // {..., result: {sources: [{source1}, {source2}] } }
        final JSONArray sources = resultObject.optJSONArray("sources");
        if (sources == null)
            return;

        if (sources.length() == 0)
            return;

        kodiRoot.clear();
        if (sources.length() > 1) {
            for (int i = 0; i < sources.length(); ++i) {
                JSONObject source = sources.optJSONObject(i);
                if (source == null)
                    continue;

                KodiItem kodiItem = new KodiItem(source);
                kodiItem.filetype = "directory";
                kodiRoot.addSource(kodiItem);
            }
            return;
        }

        try {
            final JSONObject theOnlySource = sources.getJSONObject(0);
            if (theOnlySource == null)
                return;

            final String directory = theOnlySource.getString("file");
            kodiTcpSender.sendGetMusicDirectory(directory);
        } catch (JSONException e) {
            Log.e("KodiConnection", "getSources JSONException:" + e.getMessage());
        }
    }

    private void parseGetDirectory(final JSONObject resultObject) {
        final JSONArray files = resultObject.optJSONArray("files");
        if (files == null || files.length() == 0)
            return;

        List<KodiItem> kodiItems = new ArrayList<KodiItem>(files.length());

        for (int i = 0; i < files.length(); ++i) {
            JSONObject file = files.optJSONObject(i);
            if (file == null)
                continue;

            KodiItem kodiItem = new KodiItem(file);
            kodiItems.add(kodiItem);
        }

        if (kodiRoot.isEmpty()) {
            for (KodiItem kodiItem : kodiItems)
                kodiRoot.addSource(kodiItem);
        }

        updateListener.kodiDirectoryUpdate(kodiItems);

    }

    public void onVolumeSeekBarChange(final int volumePercent) {
        if (volumePercent != 0 && isMuted == true)
            kodiTcpSender.sendSetMuted(false);

        kodiTcpSender.sendSetVolume(volumePercent);
    }

    public void onVolumeUpDown(final boolean isUp) {
        int repeat = (int) (volumePercent / 20) + 1;
        for (int i = 0; i < repeat; ++i)
            kodiTcpSender.sendHifiVolUpDown(isUp);
    }

    public void sendPlay() {
        if (kodiPlayers.isAudioPlayerPaused())
            kodiPlayers.unPauseAudioPlayer();
        else if (kodiPlayers.isAudioPlayerStopped()) {
            kodiPlayers.stopOtherPlayersIfRequired();
            kodiTcpSender.sendPlayerOpenPlaylist(kodiPlaylist.getAudioPlaylistId());
        }
        // if not paused, nor stopped -> it's playing, nothing to do.
    }

    public void sendPause() {
        if (kodiPlayers.isAudioPlayerPlaying())
            kodiPlayers.pauseAudioPlayer();
    }

    public boolean sendUpdateRequest() {
        int activePlayerId = kodiPlayers.getActivePlayerId();
        if (activePlayerId == -1) {
            kodiPlayers.getFullUpdate();
            return false;
        }

        kodiTcpSender.sendPlayerGetProperties(activePlayerId);
        return true;
    }

    public void sendPlaylistUpdateRequest() {
        kodiTcpSender.sendPlaylistGetItems(kodiPlaylist.getAudioPlaylistId());
    }

    public void onTrackPositionSeekBarChange(double percentage) {
        int activePlayerId = kodiPlayers.getActivePlayerId();
        if (activePlayerId == -1)
            return;

        kodiTcpSender.sendPlayerSeek(activePlayerId, percentage);
    }

    public void onPrevTrack() {
        int activePlayerId = kodiPlayers.getActivePlayerId();
        if (activePlayerId == -1)
            return;

        kodiTcpSender.sendPlayerGoToPrevNext(activePlayerId, false);
    }

    public void onNextTrack() {
        Log.d("KodiConnection", "onNextTrack");
        int activePlayerId = kodiPlayers.getActivePlayerId();
        if (activePlayerId == -1)
            return;

        kodiTcpSender.sendPlayerGoToPrevNext(activePlayerId, true);
    }

    public void stopOtherPlayersIfRequired() {
        kodiPlayers.stopOtherPlayersIfRequired();
    }

    public void openAudioPlaylistPosition(int position) {
        int audioPlaylistId = kodiPlaylist.getAudioPlaylistId();
        if (audioPlaylistId == -1) {
            Log.d("KodiConnection", "No audio playlist");
            return;
        }

        kodiTcpSender.sendPlayerOpenPlaylist(audioPlaylistId, position);
    }

    public void sendItemUpdateRequest() {
        int activePlayerId = kodiPlayers.getActivePlayerId();
        if (activePlayerId == -1)
            return;

        kodiTcpSender.sendPlayerGetItem(activePlayerId);
    }

    public void sendGetDirectory(final String path) {
        // if empty -> sending pre-cached root contents
        if (path.isEmpty())
            updateListener.kodiDirectoryUpdate(kodiRoot.getStorageRoot());
        else
            kodiTcpSender.sendGetMusicDirectory(path);
    }

    public void sendAddToAudioPlaylist(final KodiItem kodiItem) {
        int audioPlaylistId = kodiPlaylist.getAudioPlaylistId();
        if (audioPlaylistId == -1) {
            Log.d("KodiConnection", "No audio playlist");
            return;
        }

        if ("song".equals(kodiItem.type) && kodiItem.id != -1)
            kodiTcpSender.addToPlaylist(audioPlaylistId, kodiItem.id);
        else if ("directory".equals(kodiItem.filetype))
            kodiTcpSender.addToPlaylist(audioPlaylistId, kodiItem.file, true);
        else
            kodiTcpSender.addToPlaylist(audioPlaylistId, kodiItem.file, false);
    }
}
