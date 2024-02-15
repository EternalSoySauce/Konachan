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
    public final static String VIDEO_MUTE = "videoSilent";
    public final static String IS_POST_IMAGE_SHOWN_RECTANGULAR = "isPostImageShownRectangular";
    public final static String IMAGE_DETAIL_SWITCH_BUTTON_TOP_POSITION = "image_detail_switch_button_top_position";
    public final static String IMAGE_DETAIL_GESTURE_GUIDE_SHOWED = "image_detail_gesture_guide_showed";
    public final static String FAVORITE_TAG_SORT_BY = "favoriteTagSortBy";
    public final static String FAVORITE_TAG_SORT_ORDER = "favoriteTagSortOrder";
    public final static String SCREEN_ORIENTATION = "screenOrientation";
    public final static String PRELOAD_IMAGE_ONLY_WIFI = "preloadImageOnlyWifi";
    public final static String PIXIV_LOGIN_COOKIE = "pixiv_login_cookie";
    public final static String PIXIV_LOGIN_COOKIE_EXPIRED = "pixiv_login_cookie_expired";

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
    public final static int SEARCH_MODE_TAGS = 1001;
    public final static int SEARCH_MODE_ID = 1002;
    public final static int SEARCH_MODE_CHINESE = 1003;
    public final static int SEARCH_MODE_ADVANCED = 1004;
    public final static int SEARCH_MODE_HOME = 1005;
    public final static int SEARCH_MODE_RANDOM = 1006;
    public final static int FULLSCREEN_CODE = 2000;

    // EventBus
    public final static String CHECK_UPDATE = "checkUpdate";  // 检测到新版本后通知 MainActivity
    public final static String GET_IMAGE_DETAIL = "getImageDetail";  // 获取到图片详细信息后通知详情页显示信息，PostFragment和PoolFragment更新adapter
    public final static String RELOAD_DETAIL_BY_ID = "reloadDetailById";  // PoolPostFragment获取到imageBean后重新根据ID请求tempPost
    public final static String LOCAL_FILES_CHANGED = "localFilesChanged";  // 收藏夹本地文件发生变动后通知FullscreenActivity退出页面
    public final static String START_VIDEO = "startVideo";  // FullscreenActivity翻页后通知MultipleMediaLayout播放Video
    public final static String RESUME_VIDEO = "resumeVideo";  // ImageFragment和FullscreenActivity触发onResume()后通知MultipleMediaLayout恢复Video
    public final static String PAUSE_VIDEO = "pauseVideo";  // ImageFragment和FullscreenActivity触发onPause()后通知MultipleMediaLayout暂停Video
    public final static String TOGGLE_VIDEO_CONTROLLER = "toggleVideoController";  // FullscreenActivity单击页面后通知MultipleMediaLayout切换视频播放控制器显隐
    public final static String TOGGLE_SCREEN_ORIENTATION = "toggleScreenOrientation";  // 设置页切换强制横屏开关后通知各页面旋转屏幕

    // Image Detail
    public final static String RATING_S = "s";
    public final static String RATING_E = "e";
    public final static String RATING_Q = "q";

    // Glide
    public final static String IMAGE_DIR = Environment.getExternalStorageDirectory() + "/Konachan/konachan";
    public final static String IMAGE_TEMP = Environment.getExternalStorageDirectory() + "/Konachan/temp";
    public final static String IMAGE_DONATE = Environment.getExternalStorageDirectory() + "/Konachan/donate";
}
