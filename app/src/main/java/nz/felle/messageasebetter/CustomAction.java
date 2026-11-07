package nz.felle.messageasebetter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public abstract class CustomAction extends Action {
	protected final @NonNull IconShower shower;
	private final @DrawableRes int drawableId;

	CustomAction(final @DrawableRes int drawableId) {
		this.drawableId = drawableId;
		this.shower = new IconShower(drawableId, this.secondary());
	}

	@Override
	@DrawableRes int icon() {
		return drawableId;
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
