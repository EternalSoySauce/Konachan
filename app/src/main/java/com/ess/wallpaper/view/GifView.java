package com.ess.wallpaper.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import com.ess.wallpaper.R;

import java.io.InputStream;

public class GifView extends View {
    private Resources resources;

    /**
     * 播放gif的影视工具类
     */
    private Movie mMovie;

    /**
     * 0-播放状态 1-暂停状态
     */
    private int playStatus;
    /**
     * gif动画开始时间
     */
    private long mMovieStart;
    /**
     * gif当前播放进度时间
     */
    private int relTime;
    /**
     * 暂停/帧动画重新播放的补偿时间
     */
    private int offsetTime;

    /**
     * 宽度的缩放比例(控件宽度:gif图片宽度)
     */
    private float ratioWidth;
    /**
     * 高度的缩放比例(控件高度:gif图片高度)
     */
    private float ratioHeight;

    public GifView(Context context) {
        this(context, null);
    }

    public GifView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        setFocusable(true);

        // 3.0以上系统会自动打开硬件加速,需要手动关闭才可以正常播放gif
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        resources = context.getResources();

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.GifView);
        int drawableResId = ta.getResourceId(R.styleable.GifView_src, -1);
        setGifResource(drawableResId);

        ta.recycle();
    }

    /**
     * 设置gif资源(适用于本地drawable图片)
     *
     * @param resourceId gif图片的id
     */
    public void setGifResource(int resourceId) {
        if (resourceId == -1) {
            return;
        }
        InputStream is = resources.openRawResource(resourceId);
        mMovie = Movie.decodeStream(is);
        requestLayout();
    }

    /**
     * 设置gif输入流(适用于网络图片)
     *
     * @param is gif图片的输入流
     */
    public void setGifInputStream(InputStream is) {
        mMovie = Movie.decodeStream(is);
        requestLayout();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        playStatus = 1;
        // 开始时间置为0,onDrawa时会重新获取
        mMovieStart = 0;
        // 记录暂停时已经播放的进度,再重新播放时进行补偿修正
        offsetTime = relTime;

        invalidate();
    }

    /**
     * 恢复播放
     */
    public void resume() {
        playStatus = 0;

        invalidate();
    }

    /**
     * 重头开始播放
     */
    public void restart() {
        playStatus = 0;
        // 开始时间置为0,onDrawa时会重新获取
        mMovieStart = 0;

        invalidate();
    }

    /**
     * 是否为暂停状态
     *
     * @return true-暂停  false-播放中
     */
    public boolean isPaused() {
        // 暂停和帧播放都算是pause
        return playStatus != 0;
    }

    /**
     * 移动到指定进度
     *
     * @param progress 停留进度,小于0或大于gif动画总长度时无效
     */
    public void seekTo(int progress) {
        if (mMovie == null) {
            return;
        }

        if (progress >= 0 && progress < mMovie.duration()) {
            // 开始时间设为0,onDraw时会重置开始时间
            mMovieStart = 0;
            // 记录当前设置的进度,再重新播放时进行补偿修正
            offsetTime = progress;
            invalidate();
        }
    }

    /**
     * 获取gif动画当前进度
     *
     * @return 进度值, 动画为空时, 返回-1
     */
    public int getProgress() {
        if (mMovie == null) {
            return -1;
        }
        return relTime;
    }

    /**
     * 获取gif动画总时长
     *
     * @return 总时长, 动画为空时, 返回-1
     */
    public int getDuration() {
        if (mMovie == null) {
            return -1;
        }
        return mMovie.duration();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // gif动画非空时进行大小计算,计算方法仿造ImageView中的onMeasure
        if (mMovie != null) {
            int w;
            int h;

            // 获取gif宽高
            w = mMovie.width();
            h = mMovie.height();
            if (w <= 0) w = 1;
            if (h <= 0) h = 1;

            int pleft = getPaddingLeft();
            int pright = getPaddingRight();
            int ptop = getPaddingTop();
            int pbottom = getPaddingBottom();

            int widthSize;
            int heightSize;

            /* We are either don't want to preserve the drawables aspect ratio,
               or we are not allowed to change view dimensions. Just measure in
               the normal way.
            */
            w += pleft + pright;
            h += ptop + pbottom;

            w = Math.max(w, getSuggestedMinimumWidth());
            h = Math.max(h, getSuggestedMinimumHeight());

            // 根据宽高的MeasureSpec计算期望值,算出当前控件需要的大小
            widthSize = resolveSizeAndState(w, widthMeasureSpec, 0);
            heightSize = resolveSizeAndState(h, heightMeasureSpec, 0);

            // 计算控件宽高和gif图宽高的比例,用于onDraw绘制时让gif图片进行合理缩放,使其适应控件大小
            ratioWidth = (float) widthSize / w;
            ratioHeight = (float) heightSize / h;

            // 设置控件宽高
            setMeasuredDimension(widthSize, heightSize);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        long now = SystemClock.uptimeMillis();

        if (mMovie != null) {
            // 获取gif动画持续时间
            int dur = mMovie.duration();
            if (dur == 0) {
                dur = 1000;
            }

            switch (playStatus) {
                case 0: // 播放
                    // 重新开始播放时,再次获取起始时间
                    if (mMovieStart == 0) {
                        mMovieStart = now;
                    }

                    // 动画运行时间  % 动画持续时间,算出当前动画播放的进度时间
                    // 注意移除掉暂停/帧播放造成的动画时间偏差值offsetTime
                    relTime = (int) ((now - mMovieStart + offsetTime) % dur);

                    // 设置播放进度监听
                    if (onGifPlayingListener != null) {
                        onGifPlayingListener.onProgress(relTime);
                    }
                    break;
                case 1: // 暂停
                    // 不更新进度时间relTime,一直停留在暂停时的图片
                    relTime = (int) offsetTime;
                    break;
                default:
                    break;
            }

            // 置顶播放某个进度时间的动画
            mMovie.setTime(relTime);

            // 设置缩放比例
            canvas.scale(Math.min(ratioWidth, ratioHeight),
                    Math.min(ratioWidth, ratioHeight));

            // 绘制
            mMovie.draw(canvas, 0, 0);

            invalidate();
        }
    }

    private OnGifPlayingListener onGifPlayingListener;

    public void setOnGifPlayingListener(OnGifPlayingListener onGifPlayingListener) {
        this.onGifPlayingListener = onGifPlayingListener;
    }

    public interface OnGifPlayingListener {
        void onProgress(int time);
    }

}
