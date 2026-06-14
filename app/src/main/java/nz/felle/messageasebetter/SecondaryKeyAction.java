package nz.felle.messageasebetter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class SecondaryKeyAction extends Action {
	private final char key;
	private final @NonNull ActionShower shower;

	SecondaryKeyAction(final char key) {
		this.key = key;
		this.shower = new TextShower(Character.toString(key));
	}

	SecondaryKeyAction(final char key, final @DrawableRes int drawableId) {
		this.key = key;
		this.shower = new IconShower(drawableId, this.secondary());
	}

	@Override
	public void execute(final @NonNull InputMethodView view) {
		view.typeCharacter(key);
	}

	@NonNull
	@Override
	public ActionShower show(final @NonNull InputMethodView view) {
		shower.initialize(view);
		return shower;
	}

	@Override
	boolean secondary() {
		return true;
	}
}
