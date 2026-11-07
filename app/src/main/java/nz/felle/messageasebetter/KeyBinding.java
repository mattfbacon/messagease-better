package nz.felle.messageasebetter;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/**
 * What a circumference ("blue") slot does.
 * <ul>
 *   <li>{@link Kind#TEXT}: types {@code glyph}, which may be any unicode text (empty disables the slot).</li>
 *   <li>{@link Kind#SETTINGS}: shows a settings icon and opens the settings.</li>
 *   <li>{@link Kind#COMPOSE}: shows {@code glyph} and starts compose mode with {@code value} entered.</li>
 * </ul>
 */
final class KeyBinding {
	enum Kind {TEXT, COMPOSE, SETTINGS}

	final @NonNull Kind kind;
	final @NonNull String glyph;
	final @NonNull String value;

	private KeyBinding(final @NonNull Kind kind, final @NonNull String glyph, final @NonNull String value) {
		this.kind = kind;
		this.glyph = glyph;
		this.value = value;
	}

	static @NonNull KeyBinding text(final @NonNull String glyph) {
		return new KeyBinding(Kind.TEXT, glyph, "");
	}

	/** Opens the keyboard's settings; only offered in the function column. */
	static @NonNull KeyBinding settings() {
		return new KeyBinding(Kind.SETTINGS, "", "");
	}

	static @NonNull KeyBinding compose(final @NonNull String glyph, final @NonNull String sequence) {
		return new KeyBinding(Kind.COMPOSE, glyph, sequence);
	}

	@NonNull String toJson() {
		try {
			return new JSONObject().put("k", kind.name()).put("g", glyph).put("v", value).toString();
		} catch (JSONException e) {
			throw new IllegalStateException(e);
		}
	}

	static KeyBinding fromJson(final @NonNull String json) {
		try {
			final JSONObject o = new JSONObject(json);
			return new KeyBinding(Kind.valueOf(o.getString("k")), o.getString("g"), o.optString("v", ""));
		} catch (JSONException | IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public boolean equals(final Object obj) {
		return obj instanceof KeyBinding that && kind == that.kind && glyph.equals(that.glyph) && value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(kind, glyph, value);
	}
}
