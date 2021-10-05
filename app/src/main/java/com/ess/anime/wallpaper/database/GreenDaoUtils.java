package com.ess.anime.wallpaper.database;

import android.content.Context;

import com.ess.anime.wallpaper.download.image.DownloadBean;
import com.ess.anime.wallpaper.download.image.DownloadBeanDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

public class GreenDaoUtils {

    private static SearchTagBeanDao sSearchTagDao;
    private static FavoriteTagBeanDao sFavoriteTagDao;
    private static DownloadBeanDao sDownloadDao;

    public static void initGreenDao(Context context) {
        String name = context.getPackageName() + ".db";
        DaoMaster.DevOpenHelper helper = new MyDevOpenHelper(context, name, null);
        DaoMaster daoMaster = new DaoMaster(helper.getWritableDatabase());
        DaoSession daoSession = daoMaster.newSession();
        sSearchTagDao = daoSession.getSearchTagBeanDao();
        sFavoriteTagDao = daoSession.getFavoriteTagBeanDao();
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

    /*********************** Favorite Tag ***********************/

    public static boolean isFavoriteTag(String tag) {
        return sFavoriteTagDao.queryBuilder()
                .where(FavoriteTagBeanDao.Properties.Tag.eq(tag))
                .where(FavoriteTagBeanDao.Properties.IsFavorite.eq(true))
                .count() > 0;
    }

    public static void updateFavoriteTag(FavoriteTagBean favoriteTagBean) {
        sFavoriteTagDao.insertOrReplace(favoriteTagBean);
    }

    public static void updateFavoriteTags(List<FavoriteTagBean> tagList) {
        sFavoriteTagDao.insertOrReplaceInTx(tagList);
    }

    public static void deleteFavoriteTag(FavoriteTagBean favoriteTagBean) {
        sFavoriteTagDao.delete(favoriteTagBean);
    }

    public static void deleteFavoriteTags(List<FavoriteTagBean> tagList) {
        sFavoriteTagDao.deleteInTx(tagList);
    }

    public static FavoriteTagBean queryFavoriteTag(String tag) {
        FavoriteTagBean bean = sFavoriteTagDao.queryBuilder()
                .where(FavoriteTagBeanDao.Properties.Tag.eq(tag))
                .unique();
        if (bean == null) {
            bean = new FavoriteTagBean(tag, "", false, 0);
        }
        return bean;
    }

    public static List<FavoriteTagBean> queryAllFavoriteTagsSortByLetter(boolean reverse) {
        QueryBuilder<FavoriteTagBean> queryBuilder = sFavoriteTagDao.queryBuilder();
        queryBuilder.where(FavoriteTagBeanDao.Properties.IsFavorite.eq(true));
        if (reverse) {
            queryBuilder.orderDesc(FavoriteTagBeanDao.Properties.Tag);
        } else {
            queryBuilder.orderAsc(FavoriteTagBeanDao.Properties.Tag);
        }
        return queryBuilder.list();
    }

    public static List<FavoriteTagBean> queryAllFavoriteTagsSortByTime(boolean reverse) {
        QueryBuilder<FavoriteTagBean> queryBuilder = sFavoriteTagDao.queryBuilder();
        queryBuilder.where(FavoriteTagBeanDao.Properties.IsFavorite.eq(true));
        if (reverse) {
            queryBuilder.orderDesc(FavoriteTagBeanDao.Properties.FavoriteTime);
        } else {
            queryBuilder.orderAsc(FavoriteTagBeanDao.Properties.FavoriteTime);
        }
        return queryBuilder.list();
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
