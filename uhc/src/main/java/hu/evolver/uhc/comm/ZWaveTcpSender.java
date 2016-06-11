package hu.evolver.uhc.comm;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rits on 2016-05-02.
 */
public class ZWaveTcpSender {
    private SimpleTcpClient tcpClient = null;

    public ZWaveTcpSender(@NonNull SimpleTcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    public void zWaveSetLevel(int nodeid, int level) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("service", "zwave");
            jo.put("msg", "setLevel");
            jo.put("nodeid", nodeid);
            jo.put("level", level);
        } catch (JSONException e) {
            Log.e("ZWaveTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("ZWaveTcpSender", "SEND: " + jsonStr);
        tcpClient.send(jsonStr);
    }

    public void zWaveStopLevelChange(int nodeid) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("service", "zwave");
            jo.put("msg", "stopLevelChange");
            jo.put("nodeid", nodeid);
        } catch (JSONException e) {
            Log.e("ZWaveTcpSender", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("ZWaveTcpSender", "SEND: " + jsonStr);
        tcpClient.send(jsonStr);
    }
}
