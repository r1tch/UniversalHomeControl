package hu.evolver.uhc.model;

import android.support.annotation.NonNull;
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
import hu.evolver.uhc.ui.UiThreadDispatcher;

/**
 * Created by rits on 2016-04-26.
 * <p/>
 * Caches UHC state -- well, handles UHC connectivity, used as a comm interface for the GUI elements
 */
public class UhcState implements KodiUpdateListener {
    private Set<StateUpdateListener> listeners = new HashSet<>();       // IMPORTANT, these are all UI listeners, uiThreadDispatcher must be used for notification
    private ZWaveNodeStore zWaveNodeStore = new ZWaveNodeStore();
    private UiThreadDispatcher uiThreadDispatcher = null;
    private KodiConnection kodiConnection = null;
    private JsonState state = null;

    public UhcState(SimpleTcpClient simpleTcpClient, UiThreadDispatcher uiThreadDispatcher) {
        this.uiThreadDispatcher = uiThreadDispatcher;

        kodiConnection = new KodiConnection(this, simpleTcpClient);
    }

    public KodiConnection getKodiConnection() {
        return kodiConnection;
    }

    public JsonState getState() {
        return state;
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
        else if ("state".equals(msg) && jsonObject.has("state"))
            parseState(jsonObject.optJSONObject("state"));
        else
            kodiConnection.newUpdate(jsonObject);
    }

    private void parseState(JSONObject jsonObject) {
        if (state != null && state.equals(jsonObject))
            return;

        state = new JsonState(jsonObject);
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                uiThreadDispatcher.mainActivity.onStateChanged(state);
                for (StateUpdateListener listener : listeners)
                    listener.stateChanged(state);
            }
        });

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

        if (uiThreadDispatcher.mainActivity == null) {
            zWaveNodeStore.replaceWith(tmpStore);
            return;
        }

        // Note: if gui exists, map modifications must be run in the same thread (or be synchronized, looks uglier)
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
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

            uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
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
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                for (StateUpdateListener listener : listeners)
                    listener.kodiVolumeChanged(isMuted, volumePercent);
            }
        });
    }

    /*
    @Override
    public void kodiAudioPlaying() {
        isPlaying = true;
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                uiThreadDispatcher.mainActivity.onPlaying();
            }
        });
    }

    @Override
    public void kodiAudioPaused() {
        isPlaying = false;
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                uiThreadDispatcher.mainActivity.onStoppedPaused();
            }
        });

    }

    @Override
    public void kodiAudioStopped() {
        isPlaying = false;
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                uiThreadDispatcher.mainActivity.onStoppedPaused();
            }
        });
    }*/

    @Override
    public void kodiPlayerUpdate(final KodiPlayers.Player player) {
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                for (StateUpdateListener listener : listeners)
                    listener.kodiPlayerUpdate(player);

                if ("audio".equals(player.type)) {
                    if (player.isPlaying())
                        uiThreadDispatcher.mainActivity.onPlaying();
                    else
                        uiThreadDispatcher.mainActivity.onStoppedPaused();
                } else if (!player.isStopped())
                    uiThreadDispatcher.mainActivity.otherPlayerActive();
            }
        });
    }

    @Override
    public void kodiPlayingItem(final String label) {
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                for (StateUpdateListener listener : listeners)
                    listener.kodiPlayingItem(label);
            }
        });
    }

    @Override
    public void kodiClearAudioPlaylist() {
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                for (StateUpdateListener listener : listeners)
                    listener.kodiClearAudioPlaylist();
            }
        });
    }

    @Override
    public void kodiPlaylistUpdate(final ArrayList<KodiItem> items) {
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                for (StateUpdateListener listener : listeners)
                    listener.kodiPlaylistUpdate(items);
            }
        });
    }

    @Override
    public void kodiAddAudioPlaylistItem(final int position, final int itemid) {
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                for (StateUpdateListener listener : listeners)
                    listener.kodiAddAudioPlaylistItem(position, itemid);
            }
        });
    }

    @Override
    public void kodiRemoveAudioPlaylistItem(final int position) {
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                for (StateUpdateListener listener : listeners)
                    listener.kodiRemoveAudioPlaylistItem(position);
            }
        });
    }


    @Override
    public void kodiOnStop() {
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                for (StateUpdateListener listener : listeners)
                    listener.kodiOnStop();
            }
        });
    }

    @Override
    public void kodiDirectoryUpdate(final List<KodiItem> kodiItems) {
        uiThreadDispatcher.dispatchToMediaBrowserActivity(new Runnable() {
            @Override
            public void run() {
                uiThreadDispatcher.mediaBrowserActivity.kodiDirectoryUpdate(kodiItems);
            }
        });
    }

    /*
    @Override
    public void otherPlayerActive() {
        uiThreadDispatcher.dispatchToMainActivity(new Runnable() {
            @Override
            public void run() {
                uiThreadDispatcher.mainActivity.otherPlayerActive();
            }
        });
    }
    */
}
