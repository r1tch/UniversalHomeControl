package hu.evolver.uhc.comm;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rroman on 6/2/16.
 */
public class KodiTcpSender {
    private SimpleTcpClient simpleTcpClient = null;

    public KodiTcpSender(SimpleTcpClient simpleTcpClient) {
        this.simpleTcpClient = simpleTcpClient;
    }

    private JSONObject createCommonJSONObject(final String method) throws JSONException {
        JSONObject jo = new JSONObject();
        jo.put("service", "kodi");
        jo.put("jsonrpc", "2.0");
        jo.put("method", method);

        return jo;
    }

    public void sendGetSourcesMusic() {
        JSONObject jo = null;

        try {
            jo = createCommonJSONObject("Files.GetSources");

            JSONObject sort = new JSONObject();
            sort.put("order", "ascending");
            sort.put("method", "file");

            JSONObject params = new JSONObject();
            params.put("media", "music");
            jo.put("params", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("KodiTcpSender", "SEND: " + jsonStr);
        simpleTcpClient.send(jsonStr);
    }

    public void sendGetPlaylists() {
        JSONObject jo = null;

        try {
            jo = createCommonJSONObject("Playlist.GetPlaylists");
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("KodiTcpSender", "SEND: " + jsonStr);
        simpleTcpClient.send(jsonStr);
    }

    public void sendApplicationGetProperties() {
        JSONObject jo = null;

        try {
            jo = createCommonJSONObject("Application.GetProperties");
            JSONObject params = new JSONObject();
            JSONArray properties = new JSONArray("[\"volume\", \"muted\"]");
            params.put("properties", properties);
            jo.put("params", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("KodiTcpSender", "SEND: " + jsonStr);
        simpleTcpClient.send(jsonStr);
    }

    public void sendSetMuted(final boolean isMuted) {
        JSONObject jo = null;

        try {
            jo = createCommonJSONObject("Application.SetMuted");
            JSONObject params = new JSONObject();
            params.put("mute", isMuted);
            jo.put("params", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("KodiTcpSender", "SEND: " + jsonStr);
        simpleTcpClient.send(jsonStr);
    }

    public void sendSetVolume(final int volumePercent) {
        JSONObject jo = null;

        try {
            jo = createCommonJSONObject("Application.SetVolume");
            JSONObject params = new JSONObject();
            params.put("volume", volumePercent);
            jo.put("params", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("KodiTcpSender", "SEND: " + jsonStr);
        simpleTcpClient.send(jsonStr);
    }

    public void sendGetDirectory(final String directory) {
        JSONObject jo = null;

        try {
            jo = createCommonJSONObject("Files.GetDirectory");
            JSONObject params = new JSONObject();
            params.put("directory", directory);
            jo.put("params", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("KodiTcpSender", "SEND: " + jsonStr);
        simpleTcpClient.send(jsonStr);
    }

    public void sendPlayerGetProperties(int playerid) {
        JSONObject jo = null;

        try {
            jo = createCommonJSONObject("Player.GetProperties");
            JSONArray properties = new JSONArray("[\"type\", \"time\", \"percentage\", \"totaltime\", \"playlistid\", \"position\", \"repeat\", \"shuffled\", \"live\"]");

            JSONObject params = new JSONObject();
            params.put("playerid", playerid);
            params.put("properties", properties);
            jo.put("params", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("KodiTcpSender", "SEND: " + jsonStr);
        simpleTcpClient.send(jsonStr);
    }

    public void sendPlayerPlayPause(int playerid, boolean shouldPlay) {
        JSONObject jo = null;

        try {
            jo = createCommonJSONObject("Player.PlayPause");
            JSONObject params = new JSONObject();
            params.put("playerid", playerid);
            params.put("play", shouldPlay);
            jo.put("params", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("KodiTcpSender", "SEND: " + jsonStr);
        simpleTcpClient.send(jsonStr);
    }

    public void sendPlayerStop(int playerid) {
        JSONObject jo = null;

        try {
            jo = createCommonJSONObject("Player.Stop");
            JSONObject params = new JSONObject();
            params.put("playerid", playerid);
            jo.put("params", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("KodiTcpSender", "SEND: " + jsonStr);
        simpleTcpClient.send(jsonStr);
    }

    public void sendPlayerOpenPlaylist(int playlistid) {
        JSONObject jo = null;

        try {
            jo = createCommonJSONObject("Player.Open");
            JSONObject item = new JSONObject();
            item.put("playlistid", playlistid);
            JSONObject params = new JSONObject();
            params.put("item", item);
            jo.put("params", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("KodiTcpSender", "SEND: " + jsonStr);
        simpleTcpClient.send(jsonStr);
    }

    public void sendHifiVolUpDown(final boolean isUp) {
        if (isUp)
            sendHifiCommand("hifiVolUp");
        else
            sendHifiCommand("hifiVolDn");
    }

    private void sendHifiCommand(final String command) {
        // well, this uses the hifi UHC service, not kodi... oh well
        try {
            JSONObject jo = new JSONObject();
            jo.put("service", "hifi");
            jo.put("msg", command);

            String jsonStr = jo.toString();
            Log.d("KodiTcpSender", "SEND: " + jsonStr);
            simpleTcpClient.send(jsonStr);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }
}
