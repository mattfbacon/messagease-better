package nz.felle.messageasebetter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import java.util.Objects;

final class TextShower extends ActionShower {
	public @NonNull
	String text;

	//region Record Boilerplate
	TextShower(@NonNull String text) {
		this.text = text;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
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
		return "TextShower[" +
			"text=" + text + ']';
	}
	//endregion

	private float textOffset = Float.NaN;

	public void show(
		final @NonNull
			Canvas canvas,
		final float centerX,
		final float centerY,
		final @NonNull
			Paint paint) {
		// we do a little caching
		if (Float.isNaN(textOffset)) {
			final @NonNull Rect bounds = new Rect();
			paint.getTextBounds(text, 0, text.length(), bounds);
			textOffset = (float) bounds.height() / 2.0f;
		}
		canvas.drawText(text, centerX, centerY + textOffset, paint);
	}
}
