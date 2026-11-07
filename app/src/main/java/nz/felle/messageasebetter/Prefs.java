package nz.felle.messageasebetter;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

final class Prefs {
	private static final String FILE = "settings";
	static final String SUGGESTIONS = "suggestions";

	private Prefs() {}

	private static @NonNull SharedPreferences prefs(final @NonNull Context context) {
		return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
	}

	/** Word suggestions above the keys; off by default. */
	static boolean suggestions(final @NonNull Context context) {
		return prefs(context).getBoolean(SUGGESTIONS, false);
	}

	static void setSuggestions(final @NonNull Context context, final boolean value) {
		prefs(context).edit().putBoolean(SUGGESTIONS, value).apply();
	}
}
