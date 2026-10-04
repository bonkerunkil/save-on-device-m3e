package name.lmj001.saveondevice;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

public class LocationsFragment extends Fragment {

    public LocationsFragment() {
        super(R.layout.fragment_locations);
    }

    private LocationsAdapter adapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LocationStore store = new LocationStore(requireContext());

        RecyclerView list = view.findViewById(R.id.locationsList);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new LocationsAdapter(store, view.findViewById(R.id.emptyState));
        list.setAdapter(adapter);

        MaterialButton clear = view.findViewById(R.id.clearLocationsButton);
        clear.setOnClickListener(v -> {
            store.clear();
            adapter.reload();
        });

        Themer.apply(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) adapter.reload();
    }
}
