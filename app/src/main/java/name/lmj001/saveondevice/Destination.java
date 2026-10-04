package name.lmj001.saveondevice;

import org.json.JSONException;
import org.json.JSONObject;

/** A place a file can be written to: either a user-picked SAF folder or a public MediaStore collection. */
public class Destination {

    public static final int TYPE_SAF = 0;
    public static final int TYPE_MEDIASTORE = 1;

    public static final String MS_DOWNLOADS = "downloads";
    public static final String MS_DOCUMENTS = "documents";
    public static final String MS_PICTURES = "pictures";
    public static final String MS_MOVIES = "movies";
    public static final String MS_MUSIC = "music";

    public String id;
    public String folderName;
    public String storageName;
    public int type;
    public String value;

    public Destination() { }

    public Destination(String id, String folderName, String storageName, int type, String value) {
        this.id = id;
        this.folderName = folderName;
        this.storageName = storageName;
        this.type = type;
        this.value = value;
    }

    public static Destination mediaStore(String key, String folderName) {
        return new Destination("ms:" + key, folderName, "Internal storage", TYPE_MEDIASTORE, key);
    }

    public static Destination saf(String treeUri, String folderName, String storageName) {
        return new Destination("saf:" + treeUri, folderName, storageName, TYPE_SAF, treeUri);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("folderName", folderName);
        object.put("storageName", storageName);
        object.put("type", type);
        object.put("value", value);
        return object;
    }

    public static Destination fromJson(JSONObject object) {
        return new Destination(
                object.optString("id"),
                object.optString("folderName"),
                object.optString("storageName"),
                object.optInt("type"),
                object.optString("value"));
    }
}
