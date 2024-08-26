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
    public String type = "";            // "song", ...??
    public String filetype = "";        // file or directory

    public KodiItem(JSONObject jsonObject) {
        try {
            id = jsonObject.optInt("id", id);
            album = jsonObject.optString("album", album);
            artist = getFirstArtist(jsonObject);
            title = jsonObject.optString("title", title);
            label = jsonObject.optString("label", label);
            file = jsonObject.getString("file");
            durationSecs = jsonObject.optInt("duration", durationSecs);
            type = jsonObject.optString("type", type);
            filetype = jsonObject.optString("filetype", filetype);
        } catch (JSONException e) {
        }
    }

    public String getLabel() {
        if (label == null || label.isEmpty())
            return file;

        return label;
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
