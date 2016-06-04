package hu.evolver.uhc.model;

import android.util.Log;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import hu.evolver.uhc.comm.KodiTcpSender;
import hu.evolver.uhc.comm.KodiUpdateListener;

/**
 * Created by rits on 2016-06-04.
 */
public class KodiPlayers {
    static class Player {
        int id = 0;
        public String type = null;
        public double speed = 0.0;
        public int position = -1;

        public Player(int id, String type, double speed, int position) {
            this.id = id;
            this.type = type;
            this.speed = speed;
            this.position = position;
        }

        public boolean isPaused() {
            return speed == 0.0;
        }

        public boolean isStopped() {
            return position == -1;
        }

        public boolean isPlaying() {
            return !isPaused() && !isStopped();
        }
    }

    private Map<Integer, Player> players = new HashMap();   // ArrayList is more efficient, this is easier to code :)
    private KodiUpdateListener updateListener = null;
    private KodiTcpSender kodiTcpSender = null;
    private int gettingPlayerId = 0;
    private Player audioPlayer = null;


    public KodiPlayers(KodiUpdateListener updateListener, KodiTcpSender kodiTcpSender) {
        this.updateListener = updateListener;
        this.kodiTcpSender = kodiTcpSender;
    }

    public boolean isGettingPlayers() {
        return gettingPlayerId != -1;
    }

    public void getPlayers() {
        gettingPlayerId = 0;
        kodiTcpSender.sendPlayerGetProperties(gettingPlayerId);
    }

    public void playerPropertiesUpdate(final JSONObject props) {
        String type = props.optString("type");
        Player player = new Player(gettingPlayerId, props.optString("type"), props.optDouble("speed"), props.optInt("position"));
        if (gettingPlayerId >= 0)
            players.put(gettingPlayerId, player);

        if ("audio".equals(type)) {
            Log.d("KodiPlayers", "Audio playerid:" + gettingPlayerId);
            audioPlayer = player;

            if (player.isStopped())
                updateListener.kodiAudioStopped();
            else if (player.isPaused())
                updateListener.kodiAudioPaused();
            else if (player.isPlaying())
                updateListener.kodiAudioPlaying();
        }

        kodiTcpSender.sendPlayerGetProperties(++gettingPlayerId);
    }

    public void doneGettingPlayers() {
        gettingPlayerId = -1;
        Log.d("KodiPlayers", "Got " + players.size() + " players");
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
}
