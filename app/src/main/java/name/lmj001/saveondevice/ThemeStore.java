package name.lmj001.saveondevice;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

/** Appearance settings: theme mode, accent colour and corner roundness. */
public class ThemeStore {

    public static final int MODE_SYSTEM = 0;
    public static final int MODE_LIGHT = 1;
    public static final int MODE_DARK = 2;
    public static final int MODE_BLACK = 3;

    /** Accent presets offered in the Appearance tab. */
    public static final int[] PRESETS = {
            0xFF4C8DFF, // blue
            0xFF34C759, // green
            0xFF00BFA5, // teal
            0xFF9C6BFF, // purple
            0xFFFF5C8A, // pink
            0xFFFF9500, // orange
            0xFFFF453A, // red
            0xFFFFD60A  // yellow
    };

    private static final String PREFS = "appearance";
    private static final String KEY_MODE = "mode";
    private static final String KEY_ACCENT = "accent";
    private static final String KEY_ROUNDED = "rounded";

    private final SharedPreferences prefs;

    public ThemeStore(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int getMode() {
        return prefs.getInt(KEY_MODE, MODE_SYSTEM);
    }

    public void setMode(int mode) {
        prefs.edit().putInt(KEY_MODE, mode).apply();
    }

    /** 0 means "leave the theme's own accent alone". */
    public int getAccent() {
        return prefs.getInt(KEY_ACCENT, 0);
    }

    public void setAccent(int color) {
        prefs.edit().putInt(KEY_ACCENT, color).apply();
    }

    public boolean isRounded() {
        return prefs.getBoolean(KEY_ROUNDED, false);
    }

    public void setRounded(boolean rounded) {
        prefs.edit().putBoolean(KEY_ROUNDED, rounded).apply();
    }

    public static int nightModeFor(int mode) {
        switch (mode) {
            case MODE_LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case MODE_DARK:
            case MODE_BLACK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}
