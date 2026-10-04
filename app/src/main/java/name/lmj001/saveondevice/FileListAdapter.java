package name.lmj001.saveondevice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** The selectable list of files shown in the composer when several files are shared at once. */
public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.FileViewHolder> {

    public interface Listener {
        void onSelectionChanged();
    }

    private final List<String> names;
    private final boolean[] selected;
    private final Listener listener;

    public FileListAdapter(List<String> names, Listener listener) {
        this.names = names;
        this.selected = new boolean[names.size()];
        Arrays.fill(this.selected, true);
        this.listener = listener;
    }

    public int selectedCount() {
        int count = 0;
        for (boolean value : selected) if (value) count++;
        return count;
    }

    public boolean allSelected() {
        return selectedCount() == names.size();
    }

    public void setAll(boolean value) {
        Arrays.fill(selected, value);
        notifyDataSetChanged();
        listener.onSelectionChanged();
    }

    public List<Integer> selectedIndices() {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) out.add(i);
        }
        return out;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.name.setText(names.get(position));
        holder.check.setChecked(selected[position]);
        Themer.apply(holder.itemView);
        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            selected[pos] = !selected[pos];
            notifyItemChanged(pos);
            listener.onSelectionChanged();
        });
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final MaterialCheckBox check;

        FileViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.fileName);
            check = itemView.findViewById(R.id.fileCheck);
        }
    }
}
