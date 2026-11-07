package nz.felle.messageasebetter;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * User overrides of the circumference slots, persisted in their own preferences file. Slots are
 * identified by "row,col,MOTION"; only differences from the built-in layout are stored.
 * The keyboard service and the editor activity share a process, so a static cache is enough.
 */
final class KeyBindings {
	private static final String FILE = "key_bindings";

	private static @Nullable Map<String, KeyBinding> overrides = null;

	private KeyBindings() {}

	private static @NonNull SharedPreferences prefs(final @NonNull Context context) {
		return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
	}

	static synchronized void ensureLoaded(final @NonNull Context context) {
		if (overrides != null) {
			return;
		}
		final Map<String, KeyBinding> loaded = new HashMap<>();
		for (final Map.Entry<String, ?> e : prefs(context).getAll().entrySet()) {
			if (e.getValue() instanceof String json) {
				final KeyBinding b = KeyBinding.fromJson(json);
				if (b != null) {
					loaded.put(e.getKey(), b);
				}
			}
		}
		overrides = loaded;
		// Give every configurable action its slot id.
		KeyboardActions.slots();
		KeyboardActions.numberSlots();
	}

	/** The binding in effect for a slot, given its built-in default. */
	static synchronized @NonNull KeyBinding get(final @NonNull String slotId, final @NonNull KeyBinding builtIn) {
		if (overrides != null) {
			final KeyBinding o = overrides.get(slotId);
			if (o != null) {
				return o;
			}
		}
		return builtIn;
	}

	static synchronized void set(final @NonNull Context context, final @NonNull String slotId, final @NonNull KeyBinding binding) {
		ensureLoaded(context);
		overrides.put(slotId, binding);
		prefs(context).edit().putString(slotId, binding.toJson()).apply();
	}

	static synchronized void reset(final @NonNull Context context, final @NonNull String slotId) {
		ensureLoaded(context);
		overrides.remove(slotId);
		prefs(context).edit().remove(slotId).apply();
	}

	static synchronized void resetAll(final @NonNull Context context) {
		ensureLoaded(context);
		overrides.clear();
		prefs(context).edit().clear().apply();
	}

	/** Parse a user-typed value: "U+1F600" style code points become that character, else as is. */
	static @NonNull String parseText(final @NonNull String input) {
		final String t = input.trim();
		if (t.matches("(?i)u\\+[0-9a-f]{1,6}( u\\+[0-9a-f]{1,6})*")) {
			final StringBuilder sb = new StringBuilder();
			for (final String part : t.split(" ")) {
				final int cp = Integer.parseInt(part.substring(2), 16);
				if (Character.isValidCodePoint(cp)) {
					sb.appendCodePoint(cp);
				}
			}
			return sb.toString();
		}
		return input;
	}
}
