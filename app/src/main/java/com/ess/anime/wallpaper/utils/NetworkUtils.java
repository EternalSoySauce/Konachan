package com.ess.anime.wallpaper.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * 获取手机网络状态信息的工具包
 *
 * @author Zero
 */
public class NetworkUtils {

    /**
     * 获取当前网络模式 <br/><br/>
     * <b>需添加权限：</b><br/>
     * &emsp;&lt;uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /&gt;
     *
     * @param context 上下文
     * @return one of ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI,
     * ConnectivityManager.TYPE_WIMAX, ConnectivityManager.TYPE_ETHERNET,
     * ConnectivityManager.TYPE_BLUETOOTH, or other types defined by ConnectivityManager，
     * 如果无网络则返回-1
     */
    public static int getNetworkType(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)
				context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null) {
                return info.getType();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取当前wifi状态 <br/><br/>
     * <b>需添加权限：</b><br/>
     * &emsp;&lt;uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /&gt;
     *
     * @param context 上下文
     * @return One of WifiManager.WIFI_STATE_DISABLED, WifiManager.WIFI_STATE_DISABLING,
     * WifiManager.WIFI_STATE_ENABLED, WifiManager.WIFI_STATE_ENABLING,
     * WifiManager.WIFI_STATE_UNKNOWN
     */
    public static int getWifiState(Context context) {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifi.getWifiState();
    }
}
