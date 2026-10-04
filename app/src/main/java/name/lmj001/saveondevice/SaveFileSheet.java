package name.lmj001.saveondevice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * The "Save file" composer. Handles one file (editable name) or many files at once, where the file
 * list expands inside the panel and every file can be selected or deselected.
 */
public class SaveFileSheet extends BottomSheetDialogFragment {

    public interface Listener {
        void onSave(Destination destination, String fileName);
        void onSaveMultiple(Destination destination, List<Integer> selectedIndices);
        void onCancel();
    }

    private static final String ARG_NAME = "name";
    private static final String ARG_MIME = "mime";
    private static final String ARG_NAMES = "names";
    private static final String ARG_MIMES = "mimes";

    private static final int FILE_ROW_HEIGHT_DP = 44;
    private static final int FILE_LIST_MAX_HEIGHT_DP = 150;

    private Listener listener;
    private Destination destination;
    private boolean multiMode;
    private boolean listExpanded;

    private MaterialCardView locationCard;
    private TextInputEditText nameInput;
    private TextView folderName;
    private TextView storageName;
    private TextView storageNameInfo;
    private TextView storageInfo;
    private RecyclerView quickLocations;
    private QuickLocationsAdapter quickAdapter;
    private MaterialButton saveButton;

    private View singleFileHeader;
    private View multiFileHeader;
    private View fileListSection;
    private TextView multiFileName;
    private TextView multiFileSubtitle;
    private TextView selectAllToggle;
    private TextView selectedCount;
    private ImageView multiChevron;
    private RecyclerView fileList;
    private FileListAdapter fileAdapter;

    public static SaveFileSheet newInstance(String fileName, String mimeType) {
        SaveFileSheet sheet = new SaveFileSheet();
        Bundle args = new Bundle();
        args.putString(ARG_NAME, fileName);
        args.putString(ARG_MIME, mimeType);
        sheet.setArguments(args);
        return sheet;
    }

    public static SaveFileSheet newMultiInstance(String[] names, String[] mimes) {
        SaveFileSheet sheet = new SaveFileSheet();
        Bundle args = new Bundle();
        args.putStringArray(ARG_NAMES, names);
        args.putStringArray(ARG_MIMES, mimes);
        sheet.setArguments(args);
        return sheet;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public int getTheme() {
        return R.style.Theme_SaveOnDevice_BottomSheet;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    private final ActivityResultLauncher<Uri> folderPicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
                Context context = getContext();
                if (uri == null || context == null) return;
                try {
                    context.getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } catch (Exception ignored) { }
                setDestination(Destination.saf(
                        uri.toString(),
                        DocumentUtils.folderNameFromUri(context, uri),
                        DocumentUtils.storageNameFromUri(context, uri)));
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_save_file, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        String[] names = args == null ? null : args.getStringArray(ARG_NAMES);
        String[] mimes = args == null ? null : args.getStringArray(ARG_MIMES);
        multiMode = names != null && names.length > 1;

        locationCard = view.findViewById(R.id.locationCard);
        folderName = view.findViewById(R.id.locationFolderName);
        storageName = view.findViewById(R.id.locationStorageName);
        storageNameInfo = view.findViewById(R.id.storageNameInfo);
        storageInfo = view.findViewById(R.id.storageInfo);
        saveButton = view.findViewById(R.id.saveButton);

        View.OnClickListener change = v -> folderPicker.launch(null);
        view.findViewById(R.id.changeLocationButton).setOnClickListener(change);
        locationCard.setOnClickListener(change);

        view.findViewById(R.id.cancelButton).setOnClickListener(v -> {
            if (listener != null) listener.onCancel();
            dismissAllowingStateLoss();
        });

        setUpQuickLocations(view);

        if (multiMode) {
            setUpMulti(view, names, mimes);
        } else {
            setUpSingle(view,
                    args == null ? "" : args.getString(ARG_NAME),
                    args == null ? null : args.getString(ARG_MIME));
        }

        saveButton.setOnClickListener(v -> onSaveClicked());

        destination = new DestinationStore(requireContext()).getCurrent();

        List<Destination> quick = buildQuickList();
        quickLocations.setVisibility(quick.isEmpty() ? View.GONE : View.VISIBLE);
        quickAdapter.submit(quick, destination == null ? null : destination.id);

        renderLocation();
        updateSaveEnabled();

        Themer.apply(view);
    }

    // ------------------------------------------------------------- single file

    private void setUpSingle(View view, String fileName, String mimeType) {
        singleFileHeader = view.findViewById(R.id.singleFileHeader);
        singleFileHeader.setVisibility(View.VISIBLE);
        view.findViewById(R.id.multiFileHeader).setVisibility(View.GONE);
        view.findViewById(R.id.fileListSection).setVisibility(View.GONE);

        ((TextView) view.findViewById(R.id.fileType)).setText(prettyType(mimeType));
        ((ImageView) view.findViewById(R.id.fileIcon)).setImageResource(iconFor(mimeType));

        nameInput = view.findViewById(R.id.fileNameInput);
        nameInput.setText(fileName);
        if (nameInput.getText() != null) {
            nameInput.setSelection(nameInput.getText().length());
        }
        nameInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { updateSaveEnabled(); }
        });
    }

    // ------------------------------------------------------------ many files

    private void setUpMulti(View view, String[] names, String[] mimes) {
        ((TextView) view.findViewById(R.id.sheetTitle)).setText(R.string.save_files_title);

        singleFileHeader = view.findViewById(R.id.singleFileHeader);
        singleFileHeader.setVisibility(View.GONE);

        multiFileHeader = view.findViewById(R.id.multiFileHeader);
        fileListSection = view.findViewById(R.id.fileListSection);
        multiFileName = view.findViewById(R.id.multiFileName);
        multiFileSubtitle = view.findViewById(R.id.multiFileSubtitle);
        selectAllToggle = view.findViewById(R.id.selectAllToggle);
        selectedCount = view.findViewById(R.id.selectedCount);
        multiChevron = view.findViewById(R.id.multiChevron);
        fileList = view.findViewById(R.id.fileList);

        multiFileName.setText(getString(R.string.multi_files_more, names[0], names.length - 1));
        ((ImageView) view.findViewById(R.id.multiFileIcon))
                .setImageResource(iconFor(mimes == null || mimes.length == 0 ? null : mimes[0]));

        List<String> nameList = new ArrayList<>();
        for (String name : names) nameList.add(name);

        fileAdapter = new FileListAdapter(nameList, this::updateMultiState);
        fileList.setLayoutManager(new LinearLayoutManager(requireContext()));
        fileList.setAdapter(fileAdapter);
        fileList.setHasFixedSize(false);

        multiFileHeader.setVisibility(View.VISIBLE);
        multiFileHeader.setOnClickListener(v -> toggleFileList());
        selectAllToggle.setOnClickListener(v -> {
            fileAdapter.setAll(!fileAdapter.allSelected());
            updateMultiState();
        });

        updateMultiState();
    }

    private void toggleFileList() {
        listExpanded = !listExpanded;
        fileListSection.setVisibility(listExpanded ? View.VISIBLE : View.GONE);
        multiChevron.animate().rotation(listExpanded ? 90f : 0f).setDuration(150).start();

        if (listExpanded) {
            float density = getResources().getDisplayMetrics().density;
            int rowHeight = (int) (FILE_ROW_HEIGHT_DP * density);
            int maxHeight = (int) (FILE_LIST_MAX_HEIGHT_DP * density);
            int height = Math.min(fileAdapter.getItemCount() * rowHeight, maxHeight);
            ViewGroup.LayoutParams params = fileList.getLayoutParams();
            params.height = height;
            fileList.setLayoutParams(params);
        }
    }

    private void updateMultiState() {
        if (fileAdapter == null) return;
        int selected = fileAdapter.selectedCount();
        int total = fileAdapter.getItemCount();
        selectedCount.setText(getString(R.string.selected_count, selected, total));
        selectAllToggle.setText(fileAdapter.allSelected() ? R.string.clear_all : R.string.select_all);
        updateSaveEnabled();
    }

    // ------------------------------------------------------------------ save

    private void onSaveClicked() {
        if (multiMode) {
            List<Integer> selected = fileAdapter.selectedIndices();
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_no_files, Toast.LENGTH_SHORT).show();
                return;
            }
            if (listener != null) listener.onSaveMultiple(destination, selected);
            dismissAllowingStateLoss();
            return;
        }

        String name = nameInput.getText() == null ? "" : nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.error_name));
            return;
        }
        if (listener != null) listener.onSave(destination, name);
        dismissAllowingStateLoss();
    }

    // -------------------------------------------------------- quick locations

    private void setUpQuickLocations(View view) {
        quickLocations = view.findViewById(R.id.quickLocations);
        quickAdapter = new QuickLocationsAdapter(new QuickLocationsAdapter.Listener() {
            @Override
            public void onSelected(Destination picked) {
                setDestination(picked);
            }

            @Override
            public void onOrderChanged(List<Destination> ordered) {
                persistQuickOrder(ordered);
            }
        });
        quickLocations.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        quickLocations.setAdapter(quickAdapter);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                quickAdapter.move(viewHolder.getBindingAdapterPosition(),
                        target.getBindingAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Swipe is not used; buttons are reordered by drag only.
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                persistQuickOrder(quickAdapter.items());
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return false; // the chip itself starts the drag on long-press
            }
        });
        helper.attachToRecyclerView(quickLocations);
        quickAdapter.setTouchHelper(helper);
    }

    private List<Destination> buildQuickList() {
        DestinationStore store = new DestinationStore(requireContext());

        List<Destination> available = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            available.add(Destination.mediaStore(Destination.MS_DOWNLOADS, "Downloads"));
            available.add(Destination.mediaStore(Destination.MS_DOCUMENTS, "Documents"));
            available.add(Destination.mediaStore(Destination.MS_PICTURES, "Pictures"));
            available.add(Destination.mediaStore(Destination.MS_MOVIES, "Movies"));
            available.add(Destination.mediaStore(Destination.MS_MUSIC, "Music"));
        }
        available.addAll(store.getPinned());

        List<Destination> ordered = new ArrayList<>();
        for (String key : store.getQuickOrder()) {
            for (Destination candidate : available) {
                if (key.equals(candidate.id) && !hasId(ordered, candidate.id)) {
                    ordered.add(candidate);
                    break;
                }
            }
        }
        for (Destination candidate : available) {
            if (!hasId(ordered, candidate.id)) {
                ordered.add(candidate);
            }
        }
        return ordered;
    }

    private void persistQuickOrder(List<Destination> ordered) {
        List<String> keys = new ArrayList<>();
        for (Destination item : ordered) {
            if (item.id != null) keys.add(item.id);
        }
        new DestinationStore(requireContext()).setQuickOrder(keys);
    }

    private static boolean hasId(List<Destination> list, String id) {
        for (Destination item : list) {
            if (item.id != null && item.id.equals(id)) return true;
        }
        return false;
    }

    private void setDestination(Destination picked) {
        destination = picked;
        new DestinationStore(requireContext()).setCurrent(picked);
        renderLocation();
    }

    private void renderLocation() {
        if (destination == null) {
            folderName.setText(R.string.no_location);
            storageName.setText(R.string.tap_to_choose);
            storageNameInfo.setText(R.string.internal_storage);
        } else {
            folderName.setText(destination.folderName);
            storageName.setText(destination.storageName);
            storageNameInfo.setText(destination.storageName);
        }
        storageInfo.setText(getString(R.string.available_space, FileSaver.availableSpace(requireContext())));
        quickAdapter.setSelected(destination == null ? null : destination.id);

        locationCard.setAlpha(0.35f);
        locationCard.animate().alpha(1f).setDuration(200).start();
    }

    private void updateSaveEnabled() {
        boolean valid;
        if (multiMode) {
            valid = fileAdapter != null && fileAdapter.selectedCount() > 0;
        } else {
            valid = nameInput != null && nameInput.getText() != null
                    && nameInput.getText().toString().trim().length() > 0;
        }
        saveButton.setEnabled(valid);
        saveButton.setAlpha(valid ? 1f : 0.5f);
    }

    private String prettyType(String mimeType) {
        if (mimeType == null) return getString(R.string.unknown_type);
        if (mimeType.startsWith("image/")) return "Image";
        if (mimeType.startsWith("video/")) return "Video";
        if (mimeType.startsWith("audio/")) return "Audio";
        if (mimeType.startsWith("text/")) return "Text";
        if (mimeType.contains("pdf")) return "PDF";
        if (mimeType.contains("zip") || mimeType.contains("compressed")) return "Archive";
        if (mimeType.contains("android.package")) return "App";
        return getString(R.string.unknown_type);
    }

    private int iconFor(String mimeType) {
        if (mimeType == null) return R.drawable.ic_file;
        if (mimeType.startsWith("image/")) return R.drawable.ic_image;
        if (mimeType.startsWith("video/")) return R.drawable.ic_video;
        if (mimeType.startsWith("audio/")) return R.drawable.ic_audio;
        if (mimeType.startsWith("text/")) return R.drawable.ic_text;
        return R.drawable.ic_file;
    }
}
