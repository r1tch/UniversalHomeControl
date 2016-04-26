package hu.evolver.uhc;

import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by rits on 2016-04-26.
 * <p/>
 * Caches UHC state so we can display something before successful connectivity
 */
public class UhcState {
    private Map<String, JSONObject> valueMap;
    private static String[] storedMessageIds = {"gotNodes", "changedLevels"};

    public UhcState() {
        valueMap = new TreeMap<>();
    }

    public void newUpdate(final @NonNull String msg, final @NonNull JSONObject jsonObject) {
        for (String msgId : storedMessageIds) {
            if (msgId.equals(msg))
                valueMap.put(msg, jsonObject);
        }
    }

    public JSONObject get(final String msg) {
        return valueMap.get(msg);
    }
}
