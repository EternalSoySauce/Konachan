package com.ess.anime.wallpaper.http;

import android.util.Log;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Request;
import okhttp3.internal.Util;

/**
 * 自定义OkHttp请求优先级排序
 * Priority从Request的tag中解出，默认为0
 * 包含优先级的tag格式为 tag + PRIORITY_KEY + 数字，eg. PoolPostFragment_priority1
 */
public class PriorityExecutorService extends ThreadPoolExecutor {

    public final static String PRIORITY_KEY = "_priority";

    public PriorityExecutorService() {
        super(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                new PriorityBlockingQueue<>(60, new AsyncCallComparator()),
                Util.threadFactory("OkHttp Dispatcher", false));
    }

    private static class AsyncCallComparator implements Comparator<Runnable> {

        @Override
        public int compare(Runnable o1, Runnable o2) {
            Log.i("rrr","pro "+getPriority(o1));
            return Integer.compare(getPriority(o2), getPriority(o1));
        }

        private int getPriority(Runnable runnable) {
            String tag = getTag(runnable);
            int index = tag.indexOf(PRIORITY_KEY);
            if (index != -1) {
                String priority = tag.substring(index).replaceAll("[^0-9]", "");
                try {
                    return Integer.parseInt(priority);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return 0;
        }

        private String getTag(Runnable runnable) {
            try {
                Method method = runnable.getClass().getDeclaredMethod("request");//得到方法对象
                method.setAccessible(true);
                Request request = (Request) method.invoke(runnable);
                return (String) request.tag();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
    }

}
