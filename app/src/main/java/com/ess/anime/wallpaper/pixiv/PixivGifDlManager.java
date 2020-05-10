package com.ess.anime.wallpaper.pixiv;

import com.android.volley.Request;
import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.http.OkHttp;
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

    private final LinkedHashMap<String, PixivBean> mPixivMap = new LinkedHashMap<>();

    public void execute(String pixivId) {
        synchronized (mPixivMap) {
            PixivBean pixivBean = mPixivMap.get(pixivId);
            if (pixivBean == null) {
                pixivBean = new PixivBean(pixivId);
                mPixivMap.put(pixivId, pixivBean);
                parseJson(pixivBean);
            }
        }
    }

    private void parseJson(PixivBean pixivBean) {
        synchronized (mPixivMap) {
            if (!mPixivMap.containsKey(pixivBean.id)) {
                return;
            }

            String url = pixivBean.getJsonUrl();
            OkHttp.connect(url, pixivBean.id, new OkHttp.OkHttpCallback() {
                @Override
                public void onFailure() {
//                showDialog("P站访问失败", true);
                }

                @Override
                public void onSuccessful(String json) {
                    try {
                        JsonObject body = new JsonParser().parse(json)
                                .getAsJsonObject()
                                .getAsJsonObject("body");
                        pixivBean.zipUrl = body.get("originalSrc").getAsString();
                        float delay = body.getAsJsonArray("frames")
                                .get(0).getAsJsonObject()
                                .get("delay").getAsFloat();
                        pixivBean.fps = 1000f / delay;
                        downloadZip(pixivBean);
//                    showDialog("开始下载压缩包", false);
                    } catch (Exception e) {
                        e.printStackTrace();
//                    showDialog("不是gif", true);
                    }
                }
            }, Request.Priority.IMMEDIATE);
        }
    }

    private void downloadZip(PixivBean pixivBean) {
        synchronized (mPixivMap) {
            if (!mPixivMap.containsKey(pixivBean.id)) {
                return;
            }

            String dirPath = pixivBean.getZipCacheDirPath();
            String fileName = pixivBean.getZipFileName();
            OkGo.<File>get(pixivBean.zipUrl)
                    .tag(pixivBean.id)
                    .headers("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit / 537.36(KHTML, like Gecko) Chrome  47.0.2526.106 Safari / 537.36")
                    .headers("Referer", pixivBean.getRefererUrl())
                    .execute(new FileCallback(dirPath, fileName) {
                        @Override
                        public void onSuccess(Response<File> response) {
                            File file = response.body();
                            try {
                                ZipFile zipFile = new ZipFile(file);
                                zipFile.extractAll(dirPath);
//                            showDialog("解压完毕", true);
                                makeGif(pixivBean);
                            } catch (Exception e) {
                                IOUtils.delFileOrFolder(dirPath);
//                            showDialog("解压失败", true);
                                e.printStackTrace();
                            } finally {
                                IOUtils.delFileOrFolder(file);
                            }
                        }

                        @Override
                        public void onError(Response<File> response) {
                            super.onError(response);
                            IOUtils.delFileOrFolder(dirPath);
//                        showDialog("下载失败", true);
                        }

                        @Override
                        public void onFinish() {
                            super.onFinish();
                        }

                        @Override
                        public void downloadProgress(Progress progress) {
                            super.downloadProgress(progress);
                            String content = "进度: " + ((int) (progress.fraction * 100))
                                    + ", 已下载: " + FileUtils.computeFileSize(progress.currentSize)
                                    + ", 速度: " + FileUtils.computeFileSize(progress.speed) + "/s";
//                        showDialog(content, false);
                        }
                    });
        }
    }

    private void makeGif(PixivBean pixivBean) {
        synchronized (mPixivMap) {
            if (!mPixivMap.containsKey(pixivBean.id)) {
                return;
            }

            String dirPath = pixivBean.getZipCacheDirPath();
            File[] images = new File(dirPath).listFiles((dir1, name) -> FileUtils.isImageType(name));

            if (images.length == 0) {
//            showDialog("图片不存在", true);
                return;
            }

            String imagePath = images[0].getAbsolutePath();
            String extension = FileUtils.getFileExtensionWithDot(imagePath);
            String imageName = images[0].getName().replace(extension, "");
            String inputPath = dirPath + "/%" + imageName.length() + "d" + extension;
            String outputPath = pixivBean.getGifSavedPath();
            String fps = String.valueOf(pixivBean.fps);

            String[] cmd = new String[]{
                    "-r", fps, "-i", inputPath,
                    "-r", fps, "-y", "-f", "gif", outputPath,
            };

            FFmpeg ffmpeg = FFmpeg.getInstance(MyApp.getInstance());
            pixivBean.gifTask = ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onStart() {
//                showDialog("开始合成", false);
                }

                @Override
                public void onProgress(String message) {
//                showDialog("合成进度：" + message, false);
                }

                @Override
                public void onFailure(String message) {
//                showDialog("合成失败 " + message, true);
                }

                @Override
                public void onSuccess(String message) {
//                showDialog("合成成功 " + message, true);
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
            PixivBean pixivBean = mPixivMap.get(pixivId);
            if (pixivBean != null && pixivBean.gifTask != null) {
                pixivBean.gifTask.sendQuitSignal();
            }
            OkHttp.cancel(pixivId);
        }
    }

    public void delete(String pixivId) {
        synchronized (mPixivMap) {
            cancel(pixivId);
            mPixivMap.remove(pixivId);
        }
    }

    public List<PixivBean> getDownloadList() {
        synchronized (mPixivMap) {
            return new ArrayList<>(mPixivMap.values());
        }
    }

}
