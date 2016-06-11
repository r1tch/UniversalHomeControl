package hu.evolver.uhc.model;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rroman on 6/7/16.
 */
public class KodiItem {
    public int id = -1;
    public String album = "";
    public String artist = "";
    public String title = "";
    public String label = "";
    public String file = "";
    public int durationSecs = 0;
    public String type = "";

    public KodiItem(JSONObject jsonObject) {
        try {
            id = jsonObject.optInt("id");
            album = jsonObject.optString("album");
            artist = getFirstArtist(jsonObject);
            title = jsonObject.optString("title");
            label = jsonObject.optString("label");
            file = jsonObject.getString("file");
            durationSecs = jsonObject.optInt("duration");
            type = jsonObject.optString("type");
        } catch (JSONException e) {
        }
    }

    @NonNull
    private String getFirstArtist(JSONObject jsonObject) {
        JSONArray artistArray = jsonObject.optJSONArray("artist");
        if (artistArray == null || artistArray.length() == 0)
            return "";

        String artist = artistArray.optString(0);
        if (artist == null)
            return "";

        return artist.trim();
    }
}
