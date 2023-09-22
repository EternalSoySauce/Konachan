package com.ess.anime.wallpaper.model.helper;

import android.content.Context;
import android.text.TextUtils;

import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PostBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.download.image.DownloadBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.website.WebsiteManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageDataHelper {

    public static List<DownloadBean> makeDownloadChosenList(Context context, ThumbBean thumbBean, ImageBean imageBean) {
        String imgHead = WebsiteManager.getInstance().getWebsiteConfig().getSavedImageHead();
        PostBean postBean = imageBean.posts[0];
        List<DownloadBean> downloadList = new ArrayList<>();
        File file;
        String desc;
        boolean exists;
        // 0.Sample size
        if (postBean.sampleFileSize != 0 && !TextUtils.equals(postBean.fileUrl, postBean.sampleUrl)) {
            desc = context.getString(R.string.dialog_download_sample,
                    postBean.sampleWidth, postBean.sampleHeight,
                    FileUtils.computeFileSize(postBean.sampleFileSize),
                    getFileExtension(postBean.sampleUrl).toUpperCase());
            file = makeFileToSave(postBean.id, "-Sample", postBean.sampleUrl);
            exists = file.exists();
            if (exists) {
                desc = context.getString(R.string.dialog_download_already, desc);
            }
            downloadList.add(new DownloadBean(0, postBean.sampleUrl, postBean.sampleFileSize,
                    context.getString(R.string.download_title_sample, imgHead, postBean.id), thumbBean.thumbUrl,
                    file.getAbsolutePath(), exists, desc));
        }

        // 1.Large size
        desc = context.getString(R.string.dialog_download_large,
                postBean.jpegWidth, postBean.jpegHeight,
                FileUtils.computeFileSize(postBean.fileSize),
                getFileExtension(postBean.fileUrl).toUpperCase());
        file = makeFileToSave(postBean.id, "-Large", postBean.fileUrl);
        exists = file.exists();
        if (exists) {
            desc = context.getString(R.string.dialog_download_already, desc);
        }
        downloadList.add(new DownloadBean(1, postBean.fileUrl, postBean.fileSize,
                context.getString(R.string.download_title_large, imgHead, postBean.id), thumbBean.thumbUrl,
                file.getAbsolutePath(), exists, desc));

        // 2.Origin size
        if (postBean.jpegFileSize != 0 && !TextUtils.equals(postBean.fileUrl, postBean.jpegUrl)) {
            desc = context.getString(R.string.dialog_download_origin,
                    postBean.jpegWidth, postBean.jpegHeight,
                    FileUtils.computeFileSize(postBean.jpegFileSize),
                    getFileExtension(postBean.jpegUrl).toUpperCase());
            file = makeFileToSave(postBean.id, "-Origin", postBean.jpegUrl);
            exists = file.exists();
            if (exists) {
                desc = context.getString(R.string.dialog_download_already, desc);
            }
            downloadList.add(new DownloadBean(2, postBean.jpegUrl, postBean.jpegFileSize,
                    context.getString(R.string.download_title_origin, imgHead, postBean.id), thumbBean.thumbUrl,
                    file.getAbsolutePath(), exists, desc));
        }
        return downloadList;
    }

    private static File makeFileToSave(String postId, String fileType, String url) {
        String extension = "." + getFileExtension(url);
//        url = url.substring(0, url.lastIndexOf(extension) + extension.length()).replaceAll(".com|.net", "");
//        String bitmapName = getImageHead() + FileUtils.encodeMD5String(url) + extension;
        // 图片命名方式改为"网站名-图片id-图片尺寸"样式，eg. Konachan-123456-Sample.jpg
        // 但这样无法识别此版本(v1.7)之前下载的图片是下载过的
        String imgHead = WebsiteManager.getInstance().getWebsiteConfig().getSavedImageHead();
        String bitmapName = imgHead + postId + fileType + extension;
        return new File(Constants.IMAGE_DIR, bitmapName);
    }

    private static String getFileExtension(String url) {
        String extension = FileUtils.getFileExtension(url);
        if (TextUtils.isEmpty(extension)) {
            extension = "jpg";
        }
        return extension;
    }

}
