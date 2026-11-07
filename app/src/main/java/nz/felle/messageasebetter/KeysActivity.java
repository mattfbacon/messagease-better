package nz.felle.messageasebetter;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Edit the "blue" circumference keys. This is a normal activity (not a dialog over the keyboard)
 * so that its text fields are served by an input method: the glyph can be typed with this very
 * keyboard, pasted, or given as "U+XXXX".
 */
public final class KeysActivity extends Activity {
	private static final Motion[][] COMPASS = {
		{Motion.UP_LEFT, Motion.UP, Motion.UP_RIGHT},
		{Motion.LEFT, Motion.NONE, Motion.RIGHT},
		{Motion.DOWN_LEFT, Motion.DOWN, Motion.DOWN_RIGHT},
	};

	/** Edits not yet saved: slot id to the binding it will have (the built-in one when reset). */
	private final java.util.Map<String, KeyBinding> pending = new java.util.HashMap<>();
	private static final int TAB_LETTERS = 0;
	private static final int TAB_NUMBERS = 1;
	private static final int TAB_FUNCTION = 2;

	private int tab = TAB_LETTERS;
	private TextView help;
	private float density;
	private LinearLayout board;

	@Override
	protected void onCreate(final @Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		KeyBindings.ensureLoaded(this);
		density = getResources().getDisplayMetrics().density;

		final ScrollView scroll = new ScrollView(this);
		final LinearLayout root = new LinearLayout(this);
		root.setOrientation(LinearLayout.VERTICAL);
		root.setPadding(dp(12), dp(12), dp(12), dp(12));
		scroll.addView(root);
		final LinearLayout outer = new LinearLayout(this);
		outer.setOrientation(LinearLayout.VERTICAL);
		outer.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
		final LinearLayout bar = new LinearLayout(this);
		bar.setOrientation(LinearLayout.HORIZONTAL);
		bar.setGravity(Gravity.END);
		bar.setPadding(dp(12), dp(8), dp(12), dp(8));
		final Button cancel = new Button(this);
		cancel.setText(android.R.string.cancel);
		cancel.setOnClickListener(v -> finish());
		final Button save = new Button(this);
		save.setText(R.string.save);
		save.setOnClickListener(v -> {
			if (!hasSettingsKey()) {
				new MaterialAlertDialogBuilder(this)
					.setMessage(R.string.need_settings_key)
					.setPositiveButton(android.R.string.ok, null)
					.show();
				return;
			}
			save();
			finish();
		});
		bar.addView(cancel);
		bar.addView(save);
		outer.addView(bar);
		setContentView(outer);
		ViewCompat.setOnApplyWindowInsetsListener(outer, (v, insets) -> {
			final var bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
			v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
			return insets;
		});

		final TextView title = new TextView(this);
		title.setText(R.string.circumference_keys);
		title.setTextSize(22);
		root.addView(title);
		final MaterialButtonToggleGroup tabs = new MaterialButtonToggleGroup(this);
		tabs.setSingleSelection(true);
		tabs.setSelectionRequired(true);
		final int[] labels = {R.string.tab_letters, R.string.tab_numbers, R.string.tab_function};
		final int[] ids = new int[labels.length];
		for (int i = 0; i < labels.length; ++i) {
			final MaterialButton button = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
			ids[i] = View.generateViewId();
			button.setId(ids[i]);
			button.setText(labels[i]);
			button.setTextSize(13);
			button.setPadding(dp(2), button.getPaddingTop(), dp(2), button.getPaddingBottom());
			button.setIconPadding(0);
			tabs.addView(button, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
		}
		final LinearLayout.LayoutParams tabsLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		tabsLp.topMargin = dp(8);
		root.addView(tabs, tabsLp);
		help = new TextView(this);
		help.setAlpha(0.7f);
		help.setPadding(0, dp(8), 0, dp(12));
		root.addView(help);
		tabs.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
			if (isChecked) {
				for (int i = 0; i < ids.length; ++i) {
					if (ids[i] == checkedId) {
						tab = i;
					}
				}
				rebuild();
			}
		});

		board = new LinearLayout(this);
		board.setOrientation(LinearLayout.VERTICAL);
		root.addView(board);

		final Button resetAll = new Button(this);
		resetAll.setText(R.string.reset_all);
		resetAll.setOnClickListener(v -> new MaterialAlertDialogBuilder(this)
			.setMessage(R.string.reset_all_confirm)
			.setPositiveButton(android.R.string.ok, (d, w) -> {
				for (final KeyboardActions.Slot slot : KeyboardActions.slots()) {
					pending.put(slot.id(), defaultOf(slot));
				}
				for (final KeyboardActions.Slot slot : KeyboardActions.numberSlots()) {
					pending.put(slot.id(), defaultOf(slot));
				}
				rebuild();
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show());
		root.addView(resetAll);
		tabs.check(ids[TAB_LETTERS]);
	}

	/** What the slot does with the pending edits applied. */
	private @NonNull KeyBinding effective(final @NonNull KeyboardActions.Slot slot) {
		final KeyBinding p = pending.get(slot.id());
		return p != null ? p : slot.action().binding();
	}

	/** What the slot does when not edited. */
	private @NonNull KeyBinding defaultOf(final @NonNull KeyboardActions.Slot slot) {
		return slot.action().defaultBinding();
	}

	/** Letter mode must keep a way to open the settings, or this editor becomes unreachable. */
	private boolean hasSettingsKey() {
		for (final KeyboardActions.Slot slot : KeyboardActions.slots()) {
			if (effective(slot).kind == KeyBinding.Kind.SETTINGS) {
				return true;
			}
		}
		return false;
	}

	private void save() {
		final java.util.ArrayList<KeyboardActions.Slot> all = new java.util.ArrayList<>(KeyboardActions.slots());
		all.addAll(KeyboardActions.numberSlots());
		for (final KeyboardActions.Slot slot : all) {
			final KeyBinding p = pending.get(slot.id());
			if (p == null) {
				continue;
			}
			if (p.equals(defaultOf(slot))) {
				KeyBindings.reset(this, slot.id());
			} else {
				KeyBindings.set(this, slot.id(), p);
			}
		}
	}

	private boolean night() {
		return getResources().getConfiguration().isNightModeActive();
	}

	private int dp(final float v) {
		return (int) (v * density);
	}

	private @Nullable KeyboardActions.Slot slotAt(final int row, final int col, final @NonNull Motion motion) {
		for (final KeyboardActions.Slot s : tab == TAB_NUMBERS ? KeyboardActions.numberSlots() : KeyboardActions.slots()) {
			if (s.row() == row && s.col() == col && s.motion() == motion) {
				return s;
			}
		}
		return null;
	}

	/** The 3x3 letter keys, each as a 3x3 compass of its surrounding slots. */
	private void rebuild() {
		board.removeAllViews();
		help.setText(new int[]{R.string.help_letters, R.string.help_numbers, R.string.help_function}[tab]);
		final int firstCol = tab == TAB_FUNCTION ? 3 : 0;
		for (int row = 0; row < 3; ++row) {
			final LinearLayout line = new LinearLayout(this);
			line.setOrientation(LinearLayout.HORIZONTAL);
			if (tab == TAB_FUNCTION) {
				line.addView(new View(this), new LinearLayout.LayoutParams(0, 0, 1f));
			}
			for (int col = firstCol; col < (tab == TAB_FUNCTION ? 4 : 3); ++col) {
				final GridLayout key = new GridLayout(this) {
					@Override
					protected void onMeasure(final int widthSpec, final int heightSpec) {
						super.onMeasure(widthSpec, widthSpec);
						// Square: the height follows the width the parent assigned.
						setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
					}
				};
				key.setRowCount(3);
				key.setColumnCount(3);
				key.setPadding(dp(4), dp(4), dp(4), dp(4));
				key.setBackground(rounded((getColor(android.R.color.system_accent2_100) & 0x00FFFFFF) | 0x80000000, 22));
				for (int r = 0; r < 3; ++r) {
					for (int c = 0; c < 3; ++c) {
						key.addView(cell(row, col, COMPASS[r][c]), cellParams(r, c));
					}
				}
				final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
				lp.setMargins(dp(3), dp(3), dp(3), dp(3));
				line.addView(key, lp);
			}
			if (tab == TAB_FUNCTION) {
				line.addView(new View(this), new LinearLayout.LayoutParams(0, 0, 1f));
			}
			board.addView(line);
		}
	}

	private @NonNull GridLayout.LayoutParams cellParams(final int r, final int c) {
		final GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(r, 1f), GridLayout.spec(c, 1f));
		lp.width = 0;
		lp.height = 0;
		lp.setMargins(dp(2), dp(2), dp(2), dp(2));
		return lp;
	}

	private @NonNull View cell(final int row, final int col, final @NonNull Motion motion) {
		final UnderlinedTextView tv = new UnderlinedTextView(this);
		tv.setGravity(Gravity.CENTER);
		tv.setTextSize(18);
		final KeyboardActions.Slot slot = slotAt(row, col, motion);
		if (slot != null && effective(slot).kind == KeyBinding.Kind.SETTINGS) {
			final boolean edited = !effective(slot).equals(defaultOf(slot));
			final ImageView image = new ImageView(this);
			image.setImageResource(R.drawable.ic_settings);
			image.setImageTintList(android.content.res.ColorStateList.valueOf(
				getColor(edited ? android.R.color.system_accent3_700 : android.R.color.system_accent1_600)));
			image.setBackground(rounded((getColor(edited ? android.R.color.system_accent3_200 : android.R.color.system_accent1_200) & 0x00FFFFFF) | 0xA0000000, 14));
			image.setPadding(dp(6), dp(6), dp(6), dp(6));
			image.setOnClickListener(v -> edit(slot));
			return image;
		}
		if (slot != null) {
			final KeyBinding b = effective(slot);
			final boolean edited = !b.equals(defaultOf(slot));
			tv.setText(b.glyph);
			tv.underline = b.kind == KeyBinding.Kind.COMPOSE;
			tv.underlineColor = night() ? 0xFF53DBC9 : 0xFF006A60;
			// Edited slots use the tertiary palette so they stand out from the defaults.
			tv.setTextColor(getColor(edited ? android.R.color.system_accent3_700 : android.R.color.system_accent1_600));
			tv.setBackground(rounded((getColor(edited ? android.R.color.system_accent3_200 : android.R.color.system_accent1_200) & 0x00FFFFFF) | 0xA0000000, 14));
			tv.setOnClickListener(v -> edit(slot));
		} else {
			// Fixed keys (letters, caps, functions) are shown as half transparent fixtures.
			final int icon = KeyboardActions.layout(tab == TAB_NUMBERS).get(row).get(col).containsKey(motion)
				? KeyboardActions.layout(tab == TAB_NUMBERS).get(row).get(col).get(motion).icon() : 0;
			if (icon != 0) {
				final ImageView image = new ImageView(this);
				image.setImageResource(icon);
				image.setImageTintList(android.content.res.ColorStateList.valueOf(tv.getCurrentTextColor()));
				image.setAlpha(0.5f);
				image.setPadding(dp(6), dp(6), dp(6), dp(6));
				return image;
			}
			final String label = fixedLabel(row, col, motion);
			if (label != null) {
				tv.setText(label);
				if (KeyboardActions.layout(tab == TAB_NUMBERS).get(row).get(col).get(motion) instanceof SetCapsAction) {
					// Shift and caps lock get their own colour so their meaning is clear.
					tv.setTextColor(night() ? 0xFFFFB870 : 0xFF8B5000);
					tv.setAlpha(0.8f);
				} else {
					tv.setAlpha(0.5f);
				}
			}
		}
		return tv;
	}

	private @NonNull android.graphics.drawable.GradientDrawable rounded(final int color, final float radiusDp) {
		final var d = new android.graphics.drawable.GradientDrawable();
		d.setColor(color);
		d.setCornerRadius(dp(radiusDp));
		return d;
	}

	private @Nullable String fixedLabel(final int row, final int col, final @NonNull Motion motion) {
		final Action action = KeyboardActions.layout(tab == TAB_NUMBERS).get(row).get(col).get(motion);
		if (action instanceof KeyAction k) {
			final boolean num = tab == TAB_NUMBERS;
			final char c = num ? k.numKey : k.normalKey;
			return c == KeyAction.NONE ? null : Character.toString(c);
		} else if (action instanceof SetCapsAction) {
			return motion == Motion.UP ? "\u21E7" : "\u21E9";
		}
		return action == null ? null : "\u2022";
	}

	private void edit(final @NonNull KeyboardActions.Slot slot) {
		final KeyBinding current = effective(slot);
		final LinearLayout form = new LinearLayout(this);
		form.setOrientation(LinearLayout.VERTICAL);
		form.setPadding(dp(24), dp(12), dp(24), 0);

		final Spinner kind = new Spinner(this);
		final List<KeyBinding.Kind> kinds = List.of(KeyBinding.Kind.TEXT, KeyBinding.Kind.COMPOSE, KeyBinding.Kind.SETTINGS);
		final List<String> kindNames = List.of(
			getString(R.string.kind_text), getString(R.string.kind_compose), getString(R.string.kind_settings));
		kind.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, kindNames));
		kind.setSelection(kinds.indexOf(current.kind));
		form.addView(kind);

		final EditText glyph = new EditText(this);
		glyph.setHint(R.string.glyph_hint);
		glyph.setInputType(InputType.TYPE_CLASS_TEXT);
		glyph.setText(current.glyph);
		form.addView(glyph);

		final TextView valueLabel = new TextView(this);
		valueLabel.setText(R.string.compose_label);
		form.addView(valueLabel);
		final EditText value = new EditText(this);
		value.setHint(R.string.compose_hint);
		value.setInputType(InputType.TYPE_CLASS_TEXT);
		if (current.kind == KeyBinding.Kind.COMPOSE) {
			value.setText(current.value);
		}
		form.addView(value);

		final Runnable layoutForKind = () -> {
			final KeyBinding.Kind selected = kinds.get(kind.getSelectedItemPosition());
			final int visibility = selected == KeyBinding.Kind.COMPOSE ? View.VISIBLE : View.GONE;
			valueLabel.setVisibility(visibility);
			value.setVisibility(visibility);
			glyph.setVisibility(selected == KeyBinding.Kind.SETTINGS ? View.GONE : View.VISIBLE);
		};
		kind.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
				layoutForKind.run();
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {}
		});
		layoutForKind.run();

		new MaterialAlertDialogBuilder(this)
			.setTitle(R.string.edit_key)
			.setView(form)
			.setPositiveButton(R.string.save, (d, w) -> {
				final String g = KeyBindings.parseText(glyph.getText().toString());
				final String v = KeyBindings.parseText(value.getText().toString());
				pending.put(slot.id(), switch (kinds.get(kind.getSelectedItemPosition())) {
					case COMPOSE -> KeyBinding.compose(g, v);
					case SETTINGS -> KeyBinding.settings();
					default -> KeyBinding.text(g);
				});
				rebuild();
			})
			.setNeutralButton(R.string.reset, (d, w) -> {
				pending.put(slot.id(), defaultOf(slot));
				rebuild();
			})
			.setNegativeButton(android.R.string.cancel, null)
			.show();
	}

	/** A label that draws a line centred under its text, marking compose keys. */
	private static final class UnderlinedTextView extends TextView {
		boolean underline = false;
		int underlineColor = 0;

		UnderlinedTextView(final @NonNull android.content.Context context) {
			super(context);
		}

		@Override
		protected void onDraw(final @NonNull android.graphics.Canvas canvas) {
			super.onDraw(canvas);
			final CharSequence text = getText();
			if (!underline || text.length() == 0) {
				return;
			}
			final float width = getPaint().measureText(text, 0, text.length());
			final float thickness = Math.max(1f, getResources().getDisplayMetrics().density * 1.5f);
			final float y = getBaseline() + getPaint().getFontMetrics().descent * 0.6f + thickness;
			final float cx = getWidth() / 2f;
			final android.graphics.Paint line = new android.graphics.Paint(getPaint());
			line.setColor(underlineColor);
			line.setStrokeWidth(thickness);
			line.setStrokeCap(android.graphics.Paint.Cap.ROUND);
			canvas.drawLine(cx - width / 2f, y, cx + width / 2f, y, line);
		}
	}
}
