package hu.evolver.uhc.comm;

/**
 * Created by rroman on 6/3/16.
 */
public interface KodiUpdateListener {
    // these are all run later inside a Runnable -- params should be immutable
    void kodiVolumeChanged(boolean isMuted, double volumePercent);
    void kodiAudioPlaying();
    void kodiAudioPaused();
    void kodiAudioStopped();
}
