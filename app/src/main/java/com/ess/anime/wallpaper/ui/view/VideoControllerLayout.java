package com.ess.anime.wallpaper.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.sprylab.android.widget.TextureVideoView;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoControllerLayout extends LinearLayout implements Runnable {

    @BindView(R.id.tv_current)
    TextView mTvCurrent;
    @BindView(R.id.tv_total)
    TextView mTvTotal;
    @BindView(R.id.sb_progress)
    SeekBar mSbProgress;

    private TextureVideoView mVideoView;

    public VideoControllerLayout(Context context) {
        super(context);
    }

    public VideoControllerLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoControllerLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    public void attachTo(TextureVideoView videoView, boolean isWebPath) {
        mVideoView = videoView;
        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean isPlayingWhenTouch;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!isWebPath && fromUser) {
                    mVideoView.seekTo(progress);
                }
                if (fromUser) {
                    updateVideoProgress(progress, mVideoView.getDuration(), mVideoView.getBufferPercentage());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isPlayingWhenTouch = mVideoView.isPlaying();
                if (isPlayingWhenTouch) {
                    mVideoView.pause();
                }
                removeCallbacks(VideoControllerLayout.this);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mVideoView.seekTo(seekBar.getProgress());
                if (isPlayingWhenTouch) {
                    mVideoView.start();
                }
                startUpdateRunnable();
            }
        });
    }

    private void updateVideoProgress(int current, int total, int bufferPercentage) {
        mTvCurrent.setText(TimeFormat.durationFormat(current));
        mTvTotal.setText(TimeFormat.durationFormat(total));
        mSbProgress.setMax(total);
        mSbProgress.setProgress(current);
        mSbProgress.setSecondaryProgress(bufferPercentage);
    }

    public void reset() {
        removeCallbacks(this);
        updateVideoProgress(0, 0, 0);
    }

    public void startUpdateRunnable() {
        removeCallbacks(this);
        post(this);
    }

    @Override
    public void run() {
        if (mVideoView != null) {
            updateVideoProgress(mVideoView.getCurrentPosition(), mVideoView.getDuration(), mVideoView.getBufferPercentage());
        }
        postDelayed(this, 100);
    }
}
