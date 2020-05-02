package com.ess.anime.wallpaper.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.ui.view.image.ToggleImageView;
import com.ess.anime.wallpaper.utils.TimeFormat;
import com.sprylab.android.widget.TextureVideoView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VideoControllerLayout extends ConstraintLayout implements Runnable {

    @BindView(R.id.iv_play)
    ToggleImageView mIvPlay;
    @BindView(R.id.iv_volume)
    ToggleImageView mIvVolume;
    @BindView(R.id.tv_current)
    TextView mTvCurrent;
    @BindView(R.id.tv_total)
    TextView mTvTotal;
    @BindView(R.id.sb_progress)
    SeekBar mSbProgress;

    private MultipleMediaLayout mMediaLayout;
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

    public void attachTo(MultipleMediaLayout mediaLayout) {
        mMediaLayout = mediaLayout;
        mVideoView = mediaLayout.getVideoView();
        mIvPlay.setChecked(mVideoView.isPlaying());
        mIvVolume.setChecked(mMediaLayout.isVideoSilent());
        mSbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private boolean isPlayingWhenTouch;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mVideoView.seekTo(progress);
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
        mSbProgress.setSecondaryProgress((int) (bufferPercentage / 100f * total));
    }

    public void reset() {
        removeCallbacks(this);
        updateVideoProgress(0, 0, 0);
    }

    public void startUpdateRunnable() {
        removeCallbacks(this);
        post(this);
    }

    @OnClick(R.id.iv_play)
    void togglePlay() {
        if (mVideoView != null) {
            if (mIvPlay.isChecked()) {
                mVideoView.start();
            } else {
                mVideoView.pause();
            }
        }
    }

    @OnClick(R.id.iv_volume)
    void toggleVolume() {
        if (mMediaLayout != null) {
            mMediaLayout.setVideoSilent(mIvVolume.isChecked());
            mMediaLayout.updateVideoVolume();
        }
    }

    @Override
    public void run() {
        if (mVideoView != null) {
            updateVideoProgress(mVideoView.getCurrentPosition(), mVideoView.getDuration(), mVideoView.getBufferPercentage());
        }
        postDelayed(this, 100);
    }
}
