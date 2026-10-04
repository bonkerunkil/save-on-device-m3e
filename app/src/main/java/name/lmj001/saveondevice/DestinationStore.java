package name.lmj001.saveondevice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/** Remembers the destination the user last chose, the folders they pinned, and a default. */
public class DestinationStore {

    private static final String PREFS = "save_destination";
    private static final String KEY_CURRENT = "current";
    private static final String KEY_PINNED = "pinned";
    private static final String KEY_QUICK_ORDER = "quick_order";

    private final SharedPreferences prefs;

    public DestinationStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public Destination getCurrent() {
        try {
            String raw = prefs.getString(KEY_CURRENT, null);
            if (raw != null && !raw.isEmpty()) {
                return Destination.fromJson(new JSONObject(raw));
            }
        } catch (Exception ignored) { }
        return defaultDestination();
    }

    /** Downloads via MediaStore on Android 10+, otherwise nothing (the user must pick a folder). */
    public static Destination defaultDestination() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return Destination.mediaStore(Destination.MS_DOWNLOADS, "Downloads");
        }
        return null;
    }

    public void setCurrent(Destination destination) {
        try {
            prefs.edit().putString(KEY_CURRENT, destination == null ? "" : destination.toJson().toString()).apply();
        } catch (Exception ignored) { }
    }

    // ------------------------------------------------------------------ pinned

    public List<Destination> getPinned() {
        List<Destination> out = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(KEY_PINNED, "[]"));
            for (int i = 0; i < array.length(); i++) {
                out.add(Destination.fromJson(array.getJSONObject(i)));
            }
        } catch (Exception ignored) { }
        return out;
    }

    /** @return true if the folder was newly pinned, false if it was already there. */
    public boolean addPinned(Destination destination) {
        if (destination == null || destination.id == null) return false;
        List<Destination> list = getPinned();
        for (Destination existing : list) {
            if (destination.id.equals(existing.id)) return false;
        }
        list.add(destination);
        persistPinned(list);
        return true;
    }

    public void removePinned(String id) {
        if (id == null) return;
        List<Destination> list = getPinned();
        for (int i = 0; i < list.size(); i++) {
            if (id.equals(list.get(i).id)) {
                list.remove(i);
                break;
            }
        }
        persistPinned(list);
    }

    /** Persist the pinned folders in the given order. */
    public void setPinned(List<Destination> list) {
        persistPinned(list == null ? new ArrayList<Destination>() : list);
    }

    // ------------------------------------------------- quick-button order

    /**
     * The order the quick-location buttons appear in, as a list of destination ids. Includes the
     * built-in folders (Downloads, Documents, ...) so the user can move those too.
     */
    public List<String> getQuickOrder() {
        List<String> out = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(KEY_QUICK_ORDER, "[]"));
            for (int i = 0; i < array.length(); i++) {
                out.add(array.getString(i));
            }
        } catch (Exception ignored) { }
        return out;
    }

    public void setQuickOrder(List<String> order) {
        JSONArray array = new JSONArray();
        if (order != null) {
            for (String key : order) {
                array.put(key);
            }
        }
        prefs.edit().putString(KEY_QUICK_ORDER, array.toString()).apply();
    }

    private void persistPinned(List<Destination> list) {
        JSONArray array = new JSONArray();
        try {
            for (Destination destination : list) {
                array.put(destination.toJson());
            }
        } catch (Exception ignored) { }
        prefs.edit().putString(KEY_PINNED, array.toString()).apply();
    }
}
