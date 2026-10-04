package name.lmj001.saveondevice;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.format.Formatter;

import java.io.InputStream;
import java.io.OutputStream;

/** Writes a file into a {@link Destination}, via SAF or MediaStore. */
public final class FileSaver {

    private FileSaver() { }

    /** Save a shared file. */
    public static boolean write(Context context, Destination destination, Uri source, String name, String mimeType) {
        if (destination == null || source == null) return false;
        if (mimeType == null || mimeType.isEmpty()) mimeType = "application/octet-stream";
        if (destination.type == Destination.TYPE_SAF) {
            return writeToTree(context, destination.value, name, mimeType, source, null);
        }
        return writeToMediaStore(context, destination.value, name, mimeType, source, null);
    }

    /** Save shared text as a file. */
    public static boolean writeText(Context context, Destination destination, String text, String name) {
        if (destination == null || text == null) return false;
        byte[] bytes = text.getBytes();
        if (destination.type == Destination.TYPE_SAF) {
            return writeToTree(context, destination.value, name, "text/plain", null, bytes);
        }
        return writeToMediaStore(context, destination.value, name, "text/plain", null, bytes);
    }

    /** Write straight to a document URI returned by the system picker (used as a fallback). */
    public static boolean writeToDocumentUri(Context context, Uri outputUri, Uri source, byte[] bytes) {
        if (outputUri == null) return false;
        try {
            ContentResolver resolver = context.getContentResolver();
            OutputStream out = resolver.openOutputStream(outputUri);
            if (out == null) return false;
            if (bytes != null) {
                out.write(bytes);
            } else {
                InputStream in = resolver.openInputStream(source);
                if (in == null) { out.close(); return false; }
                copy(in, out);
                in.close();
            }
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------------------------------------------------------- SAF

    private static boolean writeToTree(Context context, String treeUriString, String name,
                                       String mimeType, Uri source, byte[] bytes) {
        try {
            ContentResolver resolver = context.getContentResolver();
            Uri treeUri = Uri.parse(treeUriString);
            Uri docUri = DocumentsContract.buildDocumentUriUsingTree(
                    treeUri, DocumentsContract.getTreeDocumentId(treeUri));
            Uri fileUri = DocumentsContract.createDocument(resolver, docUri, mimeType, name);
            if (fileUri == null) return false;

            OutputStream out = resolver.openOutputStream(fileUri);
            if (out == null) return false;
            if (bytes != null) {
                out.write(bytes);
            } else {
                InputStream in = resolver.openInputStream(source);
                if (in == null) { out.close(); return false; }
                copy(in, out);
                in.close();
            }
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------------------------------------------------- MediaStore

    /**
     * Writes into a public collection. The folder the user picked and the type of the file are
     * independent: an APK sent to "Pictures" must still land in Pictures, so if the file type does
     * not match the typed collection we fall back to MediaStore.Files, which accepts every MIME
     * type and every relative path.
     */
    private static boolean writeToMediaStore(Context context, String key, String name,
                                             String mimeType, Uri source, byte[] bytes) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false;

        String relativePath = relativePathFor(key);

        Uri primary = collectionFor(key, mimeType);
        if (attemptMediaStore(context, primary, relativePath, name, mimeType, source, bytes)) return true;

        Uri files = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        if (!primary.equals(files)
                && attemptMediaStore(context, files, relativePath, name, mimeType, source, bytes)) return true;

        return false;
    }

    private static boolean attemptMediaStore(Context context, Uri collection, String relativePath,
                                             String name, String mimeType, Uri source, byte[] bytes) {
        ContentResolver resolver = context.getContentResolver();
        Uri item = null;
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);

            item = resolver.insert(collection, values);
            if (item == null) return false;

            OutputStream out = resolver.openOutputStream(item);
            if (out == null) {
                resolver.delete(item, null, null);
                return false;
            }

            if (bytes != null) {
                out.write(bytes);
            } else {
                InputStream in = resolver.openInputStream(source);
                if (in == null) {
                    out.close();
                    resolver.delete(item, null, null);
                    return false;
                }
                copy(in, out);
                in.close();
            }
            out.flush();
            out.close();

            ContentValues done = new ContentValues();
            done.put(MediaStore.MediaColumns.IS_PENDING, 0);
            resolver.update(item, done, null, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (item != null) {
                try { resolver.delete(item, null, null); } catch (Exception ignored) { }
            }
            return false;
        }
    }

    /** The collection that accepts this MIME type for the requested folder. */
    private static Uri collectionFor(String key, String mimeType) {
        boolean image = mimeType != null && mimeType.startsWith("image/");
        boolean video = mimeType != null && mimeType.startsWith("video/");
        boolean audio = mimeType != null && mimeType.startsWith("audio/");

        switch (key) {
            case Destination.MS_PICTURES:
                if (image) return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case Destination.MS_MOVIES:
                if (video) return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case Destination.MS_MUSIC:
                if (audio) return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
            case Destination.MS_DOWNLOADS:
                return MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            default:
                break;
        }
        return MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
    }

    private static String relativePathFor(String key) {
        switch (key) {
            case Destination.MS_PICTURES:
                return Environment.DIRECTORY_PICTURES;
            case Destination.MS_MOVIES:
                return Environment.DIRECTORY_MOVIES;
            case Destination.MS_MUSIC:
                return Environment.DIRECTORY_MUSIC;
            case Destination.MS_DOCUMENTS:
                return Environment.DIRECTORY_DOCUMENTS;
            case Destination.MS_DOWNLOADS:
            default:
                return Environment.DIRECTORY_DOWNLOADS;
        }
    }

    private static void copy(InputStream in, OutputStream out) throws Exception {
        byte[] buffer = new byte[8192];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }

    /** Free space on internal storage, formatted like "42.6 GB". */
    public static String availableSpace(Context context) {
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            return Formatter.formatFileSize(context, stat.getAvailableBytes());
        } catch (Exception e) {
            return "";
        }
    }
}
