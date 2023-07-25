package nz.felle.messageasebetter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

abstract class Action {
	abstract void execute(@NonNull InputMethodView view);

	abstract @Nullable ActionShower show(final @NonNull InputMethodView view);

	boolean secondary() {
		return false;
	}
}
