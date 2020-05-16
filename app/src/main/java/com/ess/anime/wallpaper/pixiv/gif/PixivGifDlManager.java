package com.ess.anime.wallpaper.pixiv.gif;

import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.pixiv.login.PixivLoginManager;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.utils.IOUtils;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nl.bravobit.ffmpeg.ExecuteBinaryResponseHandler;
import nl.bravobit.ffmpeg.FFmpeg;

public class PixivGifDlManager {

    private static class PixivGifHolder {
        private final static PixivGifDlManager instance = new PixivGifDlManager();
    }

    public static PixivGifDlManager getInstance() {
        return PixivGifHolder.instance;
    }

    private PixivGifDlManager() {
    }

    /*******************************************************************/

    private final LinkedHashMap<String, PixivGifBean> mPixivMap = new LinkedHashMap<>();

    public void execute(String pixivId) {
        synchronized (mPixivMap) {
            PixivGifBean pixivGifBean = mPixivMap.get(pixivId);
            if (pixivGifBean == null) {
                pixivGifBean = new PixivGifBean(pixivId);
                mPixivMap.put(pixivId, pixivGifBean);
                notifyDataAdded(pixivGifBean);
                parseJson(pixivGifBean);
            } else if (pixivGifBean.isError) {
                parseJson(pixivGifBean);
            } else {
                // todo 翻译
                Toast.makeText(MyApp.getInstance(), "任务已存在于队列中", Toast.LENGTH_SHORT).show();
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
            Map<String, String> headerMap = null;
            String cookie = PixivLoginManager.getInstance().getCookie();
            if (!TextUtils.isEmpty(cookie)) {
                headerMap = new LinkedHashMap<>();
                headerMap.put("cookie", cookie);
            }
            OkHttp.connect(url, pixivGifBean.id, headerMap, new OkHttp.OkHttpCallback() {
                @Override
                public void onFailure() {
                    pixivGifBean.isError = true;
                    notifyDataChanged(pixivGifBean);
                }

                @Override
                public void onSuccessful(String json) {
                    try {
                        JsonObject result = new JsonParser().parse(json).getAsJsonObject();
                        if (result.has("error")) {
                            boolean isError = result.get("error").getAsBoolean();
                            if (!isError && result.has("body")) {
                                JsonObject body = result.getAsJsonObject("body");
                                pixivGifBean.zipUrl = body.get("originalSrc").getAsString();
                                float delay = body.getAsJsonArray("frames")
                                        .get(0).getAsJsonObject()
                                        .get("delay").getAsFloat();
                                pixivGifBean.fps = 1000f / delay;
                                downloadZip(pixivGifBean);
                                return;
                            } else if (isError && result.has("message")) {
                                String message = result.get("message").getAsString();
                                if (message != null && message.contains("您所指定的ID不是动图")) {
                                    pixivGifBean.state = PixivGifBean.PixivDlState.NOT_GIF;
                                    pixivGifBean.isError = true;
                                    notifyDataChanged(pixivGifBean);
                                    return;
                                } else if (message != null && message.contains("该作品已被删除，或作品ID不存在")) {
                                    pixivGifBean.state = PixivGifBean.PixivDlState.NEED_LOGIN;
                                    pixivGifBean.isError = true;
                                    notifyDataChanged(pixivGifBean);
                                    return;
                                }
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

            String dirPath = pixivGifBean.getZipCacheDirPath();
            String fileName = pixivGifBean.getZipFileName();
            OkGo.<File>get(pixivGifBean.zipUrl)
                    .tag(pixivGifBean.id)
                    .headers("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit / 537.36(KHTML, like Gecko) Chrome  47.0.2526.106 Safari / 537.36")
                    .headers("Referer", pixivGifBean.getRefererUrl())
                    .execute(new FileCallback(dirPath, fileName) {
                        @Override
                        public void onStart(com.lzy.okgo.request.base.Request<File, ? extends com.lzy.okgo.request.base.Request> request) {
                            super.onStart(request);
                            IOUtils.delFileOrFolder(dirPath);
                        }

                        @Override
                        public void onSuccess(Response<File> response) {
                            File file = response.body();
                            extractZip(pixivGifBean, file);
                        }

                        @Override
                        public void onError(Response<File> response) {
                            super.onError(response);
                            pixivGifBean.isError = true;
                            notifyDataChanged(pixivGifBean);
                            IOUtils.delFileOrFolder(dirPath);
                        }

                        @Override
                        public void onFinish() {
                            super.onFinish();
                        }

                        @Override
                        public void downloadProgress(Progress progress) {
                            super.downloadProgress(progress);
                            pixivGifBean.progress = progress.fraction;
                            notifyDataChanged(pixivGifBean);
                        }
                    });
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
                pixivGifBean.isError = true;
                notifyDataChanged(pixivGifBean);
                if (pixivGifBean.gifTask != null) {
                    pixivGifBean.gifTask.sendQuitSignal();
                }
                IOUtils.delFileOrFolder(pixivGifBean.getZipCacheDirPath());
            }
            OkHttp.cancel(pixivId);
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

    public List<PixivGifBean> getDownloadList() {
        synchronized (mPixivMap) {
            return new ArrayList<>(mPixivMap.values());
        }
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
