package hu.evolver.uhc.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import hu.evolver.uhc.R;
import hu.evolver.uhc.model.KodiPlayerState;
import hu.evolver.uhc.model.StateUpdateListener;
import hu.evolver.uhc.model.UhcState;

/**
 * Created by rroman on 5/31/16.
 */
public class MediaFragment extends Fragment implements StateUpdateListener {
    private boolean isCreated = false;
    private Handler updateHandler = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("MediaFragment", "onCreateView");

        updateHandler = new Handler();

        View rootView = inflater.inflate(R.layout.fragment_media, container, false);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        Log.d("MediaFragment", "onDestroyView");
        isCreated = false;

        updateHandler = null;

        super.onDestroyView();

        MainActivity mainActivity = (MainActivity) getContext();
        if (mainActivity == null)
            return;

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

                MainActivity mainActivity = (MainActivity) getContext();
                if (mainActivity == null)
                    return;

                UhcState uhcState = mainActivity.getUhcState();
                if (uhcState == null)
                    return;

                uhcState.getKodiConnection().onVolumeSeekBarChange(progress);
            }
        });

        SeekBar trackPositiSeekBar = (SeekBar) rootView.findViewById(R.id.trackPositionSeekBar);
        trackPositiSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final double percentage = seekBar.getProgress() / 100.0;

                MainActivity mainActivity = (MainActivity) getContext();
                if (mainActivity == null)
                    return;

                UhcState uhcState = mainActivity.getUhcState();
                if (uhcState == null)
                    return;


                uhcState.getKodiConnection().onTrackPositionSeekBarChange(percentage);
            }
        });

        ImageButton volumeDownButton = (ImageButton) rootView.findViewById(R.id.volumeDownButton);
        volumeDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();
                if (mainActivity == null)
                    return;

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
                if (mainActivity == null)
                    return;

                UhcState uhcState = mainActivity.getUhcState();
                if (uhcState == null)
                    return;

                final boolean isUp = true;
                uhcState.getKodiConnection().onVolumeUpDown(isUp);
            }
        });

        ImageButton prevTrackButton = (ImageButton) rootView.findViewById(R.id.prevTrackButton);
        prevTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();
                if (mainActivity == null)
                    return;

                UhcState uhcState = mainActivity.getUhcState();
                if (uhcState == null)
                    return;

                uhcState.getKodiConnection().onPrevTrack();
            }
        });

        ImageButton nextTrackButton = (ImageButton) rootView.findViewById(R.id.nextTrackButton);
        nextTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = (MainActivity) getContext();
                if (mainActivity == null)
                    return;

                UhcState uhcState = mainActivity.getUhcState();
                if (uhcState == null)
                    return;

                uhcState.getKodiConnection().onNextTrack();
            }
        });
    }

    public void kodiVolumeChanged(boolean isMuted, double volumePercent) {
        Log.d("MediaFragment", "kodiVolumeChanged to: " + volumePercent);

        if (isMuted)
            volumePercent = 0.0;

        View rootView = getView();
        if (rootView == null) {
            Log.d("MediaFragment", "kodiVolumeChanged - rootView is null");
            return;
        }

        SeekBar volumeSeekBar = (SeekBar) rootView.findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setProgress((int) volumePercent);          // max value is set to 100 in the layout XML file
    }

    @Override
    public void kodiPlayerUpdate(final String type, final KodiPlayerState kodiPlayerState) {
        if (!"audio".equals(type))
            return;


        View rootView = getView();
        if (rootView == null) {
            Log.d("MediaFragment", "kodiPlayerUpdate - rootView is null");
            return;
        }

        int seekBarProgress = (int) (kodiPlayerState.percentage * 100);
        SeekBar trackPositionSeekBar = (SeekBar) rootView.findViewById(R.id.trackPositionSeekBar);
        trackPositionSeekBar.setProgress(seekBarProgress);

        String trackTimes = kodiPlayerState.time + "/" + kodiPlayerState.totaltime;
        TextView trackTimesTextView = (TextView) rootView.findViewById(R.id.trackTimes);
        trackTimesTextView.setText(trackTimes);

        if (kodiPlayerState.speed != 0.0 && kodiPlayerState.position != -1)
            scheduleNewUpdate();

        if (kodiPlayerState.position == -1)
            kodiPlayingItem("");            // on stop, clear item's name

        // TODO playlist position update
        // TODO try what needs to be done when 'live' is true
        // TODO shuffle, repeat
    }

    @Override
    public void kodiPlayingItem(String label) {
        View rootView = getView();
        if (rootView == null) {
            Log.d("MediaFragment", "kodiPlayerUpdate - rootView is null");
            return;
        }

        TextView trackLabel = (TextView) rootView.findViewById(R.id.trackLabel);
        trackLabel.setText(label);
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
        uhcState.getKodiConnection().sendUpdateRequest();
        // TODO update state based on uhcState:
        // (re)create playlist elements -- well, first compare our list with the list to avoid flickering
    }

    private void scheduleNewUpdate() {
        MainActivity mainActivity = (MainActivity) getContext();
        if (mainActivity == null)
            return;

        final UhcState uhcState = mainActivity.getUhcState();
        if (uhcState == null)
            return;

        updateHandler.removeCallbacksAndMessages(null); // cancel all

        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!uhcState.getKodiConnection().sendUpdateRequest())
                    scheduleNewUpdate();
            }
        }, 1000);
    }
}
