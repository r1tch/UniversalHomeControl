package hu.evolver.uhc.model;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hu.evolver.uhc.ui.MainActivityDispatcher;

/**
 * Created by rits on 2016-04-26.
 * <p/>
 * Caches UHC state so we can display something before successful connectivity
 */
public class UhcState {
    private List<StateUpdateListener> listeners = new ArrayList<>();
    private ZWaveNodeStore zWaveNodeStore = new ZWaveNodeStore();
    private MainActivityDispatcher mainActivityDispatcher = null;

    public UhcState(MainActivityDispatcher mainActivityDispatcher) {
        this.mainActivityDispatcher = mainActivityDispatcher;
    }

    public void addListener(StateUpdateListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StateUpdateListener listener) {
        listeners.remove(listener);
    }

    public ZWaveNodeStore getZWaveNodeStore() {
        return zWaveNodeStore;
    }

    public void newUpdate(final @NonNull String msg, final @NonNull JSONObject jsonObject) {
        if ("gotNodes".equals(msg))
            parseGotNodes(jsonObject);
        if ("changedLevels".equals(msg))
            parseChangedLevels(jsonObject);
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
}
