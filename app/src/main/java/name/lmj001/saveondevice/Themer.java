package name.lmj001.saveondevice;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;

/**
 * Applies the appearance settings (pitch black, accent colour, rounder corners) to an inflated
 * view tree. Views opt in with {@code android:tag}, so nothing is guessed.
 *
 * Tags: surface_root, surface_card, accent_bg, accent_fg, accent_text, accent_icon,
 *       accent_chip, accent_switch, accent_nav, accent_check
 */
public final class Themer {

    private static final int BLACK_ROOT = 0xFF000000;
    private static final int BLACK_CARD = 0xFF0D0D10;
    private static final int DARK_CONTAINER = 0xFF2A2A32;
    private static final int DARK_MUTED = 0xFF4A4A54;

    private Themer() { }

    public static void apply(View root) {
        if (root == null) return;
        Context context = root.getContext();
        if (context == null) return;

        ThemeStore store = new ThemeStore(context);
        int accent = store.getAccent();
        boolean rounded = store.isRounded();
        boolean black = store.getMode() == ThemeStore.MODE_BLACK;
        float density = context.getResources().getDisplayMetrics().density;

        walk(root, context, accent, rounded, black, density);
    }

    private static void walk(View view, Context context, int accent, boolean rounded,
                             boolean black, float density) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                walk(group.getChildAt(i), context, accent, rounded, black, density);
            }
        }

        Object tagObject = view.getTag();
        String role = tagObject instanceof String ? (String) tagObject : null;

        // --- pitch black surfaces -------------------------------------------------
        if (black) {
            if (view instanceof BottomNavigationView) {
                ((BottomNavigationView) view).setBackgroundColor(BLACK_ROOT);
            }
            if (role != null) {
                if ("surface_root".equals(role)) {
                    view.setBackgroundColor(BLACK_ROOT);
                } else if ("surface_card".equals(role)) {
                    if (view instanceof MaterialCardView) {
                        ((MaterialCardView) view).setCardBackgroundColor(BLACK_CARD);
                    } else {
                        view.setBackgroundColor(BLACK_CARD);
                    }
                } else if ("surface_sheet".equals(role)) {
                    // Only the panel itself goes black - never the dialog window behind it, so the
                    // app the file was shared from stays visible.
                    view.setBackgroundResource(R.drawable.bg_sheet_panel);
                }
            }
        }

        // --- rounder corners ------------------------------------------------------
        if (rounded) {
            if (view instanceof MaterialButton) {
                ((MaterialButton) view).setCornerRadius((int) (40 * density));
            } else if (view instanceof MaterialCardView) {
                ((MaterialCardView) view).setRadius(28 * density);
            } else if (view instanceof Chip) {
                ((Chip) view).setChipCornerRadius(40 * density);
            }
        }

        if (role == null || accent == 0) return;

        int onAccent = isLight(accent) ? 0xFF000000 : 0xFFFFFFFF;
        int mutedText = themeColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, 0xFF9E9EA6);

        switch (role) {
            case "accent_bg":
                if (view instanceof MaterialButton) {
                    MaterialButton button = (MaterialButton) view;
                    button.setBackgroundTintList(ColorStateList.valueOf(accent));
                    button.setTextColor(onAccent);
                    button.setIconTint(ColorStateList.valueOf(onAccent));
                }
                break;

            case "accent_fg":
                if (view instanceof MaterialButton) {
                    MaterialButton button = (MaterialButton) view;
                    button.setTextColor(accent);
                    button.setIconTint(ColorStateList.valueOf(accent));
                    button.setStrokeColor(ColorStateList.valueOf(accent));
                }
                break;

            case "accent_text":
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(accent);
                }
                break;

            case "accent_icon":
                if (view instanceof ImageView) {
                    ((ImageView) view).setImageTintList(ColorStateList.valueOf(accent));
                }
                break;

            case "accent_chip":
                if (view instanceof Chip) {
                    Chip chip = (Chip) view;
                    chip.setChipBackgroundColor(new ColorStateList(
                            new int[][] { { android.R.attr.state_checked }, new int[] { } },
                            new int[] { accent, DARK_CONTAINER }));
                    chip.setTextColor(new ColorStateList(
                            new int[][] { { android.R.attr.state_checked }, new int[] { } },
                            new int[] { onAccent, 0xFFEDEDF0 }));
                    chip.setChipStrokeWidth(0f);
                }
                break;

            case "accent_switch":
                if (view instanceof SwitchCompat) {
                    ((SwitchCompat) view).setTrackTintList(new ColorStateList(
                            new int[][] { { android.R.attr.state_checked }, new int[] { } },
                            new int[] { accent, DARK_MUTED }));
                }
                break;

            case "accent_nav":
                if (view instanceof BottomNavigationView) {
                    BottomNavigationView nav = (BottomNavigationView) view;
                    ColorStateList items = new ColorStateList(
                            new int[][] { { android.R.attr.state_checked }, new int[] { } },
                            new int[] { accent, mutedText });
                    nav.setItemIconTintList(items);
                    nav.setItemTextColor(items);
                }
                break;

            case "accent_check":
                if (view instanceof MaterialCheckBox) {
                    ((MaterialCheckBox) view).setButtonTintList(new ColorStateList(
                            new int[][] { { android.R.attr.state_checked }, new int[] { } },
                            new int[] { accent, mutedText }));
                }
                break;

            default:
                break;
        }
    }

    /** Tints the bottom sheet's own rounded background (used for pitch black). */
    public static void tintBackground(View view, int color) {
        if (view == null) return;
        Drawable background = view.getBackground();
        if (background != null) {
            background.setTint(color);
        }
    }

    public static boolean isLight(int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color)
                + 0.114 * Color.blue(color)) / 255.0;
        return luminance > 0.6;
    }

    private static int themeColor(Context context, int attr, int fallback) {
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(attr, value, true)) {
            if (value.resourceId != 0) {
                return context.getResources().getColor(value.resourceId, context.getTheme());
            }
            return value.data;
        }
        return fallback;
    }
}
