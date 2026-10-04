package name.lmj001.saveondevice;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles shared files with a fully transparent, animation-less window, so the Save file
 * composer appears over the app the file was shared from. Several files at once are handled in a
 * single panel where each file can be selected or deselected.
 */
public class ShareActivity extends AppCompatActivity implements SaveFileSheet.Listener {

    private static final int FALLBACK_SAVE_CODE = 9;

    private Uri inputUri;
    private ArrayList<Uri> inputUris;
    private boolean textMode;
    private LocationStore locationStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(
                ThemeStore.nightModeFor(new ThemeStore(this).getMode()));
        // Keep the window fully transparent so the app the file was shared from stays visible
        // behind the composer, whatever the theme mode.
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.activity_transparent);

        locationStore = new LocationStore(this);

        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            if (intent.hasExtra(Intent.EXTRA_STREAM)) {
                inputUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                showSaveSheet();
            } else {
                textMode = true;
                showSaveSheet();
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            if (intent.hasExtra(Intent.EXTRA_STREAM)) {
                inputUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (inputUris == null || inputUris.isEmpty()) {
                    finish();
                    return;
                }
                showMultiSaveSheet();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void showSaveSheet() {
        String name;
        String mimeType;

        if (textMode) {
            String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            name = slugify(text == null ? "note" : text.substring(0, Math.min(text.length(), 20))) + ".txt";
            mimeType = "text/plain";
        } else {
            name = getOriginalFileName(this, inputUri);
            mimeType = getMimeType(inputUri);
        }

        if (name == null || name.trim().isEmpty()) name = "file";

        SaveFileSheet sheet = SaveFileSheet.newInstance(name, mimeType);
        sheet.setListener(this);
        sheet.setCancelable(false);
        sheet.show(getSupportFragmentManager(), "save_file");
    }

    private void showMultiSaveSheet() {
        int count = inputUris.size();
        String[] names = new String[count];
        String[] mimes = new String[count];
        for (int i = 0; i < count; i++) {
            String name = getOriginalFileName(this, inputUris.get(i));
            names[i] = (name == null || name.trim().isEmpty()) ? "file" : name;
            mimes[i] = getMimeType(inputUris.get(i));
        }

        SaveFileSheet sheet = SaveFileSheet.newMultiInstance(names, mimes);
        sheet.setListener(this);
        sheet.setCancelable(false);
        sheet.show(getSupportFragmentManager(), "save_file");
    }

    @Override
    public void onSave(Destination destination, String fileName) {
        boolean ok;
        if (textMode) {
            ok = FileSaver.writeText(this, destination, getIntent().getStringExtra(Intent.EXTRA_TEXT), fileName);
        } else {
            ok = FileSaver.write(this, destination, inputUri, fileName, getMimeType(inputUri));
        }

        if (!ok && !textMode && inputUri != null) {
            // The chosen folder refused the file - let the user place it with the system picker
            // so the save never just fails.
            startFallbackSave(fileName);
            return;
        }

        if (destination != null) {
            locationStore.record(destination.folderName, destination.folderName, destination.storageName);
        }
        Toast.makeText(this,
                ok ? getString(R.string.saved_to, destination == null ? "" : destination.folderName)
                        : getString(R.string.save_failed),
                Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onSaveMultiple(Destination destination, List<Integer> selectedIndices) {
        int saved = 0;
        for (int index : selectedIndices) {
            if (index < 0 || index >= inputUris.size()) continue;
            Uri uri = inputUris.get(index);
            if (FileSaver.write(this, destination, uri, getOriginalFileName(this, uri), getMimeType(uri))) {
                saved++;
            }
        }

        if (destination != null && saved > 0) {
            locationStore.record(destination.folderName, destination.folderName, destination.storageName);
        }

        if (saved == selectedIndices.size()) {
            Toast.makeText(this,
                    getString(R.string.saved_files_to, saved,
                            destination == null ? "" : destination.folderName),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.save_failed), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @Override
    public void onCancel() {
        finish();
    }

    // ------------------------------------------------------- fallback save path

    private void startFallbackSave(String fileName) {
        String mimeType = getMimeType(inputUri);
        if (mimeType == null || mimeType.isEmpty()) mimeType = "application/octet-stream";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        try {
            startActivityForResult(intent, FALLBACK_SAVE_CODE);
        } catch (Exception e) {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FALLBACK_SAVE_CODE) return;

        boolean ok = false;
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri outputUri = data.getData();
            ok = FileSaver.writeToDocumentUri(this, outputUri, inputUri, null);
            if (ok) {
                String[] parts = DocumentUtils.parentFolderAndStorage(this, outputUri);
                locationStore.record(parts[1], parts[1], parts[0]);
            }
        }
        Toast.makeText(this, ok ? getString(R.string.saved) : getString(R.string.save_failed),
                Toast.LENGTH_SHORT).show();
        finish();
    }

    // ------------------------------------------------------------------ helpers

    private String getMimeType(Uri uri) {
        if (uri == null) return null;
        return getApplicationContext().getContentResolver().getType(uri);
    }

    private String getOriginalFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String slugify(String word) {
        return Normalizer.normalize(word, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-zA-Z0-9\\s]+", "").trim()
                .replaceAll("\\s+", "-")
                .toLowerCase();
    }
}
