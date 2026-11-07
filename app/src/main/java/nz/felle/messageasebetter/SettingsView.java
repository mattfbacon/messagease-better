package nz.felle.messageasebetter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.materialswitch.MaterialSwitch;

/** The settings: a rounded card of switch rows, stored in Prefs, shown in a KeyboardDialog. */
final class SettingsView extends LinearLayout {
	private final float density;
	private final int textColor;

	SettingsView(final @NonNull Context context, final int cardColor, final int textColor) {
		super(context);
		this.density = context.getResources().getDisplayMetrics().density;
		this.textColor = textColor;
		setOrientation(VERTICAL);
		setPadding(dp(20), dp(8), dp(20), dp(8));
		final GradientDrawable card = new GradientDrawable();
		card.setColor(cardColor);
		card.setCornerRadius(dp(28));
		setBackground(card);

		addView(row(context.getString(R.string.auto_space), context.getString(R.string.auto_space_summary),
			Prefs.autoSpace(context), (checked) -> Prefs.setAutoSpace(context, checked)));
		addView(row(context.getString(R.string.auto_cap), context.getString(R.string.auto_cap_summary),
			Prefs.autoCap(context), (checked) -> Prefs.setAutoCap(context, checked)));
		addView(row(context.getString(R.string.suggestions), context.getString(R.string.suggestions_summary),
			Prefs.suggestions(context), (checked) -> Prefs.setSuggestions(context, checked)));
		addView(linkRow(context.getString(R.string.circumference_keys), context.getString(R.string.circumference_keys_summary), () -> {
			KeyboardDialog.dismiss();
			context.startActivity(new android.content.Intent(context, KeysActivity.class)
				.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));
		}));
	}

	private @NonNull LinearLayout linkRow(final @NonNull String title, final @NonNull String summary, final @NonNull Runnable onClick) {
		final LinearLayout row = row(title, summary, false, (checked) -> {});
		row.removeViewAt(1);
		row.setOnClickListener((v) -> onClick.run());
		return row;
	}

	private int dp(final float v) {
		return (int) (v * density);
	}

	private @NonNull LinearLayout row(
		final @NonNull String title,
		final @NonNull String summary,
		final boolean initial,
		final @NonNull java.util.function.Consumer<Boolean> onChange
	) {
		final Context context = getContext();
		final LinearLayout row = new LinearLayout(context);
		row.setOrientation(HORIZONTAL);
		row.setGravity(Gravity.CENTER_VERTICAL);
		row.setPadding(0, dp(14), 0, dp(14));

		final LinearLayout texts = new LinearLayout(context);
		texts.setOrientation(VERTICAL);
		final TextView name = new TextView(context);
		name.setText(title);
		name.setTextSize(16);
		name.setTextColor(textColor);
		final TextView sub = new TextView(context);
		sub.setText(summary);
		sub.setTextSize(13);
		sub.setTextColor(textColor);
		sub.setAlpha(0.65f);
		texts.addView(name);
		texts.addView(sub);
		row.addView(texts, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

		final MaterialSwitch toggle = new MaterialSwitch(context);
		toggle.setChecked(initial);
		toggle.setOnCheckedChangeListener((button, checked) -> onChange.accept(checked));
		final LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.setMarginStart(dp(16));
		row.addView(toggle, lp);
		row.setOnClickListener((v) -> toggle.toggle());
		return row;
	}
}
