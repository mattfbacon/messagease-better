package nz.felle.messageasebetter;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;

public final class MainActivity extends Activity {
	@Override
	protected void onCreate(final @Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
			.setAppearanceLightStatusBars(!getResources().getConfiguration().isNightModeActive());
		findViewById(R.id.test_input).requestFocus();
		// For voice input; not required.
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
	}
}
