package com.ess.wallpaper.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;

public class RoundImageView extends android.support.v7.widget.AppCompatImageView {

	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RoundImageView(Context context) {
		super(context);
		init();
	}

	private final RectF roundRect = new RectF();
	private float rect_radius = 90;
	private final Paint maskPaint = new Paint();
	private final Paint zonePaint = new Paint();

	private void init() {
		maskPaint.setAntiAlias(true);
		maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		maskPaint.setFilterBitmap(true);
		zonePaint.setAntiAlias(true);
		zonePaint.setFilterBitmap(true);
		float density = getResources().getDisplayMetrics().density;
		rect_radius = rect_radius * density;
	}

	public void setRectRadius(float radius) {
		rect_radius = radius;
		invalidate();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		int w = getWidth();
		int h = getHeight();
		roundRect.set(0, 0, w, h);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
		canvas.drawRoundRect(roundRect, rect_radius, rect_radius, zonePaint);
		canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
		super.draw(canvas);
		canvas.restore();
	}

}
