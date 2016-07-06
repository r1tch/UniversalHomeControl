package hu.evolver.uhc.comm;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import hu.evolver.uhc.model.KodiItem;

/**
 * Created by rroman on 7/5/16.
 */
public class KodiRoot {
    private List<KodiItem> storageRoot = new ArrayList<>(); // note: later might want to separate playlist & storage handling?


    public synchronized List<KodiItem> getStorageRoot() {
        return new ArrayList<KodiItem>(storageRoot);
    }

    public void clear() {
        storageRoot.clear();
    }

    public void addSource(KodiItem kodiItem) {
        Log.d("KodiRoot", "Adding source:" + kodiItem.getLabel());
        storageRoot.add(kodiItem);
    }

    public boolean isEmpty() {
        return storageRoot.isEmpty();
    }
}
