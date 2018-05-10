package com.ess.anime.wallpaper.listener;

import android.os.FileObserver;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.ess.anime.wallpaper.global.Constants;

import java.io.File;

// 监听收藏夹sd卡文件变动
public class LocalCollectionsListener extends FileObserver {

    private OnFilesChangedListener mListener;
    private Handler mHandler;

    public LocalCollectionsListener(OnFilesChangedListener listener) {
        super(Constants.IMAGE_DIR);
        mListener = listener;
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        if (path == null)
            return;

        File file = new File(Constants.IMAGE_DIR, path);
        if (file.isDirectory())
            return;

        switch (event) {
            case CREATE:
            case MOVED_TO:
                mListener.onFileAdded(file);
                break;

            case DELETE:
            case DELETE_SELF:
            case MOVED_FROM:
            case MOVE_SELF:
                mListener.onFileRemoved(file);
                break;
        }
    }

    public interface OnFilesChangedListener {
        void onFileAdded(File file);

        void onFileRemoved(File file);
    }
}
