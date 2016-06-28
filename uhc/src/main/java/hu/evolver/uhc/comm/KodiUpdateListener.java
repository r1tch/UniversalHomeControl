package hu.evolver.uhc.comm;

import java.util.ArrayList;

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

    void kodiClearAudioPlaylist();
    void kodiPlaylistUpdate(ArrayList<KodiItem> items);
    void kodiAddAudioPlaylistItem(int position, int itemid);
    void kodiRemoveAudioPlaylistItem(int position);
    void kodiOnStop();
}
