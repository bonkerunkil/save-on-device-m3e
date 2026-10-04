package name.lmj001.saveondevice;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/** Persists the list of folders the app has saved files into. */
public class LocationStore {

    private static final String PREFS = "save_locations";
    private static final String KEY = "items";

    private final SharedPreferences prefs;

    public LocationStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<SaveLocation> getAll() {
        List<SaveLocation> out = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString(KEY, "[]"));
            for (int i = 0; i < array.length(); i++) {
                out.add(SaveLocation.fromJson(array.getJSONObject(i)));
            }
        } catch (Exception ignored) { }
        return out;
    }

    private void persist(List<SaveLocation> list) {
        JSONArray array = new JSONArray();
        try {
            for (SaveLocation location : list) {
                array.put(location.toJson());
            }
        } catch (Exception ignored) { }
        prefs.edit().putString(KEY, array.toString()).apply();
    }

    /** Records a save into {@code key} (the folder label), bumping its counter. */
    public void record(String key, String name, String path) {
        if (key == null || key.trim().isEmpty()) return;
        List<SaveLocation> list = getAll();
        for (SaveLocation location : list) {
            if (key.equals(location.uri)) {
                location.name = name;
                location.path = path;
                location.count = location.count + 1;
                location.lastUsed = System.currentTimeMillis();
                persist(list);
                return;
            }
        }
        SaveLocation location = new SaveLocation();
        location.uri = key;
        location.name = name;
        location.path = path;
        location.count = 1;
        location.lastUsed = System.currentTimeMillis();
        list.add(0, location);
        persist(list);
    }

    public void remove(String key) {
        List<SaveLocation> list = getAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).uri != null && list.get(i).uri.equals(key)) {
                list.remove(i);
                break;
            }
        }
        persist(list);
    }

    public void clear() {
        prefs.edit().remove(KEY).apply();
    }
}
