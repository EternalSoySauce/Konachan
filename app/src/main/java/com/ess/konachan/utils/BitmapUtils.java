package com.ess.konachan.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 位图操作，使用Btimap后记得在适当位置recycle
 *
 * @author Zero
 */
public class BitmapUtils {

    /**
     * 将Bitmap转化为Byte[]
     *
     * @param bitmap 目标位图
     * @param format 读取格式
     * @return 字节数组
     */
    public byte[] bitmapToBytes(Bitmap bitmap, CompressFormat format) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            bitmap.compress(format, 100, baos);
            return baos.toByteArray();
        } finally {
            try {
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 提取ImageView中的图片
     *
     * @param iv 目标ImageView
     * @return 位图
     */
    public static Bitmap getBitmapFromImageView(ImageView iv) {
        iv.setDrawingCacheEnabled(true);
        Bitmap bitmap = iv.getDrawingCache();
        iv.setDrawingCacheEnabled(false);
        return bitmap;
    }

    /**
     * 将Btimap保存为本地图片
     *
     * @param bitmap 需要保存的位图
     * @param path   保存路径
     * @param format 存储格式
     * @return 是否保存成功
     */
    public static boolean saveBitmapToLocal(Bitmap bitmap, String path, CompressFormat format) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(path));
            bitmap.compress(format, 100, fos);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 从本地路径获取Bitmap，并根据指定view的尺寸进行缩放以防止oom <br/>
     * 若加载至ImageView中，需继续调用getLocalBitmapDegree和rotateBitmap方法调整图片方向
     *
     * @param context 上下文
     * @param path    本地图片路径
     * @param vWidth  缩放至适配view的宽度
     * @param vHeight 缩放至适配view的高度
     * @return 位图
     */
    public static Bitmap getBitmapFromLocal(Context context, String path, float vWidth, float vHeight) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, opts);
        float imgWidth = opts.outWidth;
        float imgHeight = opts.outHeight;
        int widthRatio = (int) (imgWidth / vWidth);
        int heightRatio = (int) (imgHeight / vHeight);
        opts.inSampleSize = 1;
        if (widthRatio > 1 || heightRatio > 1) {
            if (widthRatio > heightRatio) {
                opts.inSampleSize = widthRatio;
            } else {
                opts.inSampleSize = heightRatio;
            }
        }
        opts.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, opts);
    }

    /**
     * 获取本地图片的方向
     *
     * @param path 本地图片路径
     * @return 图片旋转角度
     */
    public static float getLocalBitmapDegree(String path) {
        float degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 将图片旋转至正向
     *
     * @param bitmap 目标位图
     * @param degree 旋转角度
     * @return 旋转后的位图
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, float degree) {
        if (bitmap == null)
            return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Setting post rotate to 90
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    /**
     * 将图片左右翻转
     *
     * @param bitmap 目标图片
     * @return 翻转后图片
     */
    public static Bitmap flipBitmapHor(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        Camera camera = new Camera();
        camera.save();
        camera.rotateY(180f);
        camera.getMatrix(matrix);
        camera.restore();
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 将图片上下翻转
     *
     * @param bitmap 目标图片
     * @return 翻转后图片
     */
    public static Bitmap flipBitmapVer(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        Camera camera = new Camera();
        camera.save();
        camera.rotateX(180f);
        camera.getMatrix(matrix);
        camera.restore();
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 水平拼接多张图片
     *
     * @param bitmaps 图片数组
     * @return 拼接后图片
     */
    public static Bitmap mergeBitmapsHor(Bitmap[] bitmaps) {
        int width = 0;
        int height = 0;
        for (Bitmap bitmap : bitmaps) {
            width += bitmap.getWidth();
            height = Math.max(height, bitmap.getHeight());
        }
        Bitmap mergeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mergeBitmap);
        int currentWidth = 0;
        for (Bitmap bitmap : bitmaps) {
            canvas.drawBitmap(bitmap, currentWidth, 0, null);
            currentWidth += bitmap.getWidth();
        }
        return mergeBitmap;
    }

    /**
     * 竖直拼接多张图片
     *
     * @param bitmaps 图片数组
     * @return 拼接后图片
     */
    public static Bitmap mergeBitmapsVer(Bitmap[] bitmaps) {
        int width = 0;
        int height = 0;
        for (Bitmap bitmap : bitmaps) {
            width = Math.max(width, bitmap.getWidth());
            height += bitmap.getHeight();
        }
        Bitmap mergeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mergeBitmap);
        int currentHeight = 0;
        for (Bitmap bitmap : bitmaps) {
            canvas.drawBitmap(bitmap, 0, currentHeight, null);
            currentHeight += bitmap.getHeight();
        }
        return mergeBitmap;
    }

    /**
     * 将图片切割成 m * n 张小图
     *
     * @param bitmap   目标图片
     * @param rowCount 切割行数
     * @param colCount 切割列数
     * @return 切割后图片链表
     */
    public static ArrayList<Bitmap> splitImage(Bitmap bitmap, int rowCount, int colCount) {
        ArrayList<Bitmap> splitList = new ArrayList<>();
        int splitWidth = bitmap.getWidth() / rowCount;
        int splitHeight = bitmap.getHeight() / colCount;
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                int x = col * splitWidth;
                int y = row * splitHeight;
                splitList.add(Bitmap.createBitmap(bitmap, x, y, splitWidth, splitHeight));
            }
        }
        return splitList;
    }
}
