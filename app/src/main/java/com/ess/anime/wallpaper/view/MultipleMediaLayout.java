package com.ess.anime.wallpaper.view;

import android.content.Context;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.bumptech.glide.Priority;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.other.GlideApp;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.sprylab.android.widget.TextureVideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import uk.co.senab.photoview.PhotoView;

import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;

public class MultipleMediaLayout extends FrameLayout {

    private PhotoView mPhotoView;
    private TextureVideoView mVideoView;
    private String mMediaUrl;
    private MediaPlayer mMediaPlayer;

    public MultipleMediaLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MultipleMediaLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mPhotoView = findViewById(R.id.photo_view);
        mVideoView = findViewById(R.id.video_view);
    }

    public TextureVideoView getVideoView() {
        return mVideoView;
    }

    public PhotoView getPhotoView() {
        return mPhotoView;
    }

    public String getMediaUrl() {
        return mMediaUrl;
    }

    public void setMediaPath(String path) {
        setMediaPath(path, true);
    }

    /**
     * Sets the path to the media.  This path can be a web address (e.g. http://) or
     * an absolute local path (e.g. file://)
     *
     * @param path          The path to the media
     * @param autoPlayVideo if the media is video, whether auto play or not
     */
    public void setMediaPath(String path, boolean autoPlayVideo) {
        mMediaUrl = path;
        if (FileUtils.isImageType(path)) {
            showImage(path);
        } else if (FileUtils.isVideoType(path)) {
            showVideo(path, autoPlayVideo);
        }
    }

    private void showImage(String path) {
        mVideoView.setVisibility(GONE);
        mPhotoView.setAlpha(1f);
        GlideApp.with(getContext())
                .load(path)
                .priority(Priority.IMMEDIATE)
                .into(mPhotoView);
    }

    private void showVideo(final String path, final boolean autoPlay) {
        if (path.startsWith("http")) {
            mPhotoView.setAlpha(1f);
            GlideApp.with(getContext())
                    .load(R.drawable.ic_placeholder_post)
                    .priority(Priority.IMMEDIATE)
                    .into(mPhotoView);
        } else {
            mPhotoView.setAlpha(0f);
        }

        mVideoView.setVisibility(VISIBLE);
        mVideoView.setAlpha(0);
        mVideoView.setVideoPath(path);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer = mp;
                mp.setLooping(true);
                setVideoVolume();
                if (autoPlay) {
                    mVideoView.start();
                }
            }
        });
        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if (what == MEDIA_INFO_VIDEO_RENDERING_START) {
                    mPhotoView.setAlpha(0f);
                    mVideoView.setAlpha(1);
                }
                return false;
            }
        });
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mVideoView.setVideoPath(path);
                return true;
            }
        });
    }

    private void setVideoVolume() {
        if (mMediaPlayer != null) {
            int volume = isVideoSilent() ? 0 : 1;
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    private void setVideoSilent(boolean silent) {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit().putBoolean(Constants.VIDEO_SILENT, silent).apply();
    }

    private boolean isVideoSilent() {
        return PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(Constants.VIDEO_SILENT, false);
    }

    public void reset() {
        mPhotoView.setScale(1f);
        mVideoView.stopPlayback();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    // FullscreenActivity翻页后收到的通知，obj 为 image url
    @Subscribe
    public void startVideo(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.START_VIDEO)) {
            String url = (String) msgBean.obj;
            if (url.equals(mMediaUrl)) {
                setMediaPath(url);
            }
        }
    }
}
