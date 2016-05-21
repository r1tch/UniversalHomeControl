package hu.evolver.uhc.model;

import android.util.Log;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by rits on 2016-04-27.
 */
public class ZWaveNodeStore {
    private Map<Integer, ZWaveNode> byId = new TreeMap<>();
    private Map<String, ZWaveNode> byName = new TreeMap<>();

    public void add(ZWaveNode zWaveNode) {
        byId.put(zWaveNode.getId(), zWaveNode);
        byName.put(zWaveNode.getName(), zWaveNode);

    }

    public void replaceWith(final ZWaveNodeStore another) {
        byId = another.byId;
        byName = another.byName;
    }

    public boolean equals(final ZWaveNodeStore another) {
        if (byId.size() != another.byId.size())
            return false;

        for (Map.Entry<Integer, ZWaveNode> entry : byId.entrySet()) {
            ZWaveNode other = another.byId.get(entry.getKey());
            if (other == null || !other.equals(entry.getValue()))
                return false;
        }

        // assuming that byName is consistent - not checking
        return true;
    }

    public ZWaveNode byId(int id) {
        return byId.get(id);
    }

    public ZWaveNode byName(final String name) {
        return byName.get(name);
    }

    public Map<String, ZWaveNode> allByName() {
        return byName;
    }

    public void changedLevel(int id, int level) {
        ZWaveNode zWaveNode = byId(id);
        if (zWaveNode != null)
            zWaveNode.setLevel(level);
    }
}
