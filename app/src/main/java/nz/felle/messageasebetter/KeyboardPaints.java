package nz.felle.messageasebetter;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

public class KeyboardPaints {
	// Gaps between keys and corner radius, in dp (scaled by density in the constructor)
	static final float GAP_DP = 3.0f;
	static final float RADIUS_DP = 14.0f;
	static final float KEY_TEXT_SP = 26.0f;
	static final float SECONDARY_ALPHA = 0.75f;
	static final float SECONDARY_TEXT_SP = 14.0f;
	final float gap;
	final float radius;
	final @ColorInt int backgroundColor;
	final @NonNull Paint buttonPaint;
	final @NonNull Paint keyFillPaint;
	final @NonNull Paint functionFillPaint;
	private final float density;
	private final float scaledDensity;
	final @NonNull Paint keyTextPaint;
	final @NonNull Paint smallKeyTextPaint;
	final @NonNull Paint secondaryKeyTextPaint;
	final @NonNull Paint linePaint;
	private final boolean nightMode;

	KeyboardPaints(final @NonNull Resources resources, final @NonNull Resources.Theme theme) {
		nightMode = resources.getConfiguration().isNightModeActive();
		density = resources.getDisplayMetrics().density;
		scaledDensity = density * resources.getConfiguration().fontScale;
		gap = GAP_DP * density;
		radius = RADIUS_DP * density;
		keyFillPaint = makeFillPaint(nightMode ? R.color.keyboard_key_fill_dark : R.color.keyboard_key_fill_light, resources, theme);
		functionFillPaint = makeFillPaint(nightMode ? R.color.keyboard_function_fill_dark : R.color.keyboard_function_fill_light, resources, theme);

		backgroundColor = getColor(nightMode ? R.color.keyboard_background_dark : R.color.keyboard_background_light, resources, theme);
		buttonPaint = makeButtonPaint(resources, theme);
		keyTextPaint = makeKeyTextPaint(resources, theme);
		smallKeyTextPaint = makeSmallKeyTextPaint(resources, theme);
		secondaryKeyTextPaint = makeSecondaryKeyTextPaint(resources, theme);
		linePaint = buttonPaint;
	}

	private @ColorInt int getColor(
		final @ColorRes int colorId,
		final @NonNull Resources resources,
		final @NonNull Resources.Theme theme
	) {
		return resources.getColor(colorId, theme);
	}

	private @NonNull Paint makeButtonPaint(
		final @NonNull Resources resources, final @NonNull Resources.Theme theme
	) {
		final Paint paint = new Paint();
		paint.setColor(getColor(nightMode ? R.color.keyboard_button_outline_dark : R.color.keyboard_button_outline_light, resources, theme));
		paint.setStrokeWidth(4.0f * density);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeCap(Paint.Cap.ROUND);
		return paint;
	}

	private @NonNull Paint makeFillPaint(
		final @ColorRes int colorId,
		final @NonNull Resources resources,
		final @NonNull Resources.Theme theme
	) {
		final Paint paint = new Paint();
		paint.setColor(getColor(colorId, resources, theme));
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		return paint;
	}

	private @NonNull Paint makeKeyTextPaint(
		final @NonNull Resources resources, final @NonNull Resources.Theme theme
	) {
		final Paint paint = new Paint();
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setColor(getColor(nightMode ? R.color.keyboard_key_text_color_dark : R.color.keyboard_key_text_color_light, resources, theme));
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setTextSize(KEY_TEXT_SP * scaledDensity);
		return paint;
	}

	private @NonNull Paint makeSmallKeyTextPaint(
		final @NonNull Resources resources, final @NonNull Resources.Theme theme
	) {
		final Paint paint = makeKeyTextPaint(resources, theme);
		paint.setTextSize(SECONDARY_TEXT_SP * scaledDensity);
		paint.setTypeface(Typeface.create(Typeface.DEFAULT, 500, false));
		paint.setAlpha(Math.round(paint.getAlpha() * SECONDARY_ALPHA));
		return paint;
	}

	private @NonNull Paint makeSecondaryKeyTextPaint(
		final @NonNull Resources resources, final @NonNull Resources.Theme theme
	) {
		final Paint paint = makeSmallKeyTextPaint(resources, theme);
		paint.setColor(getColor(nightMode ? R.color.keyboard_key_text_color_secondary_dark : R.color.keyboard_key_text_color_secondary_light, resources, theme));
		paint.setAlpha(Math.round(paint.getAlpha() * SECONDARY_ALPHA));
		return paint;
	}
}
