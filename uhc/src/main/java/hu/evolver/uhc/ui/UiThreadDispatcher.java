package hu.evolver.uhc.ui;

/**
 * Created by rits on 2016-05-03.
 */
public class UiThreadDispatcher {
    public MainActivity mainActivity = null;   // docs say we should not store this ref, but we're careful, pinky-promise!
    public MediaBrowserActivity mediaBrowserActivity = null;

    public void dispatchToMainActivity(Runnable runnable) {
        if (mainActivity != null)
            mainActivity.runOnUiThread(runnable);
    }

    public void dispatchToMediaBrowserActivity(Runnable runnable) {
        if (mediaBrowserActivity!= null)
            mediaBrowserActivity.runOnUiThread(runnable);
    }
}
