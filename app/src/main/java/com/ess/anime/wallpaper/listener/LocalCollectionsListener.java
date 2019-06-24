package com.ess.anime.wallpaper.listener;

import android.os.FileObserver;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.FileUtils;

import java.io.File;

// 监听收藏夹sd卡文件变动
public class LocalCollectionsListener extends FileObserver {

    private static final int EVENTS = CREATE | MOVED_TO | DELETE | DELETE_SELF | MOVED_FROM | MOVE_SELF;

    private OnFilesChangedListener mListener;
    private Handler mHandler = new Handler();

    public LocalCollectionsListener(OnFilesChangedListener listener) {
        super(Constants.IMAGE_DIR, EVENTS);
        mListener = listener;
    }

    @Override
    public void startWatching() {
        super.startWatching();
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        if (path == null)
            return;

        File file = new File(Constants.IMAGE_DIR, path);
        if (file.isDirectory() || !FileUtils.isMediaType(path))
            return;

        switch (event) {
            case CREATE:
            case MOVED_TO:
                mHandler.post(() -> mListener.onFileAdded(file));
                break;

            case DELETE:
            case DELETE_SELF:
            case MOVED_FROM:
            case MOVE_SELF:
                mHandler.post(() -> mListener.onFileRemoved(file));
                break;
        }
    }

    public interface OnFilesChangedListener {
        void onFileAdded(File file);

        void onFileRemoved(File file);
    }
}
