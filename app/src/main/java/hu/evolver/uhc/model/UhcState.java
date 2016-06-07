package hu.evolver.uhc.model;

import android.support.annotation.NonNull;
import android.util.ArraySet;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hu.evolver.uhc.comm.KodiConnection;
import hu.evolver.uhc.comm.KodiUpdateListener;
import hu.evolver.uhc.comm.SimpleTcpClient;
import hu.evolver.uhc.ui.MainActivityDispatcher;

/**
 * Created by rits on 2016-04-26.
 * <p/>
 * Caches UHC state -- well, handles UHC connectivity, used as a comm interface for the GUI elements
 */
public class UhcState implements KodiUpdateListener {
    private Set<StateUpdateListener> listeners = new HashSet<>();
    private ZWaveNodeStore zWaveNodeStore = new ZWaveNodeStore();
    private MainActivityDispatcher mainActivityDispatcher = null;
    private KodiConnection kodiConnection = null;
    boolean isPlaying = false;

    public boolean isPlaying() {
        return isPlaying;
    }

    public UhcState(SimpleTcpClient simpleTcpClient, MainActivityDispatcher mainActivityDispatcher) {
        this.mainActivityDispatcher = mainActivityDispatcher;

        kodiConnection = new KodiConnection(this, simpleTcpClient);
    }

    public KodiConnection getKodiConnection() {
        return kodiConnection;
    }

    public void addListener(StateUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StateUpdateListener listener) {
        listeners.remove(listener);
    }

    public void removeAllListeners() {
        listeners.clear();
    }

    public ZWaveNodeStore getZWaveNodeStore() {
        return zWaveNodeStore;
    }

    public void onTcpConnected() {
        kodiConnection.onTcpConnected();
    }

    public void newUpdate(final @NonNull JSONObject jsonObject) {
        final String msg = jsonObject.optString("msg");

        if ("gotNodes".equals(msg))
            parseGotNodes(jsonObject);
        else if ("changedLevels".equals(msg))
            parseChangedLevels(jsonObject);
        else
            kodiConnection.newUpdate(jsonObject);
    }

    private void parseGotNodes(final @NonNull JSONObject jsonObject) {
        Log.d("UhcState", "parseGotNodes");

        final JSONArray nodes = jsonObject.optJSONArray("nodes");
        if (nodes == null) {
            Log.e("UhcState", "gotNodes msg has no nodes");
            return;
        }

        final ZWaveNodeStore tmpStore = new ZWaveNodeStore();

        for (int i = 0; i < nodes.length(); ++i) {
            JSONObject nodeJson = nodes.optJSONObject(i);
            if (nodeJson == null) {
                Log.e("UhcState", "gotNodes has null JSONObject in nodes, at position " + i);
                continue;
            }

            ZWaveNode node = new ZWaveNode(nodeJson);
            tmpStore.add(node);
        }

        if (mainActivityDispatcher.mainActivity == null) {
            zWaveNodeStore.replaceWith(tmpStore);
            return;
        }

        // Note: if gui exists, map modifications must be run in the same thread (or be synchronized, looks uglier)
        mainActivityDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {

                if (zWaveNodeStore.equals(tmpStore))
                    return;

                zWaveNodeStore.replaceWith(tmpStore);

                Log.d("UhcState", "Nodestore elems: " + zWaveNodeStore.allByName().size());

                for (StateUpdateListener listener : listeners)
                    listener.zWaveGotNodes();
            }
        });
    }

    private void parseChangedLevels(final @NonNull JSONObject jsonObject) {
        JSONObject changedLevels = jsonObject.optJSONObject("changedLevels");
        Iterator<String> keys = changedLevels.keys();

        while (keys.hasNext()) {
            final String idStr = keys.next();
            final int id = Integer.valueOf(idStr);
            ZWaveNode node = zWaveNodeStore.byId(id);
            if (node == null)
                return;

            final int level = Integer.valueOf(changedLevels.optString(idStr, "0"));
            node.setLevel(level);

            mainActivityDispatcher.dispatch(new Runnable() {
                @Override
                public void run() {
                    for (StateUpdateListener listener : listeners)
                        listener.zWaveChangedLevels(id, level);
                }
            });
        }
    }

    @Override
    public void kodiVolumeChanged(final boolean isMuted, final double volumePercent) {
        mainActivityDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                for (StateUpdateListener listener : listeners)
                    listener.kodiVolumeChanged(isMuted, volumePercent);
            }
        });
    }

    @Override
    public void kodiAudioPlaying() {
        isPlaying = true;
        mainActivityDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                mainActivityDispatcher.mainActivity.onPlaying();
            }
        });
    }

    @Override
    public void kodiAudioPaused() {
        isPlaying = false;
        mainActivityDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                mainActivityDispatcher.mainActivity.onStoppedPaused();
            }
        });

    }

    @Override
    public void kodiAudioStopped() {
        isPlaying = false;
        mainActivityDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                mainActivityDispatcher.mainActivity.onStoppedPaused();
            }
        });
    }

    @Override
    public void kodiPlayerUpdate(final String type, final KodiPlayerState kodiPlayerState) {
        mainActivityDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                for (StateUpdateListener listener : listeners)
                    listener.kodiPlayerUpdate(type, kodiPlayerState);
            }
        });
    }

    @Override
    public void kodiPlayingItem(final String label) {
        mainActivityDispatcher.dispatch(new Runnable() {
            @Override
            public void run() {
                for (StateUpdateListener listener : listeners)
                    listener.kodiPlayingItem(label);
            }
        });
    }
}
