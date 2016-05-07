package hu.evolver.uhc.model;

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

    public void clear() {
        byId.clear();
        byName.clear();
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
