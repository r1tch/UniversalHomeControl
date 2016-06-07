package hu.evolver.uhc.comm;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hu.evolver.uhc.model.KodiPlayers;
import hu.evolver.uhc.model.KodiPlaylist;

/**
 * Created by rroman on 6/2/16.
 */
public class KodiConnection {
    private KodiUpdateListener updateListener = null;
    private SimpleTcpClient simpleTcpClient = null;
    private KodiTcpSender kodiTcpSender = null;
    private KodiPlaylist kodiPlaylist = new KodiPlaylist();
    private KodiPlayers kodiPlayers = null;

    private double volumePercent = 0;
    private boolean isMuted = false;
    private boolean gettingRoot = false;

    public KodiConnection(KodiUpdateListener updateListener, SimpleTcpClient simpleTcpClient) {
        this.updateListener = updateListener;
        this.simpleTcpClient = simpleTcpClient;
        this.kodiTcpSender = new KodiTcpSender(simpleTcpClient);
        this.kodiPlayers = new KodiPlayers(updateListener, kodiTcpSender);
    }

    public boolean isMuted() {
        return isMuted;
    }

    public double getVolumePercent() {
        return volumePercent;
    }

    public void onTcpConnected() {
        kodiPlaylist.clear();
        kodiTcpSender.sendApplicationGetProperties();
        kodiTcpSender.sendGetSourcesMusic();
        kodiPlayers.onTcpConnected();
        kodiTcpSender.sendGetPlaylists();   // TODO when received playlist ID, remember ID, then issue query for music playlist

        // for sources, logic could be: 1) get sources; if single returned, get directory too & populate "root" with that
        //     (ie if dir list received & root empty -> make this the root)
        // later when selecting files: remember last dir requested, wait for it...
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

        final JSONObject resultObject = jsonObject.optJSONObject("result");

        if ("Player.GetProperties".equals(idMethod)) {
            kodiPlayers.playerPropertiesUpdate(jsonObject.optJSONObject("result"), extraId);
            return;
        } else if ("Player.GetItem".equals(idMethod)) {
            parseItem(resultObject);
        }

        if (resultObject != null) {
            parseResultObject(resultObject);
            return;
        }

        final JSONArray resultArray = jsonObject.optJSONArray("result");
        if (resultArray != null) {
            parseResultArray(resultArray);
            return;
        }

        final JSONObject errorObject = jsonObject.optJSONObject("error");
        if (errorObject != null) {
            parseErrorJSON(jsonObject);
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
        } else if ("Application.OnVolumeChanged".equals(method)) {
            parseOnVolumeChanged(jsonObject);
            return;
        }

    }

    private void parseItem(JSONObject resultObject) {
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

        try {
            int playerid = params.getJSONObject("data").getJSONObject("player").getInt("playerid");
            kodiTcpSender.sendPlayerGetProperties(playerid);    // we need this for proper playlist position update
            if (playerid == kodiPlayers.getAudioPlayerId()) {
                updateListener.kodiAudioPlaying();
                kodiTcpSender.sendPlayerGetItem(playerid);
            }
        } catch (JSONException e) {
            Log.e("KodiConnection", e.toString());
        }
    }

    private void parseOnPause(JSONObject params) {
        if (params == null)
            return;

        try {
            int playerid = params.getJSONObject("data").getJSONObject("player").getInt("playerid");
            kodiPlayers.onPause(playerid);
            if (playerid == kodiPlayers.getAudioPlayerId()) {
                updateListener.kodiAudioPaused();
            }
        } catch (JSONException e) {
            Log.e("KodiConnection", e.toString());
        }
    }

    private void parseOnStop() {
        kodiPlayers.onStop();
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

    private void parseErrorJSON(JSONObject jsonObject) {
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
            kodiPlaylist.gotPlaylists(resultArray);
        }
    }

    private void parseResultObject(JSONObject resultObject) {
        // {..., result: {sources: [{source1}, {source2}] } }
        final JSONArray sources = resultObject.optJSONArray("sources");
        if (sources != null)
            gotSources(sources);

        else if (resultObject.has("muted") && resultObject.has("volume")) {
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

    private void gotSources(final JSONArray sources) {
        if (sources.length() == 0)
            return;

        if (sources.length() > 1) {
            kodiPlaylist.addSources(sources);
            return;
        }

        gettingRoot = true;         // TODO we have no directory processing routine yet; it shall update sources if gettingRoot is true
        try {
            final JSONObject theOnlySource = sources.getJSONObject(0);
            final String directory = theOnlySource.getString("file");
            kodiTcpSender.sendGetDirectory(directory);
        } catch (JSONException e) {
            Log.e("KodiConnection", "getSources JSONException:" + e.getMessage());
        }
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
            kodiPlayers.pauseOtherPlayers();
            kodiTcpSender.sendPlayerOpenPlaylist(kodiPlaylist.getAudioPlaylistId());
        }
        // if not paused, nor stopped -> it's playing, nothing to do.
    }

    public void sendPause() {
        if (kodiPlayers.isAudioPlayerPlaying())
            kodiPlayers.pauseAudioPlayer();
    }

    public boolean sendUpdateRequest() {
        int audioPlayerId = kodiPlayers.getAudioPlayerId();
        if (audioPlayerId == -1)
            return false;

        kodiTcpSender.sendPlayerGetProperties(audioPlayerId);
        kodiTcpSender.sendPlayerGetItem(audioPlayerId);
        return true;
    }

    public void onTrackPositionSeekBarChange(double percentage) {
        int audioPlayerId = kodiPlayers.getAudioPlayerId();
        if (audioPlayerId == -1)
            return ;

        kodiTcpSender.sendPlayerSeek(audioPlayerId, percentage);
    }

    public void onPrevTrack() {
        int audioPlayerId = kodiPlayers.getAudioPlayerId();
        if (audioPlayerId == -1)
            return;

        kodiTcpSender.sendPlayerGoToPrevNext(audioPlayerId, false);
    }

    public void onNextTrack() {
        Log.d("KodiConnection", "onNextTrack");
        int audioPlayerId = kodiPlayers.getAudioPlayerId();
        if (audioPlayerId == -1)
            return;

        kodiTcpSender.sendPlayerGoToPrevNext(audioPlayerId, true);
    }
}
