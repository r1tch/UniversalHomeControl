package hu.evolver.uhc.model;

/**
 * Created by rroman on 4/29/16.
 */
public interface StateUpdateListener {
    // Convention: <python svc name><python msg name>
    // ...except for Kodi, where Kodi's own messages are forwarded...
    void zWaveGotNodes();
    void zWaveChangedLevels(int nodeId, int newLevel);
    void kodiVolumeChanged(boolean isMuted, double volumePercent);
    void kodiPlayerUpdate(KodiPlayers.Player player);
    void kodiPlayingItem(String label);

    void kodiClearPlaylist();
    void kodiAddPlaylistItem(int position, KodiItem item, int newLength);
    void kodiRemovePlaylistItem(int position, int newLength);


    void stateChanged(JsonState state);
}
