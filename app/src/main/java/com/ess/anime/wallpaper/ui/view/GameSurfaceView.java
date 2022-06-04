package com.ess.anime.wallpaper.ui.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.model.helper.XPuzzleHelper;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.UIUtils;

import java.util.ArrayList;

import androidx.annotation.Nullable;


public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private final static int DEFAULT_COLUMN = 3;  //默认拼图行列数
    private final static float SPACE = 5;         //画拼图时每张之间的间隙宽度

    private SharedPreferences mPreferences;
    private int mColumn;
    private int[][] mCurrentState;
    private XPuzzleHelper mPuzzle;
    private int mCurrentStep = 0;
    private int mBestStep;
    private boolean mCompleted = false;
    private boolean mHasPlayedSound = false;

    private Bitmap mGameBitmap;
    private ArrayList<Bitmap> mSplitBitmapList = new ArrayList<>();
    private ArrayList<RectF> mRectList;
    private OnActionListener mActionListener;

    private Paint mPaint;

    public GameSurfaceView(Context context) {
        super(context);
        init();
    }

    public GameSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GameSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        getHolder().addCallback(this);

        initData();
    }

    private void initData() {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mColumn = mPreferences.getInt(Constants.GAME_COLUMN, DEFAULT_COLUMN);
        mBestStep = mPreferences.getInt(String.valueOf(mColumn), -1);
        mGameBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_game);
    }

    private void draw() {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }

        // 清理画布
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvas.drawPaint(mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

        // 画拼图
        for (int row = 0; row < mColumn; row++) {
            for (int col = 0; col < mColumn; col++) {
                int rectIndex = row * mColumn + col;
                int bitmapIndex = mCurrentState[row][col];
                if (bitmapIndex != 0) {
                    canvas.drawBitmap(mSplitBitmapList.get(bitmapIndex),
                            null, mRectList.get(rectIndex), mPaint);
                }
            }
        }

        RectF firstRect = mRectList.get(0);
        RectF lastRect = mRectList.get(mRectList.size() - 1);

        // 记录当前步数
        String step = getContext().getString(R.string.game_current_step, mCurrentStep);
        float x = firstRect.left / 2f + getWidth() / 4f;
        float y = firstRect.top * 0.7f;
        mPaint.setColor(getResources().getColor(R.color.color_text_unselected));
        mPaint.setTextSize(UIUtils.sp2px(getContext(), 18));
        canvas.drawText(step, x, y, mPaint);

        // 显示最佳记录
        String best = getContext().getString(R.string.game_best_step);
        best += mBestStep == -1 ? getContext().getString(R.string.game_none_best_step) : mBestStep;
        x = lastRect.right / 2f + getWidth() / 4f;
        canvas.drawText(best, x, y, mPaint);

        // 判断是否完成拼图
        if (mCompleted) {
            x = getWidth() / 2f;
            y = lastRect.bottom + (getHeight() - lastRect.bottom) * 0.65f;
            mPaint.setColor(getResources().getColor(R.color.colorAccent));
            mPaint.setTextSize(UIUtils.sp2px(getContext(), 24));
            canvas.drawText(getContext().getString(R.string.game_completed), x, y, mPaint);
            if (!mHasPlayedSound) {
                SoundHelper.getInstance().playGameWinSound(getContext());
                mHasPlayedSound = true;
            }
        }

        getHolder().unlockCanvasAndPost(canvas);
    }

    private void splitBitmaps() {
        recycleBitmaps(false);
        mSplitBitmapList = BitmapUtils.splitImage(mGameBitmap, mColumn, mColumn);
        Bitmap lastBitmap = mSplitBitmapList.get(mSplitBitmapList.size() - 1);
        mSplitBitmapList.remove(mSplitBitmapList.size() - 1);
        mSplitBitmapList.add(0, lastBitmap);
    }

    private void resetBitmapRects() {
        if (mRectList == null) {
            mRectList = new ArrayList<>();
        } else {
            mRectList.clear();
        }

        float drawLength = Math.min(getWidth(), getHeight()) / 3f * 2f / mColumn;
        float startX = getWidth() / 2f - mColumn / 2f * drawLength - SPACE * (mColumn / 2f - 0.5f);
        float startY = getHeight() / 2f - mColumn / 2f * drawLength - SPACE * (mColumn / 2f - 0.5f);
        for (int row = 0; row < mColumn; row++) {
            for (int col = 0; col < mColumn; col++) {
                float drawX = col * (drawLength + SPACE) + startX;
                float drawY = row * (drawLength + SPACE) + startY;
                mRectList.add(new RectF(drawX, drawY, drawX + drawLength, drawY + drawLength));
            }
        }
    }

    private void rebuildPuzzle() {
        mCurrentState = mPuzzle.rebuild(mColumn);
    }

    public void changeColumn(int newColumn) {
        mColumn = newColumn;
        mPreferences.edit().putInt(Constants.GAME_COLUMN, newColumn).apply();
        mBestStep = mPreferences.getInt(String.valueOf(mColumn), -1);
        splitBitmaps();
        resetBitmapRects();
        restartGame();
    }

    public void restartGame() {
        mCompleted = false;
        mHasPlayedSound = false;
        mCurrentStep = 0;
        rebuildPuzzle();
        draw();
    }

    public void recycleBitmaps(boolean includeGameBitmap) {
        if (includeGameBitmap) {
            mGameBitmap.recycle();
        }
        for (Bitmap bitmap : mSplitBitmapList) {
            bitmap.recycle();
        }
        System.gc();
    }

    public Bitmap getGameBitmap() {
        return mGameBitmap;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mPuzzle == null) {
            mPuzzle = new XPuzzleHelper();
            rebuildPuzzle();
        }
        if (mRectList == null) {
            splitBitmaps();
            resetBitmapRects();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        draw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                for (int i = 0; i < mRectList.size(); i++) {
                    RectF rect = mRectList.get(i);
                    if (rect.contains(x, y)) {
                        int touchX = i / mColumn;
                        int touchY = i % mColumn;
                        int[] touchPos = new int[]{touchX, touchY};
                        if (!mCompleted && mPuzzle.moveToNewState(mCurrentState, touchPos)) {
                            mCurrentStep++;
                            mCompleted = mPuzzle.isCompleted(mCurrentState);
                            if (mCompleted && (mBestStep == -1 || mCurrentStep < mBestStep)) {
                                mBestStep = mCurrentStep;
                                mPreferences.edit().putInt(String.valueOf(mColumn), mBestStep).apply();
                            }
                            draw();
                        }
                        break;
                    }
                }
                break;
        }
        return true;
    }

    public interface OnActionListener {
        // 切换图片
        void onChangeBitmap(Bitmap bitmap);
    }

    public void setOnActionListener(OnActionListener actionListener) {
        mActionListener = actionListener;
    }


    /******************  InstanceState  ******************/

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.column = mColumn;
        ss.currentState = mCurrentState;
        ss.currentStep = mCurrentStep;
        ss.bestStep = mBestStep;
        ss.completed = mCompleted;
        ss.hasPlayedSound = mHasPlayedSound;
        ss.puzzle = mPuzzle;
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        mColumn = ss.column;
        mCurrentState = ss.currentState;
        mCurrentStep = ss.currentStep;
        mBestStep = ss.bestStep;
        mCompleted = ss.completed;
        mHasPlayedSound = ss.hasPlayedSound;
        mPuzzle = ss.puzzle;
    }

    private static class SavedState extends BaseSavedState {
        int column;
        int[][] currentState;
        int currentStep;
        int bestStep;
        boolean completed;
        boolean hasPlayedSound;
        XPuzzleHelper puzzle;

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(column);
            out.writeSerializable(currentState);
            out.writeInt(currentStep);
            out.writeInt(bestStep);
            out.writeInt(completed ? 1 : 0);
            out.writeInt(hasPlayedSound ? 1 : 0);
            out.writeParcelable(puzzle, flags);
        }

        private SavedState(Parcel in) {
            super(in);
            column = in.readInt();
            currentState = (int[][]) in.readSerializable();
            currentStep = in.readInt();
            bestStep = in.readInt();
            completed = (in.readInt() != 0);
            hasPlayedSound = (in.readInt() != 0);
            puzzle = in.readParcelable(XPuzzleHelper.class.getClassLoader());
        }

        @Override
        public String toString() {
            return "GameSurfaceView.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " column=" + column + " currentState=" + currentState.length
                    + " currentStep=" + currentStep + " bestStep=" + bestStep
                    + " completed=" + completed + " hasPlayedSound=" + hasPlayedSound
                    + "}";
        }

        @SuppressWarnings("hiding")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
