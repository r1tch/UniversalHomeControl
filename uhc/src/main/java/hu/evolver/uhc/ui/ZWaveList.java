package hu.evolver.uhc.ui;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;
import java.util.TreeMap;

import hu.evolver.uhc.R;
import hu.evolver.uhc.model.ZWaveNode;

/**
 * Created by rits on 2016-05-05.
 */
public abstract class ZWaveList {
    protected int hpad = 0;
    protected int vpad = 0;
    protected Map<Integer, View> switchMap = new TreeMap<>();

    public ZWaveList(Fragment fragment) {
        hpad = (int) fragment.getResources().getDimension(R.dimen.activity_horizontal_margin);
        vpad = (int) fragment.getResources().getDimension(R.dimen.activity_vertical_margin);

    }

    public abstract View createElement(MainActivity mainActivity, ZWaveNode node);

    public abstract void zWaveChangedLevels(int nodeId, int newLevel);


    public void recreateElements(ViewGroup container, MainActivity mainActivity) {
        container.removeAllViews();
        switchMap.clear();

        Map<String, ZWaveNode> nodesByName = mainActivity.getUhcState().getZWaveNodeStore().allByName();
//        Map<String, ZWaveNode> nodesByName = new TreeMap<>(mainActivity.getUhcState().getZWaveNodeStore().allByName());

        for (Map.Entry<String, ZWaveNode> entry : nodesByName.entrySet()) {
            ZWaveNode node = entry.getValue();

            View v = createElement(mainActivity, node);
            if (v == null)
                continue;

            switchMap.put(node.getId(), v);
            container.addView(v);
            Log.d("ZWaveList", "added view for " + node.getName());
        }
        Log.d("ZWaveList", "ending loop");


    }

}
