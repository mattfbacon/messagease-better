package nz.felle.messageasebetter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class SecondaryKeyAction extends Action {
	private final char key;
	private final @NonNull ActionShower shower;

	//region Record Boilerplate
	SecondaryKeyAction(final char key) {
		this.key = key;
		this.shower = new TextShower(Character.toString(key));
	}

	SecondaryKeyAction(final char key, final @DrawableRes int drawableId) {
		this.key = key;
		this.shower = new IconShower(drawableId, this.secondary());
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		SecondaryKeyAction that = (SecondaryKeyAction) obj;
		return this.key == that.key;
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

	@NonNull
	@Override
	public String toString() {
		return "SecondaryKeyAction[" + "key=" + key + ']';
	}
	//endregion

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
