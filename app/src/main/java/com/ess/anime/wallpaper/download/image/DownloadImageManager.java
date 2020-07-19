package com.ess.anime.wallpaper.download.image;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.lzy.okgo.model.Progress;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadTask;
import com.unity3d.services.core.connectivity.ConnectivityChangeReceiver;
import com.unity3d.services.core.connectivity.ConnectivityMonitor;
import com.unity3d.services.core.connectivity.IConnectivityListener;

import java.util.ArrayList;
import java.util.List;

public class DownloadImageManager implements IConnectivityListener {

    private static class DownloadImageHolder {
        private final static DownloadImageManager instance = new DownloadImageManager();
    }

    public static DownloadImageManager getInstance() {
        return DownloadImageHolder.instance;
    }

    private DownloadImageManager() {
        ConnectivityChangeReceiver.register();
        ConnectivityMonitor.addListener(this);
    }

    /*******************************************************************/

    private final List<DownloadBean> mDownloadList = new ArrayList<>();

    public void addOrUpdate(DownloadBean downloadBean) {
        synchronized (mDownloadList) {
            int index = mDownloadList.indexOf(downloadBean);
            if (index == -1) {
                mDownloadList.add(downloadBean);
                notifyDataAdded(downloadBean);
            } else {
                mDownloadList.set(index, downloadBean);
                notifyDataChanged(downloadBean);
            }
        }
    }

    public void remove(DownloadBean downloadBean) {
        synchronized (mDownloadList) {
            if (mDownloadList.remove(downloadBean)) {
                notifyDataRemoved(downloadBean);
            }
        }
    }

    public List<DownloadBean> getDownloadList() {
        synchronized (mDownloadList) {
            return new ArrayList<>(mDownloadList);
        }
    }


    /*******************************************************************/

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onConnected() {
        synchronized (mDownloadList) {
            mMainHandler.post(() -> {
                // 网络可用时恢复所有断点下载
                for (DownloadBean downloadBean : mDownloadList) {
                    String tag = downloadBean.downloadUrl;
                    DownloadTask task = OkDownload.getInstance().getTask(tag);
                    if (task != null && task.progress.status == Progress.ERROR) {
                        if (!OkHttp.isUrlInDownloadQueue(downloadBean.downloadUrl)) {
                            Context context = MyApp.getInstance();
                            Intent downloadIntent = new Intent(context, DownloadImageService.class);
                            downloadIntent.putExtra(Constants.DOWNLOAD_BEAN, downloadBean);
                            context.startService(downloadIntent);
                            OkHttp.addUrlToDownloadQueue(downloadBean.downloadUrl);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDisconnected() {
    }


    /*******************************************************************/
    private final List<IDownloadImageListener> mListenerList = new ArrayList<>();

    public void addListener(IDownloadImageListener listener) {
        synchronized (mListenerList) {
            if (!mListenerList.contains(listener)) {
                mListenerList.add(listener);
            }
        }
    }

    public void removeListener(IDownloadImageListener listener) {
        synchronized (mListenerList) {
            mListenerList.remove(listener);
        }
    }

    public void notifyDataAdded(DownloadBean downloadBean) {
        synchronized (mListenerList) {
            for (IDownloadImageListener listener : mListenerList) {
                listener.onDataAdded(downloadBean);
            }
        }
    }

    public void notifyDataRemoved(DownloadBean downloadBean) {
        synchronized (mListenerList) {
            for (IDownloadImageListener listener : mListenerList) {
                listener.onDataRemoved(downloadBean);
            }
        }
    }

    public void notifyDataChanged(DownloadBean downloadBean) {
        synchronized (mListenerList) {
            for (IDownloadImageListener listener : mListenerList) {
                listener.onDataChanged(downloadBean);
            }
        }
    }
}
