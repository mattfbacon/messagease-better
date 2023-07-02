package nz.felle.messageasebetter;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

public final class Repeater implements Runnable {
	private final @NonNull
	InputMethodView view;
	private final int touchId;

	public Repeater(final @NonNull InputMethodView view, final int touchId) {
		this.view = view;
		this.touchId = touchId;
	}

	private static final float MIN_INTERVAL = 50.0f;
	private static final long INITIAL_INTERVAL = 800;
	private static final float SPEEDUP_FACTOR = 0.85f;
	private static final float SECONDARY_INTERVAL = 500.0f;
	private static final @NonNull
	Handler handler = new Handler(Looper.getMainLooper());

	private float interval = SECONDARY_INTERVAL;
	private boolean hasExecuted = false;

	public boolean hasExecuted() {
		return hasExecuted;
	}

	private void shortenInterval() {
		if (interval > MIN_INTERVAL) {
			interval *= SPEEDUP_FACTOR;
		}
	}

	@Override
	public void run() {
		hasExecuted = true;

		view.processLine(touchId);
		handler.postDelayed(this, Math.round(interval));
		shortenInterval();
	}

	private void reset() {
		hasExecuted = false;
		interval = SECONDARY_INTERVAL;
	}

	public void start() {
		stop();
		reset();
		handler.postDelayed(this, INITIAL_INTERVAL);
	}

	public void stop() {
		handler.removeCallbacks(this);
	}
}
