package name.lmj001.saveondevice;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Pinned folders, reorderable by long-press drag. */
public class PinnedAdapter extends RecyclerView.Adapter<PinnedAdapter.PinnedViewHolder> {

    public interface Listener {
        void onRemove(Destination destination);
        void onOrderChanged(List<Destination> ordered);
    }

    private final List<Destination> items = new ArrayList<>();
    private final Listener listener;
    private ItemTouchHelper touchHelper;

    public PinnedAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setTouchHelper(ItemTouchHelper helper) {
        this.touchHelper = helper;
    }

    public void submit(List<Destination> destinations) {
        items.clear();
        items.addAll(destinations);
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
    public PinnedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PinnedViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pinned_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PinnedViewHolder holder, int position) {
        Destination destination = items.get(position);
        holder.name.setText(destination.folderName);
        Themer.apply(holder.itemView);
        holder.storage.setText(destination.storageName);

        holder.unpin.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                listener.onRemove(items.get(pos));
            }
        });

        holder.drag.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN && touchHelper != null) {
                touchHelper.startDrag(holder);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PinnedViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView storage;
        final ImageButton unpin;
        final ImageView drag;

        PinnedViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.pinnedName);
            storage = itemView.findViewById(R.id.pinnedStorage);
            unpin = itemView.findViewById(R.id.unpinButton);
            drag = itemView.findViewById(R.id.dragHandle);
        }
    }
}
