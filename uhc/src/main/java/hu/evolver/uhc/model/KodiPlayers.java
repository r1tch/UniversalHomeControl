package hu.evolver.uhc.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hu.evolver.uhc.comm.KodiTcpSender;
import hu.evolver.uhc.comm.KodiUpdateListener;

/**
 * Created by rits on 2016-06-04.
 */
public class KodiPlayers {
    static public class Player {
        // playing if: active && speed != 0
        // paused if: active && speed == 0
        // stopped if: !active
        public boolean isActive = false;        // set via GetActivePlayers

        public int id = -1;
        public String type = null;
        public double speed = Double.NaN;
        public int position = -1;
        public double percentage = 0.0;
        public KodiTime time;
        public KodiTime totaltime;
        public boolean live = false;
        public boolean shuffled = false;
        public String repeat = "off";

        public Player(int id, String type) {
            this.id = id;
            this.type = type;
        }

        public Player(int id, String type, final JSONObject props) {
            this.id = id;
            this.type = type;

            updateFromJSONObject(props);
        }

        public void updateFromJSONObject(final JSONObject props) {
            speed = props.optDouble("speed");
            position = props.optInt("position");
            percentage = props.optDouble("percentage");
            time = KodiTime.fromJSONObject(props.optJSONObject("time"));
            totaltime = KodiTime.fromJSONObject(props.optJSONObject("totaltime"));
            live = props.optBoolean("live");
            shuffled = props.optBoolean("shuffled");
            repeat = props.optString("repeat");
        }


        public boolean stateKnown() {
            return speed != Double.NaN;
        }

        public boolean isPaused() {
            return isActive && speed == 0.0;
        }

        public boolean isStopped() {
            return stateKnown() && !isActive;
        }

        public boolean isPlaying() {
            return stateKnown() && isActive && !isPaused();
        }

        public String toString() {
            return "id:" + id + ", type:" + type + ", isActive:" + isActive + ", speed:" + speed + ", position:" + position +
                    ", percentage:" + percentage;
        }
    }

    private Map<Integer, Player> players = new HashMap();   // ArrayList more efficient, this easier to code :)
    private KodiUpdateListener updateListener = null;
    private KodiTcpSender kodiTcpSender = null;
    private Player audioPlayer = null;
    private KodiPlaylist kodiPlaylist = null;
    private Player activePlayer = null;

    public KodiPlayers(KodiUpdateListener updateListener, KodiTcpSender kodiTcpSender, KodiPlaylist kodiPlaylist) {
        this.updateListener = updateListener;
        this.kodiTcpSender = kodiTcpSender;
        this.kodiPlaylist = kodiPlaylist;
    }

    public int getActivePlayerId() {
        return activePlayer == null ? -1 : activePlayer.id;
    }

    public void onTcpConnected() {
        deactivateAllPlayers();

        activePlayer = null;
        audioPlayer = null;
        players.clear();
        getFullUpdate();
    }

    public boolean isAudioPlayerShuffleOn() {
        if (audioPlayer == null)
            return false;
        return  audioPlayer.shuffled;
    }

    public String getAudioPlayerRepeat() {
        if (audioPlayer == null)
            return "off";
        return audioPlayer.repeat;
    }

    public void getFullUpdate() {
        kodiTcpSender.sendGetActivePlayers();
        for (int i = 0; i < 5; ++i)        // pretty arbitrary, given that currently there are 3 players, most probably querying 10 is enough
            kodiTcpSender.sendPlayerGetProperties(i);
    }

    private void deactivateAllPlayers() {
        for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
            Player player = playerEntry.getValue();
            player.isActive = false;
            player.speed = 0;
            player.position = -1;
            updateListener.kodiPlayerUpdate(player);
        }
    }

    public void updateActivePlayers(JSONArray resultArray) {
        if (resultArray == null)
            return;

        Map<Integer, Player> activePlayers = new HashMap();

        for (int i = 0; i < resultArray.length(); ++i) {
            JSONObject jsonPlayer = resultArray.optJSONObject(i);
            if (jsonPlayer == null)
                continue;

            int playerid = jsonPlayer.optInt("playerid", -1);
            if (playerid < 0)
                continue;

            String type = jsonPlayer.optString("type");
            if (type == null)
                continue;

            if (players.get(playerid) == null)
                players.put(playerid, new Player(playerid, type));      // remember as inactive player; will set to active in next loop(and notify listeners)

            Player player = new Player(playerid, type);
            player.isActive = true;
            activePlayers.put(playerid, player);
        }

        for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
            Player player = playerEntry.getValue();
            boolean gotActive = activePlayers.get(player.id) != null;

            if (!gotActive && player.isActive == true) {
                player.isActive = false;
                updateListener.kodiPlayerUpdate(player);

            } else if (gotActive && player.isActive == false) {
                player.isActive = true;
                if (!"picture".equals(player.type))
                    activePlayer = player;

                updateListener.kodiPlayerUpdate(player);
                kodiTcpSender.sendPlayerGetItem(player.id);
            }
        }
    }

    public void playerPropertiesUpdate(final JSONObject props, final String extraId) {
        if (props == null)      // e.g on error msg
            return;


        Integer playerid = Integer.parseInt(extraId);
        if (playerid == null) {
            Log.e("KodiPlayers", "playerPropertiesUpdate could not parse id " + extraId);
            return;
        }

        boolean stateChanged = true;

        String type = props.optString("type");
        Player player = players.get(playerid);

        if (player == null) {
            player = new Player(playerid, type, props);
            players.put(playerid, player);
        } else {
            double speed = props.optDouble("speed");
            int position = props.optInt("position");

            if (player.speed == speed && player.position == position)
                stateChanged = false;

            player.updateFromJSONObject(props);
        }

        if ("audio".equals(type)) {
            if (audioPlayer == null) {
                Log.d("KodiPlayers", "Found audio playerid:" + playerid);
                audioPlayer = player;
                stateChanged = true;
            }
        }

        if (stateChanged)
            kodiTcpSender.sendPlayerGetItem(playerid);      // TODO better distinction at parsing, this will get non-audio items too

        updateListener.kodiPlayerUpdate(player);
    }

    public boolean isAudioPlayerPaused() {
        return audioPlayer != null && audioPlayer.isPaused();
    }

    public boolean isAudioPlayerStopped() {
        return audioPlayer != null && audioPlayer.isStopped();
    }

    public boolean isAudioPlayerPlaying() {
        return audioPlayer != null && audioPlayer.isPlaying();
    }

    public void pauseAudioPlayer() {
        if (audioPlayer != null)
            kodiTcpSender.sendPlayerPlayPause(audioPlayer.id, false);
    }

    public void unPauseAudioPlayer() {
        stopOtherPlayersIfRequired();
        if (audioPlayer == null) {
            kodiTcpSender.sendPlayerOpenPlaylist(kodiPlaylist.getAudioPlaylistId());
        } else {
            kodiTcpSender.sendPlayerPlayPause(audioPlayer.id, true);
        }
    }

    public void stopOtherPlayersIfRequired() {
        for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
            Player player = playerEntry.getValue();
            if (!"audio".equals(player.type) &&
                    !"picture".equals(player.type) &&
                    !player.isStopped()) {
                Log.d("KodiPlayers", "Stopping player " + player.id + " type:" + player.type);
                kodiTcpSender.sendPlayerStop(player.id);
            }
        }
    }

    public void onSeek(int playerid) {
        kodiTcpSender.sendPlayerGetProperties(playerid);
    }

    public void setAudioRepeat(String repeat) {
        if (audioPlayer == null)
            return;
        kodiTcpSender.sendSetRepeat(audioPlayer.id, repeat);
    }

    public void setAudioShuffle(boolean shuffle) {
        if (audioPlayer == null)
            return;
        kodiTcpSender.sendSetShuffle(audioPlayer.id, shuffle);
    }
}
