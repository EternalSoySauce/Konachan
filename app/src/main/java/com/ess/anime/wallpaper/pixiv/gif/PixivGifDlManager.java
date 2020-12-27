package com.ess.anime.wallpaper.pixiv.gif;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.utils.IOUtils;
import com.lzy.okserver.download.DownloadListener;
import com.unity3d.services.core.connectivity.ConnectivityChangeReceiver;
import com.unity3d.services.core.connectivity.ConnectivityMonitor;
import com.unity3d.services.core.connectivity.IConnectivityListener;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;

public class PixivGifDlManager implements IConnectivityListener {

    public final static String TAG = PixivGifDlManager.class.getSimpleName();

    private static class PixivGifHolder {
        private final static PixivGifDlManager instance = new PixivGifDlManager();
    }

    public static PixivGifDlManager getInstance() {
        return PixivGifHolder.instance;
    }

    private PixivGifDlManager() {
        ConnectivityChangeReceiver.register();
        ConnectivityMonitor.addListener(this);
    }

    /*******************************************************************/

    private final LinkedHashMap<String, PixivGifBean> mPixivMap = new LinkedHashMap<>();

    public void execute(String pixivId) {
        synchronized (mPixivMap) {
            PixivGifBean pixivGifBean = mPixivMap.get(pixivId);
            if (pixivGifBean == null) {
                OkHttp.cancelDownloadFile(TAG + pixivId);
                pixivGifBean = new PixivGifBean(pixivId);
                mPixivMap.put(pixivId, pixivGifBean);
                notifyDataAdded(pixivGifBean);
                parseJson(pixivGifBean);
            } else if (pixivGifBean.isError) {
                switch (pixivGifBean.state) {
                    case DOWNLOAD_ZIP:
                    case EXTRACT_ZIP:
                    case MAKE_GIF:
                        downloadZip(pixivGifBean);
                        break;
                    default:
                        parseJson(pixivGifBean);
                }
            } else {
                Toast.makeText(MyApp.getInstance(), R.string.pixiv_toast_task_exist, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void parseJson(PixivGifBean pixivGifBean) {
        synchronized (mPixivMap) {
            if (!mPixivMap.containsKey(pixivGifBean.id)) {
                return;
            }

            pixivGifBean.state = PixivGifBean.PixivDlState.CONNECT_PIXIV;
            pixivGifBean.progress = 0;
            pixivGifBean.isError = false;
            notifyDataChanged(pixivGifBean);

            String url = pixivGifBean.getJsonUrl();
            OkHttp.connect(url, TAG + pixivGifBean.id, new OkHttp.OkHttpCallback() {
                @Override
                public void onFailure() {
                    pixivGifBean.isError = true;
                    notifyDataChanged(pixivGifBean);
                }

                @Override
                public void onSuccessful(String json) {
                    try {
                        JsonObject result = new JsonParser().parse(json).getAsJsonObject();
                        if (result.has("status")) {
                            boolean isSuccess = TextUtils.equals("success", result.get("status").getAsString());
                            if (isSuccess) {
                                JsonObject detail = result.getAsJsonArray("response").get(0).getAsJsonObject();
                                if (detail.has("image_urls")) {
                                    JsonObject imageUrls = detail.getAsJsonObject("image_urls");
                                    for (Map.Entry<String, JsonElement> entry : imageUrls.entrySet()) {
                                        if (entry != null && entry.getValue() != null) {
                                            String thumbUrl = entry.getValue().getAsString();
                                            if (!TextUtils.isEmpty(thumbUrl)) {
                                                pixivGifBean.thumbUrl = thumbUrl;
                                                break;
                                            }
                                        }
                                    }
                                }
                                String type = detail.get("type").getAsString();
                                if (TextUtils.equals(type, "ugoira") && detail.get("metadata") != null) {
                                    JsonObject metadata = detail.getAsJsonObject("metadata");
                                    JsonObject zipUrls = metadata.getAsJsonObject("zip_urls");
                                    for (Map.Entry<String, JsonElement> entry : zipUrls.entrySet()) {
                                        pixivGifBean.zipUrl = entry.getValue().getAsString();
                                    }
                                    float delay = metadata.getAsJsonArray("frames")
                                            .get(0).getAsJsonObject()
                                            .get("delay_msec").getAsFloat();
                                    pixivGifBean.fps = 1000f / delay;
                                    downloadZip(pixivGifBean);
                                    return;
                                } else {
                                    pixivGifBean.state = PixivGifBean.PixivDlState.NOT_GIF;
                                    pixivGifBean.isError = true;
                                    notifyDataChanged(pixivGifBean);
                                    return;
                                }
                            } else {
                                pixivGifBean.state = PixivGifBean.PixivDlState.NEED_LOGIN;
                                pixivGifBean.isError = true;
                                notifyDataChanged(pixivGifBean);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    onFailure();
                }
            }, Request.Priority.IMMEDIATE);
        }
    }

    private void downloadZip(PixivGifBean pixivGifBean) {
        synchronized (mPixivMap) {
            if (!mPixivMap.containsKey(pixivGifBean.id)) {
                return;
            }

            pixivGifBean.state = PixivGifBean.PixivDlState.DOWNLOAD_ZIP;
            pixivGifBean.progress = 0;
            pixivGifBean.isError = false;
            notifyDataChanged(pixivGifBean);

            try {
                String dirPath = pixivGifBean.getZipCacheDirPath();
                String fileName = pixivGifBean.getZipFileName();
                Map<String, String> headerMap = new LinkedHashMap<>();
                headerMap.put("User-Agent", OkHttp.USER_AGENT);
                headerMap.put("Referer", pixivGifBean.getRefererUrl());
                OkHttp.startDownloadFile(pixivGifBean.zipUrl, dirPath, fileName, headerMap,
                        new DownloadListener(TAG + pixivGifBean.id) {
                            @Override
                            public void onStart(Progress progress) {
                            }

                            @Override
                            public void onProgress(Progress progress) {
                                pixivGifBean.progress = progress.fraction;
                                notifyDataChanged(pixivGifBean);
                            }

                            @Override
                            public void onError(Progress progress) {
                                pixivGifBean.isError = true;
                                notifyDataChanged(pixivGifBean);
                            }

                            @Override
                            public void onFinish(File file, Progress progress) {
                                extractZip(pixivGifBean, file);
                            }

                            @Override
                            public void onRemove(Progress progress) {
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
                pixivGifBean.isError = true;
                notifyDataChanged(pixivGifBean);
            }
        }
    }

    private void extractZip(PixivGifBean pixivGifBean, File file) {
        synchronized (mPixivMap) {
            if (!mPixivMap.containsKey(pixivGifBean.id)) {
                return;
            }

            pixivGifBean.state = PixivGifBean.PixivDlState.EXTRACT_ZIP;
            pixivGifBean.progress = 0;
            pixivGifBean.isError = false;
            notifyDataChanged(pixivGifBean);

            String dirPath = pixivGifBean.getZipCacheDirPath();
            try {
                ZipFile zipFile = new ZipFile(file);
                zipFile.extractAll(dirPath);
                makeGif(pixivGifBean);
            } catch (Exception e) {
                pixivGifBean.isError = true;
                notifyDataChanged(pixivGifBean);
                IOUtils.delFileOrFolder(dirPath);
                e.printStackTrace();
            } finally {
                IOUtils.delFileOrFolder(file);
            }
        }
    }

    private void makeGif(PixivGifBean pixivGifBean) {
        synchronized (mPixivMap) {
            if (!mPixivMap.containsKey(pixivGifBean.id)) {
                return;
            }

            pixivGifBean.state = PixivGifBean.PixivDlState.MAKE_GIF;
            pixivGifBean.progress = 0;
            pixivGifBean.isError = false;
            notifyDataChanged(pixivGifBean);

            String dirPath = pixivGifBean.getZipCacheDirPath();
            File[] images = new File(dirPath).listFiles((dir1, name) -> FileUtils.isImageType(name));

            if (images.length == 0) {
                pixivGifBean.isError = true;
                notifyDataChanged(pixivGifBean);
                return;
            }

            String imagePath = images[0].getAbsolutePath();
            String extension = FileUtils.getFileExtensionWithDot(imagePath);
            String imageName = images[0].getName().replace(extension, "");
            String inputPath = dirPath + "/%" + imageName.length() + "d" + extension;
            String outputPath = pixivGifBean.getGifSavedPath();
            String fps = String.valueOf(pixivGifBean.fps);

            String[] cmd = new String[]{
                    "-r", fps, "-i", inputPath,
                    "-r", fps, "-y", "-f", "gif", outputPath,
            };

            FFmpeg ffmpeg = FFmpeg.getInstance(MyApp.getInstance());
            pixivGifBean.gifTask = ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
                }

                @Override
                public void onProgress(String message) {
//                    float progress = 0;
//                    pixivGifBean.progress = progress;
//                    notifyDataChanged(pixivGifBean);
                }

                @Override
                public void onFailure(String message) {
                    pixivGifBean.isError = true;
                    notifyDataChanged(pixivGifBean);
                }

                @Override
                public void onSuccess(String message) {
                    pixivGifBean.state = PixivGifBean.PixivDlState.FINISH;
                    pixivGifBean.progress = 0;
                    pixivGifBean.isError = false;
                    notifyDataChanged(pixivGifBean);
                    BitmapUtils.insertToMediaStore(MyApp.getInstance(), new File(outputPath));
                }

                @Override
                public void onFinish() {
                    IOUtils.delFileOrFolder(dirPath);
                }
            });
        }
    }

    public void cancel(String pixivId) {
        synchronized (mPixivMap) {
            PixivGifBean pixivGifBean = mPixivMap.get(pixivId);
            if (pixivGifBean != null) {
                pixivGifBean.state = PixivGifBean.PixivDlState.CANCEL;
                pixivGifBean.progress = 0;
                pixivGifBean.isError = true;
                notifyDataChanged(pixivGifBean);
                if (pixivGifBean.gifTask != null) {
                    pixivGifBean.gifTask.sendQuitSignal();
                }
                IOUtils.delFileOrFolder(pixivGifBean.getZipCacheDirPath());
            }
            OkHttp.cancelDownloadFile(TAG + pixivId);
        }
    }

    public void delete(String pixivId) {
        synchronized (mPixivMap) {
            cancel(pixivId);
            PixivGifBean pixivGifBean = mPixivMap.remove(pixivId);
            if (pixivGifBean != null) {
                notifyDataRemoved(pixivGifBean);
            }
        }
    }

    public void clearAllFinished() {
        synchronized (mPixivMap) {
            Iterator<Map.Entry<String, PixivGifBean>> iterator = mPixivMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, PixivGifBean> entry = iterator.next();
                PixivGifBean pixivGifBean = entry.getValue();
                if (pixivGifBean.state == PixivGifBean.PixivDlState.FINISH) {
                    iterator.remove();
                    notifyDataRemoved(pixivGifBean);
                }
            }
        }
    }

    public List<PixivGifBean> getDownloadList() {
        synchronized (mPixivMap) {
            return new ArrayList<>(mPixivMap.values());
        }
    }


    /*******************************************************************/

    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onConnected() {
        synchronized (mPixivMap) {
            mMainHandler.post(() -> {
                // 网络可用时恢复所有断点下载
                for (Map.Entry<String, PixivGifBean> entry : mPixivMap.entrySet()) {
                    String pixivId = entry.getKey();
                    PixivGifBean pixivGifBean = entry.getValue();
                    if (pixivGifBean.isError && (pixivGifBean.state == PixivGifBean.PixivDlState.CONNECT_PIXIV
                            || pixivGifBean.state == PixivGifBean.PixivDlState.DOWNLOAD_ZIP)) {
                        execute(pixivId);
                    }
                }
            });
        }
    }

    @Override
    public void onDisconnected() {
    }


    /*******************************************************************/
    private final List<IPixivDlListener> mListenerList = new ArrayList<>();

    public void addListener(IPixivDlListener listener) {
        synchronized (mListenerList) {
            if (!mListenerList.contains(listener)) {
                mListenerList.add(listener);
            }
        }
    }

    public void removeListener(IPixivDlListener listener) {
        synchronized (mListenerList) {
            mListenerList.remove(listener);
        }
    }

    public void notifyDataAdded(PixivGifBean pixivGifBean) {
        synchronized (mListenerList) {
            for (IPixivDlListener listener : mListenerList) {
                listener.onDataAdded(pixivGifBean);
            }
        }
    }

    public void notifyDataRemoved(PixivGifBean pixivGifBean) {
        synchronized (mListenerList) {
            for (IPixivDlListener listener : mListenerList) {
                listener.onDataRemoved(pixivGifBean);
            }
        }
    }

    public void notifyDataChanged(PixivGifBean pixivGifBean) {
        synchronized (mListenerList) {
            for (IPixivDlListener listener : mListenerList) {
                listener.onDataChanged(pixivGifBean);
            }
        }
    }
}
