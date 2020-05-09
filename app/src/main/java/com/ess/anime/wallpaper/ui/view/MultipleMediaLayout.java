package com.ess.anime.wallpaper.ui.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.text.TextUtils;
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
import com.ess.anime.wallpaper.glide.glide_url.ProgressInterceptor;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.VideoCache;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.StringUtils;
import com.github.chrisbanes.photoview.PhotoView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.microshow.rxffmpeg.player.IMediaPlayer;
import io.microshow.rxffmpeg.player.RxFFmpegPlayerView;

import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;

public class MultipleMediaLayout extends FrameLayout implements RequestListener<Drawable>,
        IMediaPlayer.OnPreparedListener, IMediaPlayer.OnLoadingListener, IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnErrorListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnTimeUpdateListener {

    public MultipleMediaLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MultipleMediaLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }


    /***********************************  Path  ***********************************/
    private String mMediaPath;

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
        return StringUtils.isStartWidthProtocol(mMediaPath);
    }


    /***********************************  Image  ***********************************/
    @BindView(R.id.photo_view)
    PhotoView mPhotoView;

    public PhotoView getPhotoView() {
        return mPhotoView;
    }

    private void showImage() {
        mVideoView.setVisibility(GONE);
        mLayoutVideoController.setVisibility(GONE);
        mPhotoView.setAlpha(1f);
        mPhotoView.setZoomable(false);

        Object url = isWebPath() ? MyGlideModule.makeGlideUrl(mMediaPath) : mMediaPath;
        int placeHolder = isWebPath() ? R.drawable.ic_placeholder_detail : 0;
        Activity activity = (Activity) getContext();
        if (ComponentUtils.isActivityActive(activity)) {
            GlideApp.with(getContext())
                    .load(url)
                    .placeholder(placeHolder)
                    .listener(this)
                    .priority(Priority.IMMEDIATE)
                    .into(mPhotoView);
        }

        if (isWebPath()) {
            ProgressInterceptor.addListener(mMediaPath, progress -> {
            });
        }
    }

    @Override
    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
        ProgressInterceptor.removeListener(mMediaPath);
        mPhotoView.post(this::showImage);
        return true;
    }

    @Override
    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
        ProgressInterceptor.removeListener(mMediaPath);
        mPhotoView.setZoomable(true);
        return false;
    }


    /***********************************  Video  ***********************************/
    @BindView(R.id.video_view)
    RxFFmpegPlayerView mVideoView;
    @BindView(R.id.layout_video_controller)
    VideoControllerLayout mLayoutVideoController;

    public RxFFmpegPlayerView getVideoView() {
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
        mVideoView.setKeepScreenOn(true);
        mVideoView.mPlayer.setOnPreparedListener(this);
        mVideoView.mPlayer.setOnLoadingListener(this);
        mVideoView.mPlayer.setOnCompleteListener(this);
        mVideoView.mPlayer.setOnErrorListener(this);
        mVideoView.mPlayer.setOnTimeUpdateListener(this);
        mVideoView.mPlayer.setOnVideoSizeChangedListener(this);

        String url = isWebPath() ? VideoCache.getInstance().getCacheUrl(OkHttp.convertSchemeToHttps(mMediaPath)) : mMediaPath;
        mVideoView.play(url, true);

        mLayoutVideoController.setVisibility(VISIBLE);
    }

    @Override
    public void onPrepared(IMediaPlayer mediaPlayer) {
        updateVideoVolume();
    }

    @Override
    public void onLoading(IMediaPlayer mediaPlayer, boolean isLoading) {

    }

    @Override
    public void onCompletion(IMediaPlayer mediaPlayer) {

    }

    @Override
    public void onError(IMediaPlayer mediaPlayer, int err, String msg) {

    }

    @Override
    public void onTimeUpdate(IMediaPlayer mediaPlayer, int currentTime, int totalTime) {

    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mediaPlayer, int width, int height, float dar) {
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mMediaPlayer = mp;
        mp.setLooping(true);

        if (mAutoPlay) {
            mVideoView.start();
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        if (what == MEDIA_INFO_VIDEO_RENDERING_START) {
            mPhotoView.setAlpha(0f);
            mVideoView.setAlpha(1);
            mLayoutVideoController.setAlpha(1);
            mLayoutVideoController.attachTo(this);
            mLayoutVideoController.reset();
            mLayoutVideoController.startUpdateRunnable();
        }
        return false;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        if (isWebPath()) {
            showVideo();
        }
        return true;
    }

    public void updateVideoVolume() {
        if (mVideoView != null && mVideoView.mPlayer != null) {
            int volume = isVideoSilent() ? 0 : 1;
            mVideoView.mPlayer..setVolume(volume, volume);
            mVideoView.setShouldRequestAudioFocus(!isVideoSilent());
        }
    }

    public void setVideoSilent(boolean silent) {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .edit().putBoolean(Constants.VIDEO_SILENT, silent).apply();
    }

    public boolean isVideoSilent() {
        return PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(Constants.VIDEO_SILENT, false);
    }


    /***********************************  Other  ***********************************/
    public void reset() {
        mPhotoView.setScale(1f);
        mVideoView.setKeepScreenOn(false);
        mVideoView.stopPlayback();
        mVideoView.setVisibility(GONE);
        mLayoutVideoController.reset();
        mLayoutVideoController.setVisibility(GONE);
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

    // FullscreenActivity翻页后收到的通知，obj 为 [imageUrl, controllerVisible]
    @Subscribe
    public void startVideo(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.START_VIDEO)) {
            String url = (String) ((Object[]) msgBean.obj)[0];
            if (TextUtils.equals(url, mMediaPath)) {
                setMediaPath(url);
                if (FileUtils.isVideoType(url)) {
                    int visibility = (int) ((Object[]) msgBean.obj)[1];
                    mLayoutVideoController.setVisibility(visibility);
                }
            }
        }
    }

    // FullscreenActivity触发onResume()后收到的通知，obj 为 image url
    @Subscribe
    public void resumeVideo(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.RESUME_VIDEO)) {
            String url = (String) msgBean.obj;
            if (!TextUtils.isEmpty(mMediaPath) && TextUtils.equals(url, mMediaPath) && FileUtils.isVideoType(url)) {
                mVideoView.start();
            }
        }
    }

    // FullscreenActivity触发onPause()后收到的通知，obj 为 image url
    @Subscribe
    public void pauseVideo(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.PAUSE_VIDEO)) {
            String url = (String) msgBean.obj;
            if (!TextUtils.isEmpty(mMediaPath) && TextUtils.equals(url, mMediaPath) && FileUtils.isVideoType(url)) {
                mVideoView.pause();
            }
        }
    }

    // FullscreenActivity单击页面后收到的通知，obj 为 visibility
    @Subscribe
    public void toggleVideoController(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.TOGGLE_VIDEO_CONTROLLER)) {
            if (FileUtils.isVideoType(mMediaPath)) {
                int visibility = (int) msgBean.obj;
                mLayoutVideoController.setVisibility(visibility);
            }
        }
    }

}
