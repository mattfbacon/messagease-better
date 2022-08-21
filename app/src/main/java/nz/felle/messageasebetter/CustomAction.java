package nz.felle.messageasebetter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public abstract class CustomAction extends Action {
	protected final @NonNull IconShower shower;

	CustomAction(final @DrawableRes int drawableId) {
		this.shower = new IconShower(drawableId);
	}

	@NonNull
	@Override
	ActionShower show(final @NonNull InputMethodView view) {
		shower.initialize(view);
		return shower;
	}

	@Override
	boolean secondary() {
		return true;
	}
}
