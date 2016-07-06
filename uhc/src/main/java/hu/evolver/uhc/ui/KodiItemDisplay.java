package hu.evolver.uhc.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import hu.evolver.uhc.R;
import hu.evolver.uhc.comm.KodiConnection;
import hu.evolver.uhc.model.KodiItem;

/**
 * Created by rroman on 7/5/16.
 */
public class KodiItemDisplay extends LinearLayout {
    protected KodiItem kodiItem = null;
    private TextView titleTextView = null;

    public KodiItemDisplay(final Context context, final KodiItem kodiItem) {
        super(context);

        this.kodiItem = kodiItem;
        setupLayout();
        this.addView(createIcon());
        this.addView(createTitleArtist());
        this.addView(createDuration());

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
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f);
        params.gravity = Gravity.CENTER_VERTICAL;
        imageView.setLayoutParams(params);

        if ("file".equals(kodiItem.filetype))
            imageView.setImageResource(R.drawable.ic_music_note_black_24dp);
        else if ("directory".equals(kodiItem.filetype))
            imageView.setImageResource(R.drawable.ic_folder_black_24dp);
        else
            imageView.setImageResource(R.drawable.ic_check_box_outline_blank_black_24dp);

        // imageView.setColorFilter(Color.rgb(0x85, 0x85, 0x85));      // TODO remove if OK
        imageView.setColorFilter(R.color.colorPrimary);
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
