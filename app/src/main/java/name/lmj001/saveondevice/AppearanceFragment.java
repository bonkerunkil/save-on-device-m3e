package name.lmj001.saveondevice;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

/** Theme mode (including pitch black), accent colour and corner roundness. */
public class AppearanceFragment extends Fragment {

    public AppearanceFragment() {
        super(R.layout.fragment_appearance);
    }

    private ThemeStore store;
    private boolean suppress;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        store = new ThemeStore(requireContext());

        ChipGroup themeGroup = view.findViewById(R.id.themeGroup);
        Chip system = view.findViewById(R.id.chipSystem);
        Chip light = view.findViewById(R.id.chipLight);
        Chip dark = view.findViewById(R.id.chipDark);
        Chip black = view.findViewById(R.id.chipBlack);

        suppress = true;
        switch (store.getMode()) {
            case ThemeStore.MODE_LIGHT: light.setChecked(true); break;
            case ThemeStore.MODE_DARK: dark.setChecked(true); break;
            case ThemeStore.MODE_BLACK: black.setChecked(true); break;
            default: system.setChecked(true); break;
        }
        suppress = false;

        themeGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (suppress || checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            int mode;
            if (id == R.id.chipLight) mode = ThemeStore.MODE_LIGHT;
            else if (id == R.id.chipDark) mode = ThemeStore.MODE_DARK;
            else if (id == R.id.chipBlack) mode = ThemeStore.MODE_BLACK;
            else mode = ThemeStore.MODE_SYSTEM;

            store.setMode(mode);
            AppCompatDelegate.setDefaultNightMode(ThemeStore.nightModeFor(mode));
            requireActivity().recreate();
        });

        buildSwatches(view.findViewById(R.id.swatchRow));

        TextInputEditText hex = view.findViewById(R.id.hexInput);
        MaterialButton apply = view.findViewById(R.id.applyHexButton);
        apply.setOnClickListener(v -> {
            String raw = hex.getText() == null ? "" : hex.getText().toString().trim();
            Integer color = parseHex(raw);
            if (color == null) {
                hex.setError(getString(R.string.error_hex));
                return;
            }
            store.setAccent(color);
            requireActivity().recreate();
        });

        SwitchCompat rounded = view.findViewById(R.id.roundedSwitch);
        rounded.setChecked(store.isRounded());
        rounded.setOnCheckedChangeListener((button, checked) -> {
            store.setRounded(checked);
            requireActivity().recreate();
        });
    }

    private void buildSwatches(LinearLayout container) {
        container.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int current = store.getAccent();

        for (int color : ThemeStore.PRESETS) {
            FrameLayout frame = new FrameLayout(requireContext());
            LinearLayout.LayoutParams frameParams =
                    new LinearLayout.LayoutParams((int) (44 * density), (int) (44 * density));
            frameParams.setMarginEnd((int) (8 * density));
            frame.setLayoutParams(frameParams);
            if (color == current) {
                frame.setBackgroundResource(R.drawable.bg_swatch_ring);
            }

            View dot = new View(requireContext());
            int size = (int) (30 * density);
            FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(size, size);
            dotParams.gravity = Gravity.CENTER;
            dot.setLayoutParams(dotParams);
            dot.setBackgroundResource(R.drawable.bg_swatch);
            if (dot.getBackground() != null) {
                dot.getBackground().setTint(color);
            }
            frame.addView(dot);

            frame.setOnClickListener(v -> {
                store.setAccent(color);
                requireActivity().recreate();
            });
            container.addView(frame);
        }
    }

    private Integer parseHex(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        if (value.startsWith("#")) value = value.substring(1);
        if (!value.matches("[0-9a-fA-F]{6}")) return null;
        try {
            return Color.parseColor("#" + value);
        } catch (Exception e) {
            return null;
        }
    }
}
