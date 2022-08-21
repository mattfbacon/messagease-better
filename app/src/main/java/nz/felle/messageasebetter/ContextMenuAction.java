package nz.felle.messageasebetter;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

public class ContextMenuAction extends CustomAction {
	private final int action;

	ContextMenuAction(final @DrawableRes int drawableId, final int action) {
		super(drawableId);
		this.action = action;
	}

	@Override
	void execute(final @NonNull InputMethodView view) {
		view.performContextMenuAction(action);
	}
}
