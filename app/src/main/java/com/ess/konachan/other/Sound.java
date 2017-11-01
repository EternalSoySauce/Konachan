package com.ess.konachan.other;

import android.content.Context;
import android.media.MediaPlayer;

import com.ess.konachan.R;
import com.ess.konachan.global.Constants;

public class Sound {

    private static class SoundHolder {
        private static final Sound instance = new Sound();
    }

    private MediaPlayer mMediaPlayer;

    private Sound() {
    }

    public static Sound getInstance() {
        return SoundHolder.instance;
    }

    // 重启应用后在Splash页面播放
    public void playSplashWelcomeSound(Context context) {
        if (Constants.sRestart) {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            mMediaPlayer = MediaPlayer.create(context, R.raw.welcome);
            mMediaPlayer.start();
            Constants.sRestart = false;
        }
    }

    // 切换到R18模式播放
    public void playHentaiSound(Context context) {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
        mMediaPlayer = MediaPlayer.create(context, R.raw.hentai);
        mMediaPlayer.start();
    }

    // 退出应用时释放player
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
