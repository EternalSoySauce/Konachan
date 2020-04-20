package com.ess.anime.wallpaper.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 一些涉及正则表达式的String判断与操作
 * @author Zero
 *
 */
public class StringUtils {

	/**
	 * 判断字符串中是否包含中文
	 * @param str 需要进行判断的字符串
	 * @return 是否包含中文  true/false
	 */
	public static boolean isContainChinese(String str) {
		Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]"); 
		Matcher matcher = pattern.matcher(str); 
		return matcher.find();
	}
	
	/**
	 * 只保留字符串中的中文字符
	 * @param str 需要进行操作的字符串
	 * @return 只剩下中文的字符串
	 */
	public static String convertToChineseOnly(String str) {
		Pattern pattern = Pattern.compile("[^\u4e00-\u9fa5]");
		Matcher matcher = pattern.matcher(str);
		return matcher.replaceAll("");
	}
	
	/**
	 * 过滤字符串，只保留想要的字符
	 * @param str 需要进行操作的字符串
	 * @param pattern 想要保留字符的正则表达式
	 * @return 过滤后的字符串
	 */
	public static String filter(String str, Pattern pattern) {
		String filter = "";
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			filter += matcher.group();
		}
		return filter;
	}

	/**
	 * 判断一个字符串是否为url
	 *
	 * @param str String 字符串
	 * @return boolean 是否为url
	 **/
	public static boolean isURL(String str) {
		if (TextUtils.isEmpty(str)) {
			return false;
		}

		str = str.toLowerCase();

		String regex = "^((https|http|ftp|rtsp|mms)?://)"  //https、http、ftp、rtsp、mms

				+ "?(([0-9a-z_!~*'().&=+$%-]+: )?[0-9a-z_!~*'().&=+$%-]+@)?" //ftp的user@

				+ "(([0-9]{1,3}\\.){3}[0-9]{1,3}" // IP形式的URL- 例如：199.194.52.184

				+ "|" // 允许IP和DOMAIN（域名）

				+ "([0-9a-z_!~*'()-]+\\.)*" // 域名- www.

				+ "([0-9a-z][0-9a-z-]{0,61})?[0-9a-z]\\." // 二级域名

				+ "[a-z]{2,6})" // first level domain- .com or .museum

				+ "(:[0-9]{1,5})?" // 端口号最大为65535,5位数

				+ "((/?)|" // a slash isn't required if there is no file name

				+ "(/[0-9a-z_!~*'().;?:@&=+$,%#-]+)+/?)$";

		return str.matches(regex);
	}

	/**
	 * 判断一个字符串是否以网络协议开头
	 *
	 * @param str String 字符串
	 * @return boolean 是否以网络协议开头
	 */
	public static boolean isStartWidthProtocol(String str) {
		if (TextUtils.isEmpty(str)) {
			return false;
		}

		str = str.toLowerCase();
		String regex = "^((https|http|ftp|rtsp|mms)?://).*";
		return str.matches(regex);
	}
}
