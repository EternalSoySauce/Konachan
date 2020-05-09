package com.ess.anime.wallpaper.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.model.helper.PermissionHelper;
import com.ess.anime.wallpaper.utils.BitmapUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.utils.IOUtils;
import com.yanzhenjie.permission.runtime.Permission;

import net.lingala.zip4j.ZipFile;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;
import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;

public class PixivGifActivity extends BaseActivity {

    public final static String TAG = PixivGifActivity.class.getName();

    @BindView(R.id.et_id)
    EditText mEtId;

    private MaterialDialog mDialog;

    @Override
    int layoutRes() {
        return R.layout.activity_pixiv_gif;
    }

    @Override
    void init(Bundle savedInstanceState) {
        PermissionHelper.checkStoragePermissions(this, new PermissionHelper.SimpleRequestListener() {
            @Override
            public void onDenied() {
                finish();
            }
        });
    }

    @OnClick(R.id.btn_download)
    void startDownload() {
        String pixivId = mEtId.getText().toString();
        if (!TextUtils.isEmpty(pixivId)) {
            showDialog("正在连接P站", false);
            String url = "https://www.pixiv.net/ajax/illust/" + pixivId + "/ugoira_meta?lang=zh";
            OkHttp.connect(url, TAG, new OkHttp.OkHttpCallback() {
                @Override
                public void onFailure() {
                    showDialog("P站访问失败", true);
                }

                @Override
                public void onSuccessful(String json) {
                    try {
                        JsonObject body = new JsonParser().parse(json)
                                .getAsJsonObject()
                                .getAsJsonObject("body");
                        String zipUrl = body.get("originalSrc").getAsString();
                        float delay = body.getAsJsonArray("frames")
                                .get(0).getAsJsonObject()
                                .get("delay").getAsFloat();
                        float fps = 1000f / delay;
                        downloadZip(pixivId, String.valueOf(fps), zipUrl);
                        showDialog("开始下载压缩包", false);
                    } catch (Exception e) {
                        e.printStackTrace();
                        showDialog("不是gif", true);
                    }
                }
            }, Request.Priority.IMMEDIATE);
        }
    }

    private void downloadZip(String pixivId, String fps, String zipUrl) {
        String dirPath = new File(getCacheDir(), pixivId).getAbsolutePath();
        String fileName = pixivId + ".zip";
        OkGo.<File>get(zipUrl)
                .headers("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit / 537.36(KHTML, like Gecko) Chrome  47.0.2526.106 Safari / 537.36")
                .headers("Referer", "https://www.pixiv.net/artworks/" + pixivId)
                .execute(new FileCallback(dirPath, fileName) {
                    @Override
                    public void onSuccess(Response<File> response) {
                        File file = response.body();
                        try {
                            ZipFile zipFile = new ZipFile(file);
                            zipFile.extractAll(dirPath);
                            showDialog("解压完毕", true);
                            makeGif(pixivId, fps, dirPath);
                        } catch (Exception e) {
                            IOUtils.delFileOrFolder(dirPath);
                            showDialog("解压失败", true);
                            e.printStackTrace();
                        } finally {
                            IOUtils.delFileOrFolder(file);
                        }
                    }

                    @Override
                    public void onError(Response<File> response) {
                        super.onError(response);
                        IOUtils.delFileOrFolder(dirPath);
                        showDialog("下载失败", true);
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
                        showDialog(content, false);
                    }
                });
    }

    private void makeGif(String pixivId, String fps, String dirPath) {
        File dir = new File(dirPath);
        File[] images = dir.listFiles((dir1, name) -> FileUtils.isImageType(name));

        if (images.length == 0) {
            showDialog("图片不存在", true);
            return;
        }

        String imagePath = images[0].getAbsolutePath();
        String extension = FileUtils.getFileExtensionWithDot(imagePath);
        String imageName = images[0].getName().replace(extension, "");
        String inputPath = dirPath + "/%" + imageName.length() + "d" + extension;
        String outputPath = Constants.IMAGE_DIR + "/Pixiv_" + pixivId + "_" + System.currentTimeMillis() + ".gif";

        String[] cmd = new String[]{
                "ffmpeg", "-y",
                "-r", fps, "-i", inputPath,
                "-r", fps, "-f", "gif", outputPath,
        };

        RxFFmpegInvoke.getInstance()
                .runCommandRxJava(cmd)
                .subscribe(new RxFFmpegSubscriber() {
                    @Override
                    protected void onStart() {
                        super.onStart();
                        showDialog("开始合成", false);
                    }

                    @Override
                    public void onFinish() {
                        showDialog("合成成功 ", true);
                        BitmapUtils.insertToMediaStore(PixivGifActivity.this, new File(outputPath));
                        IOUtils.delFileOrFolder(dirPath);
                    }

                    @Override
                    public void onProgress(int progress, long progressTime) {
                        showDialog("合成进度：" + progress + "   " + progressTime, false);
                    }

                    @Override
                    public void onCancel() {
                        IOUtils.delFileOrFolder(dirPath);
                    }

                    @Override
                    public void onError(String message) {
                        showDialog("合成失败 " + message, true);
                        IOUtils.delFileOrFolder(dirPath);
                    }
                });
    }

    private void showDialog(String content, boolean showButton) {
        if (mDialog == null) {
            mDialog = new MaterialDialog.Builder(this)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .positiveText("关闭")
                    .positiveColor(Color.BLACK)
                    .build();
        }
        mDialog.setContent(content);
        mDialog.getActionButton(DialogAction.POSITIVE).setVisibility(showButton ? View.VISIBLE : View.GONE);
        mDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PermissionHelper.REQ_CODE_PERMISSION) {
            // 进入系统设置界面请求权限后的回调
            if (!PermissionHelper.hasPermissions(this, Permission.Group.STORAGE)) {
                finish();
            }
        }
    }

}
