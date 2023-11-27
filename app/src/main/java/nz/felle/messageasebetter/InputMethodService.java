package nz.felle.messageasebetter;

import android.annotation.SuppressLint;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

public final class InputMethodService extends android.inputmethodservice.InputMethodService {
	private @Nullable InputMethodView currentView = null;

	@Override
	public View onCreateInputView() {
		// There is no view root; this is a freestanding view.
		@SuppressLint("InflateParams") final @NonNull View layout = getLayoutInflater().inflate(R.layout.keyboard, null);

		final @NonNull InputMethodView input = layout.requireViewById(R.id.input);
		input.updateInputConnection(getCurrentInputConnection());
		input.service = this;
		currentView = input;

		return layout;
	}

	@Override
	public void onStartInputView(final @Nullable EditorInfo info, final boolean restarting) {
		super.onStartInputView(info, restarting);
		assert currentView != null;

		currentView.updateInputConnection(getCurrentInputConnection());

		int inputType = 0;
		int imeOptions = 0;
		boolean initialCapsMode = false;
		int selectionStart = 0;
		int selectionEnd = 0;
		@Nullable Locale locale = null;
		if (info != null) {
			inputType = info.inputType & InputType.TYPE_MASK_CLASS;

			imeOptions = info.imeOptions;

			initialCapsMode = info.initialCapsMode > 0;

			if (info.initialSelStart != -1) {
				selectionStart = info.initialSelStart;
				if (info.initialSelEnd != -1) {
					selectionEnd = info.initialSelEnd;
				} else {
					selectionEnd = selectionStart;
				}
			}

			if (info.hintLocales != null) {
				locale = info.hintLocales.get(0);
			}
		}

		currentView.setNumMode(inputType == InputType.TYPE_CLASS_NUMBER || inputType == InputType.TYPE_CLASS_PHONE);

		if ((imeOptions & EditorInfo.IME_FLAG_NO_ENTER_ACTION) > 0) {
			currentView.setActAction(EditorInfo.IME_ACTION_NONE);
		} else {
			currentView.setActAction(imeOptions & EditorInfo.IME_MASK_ACTION);
		}

		currentView.setCaps(initialCapsMode ? CapsMode.UPPER : CapsMode.LOWER);

		currentView.selection.start = selectionStart;
		currentView.selection.end = selectionEnd;

		currentView.setLocale(locale);

		currentView.endEmoji();
	}

	@Override
	public void onUpdateSelection(
		final int oldSelStart,
		final int oldSelEnd,
		final int newSelStart,
		final int newSelEnd,
		final int candidatesStart,
		final int candidatesEnd
	) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
		assert currentView != null;
		currentView.selection.start = newSelStart;
		currentView.selection.end = newSelEnd;
	}
}
