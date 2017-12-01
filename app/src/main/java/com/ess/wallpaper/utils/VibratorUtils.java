package com.ess.wallpaper.utils;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

/**
 * 手机震动功能 <br/><br/>
 * <b>需添加权限：</b><br/>
 * &emsp;&lt;uses-permission android:name="android.permission.VIBRATE" /&gt;
 * @author Zero
 *
 */
public class VibratorUtils {

	/**
	 * 仅震动一下
	 * @param context 上下文
	 * @param milliseconds 震动持续时长
	 */
	public static void Vibrate(Context context, long milliseconds) { 
		Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE); 
		vib.vibrate(milliseconds); 
	} 

	/**
	 * 每一组进行多次震动
	 * @param context 上下文
	 * @param pattern 每一组震动的各分段时长 [off, on, off, on...]
	 * @param repeat 循环次数，如果是-1则不循环，0表示无限循环
	 */
	public static void Vibrate(Context context, long[] pattern, int repeat) { 
		Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE); 
		vib.vibrate(pattern, repeat); 
	} 
}
