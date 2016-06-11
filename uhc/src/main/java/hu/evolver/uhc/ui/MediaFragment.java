package hu.evolver.uhc.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import hu.evolver.uhc.R;
import hu.evolver.uhc.comm.KodiConnection;
import hu.evolver.uhc.model.JsonState;
import hu.evolver.uhc.model.KodiItem;
import hu.evolver.uhc.model.KodiPlayers;
import hu.evolver.uhc.model.StateUpdateListener;
import hu.evolver.uhc.model.UhcState;

/**
 * Created by rroman on 5/31/16.
 */
public class MediaFragment extends Fragment implements StateUpdateListener {
    private boolean isCreated = false;
    private Handler updateHandler = null;
    private int lastKnownPosition = -1;

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

        updateHandler.removeCallbacksAndMessages(null);
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

                KodiConnection kodiConnection = getKodiConnection();
                if (kodiConnection == null)
                    return;

                kodiConnection.onVolumeSeekBarChange(progress);
            }
        });

        SeekBar trackPositionSeekBar = (SeekBar) rootView.findViewById(R.id.trackPositionSeekBar);
        trackPositionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                final double percentage = seekBar.getProgress() / 100.0;

                KodiConnection kodiConnection = getKodiConnection();
                if (kodiConnection == null)
                    return;

                kodiConnection.onTrackPositionSeekBarChange(percentage);
            }
        });

        ImageButton volumeDownButton = (ImageButton) rootView.findViewById(R.id.volumeDownButton);
        volumeDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KodiConnection kodiConnection = getKodiConnection();
                if (kodiConnection == null)
                    return;

                final boolean isUp = false;
                kodiConnection.onVolumeUpDown(isUp);
            }
        });

        ImageButton volumeUpButton = (ImageButton) rootView.findViewById(R.id.volumeUpButton);
        volumeUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KodiConnection kodiConnection = getKodiConnection();
                if (kodiConnection == null)
                    return;

                final boolean isUp = true;
                kodiConnection.onVolumeUpDown(isUp);
            }
        });

        ImageButton prevTrackButton = (ImageButton) rootView.findViewById(R.id.prevTrackButton);
        prevTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KodiConnection kodiConnection = getKodiConnection();
                if (kodiConnection == null)
                    return;

                kodiConnection.onPrevTrack();
            }
        });

        ImageButton nextTrackButton = (ImageButton) rootView.findViewById(R.id.nextTrackButton);
        nextTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KodiConnection kodiConnection = getKodiConnection();
                if (kodiConnection == null)
                    return;

                kodiConnection.onNextTrack();
            }
        });
    }

    private KodiConnection getKodiConnection() {
        MainActivity mainActivity = (MainActivity) getContext();
        if (mainActivity == null)
            return null;

        UhcState uhcState = mainActivity.getUhcState();
        if (uhcState == null)
            return null;

        return uhcState.getKodiConnection();
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
    public void kodiPlayerUpdate(KodiPlayers.Player player) {
        View rootView = getView();
        if (rootView == null) {
            Log.d("MediaFragment", "kodiPlayerUpdate - rootView is null");
            return;
        }

        if ("picture".equals(player.type))
            return;

        if (!player.isActive)
            return;

        int seekBarProgress = (int) (player.percentage * 100);
        SeekBar trackPositionSeekBar = (SeekBar) rootView.findViewById(R.id.trackPositionSeekBar);
        trackPositionSeekBar.setProgress(seekBarProgress);

        TextView trackTimesTextView = (TextView) rootView.findViewById(R.id.trackTimes);

        if (player.isPlaying())
            scheduleNewUpdate();

        String trackTimes;
        if (player.isStopped()) {
            trackTimes = "--:--/--:--";
            kodiPlayingItem("");            // on stop, clear item's name
        } else
            trackTimes = player.time + "/" + player.totaltime;

        trackTimesTextView.setText(trackTimes);

        updateAudioPosition(rootView, player.position);

        // TODO try what needs to be done when 'live' is true
        // TODO shuffle, repeat
    }

    private void updateAudioPosition(@NonNull View rootView, int position) {
        lastKnownPosition = position;       // nifty, on Stopped, this will be -1

        LinearLayout tracklistContainerLayout = (LinearLayout) rootView.findViewById(R.id.trackListContainerLayout);

        for (int i = 0; i < tracklistContainerLayout.getChildCount(); ++i) {
            ItemDisplay itemDisplay = (ItemDisplay) tracklistContainerLayout.getChildAt(i);
            if (i == position) {
                itemDisplay.playing();
            } else {
                itemDisplay.notPlaying();
            }
        }
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
    public void kodiClearPlaylist() {
        View rootView = getView();
        if (rootView == null) {
            Log.d("MediaFragment", "kodiClearPlaylist - rootView is null");
            return;
        }

        LinearLayout tracklistContainerLayout = (LinearLayout) rootView.findViewById(R.id.trackListContainerLayout);
        tracklistContainerLayout.removeAllViews();
    }

    @Override
    public void kodiAddPlaylistItem(int position, KodiItem item, int newLength) {
        View rootView = getView();
        if (rootView == null) {
            Log.d("MediaFragment", "kodiClearPlaylist - rootView is null");
            return;
        }

        LinearLayout tracklistContainerLayout = (LinearLayout) rootView.findViewById(R.id.trackListContainerLayout);
        int countPlusOne = tracklistContainerLayout.getChildCount() + 1;
        if (countPlusOne != newLength) {
            Log.e("MediaFragment", "kodiAddPlaylistItem #" + position + ", expected newLength:" + newLength
                    + ", but would result in length:" + countPlusOne);
            KodiConnection kodiConnection = getKodiConnection();
            kodiConnection.sendPlaylistUpdateRequest();         // something bad happened, try getting info again, repopulate full playlist
            return;
        }

        MainActivity mainActivity = (MainActivity) rootView.getContext();
        tracklistContainerLayout.addView(new ItemDisplay(mainActivity, item, position), position);
    }

    @Override
    public void kodiRemovePlaylistItem(int position, int newLength) {
        View rootView = getView();
        if (rootView == null) {
            Log.d("MediaFragment", "kodiClearPlaylist - rootView is null");
            return;
        }

        LinearLayout tracklistContainerLayout = (LinearLayout) rootView.findViewById(R.id.trackListContainerLayout);
        int countMinusOne = tracklistContainerLayout.getChildCount() - 1;
        if (countMinusOne != newLength) {
            Log.e("MediaFragment", "kodiRemovePlaylistItem #" + position + ", expected newLength:" + newLength
                    + ", but would result in length:" + countMinusOne);
            KodiConnection kodiConnection = getKodiConnection();
            kodiConnection.sendPlaylistUpdateRequest();         // something bad happened, try getting info again, repopulate full playlist
            return;
        }
        tracklistContainerLayout.removeViewAt(position);
    }

    @Override
    public void stateChanged(JsonState state) {

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
        uhcState.getKodiConnection().sendItemUpdateRequest();
        uhcState.getKodiConnection().sendPlaylistUpdateRequest();
    }

    private void scheduleNewUpdate() {
        final KodiConnection kodiConnection = getKodiConnection();
        if (kodiConnection == null)
            return;

        updateHandler.removeCallbacksAndMessages(null); // cancel all

        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!kodiConnection.sendUpdateRequest())
                    scheduleNewUpdate();
            }
        }, 1000);
    }

    private class ItemDisplay extends LinearLayout {

        private KodiItem kodiItem = null;
        private TextView titleTextView = null;

        public ItemDisplay(final MainActivity mainActivity, final KodiItem kodiItem, int position) {
            super(mainActivity);
            this.kodiItem = kodiItem;
            setupLayout();
            this.addView(createIcon());
            this.addView(createTitleArtist());
            this.addView(createDuration());

            if (lastKnownPosition == position)
                playing();

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    KodiConnection kodiConnection = mainActivity.getKodiConnection();
                    if (kodiConnection == null)
                        return;

                    int myPosition = ((LinearLayout)getParent()).indexOfChild(view);

                    kodiConnection.openAudioPlaylistPosition(myPosition);
                }
            });
            // TODO longpress listener
        }

        public void notPlaying() {
            titleTextView.setTypeface(null, Typeface.NORMAL);
        }

        public void playing() {
            titleTextView.setTypeface(null, Typeface.BOLD);
        }

        private void setupLayout() {
            this.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.setLayoutParams(params);
        }

        private View createIcon() {
            // TODO directory, stream icons?
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f);
            params.gravity = Gravity.CENTER_VERTICAL;
            imageView.setLayoutParams(params);
            imageView.setImageResource(R.drawable.ic_music_note_black_24dp);
            imageView.setColorFilter(Color.rgb(0x85, 0x85, 0x85));      // TODO get color from theme's default text color
            final int sidePad = (int) getResources().getDimension(R.dimen.list_icon_side_padding);
            imageView.setPadding(sidePad, 0, sidePad, 0);

            return imageView;
        }

        private View createTitleArtist() {
            LinearLayout titleArtistLayout = new LinearLayout(getContext());
            titleArtistLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            titleArtistLayout.setLayoutParams(params);

            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            titleTextView = new TextView(getContext());
            titleTextView.setLayoutParams(params);
            if (!kodiItem.title.isEmpty())
                titleTextView.setText(kodiItem.title);
            else if (!kodiItem.label.isEmpty())
                titleTextView.setText(kodiItem.label);
            else
                titleTextView.setText(kodiItem.file);

            titleTextView.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
            titleArtistLayout.addView(titleTextView);

            TextView artistTextView = new TextView(getContext());
            artistTextView.setLayoutParams(params);
            artistTextView.setText(kodiItem.artist);
            artistTextView.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
            artistTextView.setTypeface(null, Typeface.ITALIC);
            titleArtistLayout.addView(artistTextView);

            return titleArtistLayout;
        }

        private View createDuration() {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f);
            params.gravity = Gravity.CENTER_VERTICAL;
            TextView durationTextView = new TextView(getContext());
            durationTextView.setLayoutParams(params);
            durationTextView.setGravity(Gravity.RIGHT);
            durationTextView.setText(secsToHhmmss(kodiItem.durationSecs));
            durationTextView.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
            durationTextView.setTypeface(null, Typeface.ITALIC);
            final int sidePad = (int) getResources().getDimension(R.dimen.list_icon_side_padding);
            durationTextView.setPadding(sidePad, 0, sidePad, 0);

            return durationTextView;
        }


        private String secsToHhmmss(int totalSeconds) {
            int seconds = totalSeconds % 60;
            totalSeconds /= 60;
            int minutes = totalSeconds % 60;
            totalSeconds /= 60;
            int hours = totalSeconds;

            if (hours > 0)
                return String.format("%d:%02d:%02d", hours, minutes, seconds);

            return String.format("%d:%02d", minutes, seconds);
        }
    }
}
