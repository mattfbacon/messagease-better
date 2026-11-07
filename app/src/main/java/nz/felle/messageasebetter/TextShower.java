package nz.felle.messageasebetter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import java.util.Objects;

final class TextShower extends ActionShower {
	public @NonNull String text;

	//region Record Boilerplate
	TextShower(@NonNull String text) {
		this.text = text;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		TextShower that = (TextShower) obj;
		return Objects.equals(this.text, that.text);
	}

	@Override
	public int hashCode() {
		return Objects.hash(text);
	}

	@NonNull
	@Override
	public String toString() {
		return "TextShower[" + "text=" + text + ']';
	}
	//endregion

	private final Rect bounds = new Rect();
	private float cachedSize = Float.NaN;

	void setText(final @NonNull String newText) {
		if (!text.equals(newText)) {
			text = newText;
			cachedSize = Float.NaN;
		}
	}

	private void measure(final @NonNull Paint paint) {
		if (cachedSize != paint.getTextSize()) {
			paint.getTextBounds(text, 0, text.length(), bounds);
			cachedSize = paint.getTextSize();
		}
	}

	@Override
	float width(final @NonNull Paint paint) {
		measure(paint);
		return bounds.width();
	}

	@Override
	float height(final @NonNull Paint paint) {
		measure(paint);
		return bounds.height();
	}

	public void show(
		final @NonNull Canvas canvas,
		final float centerX,
		final float centerY,
		final @NonNull Paint paint,
		final boolean dark
	) {
		measure(paint);
		// centre the ink (not the baseline box) so glyphs with descenders stay inside the key
		final float baseline = centerY - (bounds.top + bounds.bottom) / 2.0f;
		canvas.drawText(text, centerX, baseline, paint);
	}
}
