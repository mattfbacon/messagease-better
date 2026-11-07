package nz.felle.messageasebetter;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

final class Prefs {
	private static final String FILE = "settings";
	static final String SUGGESTIONS = "suggestions";
	static final String AUTO_SPACE = "auto_space";   // a space follows . , ; : ! ?
	static final String AUTO_CAP = "auto_cap";       // upper case at the start of a sentence

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

	static boolean autoSpace(final @NonNull Context context) {
		return prefs(context).getBoolean(AUTO_SPACE, true);
	}

	static void setAutoSpace(final @NonNull Context context, final boolean value) {
		prefs(context).edit().putBoolean(AUTO_SPACE, value).apply();
	}

	static boolean autoCap(final @NonNull Context context) {
		return prefs(context).getBoolean(AUTO_CAP, true);
	}

	static void setAutoCap(final @NonNull Context context, final boolean value) {
		prefs(context).edit().putBoolean(AUTO_CAP, value).apply();
	}
}
