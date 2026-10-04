package name.lmj001.saveondevice;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;

/** Launcher: bottom navigation across Save, Locations, Appearance and About. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeStore themeStore = new ThemeStore(this);
        AppCompatDelegate.setDefaultNightMode(ThemeStore.nightModeFor(themeStore.getMode()));

        setContentView(R.layout.activity_main);

        // Keep the system's dynamic colour only when the user has not chosen an accent.
        if (themeStore.getAccent() == 0) {
            DynamicColors.applyToActivityIfAvailable(this);
        }

        setUpNavigation(savedInstanceState);
        Themer.apply(findViewById(android.R.id.content));
    }

    private void setUpNavigation(Bundle savedInstanceState) {
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_locations) {
                showFragment(new LocationsFragment());
                return true;
            } else if (id == R.id.nav_appearance) {
                showFragment(new AppearanceFragment());
                return true;
            } else if (id == R.id.nav_about) {
                showFragment(new AboutFragment());
                return true;
            }
            return false;
        });
        if (savedInstanceState == null) {
            navigation.setSelectedItemId(R.id.nav_home);
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
