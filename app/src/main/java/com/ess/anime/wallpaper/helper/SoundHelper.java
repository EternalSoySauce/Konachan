package com.ess.anime.wallpaper.helper;

import android.content.Context;
import android.media.MediaPlayer;

import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.R;

public class SoundHelper {

    private static class SoundHolder {
        private static final SoundHelper instance = new SoundHelper();
    }

    private MediaPlayer mMediaPlayer;

    private SoundHelper() {
    }

    public static SoundHelper getInstance() {
        return SoundHolder.instance.createPlayerIfNull();
    }

    private SoundHelper createPlayerIfNull() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
        }
        return this;
    }

    // 允许播放声音
    public void playSoundEnabled(Context context) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
        mMediaPlayer = MediaPlayer.create(context, R.raw.allow_play_sound);
        mMediaPlayer.start();
    }

    // 禁止播放声音
    public void playSoundDisabled() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
    }

    // 重启应用后在Splash页面播放
    public void playSplashWelcomeSound(Context context) {
        if (Constants.sRestart && Constants.sAllowPlaySound) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            mMediaPlayer = MediaPlayer.create(context, R.raw.welcome);
            mMediaPlayer.start();
            Constants.sRestart = false;
        }
    }

    // 切换到R18模式播放
    public void playToggleR18ModeSound(Context context) {
        if (Constants.sAllowPlaySound) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            mMediaPlayer = MediaPlayer.create(context, R.raw.toggle_r18_mode);
            mMediaPlayer.start();
        }
    }

    // 切换到Safe模式播放
    public void playToggleSafeModeSound(Context context) {
        if (Constants.sAllowPlaySound) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            mMediaPlayer = MediaPlayer.create(context, R.raw.toggle_safe_mode);
            mMediaPlayer.start();
        }
    }

    // 网络异常时播放
    public void playLoadNoNetworkSound(Context context) {
        if (Constants.sAllowPlaySound && mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer = MediaPlayer.create(context, R.raw.load_no_network);
            mMediaPlayer.start();
        }
    }

    // 搜索无结果时播放
    public void playLoadNothingSound(Context context) {
        if (Constants.sAllowPlaySound && mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer = MediaPlayer.create(context, R.raw.load_nothing);
            mMediaPlayer.start();
        }
    }

    // 游戏胜利时播放
    public void playGameWinSound(Context context) {
        if (Constants.sAllowPlaySound) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            mMediaPlayer = MediaPlayer.create(context, R.raw.game_win);
            mMediaPlayer.start();
        }
    }


    // 退出应用时释放player
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
