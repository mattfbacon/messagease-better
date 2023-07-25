package nz.felle.messageasebetter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class Selection {
	public int start;
	public int end;

	//region Record Boilerplate
	public Selection(final int start, final int end) {
		this.start = start;
		this.end = end;
	}

	public Selection(final Selection other) {
		this.start = other.start;
		this.end = other.end;
	}

	@Override
	public @NonNull Selection clone() {
		return new Selection(this);
	}

	@Override
	public boolean equals(final @Nullable Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		Selection that = (Selection) obj;
		return this.start == that.start && this.end == that.end;
	}

	@Override
	public int hashCode() {
		return Objects.hash(start, end);
	}

	@NonNull
	@Override
	public String toString() {
		return "Selection[" + "start=" + start + ", " + "end=" + end + ']';
	}
	//endregion

	public int length() {
		return end - start;
	}

	public boolean isNonCursor() {
		return length() > 0;
	}
}
