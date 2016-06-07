package hu.evolver.uhc.model;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import hu.evolver.uhc.comm.KodiTcpSender;
import hu.evolver.uhc.comm.KodiUpdateListener;

/**
 * Created by rits on 2016-06-04.
 */
public class KodiPlayers {
    static class Player {
        public int id = 0;
        public String type = null;
        public double speed = Double.NaN;
        public int position = -1;

        public Player(int id, String type, double speed, int position) {
            this.id = id;
            this.type = type;
            this.speed = speed;
            this.position = position;
        }

        public boolean stateKnown() {
            return speed != Double.NaN;
        }

        public boolean isPaused() {
            return speed == 0.0;
        }

        public boolean isStopped() {
            return stateKnown() && position == -1;
        }

        public boolean isPlaying() {
            return stateKnown() && !isPaused() && !isStopped();
        }
    }

    private Map<Integer, Player> players = new HashMap();   // ArrayList more efficient, this easier to code :)
    private KodiUpdateListener updateListener = null;
    private KodiTcpSender kodiTcpSender = null;
    private Player audioPlayer = null;

    public KodiPlayers(KodiUpdateListener updateListener, KodiTcpSender kodiTcpSender) {
        this.updateListener = updateListener;
        this.kodiTcpSender = kodiTcpSender;
    }

    public int getAudioPlayerId() {
        if (audioPlayer != null)
            return audioPlayer.id;

        return -1;
    }

    public void onTcpConnected() {
        updateListener.kodiAudioStopped();
        audioPlayer = null;
        players.clear();
        getFullUpdate();
    }

    private void getFullUpdate() {
        for (int i = 0; i < 5; ++i)        // pretty arbitrary, given that currently there are 3 players, most probably querying 10 is enough
            kodiTcpSender.sendPlayerGetProperties(i);
    }

    public void playerPropertiesUpdate(final JSONObject props, final String extraId) {
        if (props == null)
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
            player = new Player(playerid, type, props.optDouble("speed"), props.optInt("position"));
            players.put(playerid, player);
        } else {
            double speed = props.optDouble("speed");
            int position = props.optInt("position");

            if (player.speed == speed && player.position == position)
                stateChanged = false;

            player.speed = speed;
            player.position = position;
        }

        if ("audio".equals(type)) {
            if (audioPlayer == null) {
                Log.d("KodiPlayers", "Found audio playerid:" + playerid);
                audioPlayer = player;
                kodiTcpSender.sendPlayerGetItem(playerid);
                stateChanged = true;
            }

            updateAudioPlayingState(stateChanged);
        }

        updateListener.kodiPlayerUpdate(type, KodiPlayerState.fromJSONObject(props));
    }

    private void updateAudioPlayingState(boolean stateChanged) {
        Log.d("KodiPlayers", "audioPlayer state: s:" + audioPlayer.speed + " p:" + audioPlayer.position);

        if (!stateChanged)
            return;

        if (audioPlayer.isStopped())
            updateListener.kodiAudioStopped();
        else if (audioPlayer.isPaused())
            updateListener.kodiAudioPaused();
        else if (audioPlayer.isPlaying())
            updateListener.kodiAudioPlaying();
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
        if (audioPlayer != null) {
            pauseOtherPlayers();
            kodiTcpSender.sendPlayerPlayPause(audioPlayer.id, true);
        }
    }

    public void pauseOtherPlayers() {
        // pause other non-picture and non-audio players (well, the only vid player..)
        for (Map.Entry<Integer, Player> playerEntry : players.entrySet()) {
            Player player = playerEntry.getValue();
            if (!"audio".equals(player.type) && !"picture".equals(player.type)) {
                Log.d("KodiPlayers", "Player " + player.id + " type:" + player.type);
                kodiTcpSender.sendPlayerPlayPause(player.id, false);
            }
        }
    }

    public void onPause(int playerid) {
        if (playerid == getAudioPlayerId())
            audioPlayer.speed = 0.0;
    }

    public void onStop() {
        // OnStop does not tell us which player stopped -> we must find out if it's the audio player
        kodiTcpSender.sendPlayerGetProperties(getAudioPlayerId());
    }

    public void onSeek(int playerid) {
        if (playerid == getAudioPlayerId())
            kodiTcpSender.sendPlayerGetProperties(playerid);
    }
}
