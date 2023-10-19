package nz.felle.messageasebetter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.icu.text.BreakIterator;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.os.LocaleList;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class InputMethodView extends View {
	static final float HEIGHT = 300f;
	final @NonNull Selection selection = new Selection(0, 0);
	private final @NonNull KeyboardPaints paints = new KeyboardPaints(getResources(), getContext().getTheme());
	private final @NonNull ULocale defaultLocale = ULocale.forLocale(LocaleList.getDefault().get(0));
	private @NonNull
	final IconShower _actShower = new IconShower(R.drawable.ic_keyboard_return, false);
	private final @NonNull HashMap<Integer, TrackedTouch> trackedTouches = new HashMap<>();
	private final @NonNull StringBuilder composeBuffer = new StringBuilder();
	@Nullable
	InputMethodService service = null;
	private @Nullable InputConnection conn = null;
	private @NonNull ULocale locale = defaultLocale;
	private boolean _numMode = false;
	private @NonNull CapsMode _caps = CapsMode.LOWER;
	private int _actAction = EditorInfo.IME_ACTION_UNSPECIFIED;
	private boolean composeActive = false;

	//region Constructor Boilerplate
	public InputMethodView(final @Nullable Context context) {
		super(context);
	}

	public InputMethodView(final @Nullable Context context, final @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public InputMethodView(
		final @Nullable Context context, final @Nullable AttributeSet attrs, final int defStyleAttr
	) {
		super(context, attrs, defStyleAttr);
	}

	public InputMethodView(
		final @Nullable Context context,
		final @Nullable AttributeSet attrs,
		final int defStyleAttr,
		final int defStyleRes
	) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}
	//endregion

	private float buttonWidth() {
		return this.getWidth() / 4.0f;
	}

	private float buttonHeight() {
		return this.getHeight() / 4.0f;
	}

	public void updateInputConnection(@NonNull InputConnection conn) {
		this.conn = conn;
	}

	void setLocale(final @Nullable Locale setTo) {
		this.locale = setTo != null ? ULocale.forLocale(setTo) : defaultLocale;
	}

	public void performContextMenuAction(final int action) {
		assert conn != null;
		conn.performContextMenuAction(action);
	}

	public void performActAction() {
		final int actAction = getActAction();
		if (actAction == EditorInfo.IME_ACTION_NONE) {
			insert('\n');
		} else {
			assert conn != null;
			conn.performEditorAction(actAction);
		}
	}

	public void beginCompose() {
		composeActive = true;
		composeBuffer.setLength(0);
	}

	private void appendCompose(final char ch) {
		assert conn != null;

		composeBuffer.append(ch);

		final @NonNull String buffer = composeBuffer.toString();
		conn.setComposingText(buffer, buffer.length());

		final @Nullable String output = ComposeTable.get(buffer);
		if (output != null) {
			composeActive = false;
			conn.setComposingText(output, 1);
			conn.finishComposingText();
		}
	}

	public void doWord(final Direction direction, final boolean delete) {
		assert conn != null;

		final int MULTIPLIER = 10;
		int chunk = 100;

		if (selection.start != selection.end) {
			if (delete) {
				deleteSelection();
			} else {
				int pos = switch (direction) {
					case BEFORE -> selection.start;
					case AFTER -> selection.end;
				};

				conn.setSelection(pos, pos);
			}
			return;
		}

		// `start == end`.
		int pos = selection.start;
		final int initialPos = pos;

		while (true) {
			final @NonNull CharSequence chars = switch (direction) {
				case BEFORE -> conn.getTextBeforeCursor(chunk, 0);
				case AFTER -> conn.getTextAfterCursor(chunk, 0);
			};

			final int len = chars.length();
			if (len == 0) {
				break;
			}

			final BreakIterator finder = BreakIterator.getWordInstance(locale);
			finder.setText(chars);

			boolean boundaryFound = false;

			switch (direction) {
				case BEFORE -> {
					finder.last();
					finder.previous();
					final int boundary = finder.previous();
					pos -= len;
					if (boundary != BreakIterator.DONE) {
						pos += boundary;
						boundaryFound = true;
					}
				}
				case AFTER -> {
					finder.first();
					finder.next();
					final int boundary = finder.next();
					if (boundary == BreakIterator.DONE) {
						pos += len;
					} else {
						pos += boundary;
						boundaryFound = true;
					}
				}
			}

			conn.setSelection(pos, pos);

			if (boundaryFound) {
				break;
			}

			chunk *= MULTIPLIER;
		}

		if (delete) {
			conn.deleteSurroundingText(Integer.max(pos - initialPos, 0), Integer.max(initialPos - pos, 0));
		}
	}

	boolean getNumMode() {
		return _numMode;
	}

	void setNumMode(final boolean value) {
		final boolean needsUpdate = value != _numMode;
		_numMode = value;
		if (needsUpdate) {
			this.postInvalidate();
		}
	}

	CapsMode getCaps() {
		return _caps;
	}

	void setCaps(final @NonNull CapsMode value) {
		final boolean needsUpdate = value != _caps;
		_caps = value;
		if (needsUpdate) {
			this.postInvalidate();
		}
	}

	int getActAction() {
		return _actAction;
	}

	void setActAction(final int value) {
		final boolean needsUpdate = value != _actAction;
		_actAction = value;
		if (needsUpdate) {
			updateActShower();
			this.postInvalidate();
		}
	}

	private void updateActShower() {
		final @DrawableRes int icon = switch (_actAction) {
			case EditorInfo.IME_ACTION_DONE -> R.drawable.ic_done;
			case EditorInfo.IME_ACTION_GO -> R.drawable.ic_login;
			case EditorInfo.IME_ACTION_NEXT -> R.drawable.ic_navigate_next;
			case EditorInfo.IME_ACTION_PREVIOUS -> R.drawable.ic_navigate_before;
			case EditorInfo.IME_ACTION_SEARCH -> R.drawable.ic_search;
			case EditorInfo.IME_ACTION_SEND -> R.drawable.ic_send;
			case EditorInfo.IME_ACTION_NONE -> R.drawable.empty;
			default -> R.drawable.ic_keyboard_return;
		};

		_actShower.updateDrawable(icon, this);
	}

	@NonNull
	ActionShower getActShower() {
		return _actShower;
	}

	private void complainAboutMissingAction(
		final int row, final int col, final @NonNull Motion motion
	) {
		Toast.makeText(getContext(), String.format("no action for row %s, col %s, motion %s", row + 1, col + 1, motion), Toast.LENGTH_SHORT).show();
	}

	void insert(final char ch) {
		assert conn != null;

		setCaps(getCaps().next());

		if (composeActive) {
			appendCompose(ch);
		} else {
			conn.commitText(Character.toString(ch), 1);
		}
	}

	private boolean deleteSelection() {
		assert conn != null;

		if (!selection.isNonCursor()) {
			return false;
		}

		final int selectionLen = selection.length();
		conn.beginBatchEdit();
		conn.setSelection(selection.end, selection.end);
		conn.deleteSurroundingText(selectionLen, 0);
		conn.endBatchEdit();
		return true;
	}

	void delete(final int amount) {
		assert conn != null;

		if (composeActive) {
			if (amount >= 0) {
				return;
			}

			final int delete = Integer.min(-amount, composeBuffer.length());
			final int newLength = composeBuffer.length() - delete;
			composeBuffer.setLength(newLength);
			conn.setComposingText(composeBuffer.toString(), 1);
			if (composeBuffer.length() == 0) {
				conn.finishComposingText();
				composeActive = false;
			}

			return;
		}

		if (deleteSelection()) {
			return;
		}

		if (amount < 0) {
			conn.deleteSurroundingText(-amount, 0);
		} else {
			conn.deleteSurroundingText(0, amount);
		}
	}

	void moveCursor(final int offset) {
		assert conn != null;

		int newPosition;
		if (selection.isNonCursor()) {
			if (offset > 0) {
				newPosition = selection.end;
			} else {
				newPosition = selection.start;
			}
		} else {
			newPosition = selection.start + offset;
		}

		if (newPosition >= 0) {
			conn.setSelection(newPosition, newPosition);
		}
	}

	private boolean isDark() {
		return getResources().getConfiguration().isNightModeActive();
	}

	void takeVoiceInput() {
		final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_ENABLE_FORMATTING, RecognizerIntent.FORMATTING_OPTIMIZE_QUALITY);
		intent.putExtra(RecognizerIntent.EXTRA_HIDE_PARTIAL_TRAILING_PUNCTUATION, false);
		intent.putExtra(RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS, false);

		final SpeechRecognizer recognizer = SpeechRecognizer.createSpeechRecognizer(getContext());

		final InputMethodView view = this;

		recognizer.setRecognitionListener(new RecognitionListener() {
			@Override
			public void onResults(final Bundle resultsBundle) {
				if (view.conn == null) {
					return;
				}

				final ArrayList<String> results = resultsBundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
				if (results.size() == 0) {
					return;
				}

				final @NonNull String rawText = results.get(0);

				if (rawText.isEmpty()) {
					return;
				}

				final @NonNull String text = switch (view.getCaps()) {
					case LOWER -> rawText;
					case UPPER -> Character.toUpperCase(rawText.charAt(0)) + rawText.substring(1);
					case UPPER_PERMANENT -> rawText.toUpperCase(locale.toLocale());
				};

				view.setCaps(view.getCaps().next());

				view.conn.commitText(text, 1);
			}

			@Override
			public void onError(final int error) {
				Log.e("nz.felle.messageasebetter takeVoiceInput", "onError " + error);
				Toast.makeText(getContext(), String.format("voice input returned error %s", error), Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onEvent(final int event, final Bundle params) {
			}

			@Override
			public void onReadyForSpeech(final Bundle params) {
			}

			@Override
			public void onPartialResults(final Bundle params) {
			}

			@Override
			public void onBufferReceived(final byte[] _data) {
			}

			@Override
			public void onEndOfSpeech() {
			}

			@Override
			public void onBeginningOfSpeech() {
			}

			@Override
			public void onRmsChanged(final float _dbms) {
			}
		});

		recognizer.startListening(intent);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		final @Nullable FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
		if (params != null) {
			params.gravity = Gravity.BOTTOM;
			params.height = Math.round(getResources().getDisplayMetrics().density * HEIGHT);
		}
		setLayoutParams(params);
	}

	private void drawButton(
		final @NonNull Canvas canvas,
		final float x,
		final float y,
		final float width,
		final float height
	) {
		final float strokeWidth = KeyboardPaints.STROKE_WIDTH;
		final float radius = KeyboardPaints.RADIUS;
		canvas.drawRoundRect(x + strokeWidth, y + strokeWidth, x + width - strokeWidth, y + height - strokeWidth, radius, radius, paints.buttonPaint);
	}

	private void drawButton(
		final @NonNull Canvas canvas,
		final float x,
		final float y,
		final float width,
		final float height,
		@Nullable ActionShower key
	) {
		drawButton(canvas, x, y, width, height);
		if (key != null) {
			key.show(canvas, x + (width / 2), y + (height / 2), paints.keyTextPaint, isDark());
		}
	}

	private void drawButton(
		final @NonNull Canvas canvas,
		final float x,
		final float y,
		final float width,
		final float height,
		@NonNull Map<Motion, Action> keys
	) {
		final @Nullable Action noneAction = keys.get(Motion.NONE);
		@Nullable ActionShower noneShower = null;
		if (noneAction != null) {
			noneShower = noneAction.show(this);
		}
		drawButton(canvas, x, y, width, height, noneShower);

		final float centerX = x + (width / 2);
		final float centerY = y + (height / 2);
		final float offsetX = (centerX - x) - 40;
		final float offsetY = (centerY - y) - 40;

		for (final Map.Entry<Motion, Action> entry : keys.entrySet()) {
			final @NonNull Motion motion = entry.getKey();
			final @NonNull Action action = entry.getValue();

			if (motion == Motion.NONE) {
				continue;
			}

			final @Nullable ActionShower actionShower = action.show(this);
			if (actionShower != null) {
				@Nullable Paint paint = paints.smallKeyTextPaint;
				if (action.secondary()) {
					paint = paints.secondaryKeyTextPaint;
				}

				final @NonNull PointF thisLetter = motion.offset(centerX, centerY, offsetX, offsetY);

				actionShower.show(canvas, thisLetter.x, thisLetter.y, paint, isDark());
			}
		}
	}

	// Symmetry
	@SuppressWarnings("UnusedReturnValue")
	boolean processLine(final int pointerId) {
		return processLine(Objects.requireNonNull(this.trackedTouches.get(pointerId), "invalid pointer id").line);
	}

	boolean processLine(final @NonNull Line line) {
		final float motionThresholdX = this.buttonWidth() / 4;
		final float motionThresholdY = this.buttonHeight() / 4;
		final @NonNull Motion motion = line.asMotion(motionThresholdX, motionThresholdY);

		final int actionRow = (int) Math.floor((line.startY - this.getY()) / this.buttonHeight());
		final float colFractional = (line.startX - this.getX()) / this.buttonWidth();
		int actionCol = (int) Math.floor(colFractional);

		if (actionRow == 3) {
			if (_numMode && colFractional < 1.5) {
				actionCol = 0;
			} else if (colFractional < 3) {
				actionCol = 1;
			} else {
				actionCol = 2;
			}
		}

		final @Nullable Action action = KeyboardActions.ACTIONS.get(actionRow).get(actionCol).get(motion);
		if (action != null) {
			action.execute(this);
			return true;
		} else {
			complainAboutMissingAction(actionRow, actionCol, motion);
			return false;
		}
	}

	// `performClick` means nothing here. This is all about swiping and start and end positions.
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(final @Nullable MotionEvent event) {
		if (event == null) {
			return super.onTouchEvent(null);
		}

		boolean processed = false;

		for (int i = 0; i < event.getPointerCount(); ++i) {
			boolean released = false;

			switch (event.getActionMasked()) {
				case MotionEvent.ACTION_UP:
					assert event.getPointerCount() == 1;
					released = true;
					break;
				case MotionEvent.ACTION_POINTER_UP:
					released = event.getActionIndex() == i;
					break;
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
				case MotionEvent.ACTION_MOVE:
					break;
				default:
					continue;
			}

			final int pointerId = event.getPointerId(i);
			final float x = event.getX(i);
			final float y = event.getY(i);
			final @NonNull TrackedTouch trackedTouch = trackedTouches.computeIfAbsent(pointerId, (_id) -> new TrackedTouch(this, pointerId, x, y));

			processed = true;
			trackedTouch.updateEnd(x, y);

			if (released) {
				trackedTouches.remove(pointerId);

				if (trackedTouch.finish()) {
					processLine(trackedTouch.line);
				}
			}
		}

		if (processed) {
			postInvalidate();
			return true;
		} else {
			return super.onTouchEvent(event);
		}
	}

	@Override
	protected void onDraw(final @Nullable Canvas canvas) {
		super.onDraw(canvas);

		if (canvas == null) {
			return;
		}

		canvas.drawColor(paints.backgroundColor);

		final float buttonHeight = buttonHeight();
		final float buttonWidth = buttonWidth();
		for (int row = 0; row < 3; ++row) {
			final float y = row * buttonHeight;
			for (int col = 0; col < 4; ++col) {
				final float x = col * buttonWidth;
				drawButton(canvas, x, y, buttonWidth, buttonHeight, KeyboardActions.ACTIONS.get(row).get(col));
			}
		}

		// render the fourth row manually
		final float lastRowY = 3 * buttonHeight;
		final float threeWidth = buttonWidth * 3;
		if (_numMode) {
			final float theseWidth = buttonWidth * 1.5f;
			drawButton(canvas, 0.0f, lastRowY, theseWidth, buttonHeight, Objects.requireNonNull(KeyboardActions.ACTIONS.get(3).get(0).get(Motion.NONE)).show(this));
			drawButton(canvas, theseWidth, lastRowY, theseWidth, buttonHeight);
		} else {
			drawButton(canvas, 0.0f, lastRowY, threeWidth, buttonHeight);
		}

		drawButton(canvas, threeWidth, lastRowY, buttonWidth, buttonHeight, _actShower);

		this.trackedTouches.forEach((_id, trackedTouch) -> {
			final @NonNull Line line = trackedTouch.line;
			if (line.length() > 1.0f) {
				canvas.drawLine(line.startX, line.startY, line.endX, line.endY, paints.linePaint);
			}
		});
	}

	static final class TrackedTouch {
		@NonNull
		private final Line line;
		@NonNull
		private final Repeater repeater;

		TrackedTouch(final @NonNull Line line, final @NonNull Repeater repeater) {
			this.line = line;
			this.repeater = repeater;
		}

		TrackedTouch(
			final @NonNull InputMethodView view,
			final int pointerId,
			final float initialX,
			final float initialY
		) {
			this(new Line(initialX, initialY), new Repeater(view, pointerId));
			this.repeater.start();
		}

		void updateEnd(final float endX, final float endY) {
			this.line.endX = endX;
			this.line.endY = endY;
		}

		boolean finish() {
			final boolean hasExecuted = this.repeater.hasExecuted();
			this.repeater.stop();
			return !hasExecuted;
		}

		@NonNull
		public Line line() {
			return line;
		}

		@NonNull
		public Repeater repeater() {
			return repeater;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj == null || obj.getClass() != this.getClass()) {
				return false;
			}
			var that = (TrackedTouch) obj;
			return Objects.equals(this.line, that.line) && Objects.equals(this.repeater, that.repeater);
		}

		@Override
		public int hashCode() {
			return Objects.hash(line, repeater);
		}

		@Override
		public @NonNull String toString() {
			return "TrackedTouch[" + "line=" + line + ", " + "repeater=" + repeater + ']';
		}

	}
}
