package com.ess.anime.wallpaper.ui.view.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.utils.UIUtils;

import androidx.appcompat.widget.AppCompatImageView;

public class RoundImageView extends AppCompatImageView {

    private final RectF roundRect = new RectF();
    private float cornerRadius = UIUtils.dp2px(getContext(), 90);
    private final Paint maskPaint = new Paint();
    private final Paint zonePaint = new Paint();

    public RoundImageView(Context context) {
        this(context, null);
    }

    public RoundImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoundImageView);
        cornerRadius = array.getDimension(R.styleable.RoundImageView_corner_radius, cornerRadius);
        array.recycle();
        init();
    }

    private void init() {
        maskPaint.setAntiAlias(true);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        maskPaint.setFilterBitmap(true);
        zonePaint.setAntiAlias(true);
        zonePaint.setFilterBitmap(true);
    }

    public void setRectRadius(float radius) {
        cornerRadius = radius;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int w = getWidth();
        int h = getHeight();
        roundRect.set(0, 0, w, h);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.saveLayer(roundRect, zonePaint, Canvas.ALL_SAVE_FLAG);
        canvas.drawRoundRect(roundRect, cornerRadius, cornerRadius, zonePaint);
        canvas.saveLayer(roundRect, maskPaint, Canvas.ALL_SAVE_FLAG);
        super.draw(canvas);
        canvas.restore();
    }

}
