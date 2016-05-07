package hu.evolver.uhc.ui;

import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import hu.evolver.uhc.R;
import hu.evolver.uhc.comm.UhcTcpEncoder;
import hu.evolver.uhc.model.ZWaveNode;

/**
 * Created by rits on 2016-05-04.
 * <p/>
 * Given a view, creates shade controller buttons
 */
public class ShadeButtons extends ZWaveList {
    public ShadeButtons(Fragment fragment) {
        super(fragment);
    }

    @Override
    public View createElement(MainActivity mainActivity, ZWaveNode node) {
        if (!node.isShade())
            return null;

        ButtonRow buttonRow = new ButtonRow(mainActivity, node.getName(), node.getId());
        // TODO set button state based on status

        return buttonRow;
    }

    private void onUpButton(int nodeId, final UhcTcpEncoder encoder) {
        encoder.zWaveSetLevel(nodeId, 100);
    }

    private void onDownButton(int nodeId, final UhcTcpEncoder encoder) {
        encoder.zWaveSetLevel(nodeId, 0);
    }

    private void onStopButton(int nodeId, final UhcTcpEncoder encoder) {
        encoder.zWaveStopLevelChange(nodeId);
    }

    @Override
    public void zWaveChangedLevels(int nodeId, int newLevel) {
        // TODO indicate somehow; only new apis support different colors...
    }

    private class ButtonRow extends LinearLayout {
        public ButtonRow(MainActivity mainActivity, final String name, final int nodeId) {
            super(mainActivity);
            setupLayout();
            final float text_layout_weight = 5f;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, text_layout_weight);
            TextView text = new TextView(mainActivity);
            text.setLayoutParams(params);
            text.setText(name);
            text.setPadding(hpad, vpad, hpad, vpad);
            this.addView(text);

            final UhcTcpEncoder encoder = mainActivity.getEncoder();

            int buttonWidth = (int) getResources().getDimension(R.dimen.shade_button_width);

            params = new LinearLayout.LayoutParams(buttonWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

            ImageButton button = new ImageButton(mainActivity, null, R.attr.borderlessButtonStyle);
            button.setLayoutParams(params);
            button.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onUpButton(nodeId, encoder);
                }
            });
            this.addView(button);

            button = new ImageButton(mainActivity, null, R.attr.borderlessButtonStyle);
            button.setLayoutParams(params);
            button.setImageResource(R.drawable.ic_remove_black_24dp);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onStopButton(nodeId, encoder);
                }
            });
            this.addView(button);

            button = new ImageButton(mainActivity, null, R.attr.borderlessButtonStyle);

            button.setLayoutParams(params);
            button.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDownButton(nodeId, encoder);
                }
            });
            this.addView(button);
        }

        private void setupLayout() {
            this.setOrientation(LinearLayout.HORIZONTAL);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            this.setLayoutParams(params);
        }
    }
}
