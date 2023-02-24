package nz.felle.messageasebetter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

public class KeyboardPaints {
	static final float STROKE_WIDTH = 10.0f;
	static final float RADIUS = 20.0f;

	private final boolean nightMode;

	private @ColorInt
	int getColor(final @ColorRes int colorId, final @NonNull Resources resources, final @NonNull Resources.Theme theme) {
		return resources.getColor(colorId, theme);
	}

	final @ColorInt
	int backgroundColor;

	private @NonNull
	Paint makeButtonPaint(final @NonNull Resources resources, final @NonNull Resources.Theme theme) {
		final Paint paint = new Paint();
		paint.setColor(getColor(nightMode ? R.color.keyboard_button_outline_dark : R.color.keyboard_button_outline_light, resources, theme));
		paint.setStrokeWidth(STROKE_WIDTH);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeCap(Paint.Cap.ROUND);
		return paint;
	}

	final @NonNull
	Paint buttonPaint;

	private @NonNull
	Paint makeKeyTextPaint(final @NonNull Resources resources, final @NonNull Resources.Theme theme) {
		final Paint paint = new Paint();
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setColor(getColor(nightMode ? R.color.keyboard_key_text_color_dark : R.color.keyboard_key_text_color_light, resources, theme));
		paint.setStyle(Paint.Style.FILL);
		paint.setTextSize(60.0f);
		return paint;
	}

	final @NonNull
	Paint keyTextPaint;

	private @NonNull
	Paint makeSmallKeyTextPaint(final @NonNull Resources resources, final @NonNull Resources.Theme theme) {
		final Paint paint = makeKeyTextPaint(resources, theme);
		paint.setTextSize(32.0f);
		return paint;
	}

	final @NonNull
	Paint smallKeyTextPaint;

	private @NonNull
	Paint makeSecondaryKeyTextPaint(final @NonNull Resources resources, final @NonNull Resources.Theme theme) {
		final Paint paint = makeSmallKeyTextPaint(resources, theme);
		paint.setColor(getColor(nightMode ? R.color.keyboard_key_text_color_secondary_dark : R.color.keyboard_key_text_color_secondary_light, resources, theme));
		return paint;
	}

	final @NonNull
	Paint secondaryKeyTextPaint;

	final @NonNull
	Paint linePaint;

	KeyboardPaints(final @NonNull Resources resources, final @NonNull Resources.Theme theme) {
		nightMode = resources.getConfiguration().isNightModeActive();

		backgroundColor = getColor(nightMode ? R.color.keyboard_background_dark : R.color.keyboard_background_light, resources, theme);
		buttonPaint = makeButtonPaint(resources, theme);
		keyTextPaint = makeKeyTextPaint(resources, theme);
		smallKeyTextPaint = makeSmallKeyTextPaint(resources, theme);
		secondaryKeyTextPaint = makeSecondaryKeyTextPaint(resources, theme);
		linePaint = buttonPaint;
	}
}
