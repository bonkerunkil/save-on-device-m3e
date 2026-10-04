package name.lmj001.saveondevice;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * The quick-location buttons in the Save file panel. Long-press and drag to reorder — this includes
 * the built-in folders (Downloads, Documents, Pictures, Movies, Music), not just pinned ones.
 */
public class QuickLocationsAdapter extends RecyclerView.Adapter<QuickLocationsAdapter.ChipViewHolder> {

    public interface Listener {
        void onSelected(Destination destination);
        void onOrderChanged(List<Destination> ordered);
    }

    private final List<Destination> items = new ArrayList<>();
    private final Listener listener;
    private ItemTouchHelper touchHelper;
    private String selectedId;

    public QuickLocationsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setTouchHelper(ItemTouchHelper helper) {
        this.touchHelper = helper;
    }

    public void submit(List<Destination> destinations, String selectedId) {
        items.clear();
        items.addAll(destinations);
        this.selectedId = selectedId;
        notifyDataSetChanged();
    }

    public void setSelected(String id) {
        this.selectedId = id;
        notifyDataSetChanged();
    }

    public List<Destination> items() {
        return items;
    }

    public void move(int from, int to) {
        if (from < 0 || to < 0 || from >= items.size() || to >= items.size()) return;
        Destination moved = items.remove(from);
        items.add(to, moved);
        notifyItemMoved(from, to);
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChipViewHolder((Chip) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quick_chip, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        Destination destination = items.get(position);
        holder.chip.setText(destination.folderName);
        Themer.apply(holder.chip);
        holder.chip.setChecked(destination.id != null && destination.id.equals(selectedId));

        holder.chip.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Destination picked = items.get(pos);
            setSelected(picked.id);
            listener.onSelected(picked);
        });

        holder.chip.setOnLongClickListener(v -> {
            if (touchHelper == null) return false;
            touchHelper.startDrag(holder);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        final Chip chip;

        ChipViewHolder(@NonNull Chip chip) {
            super(chip);
            this.chip = chip;
        }
    }
}
