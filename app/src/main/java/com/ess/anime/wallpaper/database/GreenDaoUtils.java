package com.ess.anime.wallpaper.database;

import android.content.Context;

import java.util.List;

public class GreenDaoUtils {

    private static SearchTagBeanDao sDao;

    public static void initGreenDao(Context context) {
        String name = context.getPackageName() + ".db";
        DaoMaster.DevOpenHelper helper = new MyDevOpenHelper(context, name, null);
        DaoMaster daoMaster = new DaoMaster(helper.getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        sDao = daoSession.getSearchTagBeanDao();
    }

    public static void updateSearchTag(SearchTagBean searchTagBean) {
        sDao.insertOrReplace(searchTagBean);
    }

    public static void deleteSearchTag(SearchTagBean searchTagBean) {
        sDao.delete(searchTagBean);
    }

    public static List<SearchTagBean> queryAllSearchTags() {
        return sDao.queryBuilder()
                .orderDesc(SearchTagBeanDao.Properties.UpdateTime)
                .list();
    }

    public static void deleteAllSearchTags() {
        sDao.deleteAll();
    }

}
