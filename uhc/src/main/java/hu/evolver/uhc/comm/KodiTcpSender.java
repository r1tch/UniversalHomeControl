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

    private void sendMethodWithParams(final String method, final JSONObject params, final String extraId) throws JSONException {
        JSONObject jo = createCommonJSONObject(method);

        if (params != null)
            jo.put("params", params);

        jo.put("id", method + "," + extraId);

        String jsonStr = jo.toString();
        Log.d("KodiTcpSender", "SEND: " + jsonStr);
        simpleTcpClient.send(jsonStr);
    }

    private void sendMethodWithParams(final String method, final JSONObject params) throws JSONException {
        sendMethodWithParams(method, params, "");
    }

    public void sendGetSourcesMusic() {
        try {
            JSONObject sort = new JSONObject();
            sort.put("order", "ascending");
            sort.put("method", "file");

            JSONObject params = new JSONObject();
            params.put("media", "music");
            params.put("sort", sort);
            sendMethodWithParams("Files.GetSources", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendGetActivePlayers() {
        try {
            sendMethodWithParams("Player.GetActivePlayers", null);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendGetPlaylists() {
        try {
            sendMethodWithParams("Playlist.GetPlaylists", null);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendApplicationGetProperties() {
        try {
            JSONArray properties = new JSONArray("[\"volume\", \"muted\"]");
            JSONObject params = new JSONObject();
            params.put("properties", properties);
            sendMethodWithParams("Application.GetProperties", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendSetMuted(final boolean isMuted) {
        try {
            JSONObject params = new JSONObject();
            params.put("mute", isMuted);
            sendMethodWithParams("Application.SetMuted", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendSetVolume(final int volumePercent) {
        try {
            JSONObject params = new JSONObject();
            params.put("volume", volumePercent);
            sendMethodWithParams("Application.SetVolume", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendGetDirectory(final String directory) {
        try {
            JSONObject params = new JSONObject();
            params.put("directory", directory);
            sendMethodWithParams("Files.GetDirectory", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendPlayerGetProperties(int playerid) {
        try {
            JSONArray properties = new JSONArray("[\"type\", \"time\", \"percentage\", \"totaltime\", \"playlistid\", \"speed\", \"position\", \"repeat\", \"shuffled\", \"live\"]");

            JSONObject params = new JSONObject();
            params.put("playerid", playerid);
            params.put("properties", properties);
            sendMethodWithParams("Player.GetProperties", params, Integer.toString(playerid));
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendPlayerPlayPause(int playerid, boolean shouldPlay) {
        try {
            JSONObject params = new JSONObject();
            params.put("playerid", playerid);
            params.put("play", shouldPlay);
            sendMethodWithParams("Player.PlayPause", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendPlayerStop(int playerid) {

        try {
            JSONObject params = new JSONObject();
            params.put("playerid", playerid);
            sendMethodWithParams("Player.Stop", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendPlayerOpenPlaylist(int playlistid) {
        try {
            JSONObject item = new JSONObject();
            item.put("playlistid", playlistid);
            JSONObject params = new JSONObject();
            params.put("item", item);
            sendMethodWithParams("Player.Open", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendPlayerOpenPlaylist(int playlistid, int position) {
        try {
            JSONObject item = new JSONObject();
            item.put("playlistid", playlistid);
            item.put("position", position);
            JSONObject params = new JSONObject();
            params.put("item", item);
            sendMethodWithParams("Player.Open", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendPlayerGetItem(int playerid) {
        try {
            JSONArray properties = new JSONArray("[\"title\", \"album\", \"artist\", \"duration\", \"file\", \"streamdetails\"]");

            JSONObject params = new JSONObject();
            params.put("playerid", playerid);
            params.put("properties", properties);
            sendMethodWithParams("Player.GetItem", params, Integer.toString(playerid));
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendPlayerSeek(int playerid, double percentage) {
        try {
            JSONObject params = new JSONObject();
            params.put("playerid", playerid);
            params.put("value", percentage);
            sendMethodWithParams("Player.Seek", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendPlayerGoToPrevNext(int playerid, boolean toNext) {
        try {
            JSONObject params = new JSONObject();
            params.put("playerid", playerid);
            params.put("to", toNext ? "next" : "previous");
            sendMethodWithParams("Player.GoTo", params);
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
    }

    public void sendPlaylistGetItems(int playlistid) {
        try {
            JSONArray properties = new JSONArray("[\"title\", \"album\", \"artist\", \"duration\", \"file\", \"streamdetails\"]");
            JSONObject limits = new JSONObject();
            limits.put("end", 200);             // yeah, extract constant -- better, make it configable -- better: pagination...?

            JSONObject params = new JSONObject();
            params.put("playlistid", playlistid);
            params.put("properties", properties);
            params.put("limits", limits);
            sendMethodWithParams("Playlist.GetItems", params, Integer.toString(playlistid));
        } catch (JSONException e) {
            Log.e("KodiTcpSender", "Json exception:" + e.getMessage());
        }
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
