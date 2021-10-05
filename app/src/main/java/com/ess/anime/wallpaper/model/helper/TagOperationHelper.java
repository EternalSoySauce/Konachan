package com.ess.anime.wallpaper.model.helper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.ess.anime.wallpaper.MyApp;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.database.FavoriteTagBean;
import com.ess.anime.wallpaper.database.GreenDaoUtils;
import com.ess.anime.wallpaper.database.SearchTagBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.ui.activity.MainActivity;
import com.ess.anime.wallpaper.utils.SystemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TagOperationHelper {

    /************************** Tag 操作 **************************/

    public static void searchTag(Activity activity, String tag) {
        if (SystemUtils.isActivityActive(activity)) {
            int searchMode = Constants.SEARCH_MODE_TAGS;
            GreenDaoUtils.updateSearchTag(new SearchTagBean(tag, searchMode, System.currentTimeMillis()));
            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra(Constants.SEARCH_TAG, tag);
            intent.putExtra(Constants.SEARCH_MODE, searchMode);
            activity.startActivity(intent);
        }
    }

    public static void copyTagToClipboard(Activity activity, String tag) {
        if (SystemUtils.isActivityActive(activity)) {
            SystemUtils.setClipString(activity, tag);
            cancelCurrentToast();
            showNewToast(activity.getString(R.string.already_set_clipboard));
        }
    }

    public static void appendTagToClipboard(Activity activity, String tag) {
        if (SystemUtils.isActivityActive(activity)) {
            String firstClipString = SystemUtils.getFirstClipString(activity);
            if (TextUtils.isEmpty(firstClipString)) {
                firstClipString = tag;
            } else {
                String[] tags = firstClipString.split("[,，]");
                if (!Arrays.asList(tags).contains(tag)) {
                    firstClipString += "," + tag;
                }
            }
            SystemUtils.setClipString(activity, firstClipString);
            cancelCurrentToast();
            showNewToast(activity.getString(R.string.already_add_to_clipboard));
        }
    }

    /************************** Favorite Tag **************************/

    public enum FavoriteTagSortBy {
        FIRST_LETTER(R.string.sort_by_first_letter),
        FAVORITE_TIME(R.string.sort_by_favorite_time),
        ;

        private int textResId;

        FavoriteTagSortBy(int textResId) {
            this.textResId = textResId;
        }

        @Override
        public String toString() {
            return MyApp.getInstance().getString(textResId);
        }
    }

    public enum FavoriteTagSortOrder {
        ASCEND(R.string.sort_order_ascend),
        DESCEND(R.string.sort_order_descend),
        ;

        private int textResId;

        FavoriteTagSortOrder(int textResId) {
            this.textResId = textResId;
        }

        @Override
        public String toString() {
            return MyApp.getInstance().getString(textResId);
        }
    }

    public static void saveFavoriteTagSortParam(FavoriteTagSortBy by, FavoriteTagSortOrder order) {
        PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance())
                .edit()
                .putInt(Constants.FAVORITE_TAG_SORT_BY, by.ordinal())
                .putInt(Constants.FAVORITE_TAG_SORT_ORDER, order.ordinal())
                .apply();
    }

    public static FavoriteTagSortBy getFavoriteTagSortBy() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        int ordinal = preferences.getInt(Constants.FAVORITE_TAG_SORT_BY, FavoriteTagSortBy.FAVORITE_TIME.ordinal());
        return FavoriteTagSortBy.values()[ordinal];
    }

    public static FavoriteTagSortOrder getFavoriteTagSortOrder() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance());
        int ordinal = preferences.getInt(Constants.FAVORITE_TAG_SORT_ORDER, FavoriteTagSortOrder.DESCEND.ordinal());
        return FavoriteTagSortOrder.values()[ordinal];
    }

    public static List<FavoriteTagBean> queryAllFavoriteTags() {
        FavoriteTagSortBy sortBy = getFavoriteTagSortBy();
        FavoriteTagSortOrder sortOrder = getFavoriteTagSortOrder();
        switch (sortBy) {
            case FIRST_LETTER:
                return GreenDaoUtils.queryAllFavoriteTagsSortByLetter(sortOrder == FavoriteTagSortOrder.DESCEND);
            case FAVORITE_TIME:
                return GreenDaoUtils.queryAllFavoriteTagsSortByTime(sortOrder == FavoriteTagSortOrder.DESCEND);
            default:
                return new ArrayList<>();
        }
    }

    public static void setTagFavorite(String tag, boolean isFavorite) {
        FavoriteTagBean tagBean = GreenDaoUtils.queryFavoriteTag(tag);
        tagBean.setIsFavorite(isFavorite);
        tagBean.setFavoriteTime(System.currentTimeMillis());
        GreenDaoUtils.updateFavoriteTag(tagBean);
    }

    /************************** Toast **************************/

    private static Toast mCurrentToast;

    private static void cancelCurrentToast() {
        if (mCurrentToast != null) {
            mCurrentToast.cancel();
            mCurrentToast = null;
        }
    }

    private static void showNewToast(String toast) {
        mCurrentToast = Toast.makeText(MyApp.getInstance(), toast, Toast.LENGTH_SHORT);
        mCurrentToast.show();
    }

}
