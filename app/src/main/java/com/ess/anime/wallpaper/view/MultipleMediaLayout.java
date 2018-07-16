package com.ess.anime.wallpaper.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.VideoCache;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.sprylab.android.widget.TextureVideoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import uk.co.senab.photoview.PhotoView;

import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;

public class MultipleMediaLayout extends FrameLayout implements RequestListener<Drawable>,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener {

    private PhotoView mPhotoView;
    private TextureVideoView mVideoView;
    private MediaPlayer mMediaPlayer;

    private String mMediaPath;
    private boolean mAutoPlay;

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


    /***********************************  Path  ***********************************/
    public String getMediaPath() {
        return mMediaPath;
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
        mMediaPath = path;
        mAutoPlay = autoPlayVideo;
        if (FileUtils.isImageType(path)) {
            showImage();
        } else if (FileUtils.isVideoType(path)) {
            showVideo();
        }
        setBackgroundColor(isWebPath() ? Color.TRANSPARENT : Color.BLACK);
    }

    private boolean isWebPath() {
        return mMediaPath.startsWith("http");
    }


    /***********************************  Image  ***********************************/
    public PhotoView getPhotoView() {
        return mPhotoView;
    }

    private void showImage() {
        mVideoView.setVisibility(GONE);
        mPhotoView.setAlpha(1f);
        mPhotoView.setZoomable(false);

        Object url = isWebPath() ? MyGlideModule.makeGlideUrl(mMediaPath) : mMediaPath;
        int placeHolder = isWebPath() ? R.drawable.ic_placeholder_detail : 0;
        Activity activity = (Activity) getContext();
        if (activity != null && !activity.isDestroyed()) {
            GlideApp.with(getContext())
                    .load(url)
                    .placeholder(placeHolder)
                    .listener(this)
                    .priority(Priority.IMMEDIATE)
                    .into(mPhotoView);
        }
    }

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
        mPhotoView.post(new Runnable() {
            @Override
            public void run() {
                showImage();
            }
        });
        return true;
    }

    @Override
    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
        mPhotoView.setZoomable(true);
        return false;
    }


    /***********************************  Video  ***********************************/
    public TextureVideoView getVideoView() {
        return mVideoView;
    }

    private void showVideo() {
        mPhotoView.setZoomable(false);
        if (isWebPath()) {
            mPhotoView.setAlpha(1f);
            GlideApp.with(getContext())
                    .load(R.drawable.ic_placeholder_detail)
                    .priority(Priority.IMMEDIATE)
                    .into(mPhotoView);
        } else {
            mPhotoView.setAlpha(0f);
        }

        mVideoView.setVisibility(VISIBLE);
        mVideoView.setAlpha(0);
        mVideoView.setKeepScreenOn(true);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setOnInfoListener(this);
        mVideoView.setOnErrorListener(this);

        String url = isWebPath() ? VideoCache.getInstance(getContext()).getCacheUrl(mMediaPath) : mMediaPath;
        mVideoView.setVideoPath(url);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer = mp;
        mp.setLooping(true);
        setVideoVolume();
        if (mAutoPlay) {
            mVideoView.start();
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MEDIA_INFO_VIDEO_RENDERING_START) {
            mPhotoView.setAlpha(0f);
            mVideoView.setAlpha(1);
        }
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        showVideo();
        return true;
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


    /***********************************  Other  ***********************************/
    public void reset() {
        mPhotoView.setScale(1f);
        mVideoView.setKeepScreenOn(false);
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
            if (url.equals(mMediaPath)) {
                setMediaPath(url);
            }
        }
    }
}
