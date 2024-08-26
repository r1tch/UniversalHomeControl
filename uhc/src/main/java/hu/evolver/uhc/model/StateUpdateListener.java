package hu.evolver.uhc.model;

import java.util.ArrayList;

/**
 * Created by rroman on 4/29/16.
 *
 * We should really divide this into multiple pieces by usage; now everybody has to implement all methods.
 */
public interface StateUpdateListener {
    // Convention: <python svc name><python msg name>
    // ...except for Kodi, where Kodi's own messages are forwarded...
    void zWaveGotNodes();
    void zWaveChangedLevels(int nodeId, int newLevel);
    void kodiVolumeChanged(boolean isMuted, double volumePercent);
    void kodiPlayerUpdate(KodiPlayers.Player player);
    void kodiPlayingItem(String label);
    void kodiOnStop();

    void kodiClearAudioPlaylist();
    void kodiPlaylistUpdate(ArrayList<KodiItem> items);
    void kodiAddAudioPlaylistItem(int position, int songid);
    void kodiRemoveAudioPlaylistItem(int position);

    void stateChanged(JsonState state);
}
