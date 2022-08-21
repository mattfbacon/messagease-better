package nz.felle.messageasebetter;

import androidx.annotation.NonNull;

public enum CapsMode {
	LOWER,
	UPPER,
	UPPER_PERMANENT,;

	public @NonNull
	CapsMode next() {
		if (this == UPPER) {
			return LOWER;
		} else {
			return this;
		}
	}

	public boolean isUppercase() {
		return this != LOWER;
	}
}
