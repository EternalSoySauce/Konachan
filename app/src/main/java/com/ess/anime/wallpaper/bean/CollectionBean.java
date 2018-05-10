package com.ess.anime.wallpaper.bean;

import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import com.ess.anime.wallpaper.global.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CollectionBean implements Parcelable {

    public String url;

    public int width;

    public int height;

    public boolean isChecked;

    public CollectionBean(String url, int width, int height) {
        this(url, width, height, false);
    }

    public CollectionBean(String url, int width, int height, boolean isChecked) {
        this.url = url;
        this.width = width;
        this.height = height;
        this.isChecked = isChecked;
    }

    protected CollectionBean(Parcel in) {
        url = in.readString();
        width = in.readInt();
        height = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
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
        if (obj != null && obj instanceof CollectionBean) {
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
            List<File> imageFiles = Arrays.asList(folder.listFiles());
            Collections.sort(imageFiles, new FileOrderComparator());
            for (File file : imageFiles) {
                collectionList.add(createCollectionFromFile(file));
            }
        }
        return collectionList;
    }

    public static CollectionBean createCollectionFromFile(File file) {
        String imagePath = file.getAbsolutePath();
        String imageUrl = "file://" + imagePath;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        return new CollectionBean(imageUrl, options.outWidth, options.outHeight);
    }

    static class FileOrderComparator implements Comparator<File> {

        @Override
        public int compare(File lhs, File rhs) {
            return ((Long) rhs.lastModified()).compareTo(lhs.lastModified());
        }
    }
}
