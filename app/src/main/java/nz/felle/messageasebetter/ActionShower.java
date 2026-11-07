package nz.felle.messageasebetter;

import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;

public abstract class ActionShower {
	abstract void show(
		@NonNull Canvas canvas, float centerX, float centerY, @NonNull Paint paint, boolean dark
	);

	/** Rendered width when drawn with the given paint. */
	float width(@NonNull Paint paint) {
		return paint.getTextSize();
	}

	/** Rendered height when drawn with the given paint. */
	float height(@NonNull Paint paint) {
		return paint.getTextSize();
	}

	void initialize(@NonNull InputMethodView view) {
	}
}
