package name.lmj001.saveondevice;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/** Credits: the original project, this fork and its developer. */
public class AboutFragment extends Fragment {

    private static final String ORIGINAL_REPO = "https://github.com/lmj0011/save-on-device";
    private static final String FORK_TELEGRAM = "https://t.me/BonkerUnkilBonki";
    private static final String FORK_GITHUB = "https://github.com/BonkerUnkilBonki";

    public AboutFragment() {
        super(R.layout.fragment_about);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView version = view.findViewById(R.id.versionText);
        try {
            String name = requireContext().getPackageManager()
                    .getPackageInfo(requireContext().getPackageName(), 0).versionName;
            version.setText(getString(R.string.version_label, name));
        } catch (Exception e) {
            version.setText("");
        }

        view.findViewById(R.id.originalRepoButton).setOnClickListener(v -> open(ORIGINAL_REPO));
        view.findViewById(R.id.telegramButton).setOnClickListener(v -> open(FORK_TELEGRAM));
        view.findViewById(R.id.githubButton).setOnClickListener(v -> open(FORK_GITHUB));

        Themer.apply(view);
    }

    private void open(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(requireContext(), url, Toast.LENGTH_LONG).show();
        }
    }
}
