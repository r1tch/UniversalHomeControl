package hu.evolver.uhc.model;

import org.json.JSONObject;

/**
 * Created by rits on 2016-04-27.
 */
public class ZWaveNode implements Comparable<ZWaveNode> {
    private int id = 0;
    private String name = "";
    private int level = 0;
    private Type type = Type.Unknown;

    @Override
    public int compareTo(ZWaveNode another) {
        // assuming unique ID
        if (id < another.id)
            return -1;
        if (id > another.id)
            return 1;
        return 0;
    }

    public enum Type {
        ZWaveNetwork, SceneController, BinaryLight, DimmableLight, WindowCovering, Unknown
    }

    public static Type stringToType(final String typeStr) {
        if ("ZWaveNetwork".equals(typeStr))
            return Type.ZWaveNetwork;
        if ("SceneController".equals(typeStr))
            return Type.SceneController;
        if ("BinaryLight".equals(typeStr))
            return Type.BinaryLight;
        if ("DimmableLight".equals(typeStr))
            return Type.DimmableLight;
        if ("WindowCovering".equals(typeStr))
            return Type.WindowCovering;

        return Type.Unknown;
    }

    public ZWaveNode(JSONObject jsonObject) {
        id = jsonObject.optInt("id");
        name = jsonObject.optString("name");
        level = jsonObject.optInt("level");
        type = stringToType(jsonObject.optString("type"));
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public Type getType() {
        return type;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isLight() {
        return type == Type.DimmableLight || type == Type.BinaryLight;
    }

    public boolean isShade() {
        return type == Type.WindowCovering;
    }
}
