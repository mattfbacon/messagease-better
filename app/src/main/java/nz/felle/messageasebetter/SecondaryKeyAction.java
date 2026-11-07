package nz.felle.messageasebetter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A "blue" circumference key. Unless it is drawn as an icon, what it does is configurable per
 * slot (see KeyBindings); the constructor arguments are the built-in default.
 */
public class SecondaryKeyAction extends Action {
	private final @NonNull KeyBinding builtIn;
	private final @NonNull TextShower textShower;
	private final @Nullable ActionShower iconShower;
	private final @Nullable SecondaryKeyAction fallback;
	/** The centre of a key rather than its circumference. */
	private final boolean center;
	private @Nullable IconShower settingsShower = null;
	private @Nullable String slotId = null;

	SecondaryKeyAction(final char key) {
		this(KeyBinding.text(Character.toString(key)));
	}

	SecondaryKeyAction(final @NonNull KeyBinding builtIn) {
		this(builtIn, null);
	}

	/** A configurable action in the centre of a key (center) or on its circumference. */
	SecondaryKeyAction(final @NonNull KeyBinding builtIn, final boolean center) {
		this(builtIn, null, center);
	}

	/** Like the above, but until the user sets it, behaves like fallback instead of builtIn. */
	SecondaryKeyAction(final @NonNull KeyBinding builtIn, final @Nullable SecondaryKeyAction fallback) {
		this(builtIn, fallback, false);
	}

	private SecondaryKeyAction(final @NonNull KeyBinding builtIn, final @Nullable SecondaryKeyAction fallback, final boolean center) {
		this.center = center;
		this.builtIn = builtIn;
		this.fallback = fallback;
		this.textShower = new TextShower(builtIn.glyph);
		this.iconShower = null;
	}

	/** A fixed (not configurable) key drawn as an icon. */
	SecondaryKeyAction(final char key, final @DrawableRes int drawableId) {
		this.center = false;
		this.builtIn = KeyBinding.text(Character.toString(key));
		this.fallback = null;
		this.textShower = new TextShower("");
		this.iconShower = new IconShower(drawableId, this.secondary());
	}

	boolean editable() {
		return iconShower == null;
	}

	@NonNull KeyBinding builtIn() {
		return builtIn;
	}

	void bind(final @NonNull String slotId) {
		this.slotId = slotId;
	}

	/** What this slot does when the user hasn't changed it. */
	@NonNull KeyBinding defaultBinding() {
		// Number mode starts from the built-in symbols, not from edits made for letter mode.
		return fallback != null ? fallback.defaultBinding() : builtIn;
	}

	boolean hasFallback() {
		return fallback != null;
	}

	@NonNull KeyBinding binding() {
		return slotId == null ? defaultBinding() : KeyBindings.get(slotId, defaultBinding());
	}

	@Override
	public void execute(final @NonNull InputMethodView view) {
		final KeyBinding b = binding();
		if (b.kind == KeyBinding.Kind.SETTINGS) {
			view.openSettings();
			return;
		}
		if (b.glyph.isEmpty()) {
			return;
		}
		if (b.kind == KeyBinding.Kind.COMPOSE) {
			view.beginCompose();
			view.insertString(b.value);
		} else {
			view.typeString(b.glyph);
		}
	}

	@Nullable
	@Override
	public ActionShower show(final @NonNull InputMethodView view) {
		if (iconShower != null) {
			((IconShower) iconShower).initialize(view);
			return iconShower;
		}
		final KeyBinding b = binding();
		if (b.kind == KeyBinding.Kind.SETTINGS) {
			if (settingsShower == null) {
				settingsShower = new IconShower(R.drawable.ic_settings, secondary());
			}
			settingsShower.initialize(view);
			return settingsShower;
		}
		final String glyph = b.glyph;
		if (glyph.isEmpty()) {
			return null;
		}
		textShower.setText(glyph);
		return textShower;
	}

	@Override
	boolean secondary() {
		return !center;
	}
}
