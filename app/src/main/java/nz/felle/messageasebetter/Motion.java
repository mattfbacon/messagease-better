package nz.felle.messageasebetter;

import android.graphics.PointF;

import androidx.annotation.NonNull;

public enum Motion {
	NONE, UP, DOWN, LEFT, RIGHT, UP_RIGHT, UP_LEFT, DOWN_RIGHT, DOWN_LEFT,
	;

	@NonNull
	PointF offset(
		final float centerX, final float centerY, final float offsetX, final float offsetY
	) {
		final float x = switch (this) {
			case NONE, UP, DOWN -> centerX;
			case UP_LEFT, LEFT, DOWN_LEFT -> centerX - offsetX;
			case UP_RIGHT, RIGHT, DOWN_RIGHT -> centerX + offsetX;
		};

		final float y = switch (this) {
			case NONE, LEFT, RIGHT -> centerY;
			case UP_LEFT, UP, UP_RIGHT -> centerY - offsetY;
			case DOWN_LEFT, DOWN, DOWN_RIGHT -> centerY + offsetY;
		};

		return new PointF(x, y);
	}
}
