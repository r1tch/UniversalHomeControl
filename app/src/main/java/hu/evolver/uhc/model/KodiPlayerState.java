package hu.evolver.uhc.model;

import org.json.JSONObject;

/**
 * Created by rits on 2016-06-05.
 *
 * Exposed state (properties) of a player
 */
public class KodiPlayerState {
    public double percentage;
    public KodiTime time;
    public KodiTime totaltime;
    public int position;           // Kodi means playlist position with this
    public boolean live;
    public double speed;
    public boolean shuffled;
    public String repeat;

    static KodiPlayerState fromJSONObject(final JSONObject props) {

        KodiPlayerState kodiPlayerState = new KodiPlayerState();

        kodiPlayerState.percentage = props.optDouble("percentage");
        kodiPlayerState.time = KodiTime.fromJSONObject(props.optJSONObject("time"));
        kodiPlayerState.totaltime = KodiTime.fromJSONObject(props.optJSONObject("totaltime"));
        kodiPlayerState.position = props.optInt("position");
        kodiPlayerState.live = props.optBoolean("live");
        kodiPlayerState.speed = props.optDouble("speed");
        kodiPlayerState.shuffled = props.optBoolean("shuffled");
        kodiPlayerState.repeat = props.optString("repeat");

        return kodiPlayerState;
    }
}
