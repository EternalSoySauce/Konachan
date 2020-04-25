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
    public final static String ALREADY_ADD_USER = "alreadyAddUser";
    public final static String VIDEO_SILENT = "videoSilent";
    public final static String IS_POST_IMAGE_SHOWN_RECTANGULAR = "isPostImageShownRectangular";

    // Intent
    public final static String APK_BEAN = "APK_BEAN";
    public final static String THUMB_BEAN = "THUMB_BEAN";
    public final static String IMAGE_BEAN = "IMAGE_BEAN";
    public final static String DOWNLOAD_BEAN = "DOWNLOAD_BEAN";
    public final static String LINK_TO_SHOW = "LINK_TO_SHOW";
    public final static String CURRENT_PAGE = "CURRENT_PAGE";
    public final static String CURRENT_FRAGMENT = "CURRENT_FRAGMENT";
    public final static String SEARCH_TAG = "SEARCH_TAG";
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

    // EventBus
    public final static String CHECK_UPDATE = "checkUpdate";  // 检测到新版本后通知 MainActivity
    public final static String GET_IMAGE_DETAIL = "getImageDetail";  // 获取到图片详细信息后通知详情页显示信息，PostFragment和PoolFragment更新adapter
    public final static String RELOAD_DETAIL_BY_ID = "reloadDetailById";  // PoolPostFragment获取到imageBean后重新根据ID请求tempPost
    public final static String LOCAL_FILES_CHANGED = "localFilesChanged";  // 收藏夹本地文件发生变动后通知FullscreenActivity退出页面
    public final static String START_VIDEO = "startVideo";  // FullscreenActivity翻页后通知MultipleMediaLayout播放Video
    public final static String RESUME_VIDEO = "resumeVideo";  // ImageFragment和FullscreenActivity触发onResume()后通知MultipleMediaLayout恢复Video
    public final static String PAUSE_VIDEO = "pauseVideo";  // ImageFragment和FullscreenActivity触发onPause()后通知MultipleMediaLayout暂停Video

    // Image Detail
    public final static String RATING_S = "s";
    public final static String RATING_E = "e";
    public final static String RATING_Q = "q";

    // Glide
    public final static String IMAGE_DIR = Environment.getExternalStorageDirectory() + "/Konachan/konachan";
    public final static String IMAGE_TEMP = Environment.getExternalStorageDirectory() + "/Konachan/temp";
    public final static String IMAGE_DONATE = Environment.getExternalStorageDirectory() + "/Konachan/donate";
}
