package com.ess.anime.wallpaper.bean;

import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollectionBean implements Parcelable {

    public String filePath;

    public String url;

    public int width;

    public int height;

    public CollectionBean(String filePath, int width, int height) {
        this.filePath = filePath;
        this.url = "file://" + filePath;
        this.width = width;
        this.height = height;
    }

    protected CollectionBean(Parcel in) {
        filePath = in.readString();
        url = in.readString();
        width = in.readInt();
        height = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(filePath);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CollectionBean> CREATOR = new Creator<CollectionBean>() {
        @Override
        public CollectionBean createFromParcel(Parcel in) {
            return new CollectionBean(in);
        }

        @Override
        public CollectionBean[] newArray(int size) {
            return new CollectionBean[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CollectionBean) {
            CollectionBean collectionBean = (CollectionBean) obj;
            return !(this.url == null || collectionBean.url == null) && this.url.equals(collectionBean.url);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    public static ArrayList<CollectionBean> getCollectionImages() {
        ArrayList<CollectionBean> collectionList = new ArrayList<>();
        File folder = new File(Constants.IMAGE_DIR);
        if (folder.exists() && !folder.isFile()) {
            List<File> imageFiles = Arrays.asList(folder.listFiles((dir, name) -> FileUtils.isMediaType(name)));
            Collections.sort(imageFiles, new FileOrderComparator());
            for (File file : imageFiles) {
                collectionList.add(createCollectionFromFile(file));
            }
        }
        return collectionList;
    }

    public static CollectionBean createCollectionFromFile(File file) {
        String imagePath = file.getAbsolutePath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return new CollectionBean(imagePath, options.outWidth, options.outHeight);
    }

    static class FileOrderComparator implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return Long.compare(rhs.lastModified(), lhs.lastModified());
        }
    }
}
