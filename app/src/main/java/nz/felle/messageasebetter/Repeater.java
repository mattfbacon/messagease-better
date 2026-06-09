package nz.felle.messageasebetter;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

public final class Repeater implements Runnable {
	private static final long INITIAL_INTERVAL = 500;
	private static final long REPEAT_INTERVAL = 150;

	private static final @NonNull Handler handler = new Handler(Looper.getMainLooper());
	private final @NonNull InputMethodView view;
	private final int touchId;

	public Repeater(final @NonNull InputMethodView view, final int touchId) {
		this.view = view;
		this.touchId = touchId;
	}

	@Override
	public void run() {
		view.processLine(touchId);
		handler.postDelayed(this, REPEAT_INTERVAL);
	}

	public void start() {
		handler.removeCallbacks(this);
		handler.postDelayed(this, INITIAL_INTERVAL);
	}

	public void stop() {
		handler.removeCallbacks(this);
	}
}
