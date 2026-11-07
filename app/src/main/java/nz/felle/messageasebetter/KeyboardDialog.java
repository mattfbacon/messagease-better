package nz.felle.messageasebetter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

/**
 * Floating dialogs owned by the keyboard window: the app underneath keeps its focus (and the
 * keyboard stays up), unlike an activity, which would take the whole screen as another app.
 * Same approach as DovUniStroke.
 */
final class KeyboardDialog {
	interface ContentFactory {
		@NonNull View create(@NonNull Context context);
	}

	private static Dialog dialog = null;

	private KeyboardDialog() {}

	/**
	 * Show the content in a dialog attached to the window of anchor (a view of the keyboard). The
	 * window is transparent: the content draws its own rounded card, which floats in a 16dp margin
	 * (room for its shadow) over a dimmed screen.
	 */
	static void show(final @NonNull View anchor, final @NonNull ContentFactory factory) {
		dismiss();
		final Context themed = new ContextThemeWrapper(anchor.getContext(), com.google.android.material.R.style.Theme_Material3_DayNight_Dialog);
		final Dialog d = new Dialog(themed);
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		final float density = themed.getResources().getDisplayMetrics().density;
		final int margin = (int) (16 * density);
		final FrameLayout frame = new FrameLayout(d.getContext());
		frame.setPadding(margin, margin, margin, margin);
		final View content = factory.create(d.getContext());
		content.setElevation(6 * density);
		frame.addView(content, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
		d.setContentView(frame);
		d.setCanceledOnTouchOutside(true);

		final Window window = d.getWindow();
		final WindowManager.LayoutParams lp = window.getAttributes();
		lp.token = anchor.getWindowToken();
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		lp.width = WindowManager.LayoutParams.MATCH_PARENT;
		window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		// The scrim separates the card from the app behind it, light or dark
		window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		window.setDimAmount(0.4f);
		window.setAttributes(lp);
		window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		dialog = d;
		d.setOnDismissListener(x -> {
			if (dialog == x) {
				dialog = null;
			}
		});
		d.show();
	}

	/** Close the dialog, if any (the keyboard window it is attached to is going away). */
	static void dismiss() {
		if (dialog != null) {
			try {
				dialog.dismiss();
			} catch (RuntimeException e) {
				// the window is already gone
			}
			dialog = null;
		}
	}
}
