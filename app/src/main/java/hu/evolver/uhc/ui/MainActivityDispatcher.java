package hu.evolver.uhc.ui;

/**
 * Created by rits on 2016-05-03.
 */
public class MainActivityDispatcher {
    public MainActivity mainActivity = null;   // docs say we should not store this ref, but we're careful, pinky-promise!

    public void dispatch(Runnable runnable) {
        if (mainActivity != null)
            mainActivity.runOnUiThread(runnable);
    }
}
