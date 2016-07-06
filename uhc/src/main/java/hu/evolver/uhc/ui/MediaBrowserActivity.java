package hu.evolver.uhc.ui;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.List;

import hu.evolver.uhc.R;
import hu.evolver.uhc.comm.UhcConnectivityService;
import hu.evolver.uhc.model.KodiItem;

public class MediaBrowserActivity extends AppCompatActivity {
    private UhcConnectivityService uhcConnectivityService = null;
    private PrefWrap prefWrap = null;

    private ProgressDialog progressDialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_browser);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Context applicationContext = getApplicationContext();
        prefWrap = new PrefWrap(applicationContext);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, UhcConnectivityService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        uhcConnectivityService = null; // should not be necessary (serviceConnection does it)
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause");

        if (uhcConnectivityService != null) {
            if (!prefWrap.wakeup())
                uhcConnectivityService.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MediaBrowserActivity", "onResume");

        Context applicationContext = getApplicationContext();
        applicationContext.startService(UhcConnectivityService.getReconnectIntent(applicationContext));

        initBreadcrumbs(false);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("MBA_ServiceConnection", "onServiceConnected");
            UhcConnectivityService.LocalBinder localBinder =
                    (UhcConnectivityService.LocalBinder) service;
            uhcConnectivityService = localBinder.getService();
            uhcConnectivityService.setMediaBrowserActivity(MediaBrowserActivity.this);

            initBreadcrumbs(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("MBA_ServiceConnection", "onServiceDisconnected");

            // IMPORTANT, this is not being called !!!

            if (uhcConnectivityService != null) {
                uhcConnectivityService.unsetMediaBrowserActivity();
                uhcConnectivityService = null;
            }
        }
    };

    private void cleanupProgressDialog() {
        if (progressDialog == null)
            return;

        progressDialog.setOnDismissListener(null);
        progressDialog.dismiss();
        progressDialog = null;
    }

    private void initBreadcrumbs(boolean clear) {
        LinearLayout breadcrumbLayout = (LinearLayout) findViewById(R.id.breadcrumbLayout);

        if (clear)
            breadcrumbLayout.removeAllViews();

        if (breadcrumbLayout.getChildCount() == 0) {
            addBreadcrumbButton("/", "");
            requestPath("");
        }
    }

    private void requestPath(final String path) {
        cleanupProgressDialog();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.loading);
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                MediaBrowserActivity.this.finish();
            }
        });
        progressDialog.show();

        if (uhcConnectivityService == null)
            return;

        uhcConnectivityService.getKodiConnection().sendGetDirectory(path);
    }

    public void addBreadcrumbButton(String display, String file) {
        LinearLayout breadcrumbLayout = (LinearLayout) findViewById(R.id.breadcrumbLayout);

        Button breadcrumbButton = createBreadcrumbButton(display, file);
        breadcrumbLayout.addView(breadcrumbButton);

        HorizontalScrollView breadcrumbScrollView = (HorizontalScrollView) findViewById(R.id.breadcrumbScrollView);
        breadcrumbScrollView.fullScroll(View.FOCUS_RIGHT);
    }

    public Button createBreadcrumbButton(String display, final String file) {
        Button button = new Button(this, null, R.attr.borderlessButtonStyle);
        button.setText(display);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // remove all buttons greater than us
                LinearLayout parent = (LinearLayout) v.getParent();
                int position = parent.indexOfChild(v);
                for (int i = parent.getChildCount() - 1; i > position; ++i)
                    parent.removeViewAt(i);

                requestPath(file);
            }
        });

        return button;
    }

    public void kodiDirectoryUpdate(final List<KodiItem> kodiItems) {
        cleanupProgressDialog();

        LinearLayout songlistContainerLayout = (LinearLayout) findViewById(R.id.songlistContainerLayout);
        songlistContainerLayout.removeAllViews();

        // TODO clear buttons

        if (kodiItems == null) {        // means error -> return to root
            initBreadcrumbs(true);
            return;
        }

        for (KodiItem kodiItem : kodiItems) {
            Log.d("MediaBrowserActivity", "Adding " + kodiItem.getLabel());
            songlistContainerLayout.addView(new ItemDisplay(this, kodiItem));
        }

    }

    private void addItemToPlaylist(KodiItem kodiItem) {
        if (uhcConnectivityService == null)
            return;

        uhcConnectivityService.getKodiConnection().sendAddToAudioPlaylist(kodiItem);
    }

    private void descendIntoDirectory(KodiItem directoryItem) {
        LinearLayout songlistContainerLayout = (LinearLayout) findViewById(R.id.songlistContainerLayout);
        songlistContainerLayout.removeAllViews();

        addBreadcrumbButton(directoryItem.label, directoryItem.file);
        requestPath(directoryItem.file);
    }

    private class ItemDisplay extends KodiItemDisplay {
        public ItemDisplay(final MediaBrowserActivity mediaBrowserActivity, final KodiItem kodiItem) {
            super(mediaBrowserActivity, kodiItem);

            this.addView(createAdderIcon());

            final boolean isDirectory = "directory".equals(this.kodiItem.filetype);
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isDirectory)
                        descendIntoDirectory(kodiItem);
                    // else branch:  would be nice if we could play and add right away - Kodi does not support this :(
                }
            });
        }

        private View createAdderIcon() {
            ImageButton imageButton = new ImageButton(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0f);
            params.gravity = Gravity.CENTER_VERTICAL;
            imageButton.setLayoutParams(params);

            imageButton.setImageResource(R.drawable.ic_add_black_24dp);

            imageButton.setColorFilter(R.color.colorPrimary);
            // TODO check design
//            final int sidePad = (int) getResources().getDimension(R.dimen.list_icon_side_padding);
//            imageButton.setPadding(sidePad, 0, sidePad, 0);

            imageButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    addItemToPlaylist(kodiItem);
                }
            });

            return imageButton;
        }
    }
}
