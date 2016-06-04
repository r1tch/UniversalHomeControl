package hu.evolver.uhc.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import hu.evolver.uhc.R;
import hu.evolver.uhc.model.StateUpdateListener;
import hu.evolver.uhc.model.UhcState;

/**
 * Created by rroman on 5/31/16.
 */
public class MediaFragment extends Fragment implements StateUpdateListener {
    private boolean isCreated = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("MediaFragment", "onCreateView");

        View rootView = inflater.inflate(R.layout.fragment_media, container, false);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        Log.d("MediaFragment", "onDestroyView");
        isCreated = false;

        super.onDestroyView();

        MainActivity mainActivity = (MainActivity) getContext();

        if (mainActivity.getUhcState() != null)
            mainActivity.getUhcState().removeListener(this);

        mainActivity.fragmentHolder.mediaFragment = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d("MediaFragment", "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        isCreated = true;

        createCallbacks(view);

        Context context = getContext();
        MainActivity mainActivity = (MainActivity) context;
        mainActivity.fragmentHolder.mediaFragment = this;

        UhcState uhcState = mainActivity.getUhcState();
        if (uhcState != null)
            onUhcStateCreated(uhcState);
    }

    private void createCallbacks(View rootView) {
        SeekBar volumeSeekBar = (SeekBar) rootView.findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final int progress = seekBar.getProgress();
                Log.d("MediaFragment", "onStopTrackingTouch, progress: " + progress);

                MainActivity mainActivity = (MainActivity) getContext();

                UhcState uhcState = mainActivity.getUhcState();
                if (uhcState == null)
                    return;

                uhcState.getKodiConnection().onVolumeSeekBarChange(progress);
            }
        });

        ImageButton volumeDownButton = (ImageButton) rootView.findViewById(R.id.volumeDownButton);
        volumeDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();

                UhcState uhcState = mainActivity.getUhcState();
                if (uhcState == null)
                    return;

                final boolean isUp = false;
                uhcState.getKodiConnection().onVolumeUpDown(isUp);
            }
        });

        ImageButton volumeUpButton = (ImageButton) rootView.findViewById(R.id.volumeUpButton);
        volumeUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();

                UhcState uhcState = mainActivity.getUhcState();
                if (uhcState == null)
                    return;

                final boolean isUp = true;
                uhcState.getKodiConnection().onVolumeUpDown(isUp);
            }
        });
    }

    public void kodiVolumeChanged(boolean isMuted, double volumePercent) {
        Log.d("MediaFragment", "kodiVolumeChanged to: " + volumePercent);

        if (isMuted)
            volumePercent = 0.0;

        View rootView = getView();
        if (rootView == null) {
            Log.d("MediaFragment", "rootView is null");
            return;
        }

        SeekBar volumeSeekBar = (SeekBar) rootView.findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setProgress((int) volumePercent);          // max value is set to 100 in the layout XML file
    }

    @Override
    public void zWaveGotNodes() {
    }

    @Override
    public void zWaveChangedLevels(int nodeId, int newLevel) {
    }

    public void onUhcStateCreated(UhcState uhcState) {
        Log.d("MediaFragment", "onUhcStateCreated");
        if (!isCreated)
            return;

        uhcState.addListener(this);
        kodiVolumeChanged(uhcState.getKodiConnection().isMuted(), uhcState.getKodiConnection().getVolumePercent());
        // TODO update state based on uhcState:
        // position slider update
        // (re)create playlist elements -- well, first compare our list with the list to avoid flickering
    }
}
