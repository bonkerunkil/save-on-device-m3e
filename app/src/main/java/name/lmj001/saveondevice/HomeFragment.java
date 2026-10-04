package name.lmj001.saveondevice;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        super(R.layout.fragment_home);
    }

    private DestinationStore destinationStore;
    private PinnedAdapter pinnedAdapter;
    private TextView pinnedEmpty;
    private TextView reorderHint;

    private final ActivityResultLauncher<Uri> folderPicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
                Context context = getContext();
                if (uri == null || context == null) return;
                try {
                    context.getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                } catch (Exception ignored) { }
                Destination destination = Destination.saf(
                        uri.toString(),
                        DocumentUtils.folderNameFromUri(context, uri),
                        DocumentUtils.storageNameFromUri(context, uri));
                boolean added = new DestinationStore(context).addPinned(destination);
                Toast.makeText(context,
                        getString(added ? R.string.pinned_added : R.string.pinned_exists, destination.folderName),
                        Toast.LENGTH_SHORT).show();
                renderPinned();
            });

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Context context = requireContext();
        destinationStore = new DestinationStore(context);

        pinnedEmpty = view.findViewById(R.id.pinnedEmpty);
        reorderHint = view.findViewById(R.id.reorderHint);

        RecyclerView pinnedList = view.findViewById(R.id.pinnedList);
        pinnedAdapter = new PinnedAdapter(new PinnedAdapter.Listener() {
            @Override
            public void onRemove(Destination destination) {
                destinationStore.removePinned(destination.id);
                renderPinned();
            }

            @Override
            public void onOrderChanged(List<Destination> ordered) {
                destinationStore.setPinned(ordered);
            }
        });
        pinnedList.setLayoutManager(new LinearLayoutManager(context));
        pinnedList.setAdapter(pinnedAdapter);

        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                pinnedAdapter.move(viewHolder.getBindingAdapterPosition(),
                        target.getBindingAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Swipe is not used; folders are reordered by drag only.
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                destinationStore.setPinned(pinnedAdapter.items());
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }
        });
        helper.attachToRecyclerView(pinnedList);
        pinnedAdapter.setTouchHelper(helper);

        MaterialButton pinButton = view.findViewById(R.id.pinFolderButton);
        pinButton.setOnClickListener(v -> folderPicker.launch(null));

        renderPinned();
        Themer.apply(view);
    }

    private void renderPinned() {
        if (pinnedAdapter == null) return;
        List<Destination> pinned = destinationStore.getPinned();
        pinnedEmpty.setVisibility(pinned.isEmpty() ? View.VISIBLE : View.GONE);
        reorderHint.setVisibility(pinned.size() > 1 ? View.VISIBLE : View.GONE);
        pinnedAdapter.submit(pinned);
    }
}
