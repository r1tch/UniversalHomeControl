package hu.evolver.uhc.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import hu.evolver.uhc.R;
import hu.evolver.uhc.model.KodiPlayerState;
import hu.evolver.uhc.model.StateUpdateListener;
import hu.evolver.uhc.model.UhcState;

/**
 * Created by rits on 2016-04-26.
 */
public class LightsShadesFragment extends Fragment implements StateUpdateListener {
    private static final String ARG_FRAGMENT_TYPE = "fragment_type";

    private LinearLayout containerLayout = null;
    private NodeType nodeType = NodeType.Light;
    private ZWaveList zWaveList = null;
    private boolean isCreated = false;

    public enum NodeType {Light, Shade}

    public static LightsShadesFragment newInstance(NodeType nodeType) {
        LightsShadesFragment fragment = new LightsShadesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FRAGMENT_TYPE, nodeType.ordinal());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("LightsShadesFragment", "onCreateView");

        nodeType = NodeType.values()[getArguments().getInt(ARG_FRAGMENT_TYPE)];

        View rootView = inflater.inflate(R.layout.fragment_lightsshades, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isCreated = true;

        containerLayout = (LinearLayout) view.findViewById(R.id.the_switches);

        Context context = getContext();
        MainActivity mainActivity = (MainActivity) context;
        if (nodeType == NodeType.Light) {
            mainActivity.fragmentHolder.lightsFragment = this;
            zWaveList = new LightSwitches(this);
        } else {
            mainActivity.fragmentHolder.shadesFragment = this;
            zWaveList = new ShadeButtons(this);
        }

        if (mainActivity.getUhcState() != null)
            onUhcStateCreated(mainActivity.getUhcState());
    }

    @Override
    public void onDestroyView() {
        Log.d("LightsShadesFragment", "onDestroyView");
        super.onDestroyView();

        isCreated = false;

        MainActivity mainActivity = (MainActivity) getContext();

        if (nodeType == NodeType.Light)
            mainActivity.fragmentHolder.lightsFragment = null;
        else
            mainActivity.fragmentHolder.shadesFragment = null;

        if (mainActivity.getUhcState() != null)
            mainActivity.getUhcState().removeListener(this);
        containerLayout = null;
        zWaveList = null;
    }

    public void recreateSwitches() {
        if (containerLayout == null || zWaveList == null)
            return;

        MainActivity mainActivity = (MainActivity) getContext();
        UhcState uhcState = mainActivity.getUhcState();
        if (uhcState == null) {
            Log.d("LightsShadesFragment", "uhcState null");
            return;
        }

        zWaveList.recreateElements(containerLayout, mainActivity);
    }

    @Override
    public void zWaveGotNodes() {
        recreateSwitches();
    }

    @Override
    public void zWaveChangedLevels(int nodeId, int newLevel) {
        if (zWaveList == null)
            return;

        zWaveList.zWaveChangedLevels(nodeId, newLevel);
    }

    public void kodiVolumeChanged(boolean isMuted, double volumePercent) {
    }

    @Override
    public void kodiPlayerUpdate(String type, KodiPlayerState kodiPlayerState) {
    }

    @Override
    public void kodiPlayingItem(String label) {
    }

    public void onUhcStateCreated(UhcState uhcState) {
        if (!isCreated)
            return;

        uhcState.addListener(this);
        recreateSwitches();
    }
}
