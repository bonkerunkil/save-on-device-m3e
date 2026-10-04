package name.lmj001.saveondevice;

import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;

/** Turns Storage Access Framework URIs into friendly folder / storage names. */
public final class DocumentUtils {

    private DocumentUtils() { }

    /** "Internal storage" or "SD card (XXXX-XXXX)". */
    public static String storageNameFromUri(Context context, Uri uri) {
        return parse(context, uri)[0];
    }

    /** The leaf folder name, e.g. "Download" or "Test". */
    public static String folderNameFromUri(Context context, Uri uri) {
        return parse(context, uri)[1];
    }

    /** Full label, e.g. "Internal storage / Download". */
    public static String folderLabelFromUri(Context context, Uri uri) {
        String[] parts = parse(context, uri);
        return parts[1].isEmpty() || parts[1].equals(parts[0]) ? parts[0] : parts[0] + " / " + parts[1];
    }

    /** Folder label for a document URI (a file) - drops the file name. */
    public static String parentFolderLabelFromDocumentUri(Context context, Uri uri) {
        String[] parts = parentFolderAndStorage(context, uri);
        return parts[1].isEmpty() ? parts[0] : parts[0] + " / " + parts[1];
    }

    /** Returns {storageName, folderName} for a document URI, dropping the file name. */
    public static String[] parentFolderAndStorage(Context context, Uri uri) {
        String docId = null;
        try {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                docId = DocumentsContract.getDocumentId(uri);
            }
        } catch (Exception ignored) { }
        if (docId == null) {
            return new String[] { "Internal storage", "" };
        }

        int colon = docId.indexOf(':');
        String volume = colon >= 0 ? docId.substring(0, colon) : "";
        String path = colon >= 0 ? docId.substring(colon + 1) : docId;

        int slash = path.lastIndexOf('/');
        if (slash >= 0) path = path.substring(0, slash);

        String storage = storageFor(volume);
        String folder;
        if (path.isEmpty()) {
            folder = storage;
        } else {
            int lastSlash = path.lastIndexOf('/');
            folder = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        }
        return new String[] { storage, folder };
    }

    /** Returns {storageName, folderName}. */
    private static String[] parse(Context context, Uri uri) {
        String docId = null;
        try {
            if (DocumentsContract.isTreeUri(uri)) {
                docId = DocumentsContract.getTreeDocumentId(uri);
            } else if (DocumentsContract.isDocumentUri(context, uri)) {
                docId = DocumentsContract.getDocumentId(uri);
            }
        } catch (Exception ignored) { }

        if (docId == null) {
            String segment = uri.getLastPathSegment();
            String name = segment == null ? "" : Uri.decode(segment);
            int slash = name.lastIndexOf('/');
            if (slash >= 0) name = name.substring(slash + 1);
            return new String[] { "Internal storage", name };
        }

        int colon = docId.indexOf(':');
        String volume = colon >= 0 ? docId.substring(0, colon) : "";
        String path = colon >= 0 ? docId.substring(colon + 1) : docId;

        String storage = storageFor(volume);
        String folder;
        if (path.isEmpty()) {
            folder = storage;
        } else {
            int slash = path.lastIndexOf('/');
            folder = slash >= 0 ? path.substring(slash + 1) : path;
        }
        return new String[] { storage, folder };
    }

    private static String storageFor(String volume) {
        if (volume.isEmpty() || "primary".equalsIgnoreCase(volume)) {
            return "Internal storage";
        }
        return "SD card (" + volume + ")";
    }
}
