package com.ess.anime.wallpaper.ui.view.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.ess.anime.wallpaper.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;

public class PixivGifDlProgressView extends View {

    private final static int DEFAULT_CIRCLE_COUNT = 4;
    private final static float DEFAULT_NORMAL_CIRCLE_RADIUS = 9;
    private final static float DEFAULT_ACTIVATING_CIRCLE_RADIUS = 15;
    private final static float DEFAULT_STROKE_WIDTH = 6;
    private final static int DEFAULT_ACTIVE_COLOR = Color.GREEN;
    private final static int DEFAULT_INACTIVE_COLOR = Color.LTGRAY;

    private float mNormalCircleRadius;
    private float mActivatingCircleRadius;
    private float mCircleStrokeWidth;
    private float mLineStrokeWidth;
    private int mActiveColor;
    private int mInactiveColor;

    private int mCircleCount;
    private int mCurrentActiveIndex;
    private float mCurrentActiveProgress;

    private Paint mPaint = new Paint();

    public PixivGifDlProgressView(Context context) {
        this(context, null);
    }

    public PixivGifDlProgressView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PixivGifDlProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.PixivGifDlProgressView);
        mNormalCircleRadius = typedArray.getDimension(R.styleable.PixivGifDlProgressView_normal_circle_radius, DEFAULT_NORMAL_CIRCLE_RADIUS);
        mActivatingCircleRadius = typedArray.getDimension(R.styleable.PixivGifDlProgressView_activating_circle_radius, DEFAULT_ACTIVATING_CIRCLE_RADIUS);
        mCircleStrokeWidth = typedArray.getDimension(R.styleable.PixivGifDlProgressView_circle_stroke_width, DEFAULT_STROKE_WIDTH);
        mLineStrokeWidth = typedArray.getDimension(R.styleable.PixivGifDlProgressView_line_stroke_width, DEFAULT_STROKE_WIDTH);
        mActiveColor = typedArray.getColor(R.styleable.PixivGifDlProgressView_active_color, DEFAULT_ACTIVE_COLOR);
        mInactiveColor = typedArray.getColor(R.styleable.PixivGifDlProgressView_inactive_color, DEFAULT_INACTIVE_COLOR);
        mCircleCount = typedArray.getInt(R.styleable.PixivGifDlProgressView_circle_count, DEFAULT_CIRCLE_COUNT);
        typedArray.recycle();

        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            List<Float> sizeList = Arrays.asList((mNormalCircleRadius + mCircleStrokeWidth) * 2f, (mActivatingCircleRadius + mCircleStrokeWidth) * 2f, mLineStrokeWidth);
            int height = (int) (Collections.max(sizeList) + 0.5f);
            setMeasuredDimension(View.MEASURED_SIZE_MASK, height);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 等分圆间距
        float maxCircleRadius = Math.max(mNormalCircleRadius, mActivatingCircleRadius);
        float spaceWidth = (getWidth() - (mCircleStrokeWidth + maxCircleRadius) * 2f * mCircleCount) / (mCircleCount - 1f);

        // 线在下
        for (int i = 0; i < mCircleCount - 1; i++) {
            float radius = (i == mCurrentActiveIndex) ? mActivatingCircleRadius : mNormalCircleRadius;
            float cx = i * ((mCircleStrokeWidth + maxCircleRadius) * 2f + spaceWidth) + mCircleStrokeWidth + radius;
            float cy = getHeight() / 2f;
            boolean isBesideActivatingCircle = i == mCurrentActiveIndex || ((i + 1) == mCurrentActiveIndex && (i + 1) < mCurrentActiveIndex);
            float lineWidth = spaceWidth + (mActivatingCircleRadius - mNormalCircleRadius) * (isBesideActivatingCircle ? 0 : 1);
            lineWidth += mCircleStrokeWidth * (isBesideActivatingCircle ? 2 : 3); // 延长线宽避免衔接有缝隙
            float activeWidth;
            if (i < mCurrentActiveIndex) {
                activeWidth = lineWidth;
            } else if (i == mCurrentActiveIndex) {
                activeWidth = lineWidth * mCurrentActiveProgress;
            } else {
                activeWidth = 0;
            }
            float inactiveWidth = lineWidth - activeWidth;
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setColor(mActiveColor);
            mPaint.setStrokeWidth(mLineStrokeWidth);
            float startX = cx + radius;
            float stopX = startX + activeWidth;
            canvas.drawLine(startX, cy, stopX, cy, mPaint);
            mPaint.setColor(mInactiveColor);
            startX = stopX;
            stopX = startX + inactiveWidth;
            canvas.drawLine(startX, cy, stopX, cy, mPaint);
        }

        // 圆在上
        for (int i = 0; i < mCircleCount; i++) {
            float radius;
            if (i < mCurrentActiveIndex) {
                radius = mNormalCircleRadius;
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                mPaint.setColor(mActiveColor);
            } else if (i == mCurrentActiveIndex) {
                if (i < mCircleCount - 1) {
                    radius = mActivatingCircleRadius;
                    mPaint.setStyle(Paint.Style.STROKE);
                } else {
                    radius = mNormalCircleRadius;
                    mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                }
                mPaint.setColor(mActiveColor);
            } else {
                radius = mNormalCircleRadius;
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(mInactiveColor);
            }
            mPaint.setStrokeWidth(mCircleStrokeWidth);
            float cx = i * ((mCircleStrokeWidth + maxCircleRadius) * 2f + spaceWidth) + mCircleStrokeWidth + radius;
            float cy = getHeight() / 2f;
            canvas.drawCircle(cx, cy, radius, mPaint);
        }
    }

    public void updateProgress(int currentCircleIndex, float currentCircleProgress) {
        mCurrentActiveIndex = currentCircleIndex;
        mCurrentActiveProgress = Math.min(Math.max(currentCircleProgress, 0), 1);
        invalidate();
    }
}
