package hu.evolver.uhc.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rits on 2016-06-08.
 */
public class JsonState {
    private JSONObject state;

    public JsonState(JSONObject state) {
        this.state = state;
    }

    public JsonState clone() {
        try {
            // ugly as hell, http://stackoverflow.com/questions/12809779 not available here though
            JSONObject copy = new JSONObject(state.toString());
            return new JsonState(copy);
        } catch (JSONException e) {
            return new JsonState(new JSONObject());
        }
    }

    public boolean equals(JSONObject otherState) {
        return state.equals(otherState);
    }

    public boolean isProjectorOn() {
        return state.optBoolean("projectorOn");
    }

    public boolean isHifiOn() {
        return state.optBoolean("hifiOn");
    }

    public boolean isAtHome() {
        return state.optBoolean("atHome");
    }

    public boolean isAsleep() {
        return state.optBoolean("asleep");
    }

    public boolean isGuestMode() { return state.optBoolean("guestMode"); }

    public AirconditionerState getAirconditionerState() {
        return new AirconditionerState(state.optBoolean("acOn"), state.optString("acMode"));
    }

    public void setAirconditionerState(AirconditionerState airconditionerState) {
        try {
            state.put("acOn", airconditionerState.isOn());
            state.put("acMode", airconditionerState.stateCode());
        } catch (JSONException e) {
            Log.e("JsonState", "Can't set AC state: " + e);
        }
    }
}
