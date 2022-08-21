package nz.felle.messageasebetter;

import android.graphics.PointF;

import androidx.annotation.NonNull;

public enum Motion {
	NONE,
	UP,
	DOWN,
	LEFT,
	RIGHT,
	UP_RIGHT,
	UP_LEFT,
	DOWN_RIGHT,
	DOWN_LEFT,;

	@NonNull PointF offset(final float centerX, final float centerY, final float offsetX, final float offsetY) {
		final @NonNull PointF ret = new PointF(centerX, centerY);

		switch (this) {
			case UP_LEFT:
			case LEFT:
			case DOWN_LEFT:
				ret.x = centerX - offsetX;
				break;
			case UP_RIGHT:
			case RIGHT:
			case DOWN_RIGHT:
				ret.x = centerX + offsetX;
				break;
		}

		switch (this) {
			case UP_LEFT:
			case UP:
			case UP_RIGHT:
				ret.y = centerY - offsetY;
				break;
			case DOWN_LEFT:
			case DOWN:
			case DOWN_RIGHT:
				ret.y = centerY + offsetY;
				break;
		}

		return ret;
	}
}
