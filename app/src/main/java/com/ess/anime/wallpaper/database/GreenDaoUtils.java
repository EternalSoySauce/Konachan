package com.ess.anime.wallpaper.database;

import android.content.Context;

import com.ess.anime.wallpaper.download.image.DownloadBean;
import com.ess.anime.wallpaper.download.image.DownloadBeanDao;

import java.util.List;

public class GreenDaoUtils {

    private static SearchTagBeanDao sSearchTagDao;
    private static DownloadBeanDao sDownloadDao;

    public static void initGreenDao(Context context) {
        String name = context.getPackageName() + ".db";
        DaoMaster.DevOpenHelper helper = new MyDevOpenHelper(context, name, null);
        DaoMaster daoMaster = new DaoMaster(helper.getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        sSearchTagDao = daoSession.getSearchTagBeanDao();
        sDownloadDao = daoSession.getDownloadBeanDao();
    }

    /*********************** Search Tag ***********************/

    public static void updateSearchTag(SearchTagBean searchTagBean) {
        sSearchTagDao.insertOrReplace(searchTagBean);
    }

    public static void deleteSearchTag(SearchTagBean searchTagBean) {
        sSearchTagDao.delete(searchTagBean);
    }

    public static List<SearchTagBean> queryAllSearchTags() {
        return sSearchTagDao.queryBuilder()
                .orderDesc(SearchTagBeanDao.Properties.UpdateTime)
                .list();
    }

    public static void deleteAllSearchTags() {
        sSearchTagDao.deleteAll();
    }

    /*********************** Download Image ***********************/

    public static void updateDownloadBean(DownloadBean downloadBean) {
        sDownloadDao.insertOrReplace(downloadBean);
    }

    public static void deleteDownloadBean(DownloadBean downloadBean) {
        sDownloadDao.delete(downloadBean);
    }

    public static List<DownloadBean> queryAllDownloadBeans() {
        return sDownloadDao.queryBuilder()
                .orderAsc(DownloadBeanDao.Properties.AddedTime)
                .list();
    }

}
