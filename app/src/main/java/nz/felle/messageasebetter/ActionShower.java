package nz.felle.messageasebetter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import androidx.annotation.NonNull;

abstract class ActionShower {
	abstract void show(@NonNull Canvas canvas, float centerX, float centerY, @NonNull Paint paint);
	void initialize(@NonNull InputMethodView view) {}
}
