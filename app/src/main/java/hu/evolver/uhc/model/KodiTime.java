package hu.evolver.uhc.model;

import org.json.JSONObject;

/**
 * Created by rits on 2016-06-05.
 */
public class KodiTime {
    public int hours = 0;
    public int minutes = 0;
    public int seconds = 0;
    public int milliseconds = 0;

    static KodiTime fromJSONObject(final JSONObject time) {
        if (time == null)
            return new KodiTime();

        KodiTime kodiTime = new KodiTime();
        kodiTime.hours = time.optInt("hours");
        kodiTime.minutes = time.optInt("minutes");
        kodiTime.seconds = time.optInt("seconds");
        kodiTime.milliseconds = time.optInt("milliseconds");
        return kodiTime;
    }

    public String toString() {
        if (hours > 0)
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return String.format("%02d:%02d", minutes, seconds);
    }
}
