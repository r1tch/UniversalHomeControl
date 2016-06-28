package hu.evolver.uhc.comm;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import hu.evolver.uhc.model.AirconditionerState;

/**
 * Created by rits on 2016-06-08.
 */
public class UhcTcpSender {
    private SimpleTcpClient tcpClient = null;

    public UhcTcpSender(@NonNull SimpleTcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    private void sendJsonObject(JSONObject jo) {
        String jsonStr = jo.toString();
        Log.d("UhcTcpSender", "SEND: " + jsonStr);
        tcpClient.send(jsonStr);
    }

    public void projectorOnOff(boolean on) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("service", "projector");
            jo.put("msg", on ? "projectorOn" : "projectorOff");
        } catch (JSONException e) {
            Log.e("UhcTcpSender", "Json exception:" + e.getMessage());
        }
        sendJsonObject(jo);
    }

    public void hifiOnOff(boolean on) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("service", "hifi");
            jo.put("msg", on ? "hifiOn" : "hifiOff");
        } catch (JSONException e) {
            Log.e("UhcTcpSender", "Json exception:" + e.getMessage());
        }
        sendJsonObject(jo);
    }

    public void hifiSetMediaSource() {
        JSONObject jo = new JSONObject();
        try {
            jo.put("service", "hifi");
            jo.put("msg", "setMediaSource");
        } catch (JSONException e) {
            Log.e("UhcTcpSender", "Json exception:" + e.getMessage());
        }
        sendJsonObject(jo);
    }

    public void airconditionerOnOff(boolean on) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("service", "airconditioner");
            jo.put("msg", on ? "acOn" : "acOff");
        } catch (JSONException e) {
            Log.e("UhcTcpSender", "Json exception:" + e.getMessage());
        }
        sendJsonObject(jo);
    }

    public void airconditionerMode(AirconditionerState airconditionerState) {
        if (!airconditionerState.isValid()) {
            Log.e("UhcTcpSender", "Trying to send invalid AC state of " + airconditionerState.stateCode());
            return;
        }
        JSONObject jo = new JSONObject();
        try {
            jo.put("service", "airconditioner");
            jo.put("msg", "acMode");
            jo.put("setTo", airconditionerState.stateCode());
        } catch (JSONException e) {
            Log.e("UhcTcpSender", "Json exception:" + e.getMessage());
        }
        sendJsonObject(jo);
    }

    public void setGuestMode(final boolean onOff) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("service", "autoshade");
            jo.put("msg", "setGuestMode");
            jo.put("setTo", onOff);
        } catch (JSONException e) {
            Log.e("UhcTcpSender", "Json exception:" + e.getMessage());
        }
        sendJsonObject(jo);
    }
}
