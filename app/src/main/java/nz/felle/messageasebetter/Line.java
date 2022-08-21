package nz.felle.messageasebetter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class Line {
	public float startX;
	public float startY;
	public float endX;
	public float endY;

	//region Record Boilerplate
	Line(final float startX, final float startY, final float endX, final float endY) {
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		Line that = (Line) obj;
		return Float.floatToIntBits(this.startX) == Float.floatToIntBits(that.startX) &&
			Float.floatToIntBits(this.startY) == Float.floatToIntBits(that.startY) &&
			Float.floatToIntBits(this.endX) == Float.floatToIntBits(that.endX) &&
			Float.floatToIntBits(this.endY) == Float.floatToIntBits(that.endY);
	}

	@Override
	public int hashCode() {
		return Objects.hash(startX, startY, endX, endY);
	}

	@NonNull
	@Override
	public String toString() {
		return "Line[" +
			"startX=" + startX + ", " +
			"startY=" + startY + ", " +
			"endX=" + endX + ", " +
			"endY=" + endY + ']';
	}
	//endregion

	public float deltaX() {
		return endX - startX;
	}

	public float deltaY() {
		return endY - startY;
	}

	public @NonNull Motion asMotion(final float thresholdX, final float thresholdY) {
		final float deltaX = deltaX();
		final float deltaY = deltaY();

		if (Math.abs(deltaX) < thresholdX && Math.abs(deltaY) < thresholdY) {
			return Motion.NONE;
		}

		// ranges from -180 to 180.
		// both -180 and 180 are left.
		// the number goes counterclockwise and zero is right.
		// graphics treat positive Y as down whereas `atan2` treats it as up, so negate the Y.
		final double angle = Math.toDegrees(Math.atan2(-deltaY, deltaX));
		if (angle < -157.5) {
			return Motion.LEFT;
		} else if (angle < -112.5) {
			return Motion.DOWN_LEFT;
		} else if (angle < -67.5) {
			return Motion.DOWN;
		} else if (angle < -22.5) {
			return Motion.DOWN_RIGHT;
		} else if (angle < 22.5) {
			return Motion.RIGHT;
		} else if (angle < 67.5) {
			return Motion.UP_RIGHT;
		} else if (angle < 112.5) {
			return Motion.UP;
		} else if (angle < 157.5) {
			return Motion.UP_LEFT;
		} else {
			return Motion.LEFT;
		}
	}

	public float length() {
		final float deltaX = deltaX();
		final float deltaY = deltaY();
		return (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}
}
