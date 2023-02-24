package nz.felle.messageasebetter;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class SetCapsAction extends Action {
	private static final @NonNull
	IconShower upperIcon = new IconShower(R.drawable.ic_caps, true);
	private static final @NonNull
	IconShower doubleUpperIcon = new IconShower(R.drawable.ic_caps_double, true);
	private static final @NonNull
	IconShower lowerIcon = new IconShower(R.drawable.ic_lower, true);

	private final boolean setTo;

	// region Record Boilerplate
	SetCapsAction(final boolean setTo) {
		this.setTo = setTo;
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		SetCapsAction that = (SetCapsAction) obj;
		return this.setTo == that.setTo;
	}

	@Override
	public int hashCode() {
		return Objects.hash(setTo);
	}

	@NonNull
	@Override
	public String toString() {
		return "SetCapsAction[" +
			"setTo=" + setTo + ']';
	}
	//endregion

	@Override
	public void execute(final @NonNull InputMethodView view) {
		if (setTo) {
			switch (view.getCaps()) {
				case LOWER:
				case UPPER_PERMANENT:
					view.setCaps(CapsMode.UPPER);
					break;
				case UPPER:
					view.setCaps(CapsMode.UPPER_PERMANENT);
					break;
			}
		} else {
			view.setCaps(CapsMode.LOWER);
		}
	}

	@Nullable
	@Override
	public ActionShower show(final @NonNull InputMethodView view) {
		upperIcon.initialize(view);
		doubleUpperIcon.initialize(view);
		lowerIcon.initialize(view);

		if (setTo) {
			if (view.getCaps() == CapsMode.UPPER) {
				return doubleUpperIcon;
			} else {
				return upperIcon;
			}
		} else {
			if (view.getCaps() == CapsMode.LOWER) {
				return null;
			} else {
				return lowerIcon;
			}
		}
	}
}
