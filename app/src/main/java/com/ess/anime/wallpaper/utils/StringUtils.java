package com.ess.anime.wallpaper.utils;

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
}
