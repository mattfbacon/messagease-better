package nz.felle.messageasebetter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

final class KeyAction extends Action {
	public char normalKey;
	public char numKey;

	public static char NONE = '\0';

	//region Record Boilerplate
	KeyAction(char normalKey, char numKey) {
		this.normalKey = normalKey;
		this.numKey = numKey;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		KeyAction that = (KeyAction) obj;
		return Objects.equals(this.normalKey, that.normalKey) &&
			Objects.equals(this.numKey, that.numKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(normalKey, numKey);
	}

	@NonNull
	@Override
	public String toString() {
		return "KeyAction[" +
			"normalKey=" + normalKey + ", " +
			"numKey=" + numKey + ']';
	}
	//endregion

	private char getKey(final boolean numMode, final boolean uppercase) {
		if (numMode) {
			return numKey;
		} else if (uppercase) {
			return Character.toUpperCase(normalKey);
		} else {
			return normalKey;
		}
	}

	private char getKey(final @NonNull InputMethodView view) {
		return getKey(view.getNumMode(), view.getCaps().isUppercase());
	}

	public void execute(final @NonNull InputMethodView view) {
		final char key = getKey(view);
		if (key != NONE) {
			view.insert(key);
		}
	}

	@Override
	public boolean executeLongPress(final @NonNull InputMethodView view) {
		if (!view.getNumMode()) {
			view.insert(numKey);
			return true;
		}
		return false;
	}

	private final @NonNull TextShower shower = new TextShower("");
	private char lastShownKey = NONE;

	public @Nullable
	@Override
	ActionShower show(final @NonNull InputMethodView view) {
		final char key = getKey(view);
		if (key == NONE) {
			return null;
		} else {
			if (lastShownKey != key) {
				lastShownKey = key;
				shower.text = Character.toString(key).intern();
			}
			return shower;
		}
	}
}
