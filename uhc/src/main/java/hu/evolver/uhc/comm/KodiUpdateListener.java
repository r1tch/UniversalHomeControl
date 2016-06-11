package hu.evolver.uhc.comm;

import hu.evolver.uhc.model.KodiItem;
import hu.evolver.uhc.model.KodiPlayers;

/**
 * Created by rroman on 6/3/16.
 */
public interface KodiUpdateListener {
    // these are all run later inside a Runnable -- params should be immutable
    void kodiVolumeChanged(boolean isMuted, double volumePercent);

    void kodiPlayerUpdate(KodiPlayers.Player player);
    void kodiPlayingItem(final String label);

    void kodiClearPlaylist();
    void kodiAddPlaylistItem(int position, KodiItem item, int newLength);
    void kodiRemovePlaylistItem(int position, int newLength);
}
