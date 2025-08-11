package nz.felle.messageasebetter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

/* package-private */ class KeyboardActions {
	// @formatter:off
	/* package-private */ static final List<List<Map<Motion, Action>>> ACTIONS = List.of(
		// first row
		List.of(
			// first row, first column
			Map.of(
				Motion.NONE, new KeyAction('a', '1'),
				Motion.DOWN_LEFT, new SecondaryKeyAction('$'),
				Motion.DOWN, new SecondaryKeyAction('·'),
				Motion.DOWN_RIGHT, new KeyAction('v', KeyAction.NONE),
				Motion.RIGHT, new SecondaryKeyAction('-')
			),
			// first row, second column
			Map.of(
				Motion.NONE, new KeyAction('n', '2'),
				Motion.UP_LEFT, new SecondaryKeyAction('`'),
				Motion.UP, new SecondaryKeyAction('^'),
				Motion.RIGHT, new SecondaryKeyAction('!'),
				Motion.DOWN_RIGHT, new SecondaryKeyAction('\\'),
				Motion.DOWN, new KeyAction('l', KeyAction.NONE),
				Motion.DOWN_LEFT, new SecondaryKeyAction('/'),
				Motion.LEFT, new SecondaryKeyAction('+')
			),
			// first row, third column
			Map.of(
				Motion.NONE, new KeyAction('i', '3'),
				Motion.LEFT, new SecondaryKeyAction('?'),
				Motion.DOWN_LEFT, new KeyAction('x', KeyAction.NONE),
				Motion.DOWN, new SecondaryKeyAction('='),
				Motion.DOWN_RIGHT, new SecondaryKeyAction('€')
			),
			// first row, fourth column
			Map.of(
			)
		),
		// second row
		List.of(
			// second row, first column
			Map.of(
				Motion.NONE, new KeyAction('h', '4'),
				Motion.UP_LEFT, new SecondaryKeyAction('{'),
				Motion.LEFT, new SecondaryKeyAction('('),
				Motion.DOWN_LEFT, new SecondaryKeyAction('['),
				Motion.DOWN_RIGHT, new SecondaryKeyAction('_'),
				Motion.RIGHT, new KeyAction('k', KeyAction.NONE),
				Motion.UP_RIGHT, new SecondaryKeyAction('%')
			),
			// second row, second column
			Map.of(
				Motion.NONE, new KeyAction('o', '5'),
				Motion.UP_LEFT, new KeyAction('q', KeyAction.NONE),
				Motion.UP, new KeyAction('u', KeyAction.NONE),
				Motion.UP_RIGHT, new KeyAction('p', KeyAction.NONE),
				Motion.RIGHT, new KeyAction('b', KeyAction.NONE),
				Motion.DOWN_RIGHT, new KeyAction('j', KeyAction.NONE),
				Motion.DOWN, new KeyAction('d', KeyAction.NONE),
				Motion.DOWN_LEFT, new KeyAction('g', KeyAction.NONE),
				Motion.LEFT, new KeyAction('c', KeyAction.NONE)
			),
			// second row, third column
			Map.of(
				Motion.NONE, new KeyAction('r', '6'),
				Motion.UP_LEFT, new SecondaryKeyAction('|'),
				Motion.UP, new SetCapsAction(true),
				Motion.UP_RIGHT, new SecondaryKeyAction('}'),
				Motion.RIGHT, new SecondaryKeyAction(')'),
				Motion.DOWN_RIGHT, new SecondaryKeyAction(']'),
				Motion.DOWN, new SetCapsAction(false),
				Motion.DOWN_LEFT, new SecondaryKeyAction('@'),
				Motion.LEFT, new KeyAction('m', KeyAction.NONE)
			),
			// second row, fourth column
			Map.of(
				Motion.NONE, new Action() {
					private final @NonNull
					IconShower ifNum = new IconShower(R.drawable.ic_abc_mode, false);
					private final @NonNull
					IconShower ifNotNum = new IconShower(R.drawable.ic_num_mode, false);

					@Override
					public void execute(final @NonNull InputMethodView view) {
						view.setNumMode(!view.getNumMode());
					}

					@NonNull
					@Override
					public ActionShower show(final @NonNull InputMethodView view) {
						ifNum.initialize(view);
						ifNotNum.initialize(view);
						return view.getNumMode() ? ifNum : ifNotNum;
					}
				},
				Motion.UP, new CustomAction(R.drawable.ic_group_work) {
					@Override void execute(final @NonNull InputMethodView view) {
						view.beginCompose();
					}
				},
				Motion.UP_LEFT, new ContextMenuAction(R.drawable.ic_select_all, android.R.id.selectAll),
				Motion.UP_RIGHT, new ContextMenuAction(R.drawable.ic_copy, android.R.id.copy),
				Motion.DOWN_RIGHT, new ContextMenuAction(R.drawable.ic_paste, android.R.id.paste),
				Motion.DOWN, new CustomAction(R.drawable.ic_face) {
					@Override void execute(final @NonNull InputMethodView view) {
						view.beginEmoji();
					}
				},
				Motion.DOWN_LEFT, new ContextMenuAction(R.drawable.ic_cut, android.R.id.cut),
				Motion.LEFT, new CustomAction(R.drawable.ic_go_to_start) {
					@Override
					public void execute(final @NonNull InputMethodView view) {
						view.doWord(Direction.BEFORE, false);
					}
				},
				Motion.RIGHT, new CustomAction(R.drawable.ic_go_to_end) {
					@Override
					public void execute(final @NonNull InputMethodView view) {
						view.doWord(Direction.AFTER, false);
					}
				}
			)
		),
		// third row
		List.of(
			// third row, first column
			Map.of(
				Motion.NONE, new KeyAction('t', '7'),
				Motion.UP_LEFT, new SecondaryKeyAction('~'),
				Motion.UP, new SecondaryKeyAction('¨') {
					@Override
					public void execute(final @NonNull InputMethodView view) {
						// Lazy but works.
						view.beginCompose();
						view.insertString("\"");
					}
				},
				Motion.UP_RIGHT, new KeyAction('y', KeyAction.NONE),
				Motion.RIGHT, new SecondaryKeyAction('*'),
				Motion.LEFT, new SecondaryKeyAction('<'),
				Motion.DOWN_RIGHT, new SecondaryKeyAction('ß'),
				Motion.DOWN, new SecondaryKeyAction('–'),
				Motion.DOWN_LEFT, new SecondaryKeyAction('—')
			),
			// third row, second column
			Map.of(
				Motion.NONE, new KeyAction('e', '8'),
				Motion.UP_LEFT, new SecondaryKeyAction('"'),
				Motion.UP, new KeyAction('w', KeyAction.NONE),
				Motion.UP_RIGHT, new SecondaryKeyAction('\''),
				Motion.RIGHT, new KeyAction('z', KeyAction.NONE),
				Motion.DOWN_RIGHT, new SecondaryKeyAction(':'),
				Motion.DOWN, new SecondaryKeyAction('.'),
				Motion.DOWN_LEFT, new SecondaryKeyAction(',')
			),
			// third row, third column
			Map.of(
				Motion.NONE, new KeyAction('s', '9'),
				Motion.UP_LEFT, new KeyAction('f', KeyAction.NONE),
				Motion.UP, new SecondaryKeyAction('&'),
				Motion.UP_RIGHT, new SecondaryKeyAction('°'),
				Motion.RIGHT, new SecondaryKeyAction('>'),
				Motion.DOWN_LEFT, new SecondaryKeyAction(';'),
				Motion.LEFT, new SecondaryKeyAction('#')
			),
			// third row, fourth column
			Map.of(
				Motion.NONE, new Action() {
					private final @NonNull
					IconShower shower = new IconShower(R.drawable.ic_backspace, false);

					@Override
					public void execute(final @NonNull InputMethodView view) {
						view.delete(-1);
					}

					@NonNull
					@Override
					public ActionShower show(final @NonNull InputMethodView view) {
						shower.initialize(view);
						return shower;
					}
				},
				Motion.LEFT, new CustomAction(R.drawable.ic_undo) {
					@Override
					void execute(@NonNull InputMethodView view) {
						view.doUndo();
					}
				},
				Motion.UP_LEFT, new CustomAction(R.drawable.ic_line_start_diamond) {
					@Override
					void execute(@NonNull InputMethodView view) {
						view.doWord(Direction.BEFORE, true);
					}
				},
				Motion.UP_RIGHT, new CustomAction(R.drawable.ic_line_end_diamond) {
					@Override
					void execute(@NonNull InputMethodView view) {
						view.doWord(Direction.AFTER, true);
					}
				},
				Motion.RIGHT, new CustomAction(R.drawable.ic_arrow_right_alt) {
					@Override
					public void execute(@NonNull InputMethodView view) {
						view.delete(1);
					}
				},
				Motion.UP, new CustomAction(R.drawable.ic_microphone) {
					@Override
					void execute(final @NonNull InputMethodView view) {
						view.takeVoiceInput();
					}
				},
				Motion.DOWN, new CustomAction(R.drawable.ic_bottom_panel_close) {
					@Override
					void execute(final @NonNull InputMethodView view) {
						if (view.service != null) {
							view.service.requestHideSelf(0);
						}
					}
				}
			)
		),
		// fourth row
		List.of(
			// zero
			Map.of(
				Motion.NONE, new KeyAction(KeyAction.NONE, '0')
			),
			// space
			Map.of(
				Motion.NONE, new Action() {
					@Override
					@Nullable
					ActionShower show(final @NonNull InputMethodView _view) {
						return null;
					}

					@Override
					public void execute(@NonNull InputMethodView view) {
						view.typeCharacter(' ');
					}
				},
				Motion.LEFT, new Action() {
					@Override
					@Nullable
					ActionShower show(final @NonNull InputMethodView _view) {
						return null;
					}

					@Override
					public void execute(@NonNull InputMethodView view) {
						view.moveCursor(-1);
					}
				},
				Motion.RIGHT, new Action() {
					@Override
					@Nullable
					ActionShower show(final @NonNull InputMethodView _view) {
						return null;
					}

					@Override
					void execute(@NonNull InputMethodView view) {
						view.moveCursor(1);
					}
				}
			),
			// editor action
			Map.of(
				Motion.NONE, new Action() {
					@Override
					void execute(final @NonNull InputMethodView view) {
						view.performActAction();
					}

					@NonNull
					@Override
					ActionShower show(final @NonNull InputMethodView view) {
						return view.getActShower();
					}
				},
				Motion.DOWN, new SecondaryKeyAction('\n', R.drawable.ic_newline)
			)
		)
	);
	// @formatter:on
}
