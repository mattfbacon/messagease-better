package nz.felle.messageasebetter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

abstract class Action {
	abstract void execute(@NonNull InputMethodView view);

	abstract @Nullable ActionShower show(final @NonNull InputMethodView view);

	/** Icon shown for this action in the key editor, or 0 if it has none. */
	@androidx.annotation.DrawableRes int icon() {
		return 0;
	}

	boolean secondary() {
		return false;
	}

	boolean cursorControl() {
		return false;
	}
}
