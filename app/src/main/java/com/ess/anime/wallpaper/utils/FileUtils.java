package com.ess.anime.wallpaper.utils;

import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 整合了一些常用的IO流与File类的功能操作 <br/><br/>
 * <b>需添加权限：</b><br/>
 * &emsp;&lt;uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /&gt;
 *
 * @author Zero
 */
public class FileUtils {

    /**
     * 对字符串（UTF-8编码）进行MD5加密
     *
     * @param info 需要加密的字符串
     * @return 加密后的字符串，如果抛出异常则返回空字符串
     */
    public static String encodeMD5String(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] digest = md5.digest();

            StringBuilder strBuilder = new StringBuilder();
            for (byte aDigest : digest) {
                String s = Integer.toHexString(0xff & aDigest);
                if (s.length() == 1) {
                    strBuilder.append("0");
                }
                strBuilder.append(s);
            }
            return strBuilder.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * 对字符串进行异或加密
     *
     * @param info          需要加密的字符串
     * @param cryptographic 密钥
     * @return 加密后的字符串
     */
    public static String encodeXorString(String info, String cryptographic) {
        char[] infoArray = info.toCharArray();
        char[] keyArray = cryptographic.toCharArray();
        int j = 0;
        for (int i = 0; i < infoArray.length; i++) {
            infoArray[i] = (char) (infoArray[i] ^ keyArray[j]);
            if (++j == keyArray.length) {
                j = 0;
            }
        }
        return String.valueOf(infoArray);
    }

    /**
     * 对字符串进行异或解密
     *
     * @param info          需要解密的字符串
     * @param cryptographic 密钥
     * @return 解密后的字符串
     */
    public static String decodeXorString(String info, String cryptographic) {
        char[] infoArray = info.toCharArray();
        char[] keyArray = cryptographic.toCharArray();
        int j = 0;
        for (int i = 0; i < infoArray.length; i++) {
            infoArray[i] = (char) (infoArray[i] ^ keyArray[j]);
            if (++j == keyArray.length) {
                j = 0;
            }
        }
        return String.valueOf(infoArray);
    }

    /**
     * 对字符串（UTF-8编码）进行Base64加密
     *
     * @param info 需要加密的字符串
     * @return 加密后的字符串，如果抛出异常则返回空字符串
     */
    public static String encodeBase64String(String info) {
        try {
            return Base64.encodeToString(info.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * 对字符串（UTF-8编码）进行Base64解密
     *
     * @param info 需要解密的字符串
     * @return 解密后的字符串
     */
    public static String decodeBase64String(String info) {
        return new String(Base64.decode(info, Base64.DEFAULT));
    }

    /**
     * 对文件进行Base64加密
     *
     * @param file 需要加密的文件
     * @return 加密后的文件字符串，如果抛出异常则返回空字符串
     */
    public static String encodeBase64File(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            fis.close();
            return Base64.encodeToString(buffer, Base64.DEFAULT);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 对文件进行Base64解密
     *
     * @param encodeStr 加密的文件字符串
     * @param file      解密后保存为的文件
     * @return 文件是否保存成功
     */
    public static boolean decodeBase64File(String encodeStr, File file) {
        FileOutputStream fos;
        try {
            byte[] buffer = Base64.decode(encodeStr, Base64.DEFAULT);
            fos = new FileOutputStream(file);
            fos.write(buffer);
            fos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 流转换成字符串（UTF-8编码）
     *
     * @param is 流对象
     * @return 流转换成的字符串    返回null代表异常
     */
    public static String streamToString(InputStream is) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int temp;
        try {
            while ((temp = is.read(buffer)) != -1) {
                baos.write(buffer, 0, temp);
            }
            return baos.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 流保存为文件
     *
     * @param is   输入流对象
     * @param file 要保存成为的目标文件
     * @return 是否保存成功 true/false
     */
    public static boolean streamToFile(InputStream is, File file) {
        BufferedInputStream bis = new BufferedInputStream(is);
        BufferedOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            int temp;
            while ((temp = bis.read()) != -1) {
                bos.write(temp);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                bis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 字符串保存为文件
     *
     * @param str  输入字符串
     * @param file 要保存成为的目标文件
     * @return 是否保存成功 true/false
     */
    public static boolean stringToFile(String str, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(str.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 读取文件为字符串
     *
     * @param file 目标文件
     * @return 读取到的字符串
     */
    public static String fileToString(File file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            return streamToString(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 移动文件，eg. 把路径为 a/img.jpg 的文件移动为路径为 b/img.jpg 的文件
     *
     * @param fromFile 要移动的文件
     * @param toFile   要保存到的目标文件
     * @return 是否移动成功  true/false
     */
    public static boolean moveFile(File fromFile, File toFile) {
        if (!fromFile.exists() || !fromFile.isFile()) {
            return false;
        }

        try {
            if (toFile.exists()) {
                toFile.delete();
            }

            File parentFile = toFile.getParentFile();
            if (parentFile != null && !parentFile.exists()) {
                parentFile.mkdirs();
            }

            boolean renameToSuccess = false;
            try {
                renameToSuccess = fromFile.renameTo(toFile);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!renameToSuccess) {
                //在文件系统不同的情况下，renameTo会失败，此时使用copy，然后删除原文件
                copyFile(fromFile, toFile);
                fromFile.delete();
            }
            return toFile.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 复制文件，eg. 把路径为 a/img.jpg 的文件复制为路径为 b/img.jpg 的文件
     *
     * @param fromFile 要复制的文件
     * @param toFile   要保存到的目标文件
     * @return 是否复制成功  true/false
     */
    public static boolean copyFile(File fromFile, File toFile) {
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            if (fromFile.exists()) {
                bis = new BufferedInputStream(new FileInputStream(fromFile));
                bos = new BufferedOutputStream(new FileOutputStream(toFile));
                int temp;
                while ((temp = bis.read()) != -1) {
                    bos.write(temp);
                }
            }
            return fromFile.exists();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 复制文件夹，eg. 把路径 root/a 的文件夹复制到路径 root/b，其结果为 root/b/a
     *
     * @param fromPath 要复制的文件夹路径
     * @param toPath   要保存到的目标位置的路径
     * @return 是否复制成功  true/false
     */
    public static boolean copyFolder(String fromPath, String toPath) {
        File fromFile = new File(fromPath);
        toPath = toPath + "/" + fromFile.getName();
        File toFile = new File(toPath);
        if (fromFile.exists()) {
            if (fromFile.isDirectory()) {
                if (!toFile.exists()) {
                    toFile.mkdirs();
                }
                for (File childFile : fromFile.listFiles()) {
                    String tempPath = fromPath + "/" + childFile.getName();
                    if (!copyFolder(tempPath, toPath)) {
                        return false;
                    }
                }
                return true;
            } else {
                return copyFile(fromFile, toFile);
            }
        }
        return false;
    }

    /**
     * 删除文件或文件夹（包括其所有子文件夹和文件）
     *
     * @param path 要删除的文件或文件夹路径
     * @return 是否删除成功  true/false
     */
    public static boolean deleteFile(String path) {
        return deleteFile(new File(path));
    }

    /**
     * 删除文件或文件夹（包括其所有子文件夹和文件）
     *
     * @param file 要删除的文件或文件夹
     * @return 是否删除成功  true/false
     */
    public static boolean deleteFile(File file) {
        try {
            if (file.exists()) {
                if (file.isDirectory()) {
                    for (File childFile : file.listFiles()) {
                        String tempPath = file.getAbsolutePath() + "/" + childFile.getName();
                        if (!deleteFile(tempPath)) {
                            return false;
                        }
                    }
                }
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取文件或文件夹的大小，如果文件或文件夹不存在，则返回0。附：获取文件大小可直接使用file.length();
     *
     * @param path 文件夹路径
     * @return 文件夹大小，单位：B
     */
    public static long getFileLength(String path) {
        return getFileLength(new File(path));
    }

    /**
     * 获取文件或文件夹的大小，如果文件或文件夹不存在，则返回0。附：获取文件大小可直接使用file.length();
     *
     * @param file 文件夹
     * @return 文件夹大小，单位：B
     */
    public static long getFileLength(File file) {
        long length = 0;
        try {
            if (file.exists()) {
                if (file.isDirectory()) {
                    for (File f : file.listFiles()) {
                        length += getFileLength(f.getAbsolutePath());
                    }
                } else {
                    length += file.length();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return length;
    }

    private final static long KB = 1024;
    private final static long MB = KB * 1024;
    private final static long GB = MB * 1024;

    /**
     * 将文件大小转换为KB、MB、GB 等字符串
     *
     * @param b 文件字节大小
     * @return B（整数位）、KB（整数位）、MB（四舍五入，两位精确度）、GB（四舍五入，两位精确度）
     */
    public static String computeFileSize(long b) {
        float size;
        BigDecimal decimal;

        if (b / GB >= 1) {
            size = b / (float) GB;
            decimal = new BigDecimal(size);
            return decimal.setScale(2, RoundingMode.HALF_UP).floatValue() + "G";
        } else if (b / MB >= 1) {
            size = b / (float) MB;
            decimal = new BigDecimal(size);
            return decimal.setScale(2, RoundingMode.HALF_UP).floatValue() + "M";
        } else if (b / KB >= 1) {
            size = b / (float) KB;
            decimal = new BigDecimal(size);
            return decimal.setScale(0, RoundingMode.HALF_UP).intValue() + "K";
        } else {
            return b + "B";
        }
    }

    /**
     * 将文件大小由KB、MB、GB 等字符串转换为long值
     *
     * @param fileSize 文件大小字符串
     * @return 文件大小
     */
    public static long parseFileSize(String fileSize) {
        try {
            double size = Double.parseDouble(fileSize.replaceAll("[^-*\\d+(\\.)?]", "")); // 提取数字
            if (fileSize.toUpperCase().contains("G")) {
                size *= 1024 * 1024 * 1024;
            } else if (fileSize.toUpperCase().contains("M")) {
                size *= 1024 * 1024;
            } else if (fileSize.toUpperCase().contains("K")) {
                size *= 1024;
            }
            return (long) size;
        } catch (NumberFormatException ignore) {
            return 0;
        }
    }

    /**
     * 获取文件后缀，不带·
     *
     * @param path 文件路径
     * @return 文件后缀
     */
    public static String getFileExtension(String path) {
        Pattern pattern = Pattern.compile("\\.(\\w+)(\\?|$)");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    /**
     * 获取文件后缀，带·
     *
     * @param path 文件路径
     * @return 文件后缀
     */
    public static String getFileExtensionWithDot(String path) {
        String extension = getFileExtension(path);
        if (TextUtils.isEmpty(extension)) {
            return "";
        } else {
            return "." + extension;
        }
    }

    /**
     * 判断文件是否为图片格式
     *
     * @return boolean
     */
    public static boolean isImageType(String filePath) {
        try {
            if (!TextUtils.isEmpty(filePath)) {
                String extension = getFileExtension(filePath).toLowerCase();
                return extension.equals("bmp") || extension.equals("jpg") || extension.equals("jpeg")
                        || extension.equals("png") || extension.equals("gif") || extension.equals("webp");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断文件是否为视频格式
     *
     * @return boolean
     */
    public static boolean isVideoType(String filePath) {
        try {
            if (!TextUtils.isEmpty(filePath)) {
                String extension = getFileExtension(filePath).toLowerCase();
                return extension.equals("avi") || extension.equals("wmv") || extension.equals("mp4")
                        || extension.equals("webm") || extension.equals("mpg") || extension.equals("mpeg")
                        || extension.equals("3gp") || extension.equals("mov") || extension.equals("flv");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 判断文件是否为媒体格式
     *
     * @return boolean
     */
    public static boolean isMediaType(String filePath) {
        return isImageType(filePath) || isVideoType(filePath);
    }

}
