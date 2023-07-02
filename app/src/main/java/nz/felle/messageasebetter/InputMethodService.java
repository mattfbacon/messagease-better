package nz.felle.messageasebetter;

import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.Nullable;

public final class InputMethodService extends android.inputmethodservice.InputMethodService {
	private @Nullable InputMethodView currentView = null;

	@Override
	public View onCreateInputView() {
		InputMethodView view = (InputMethodView)getLayoutInflater().inflate(R.layout.keyboard, null);
		view.updateInputConnection(getCurrentInputConnection());
		currentView = view;
		return view;
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

		currentView.updateQuirks(info.packageName);

		currentView.setLocale(info.hintLocales != null ? info.hintLocales.get(0) : null);
	}

	@Override
	public void onUpdateSelection(final int oldSelStart, final int oldSelEnd, final int newSelStart, final int newSelEnd, final int candidatesStart, final int candidatesEnd) {
		super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd);
		assert currentView != null;
		currentView.selection.start = newSelStart;
		currentView.selection.end = newSelEnd;
	}
}
