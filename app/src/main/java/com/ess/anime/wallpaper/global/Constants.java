package com.ess.anime.wallpaper.global;

import android.os.Environment;

public class Constants {

    // 用于Sound标志位
    public static boolean sRestart = true;
    public static boolean sAllowPlaySound;

    // Shared Preference
    public final static String BASE_URL = "baseUrl";
    public final static String SEARCH_MODE = "searchMode";
    public final static String GAME_COLUMN = "gameColumn";
    public final static String ALLOW_PLAY_SOUND = "allowPlaySound";

    // Fragment
    public final static String PAGE_TITLE = "pageTitle";

    // Intent
    public final static String APK_BEAN = "APK_BEAN";
    public final static String THUMB_BEAN = "THUMB_BEAN";
    public final static String IMAGE_BEAN = "IMAGE_BEAN";
    public final static String LINK_TO_SHOW = "LINK_TO_SHOW";
    public final static String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";
    public final static String JPEG_URL = "JPEG_URL";
    public final static String BITMAP_PATH = "BITMAP_PATH";
    public final static String SEARCH_TAG = "SEARCH_TAG";
    public final static String FULLSCREEN_POSITION = "FULLSCREEN_POSITION";
    public final static String COLLECTION_LIST = "COLLECTION_LIST";
    public final static String ENLARGE = "ENLARGE";

    // Activity Result
    public final static int SEARCH_CODE = 1000;
    public final static int SEARCH_CODE_TAGS = 1001;
    public final static int SEARCH_CODE_ID = 1002;
    public final static int SEARCH_CODE_CHINESE = 1003;
    public final static int SEARCH_CODE_ADVANCED = 1004;
    public final static int SEARCH_CODE_HOME = 1005;
    public final static int SEARCH_CODE_RANDOM = 1006;
    public final static int FULLSCREEN_CODE = 2000;
    public final static int STORAGE_PERMISSION_CODE = 3000;

    // EventBus
    public final static String CHECK_UPDATE = "checkUpdate";  //检测到新版本后通知 MainActivity
    public final static String TOGGLE_SCAN_MODE = "toggleScanMode";  //更换浏览模式后通知 PostFragment和PoolFragment更新adapter
    public final static String CHANGE_BASE_URL = "changeBaseUrl";  //切换搜图网站后通知 PostFragment和PoolFragment更新adapter
    public final static String GET_IMAGE_DETAIL = "getImageDetail";  //获取到图片详细信息后通知详情页显示信息，PostFragment和PoolFragment更新adapter

    // Http
    public final static String BASE_URL_KONACHAN_S = "https://www.konachan.net/";
    public final static String BASE_URL_KONACHAN_E = "https://www.konachan.com/";
    public final static String BASE_URL_YANDE = "https://yande.re/";
    public final static String BASE_URL_LOLIBOORU = "https://lolibooru.moe/";
    public final static String BASE_URL_DANBOORU = "https://www.idanbooru.com/";
    public final static String BASE_URL_BAIDU = "https://baike.baidu.com/item/";
    public final static String TAG_JSON_URL_KONACHAN_S = "https://konachan.net/tag/summary.json";
    public final static String TAG_JSON_URL_KONACHAN_E = "https://konachan.com/tag/summary.json";
    public final static String TAG_JSON_URL_YANDE = "https://yande.re/tag/summary.json";
    public final static String TAG_JSON_URL_LOLIBOORU = "https://lolibooru.moe/tag/summary.json";
//    public final static String TAG_JSON_URL_DANBOORU = "";  // Danbooru没有搜索提示

    public final static String[] BASE_URLS = {
            BASE_URL_KONACHAN_S, BASE_URL_KONACHAN_E, BASE_URL_YANDE, BASE_URL_LOLIBOORU,
            BASE_URL_DANBOORU
    };

    public final static String[] TAG_JSON_URLS = {
            TAG_JSON_URL_KONACHAN_S, TAG_JSON_URL_KONACHAN_E, TAG_JSON_URL_YANDE, TAG_JSON_URL_LOLIBOORU
    };

    // Image Detail
    public final static String RATING_S = "s";
    public final static String RATING_E = "e";
    public final static String RATING_Q = "q";

    // Glide
    public final static String IMAGE_DIR = Environment.getExternalStorageDirectory() + "/Konachan/konachan";
    public final static String IMAGE_TEMP = Environment.getExternalStorageDirectory() + "/Konachan/temp";
    public final static String IMAGE_HEAD_KONACHAN = "Konachan-";
    public final static String IMAGE_HEAD_YANDE = "Yande-";
    public final static String IMAGE_HEAD_LOLIBOORU = "Lolibooru-";
    public final static String IMAGE_HEAD_DANBOORU = "Danbooru-";
}
