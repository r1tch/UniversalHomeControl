package hu.evolver.uhc.comm;

import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rits on 2016-05-02.
 */
public class UhcTcpEncoder {
    private SimpleTcpClient tcpClient = null;

    public UhcTcpEncoder(@NonNull SimpleTcpClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    public void zWaveSetLevel(int id, int level) {
        JSONObject jo = new JSONObject();
        try {
            jo.put("service", "zwave");
            jo.put("msg", "setLevel");
            jo.put("id", id);
            jo.put("level", level);
        } catch (JSONException e) {
            Log.e("UhcTcpEncoder", "Json exception:" + e.getMessage());
        }

        String jsonStr = jo.toString();
        Log.d("UhcTcpEncoder", "SEND: " + jsonStr);
        tcpClient.send(jsonStr);
    }
}
