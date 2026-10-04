package name.lmj001.saveondevice;

import org.json.JSONException;
import org.json.JSONObject;

/** One folder the app has saved files into. */
public class SaveLocation {
    public String uri;
    public String name;
    public String path;
    public long lastUsed;
    public int count;

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("uri", uri);
        object.put("name", name);
        object.put("path", path);
        object.put("lastUsed", lastUsed);
        object.put("count", count);
        return object;
    }

    public static SaveLocation fromJson(JSONObject object) {
        SaveLocation location = new SaveLocation();
        location.uri = object.optString("uri");
        location.name = object.optString("name");
        location.path = object.optString("path");
        location.lastUsed = object.optLong("lastUsed");
        location.count = object.optInt("count");
        return location;
    }
}
