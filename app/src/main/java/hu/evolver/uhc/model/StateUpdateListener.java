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
}
