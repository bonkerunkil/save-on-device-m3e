package name.lmj001.saveondevice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.LocationViewHolder> {

    private final LocationStore store;
    private final View emptyState;
    private final List<SaveLocation> items = new ArrayList<>();

    public LocationsAdapter(LocationStore store, View emptyState) {
        this.store = store;
        this.emptyState = emptyState;
    }

    public void reload() {
        items.clear();
        items.addAll(store.getAll());
        if (emptyState != null) {
            emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        SaveLocation location = items.get(position);
        holder.name.setText(location.name);
        Themer.apply(holder.itemView);
        holder.path.setText(location.path);
        holder.meta.setText(holder.itemView.getContext().getString(
                R.string.location_meta,
                location.count,
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(location.lastUsed))));
        holder.delete.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                store.remove(items.get(adapterPosition).uri);
                reload();
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView path;
        final TextView meta;
        final ImageButton delete;

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.locationName);
            path = itemView.findViewById(R.id.locationPath);
            meta = itemView.findViewById(R.id.locationMeta);
            delete = itemView.findViewById(R.id.deleteLocation);
        }
    }
}
