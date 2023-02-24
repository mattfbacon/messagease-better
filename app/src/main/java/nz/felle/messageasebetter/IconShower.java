package nz.felle.messageasebetter;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.Objects;

public final class IconShower extends ActionShower {
	private @Nullable
	Drawable drawable = null;
	private final @DrawableRes
	int drawableId;
	private final boolean secondary;

	private @ColorInt int lightColor = -1;
	private @ColorInt int darkColor = -1;

	IconShower(final @DrawableRes int drawableId, final boolean secondary) {
		this.drawableId = drawableId;
		this.secondary = secondary;
	}

	IconShower(final @NonNull Drawable drawable, final boolean secondary) {
		this.drawable = drawable;
		this.drawableId = 0;
		this.secondary = secondary;
	}

	//region Boilerplate
	@Override
	public boolean equals(final @Nullable Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		IconShower that = (IconShower) obj;
		return Objects.equals(this.drawable, that.drawable);
	}

	@Override
	public int hashCode() {
		return Objects.hash(drawable);
	}

	@NonNull
	@Override
	public String toString() {
		return "IconShower[" +
			"drawable=" + drawable + ']';
	}
	//endregion

	@Override
	public void show(final @NonNull Canvas canvas, final float centerX, final float centerY, final @NonNull Paint paint, final boolean dark) {
		assert drawable != null;

		drawable.setTint(dark ? darkColor : lightColor);

		final int size = Math.round(paint.getTextSize());
		final float offset = paint.getTextSize() / 2;
		final int left = Math.round(centerX - offset);
		final int top = Math.round(centerY - offset);
		drawable.setBounds(left, top, left + size, top + size);
		drawable.draw(canvas);
	}

	@Override
	public void initialize(final @NonNull InputMethodView view) {
		if (drawable != null) {
			return;
		}

		drawable = Objects.requireNonNull(ResourcesCompat.getDrawable(view.getResources(), drawableId, view.getContext().getTheme()), "could not get drawable");

		final Context context = view.getContext();
		lightColor = context.getColor(secondary ? R.color.keyboard_key_text_color_secondary_light : R.color.keyboard_key_text_color_light);
		darkColor = context.getColor(secondary ? R.color.keyboard_key_text_color_secondary_dark : R.color.keyboard_key_text_color_dark);
	}
}
