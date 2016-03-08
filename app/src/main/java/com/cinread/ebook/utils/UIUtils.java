package com.cinread.ebook.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;

import com.cinread.ebook.base.BaseApplication;

/**
 * @Project: mupdfdemo
 * @Package: com.cinread.cindle
 * @Author: Think
 * @Time: 2016/2/26
 * @desc: TODO
 */
public class UIUtils {
    /**
     * 得到上下文
     */
    public static Context getContext() {
        return BaseApplication.getContext();
    }

    /**
     * 得到Resource对象
     */
    public static Resources getResources() {
        return getContext().getResources();
    }

    /**
     * 得到String.xml中的字符
     */
    public static String getString(int resId) {
        return getResources().getString(resId);
    }

    /**
     * 得到String.xml中的字符数组
     */
    public static String[] getStrings(int resId) {
        return getResources().getStringArray(resId);
    }

    /**
     * 得到color.xml中的颜色信息
     */
    public static int getColor(int resId) {
        return getResources().getColor(resId);
    }

    /**
     * 得到应用程序的包名
     *
     * @return
     */
    public static String getPackageName() {
        return getContext().getPackageName();
    }

    /**
     * 得到主线程的线程id
     *
     * @return
     */
    public static long getMainThreadId() {
        return BaseApplication.getMainThreadId();
    }

    /**
     * 得到主线程的Handler对象
     */
    public static Handler getMainThreadHandler() {
        return BaseApplication.getHandler();
    }

    /**
     * 安全的执行一个任务
     * 1.当前任务所在线程子线程-->使用消息机制发送到主线程执行
     * 2.当前任务所在线程主线程-->直接执行
     */
    public static void postTask(Runnable task) {
        //得到当前线程的线程id
        long curThreadId = android.os.Process.myTid();
        long mainThreadId = getMainThreadId();
        if (curThreadId == mainThreadId) {//主线程
            task.run();
        } else {//子线程
            getMainThreadHandler().post(task);
        }
    }

    /**
     * dp-->px
     *
     * @param dip
     * @return
     */
    public static int dp2Px(int dip) {
        //px/dp = density ①
        //px/(ppi/160) = dp ②

        //px和dp倍数关系
        float density = UIUtils.getResources().getDisplayMetrics().density;

        //ppi
        int densityDpi = UIUtils.getResources().getDisplayMetrics().densityDpi;


        /**
         320x480    1  1px=1dp            160
         480x800   1.5 1.5px = 1dp        240
         1280x720  2    2px=1dp           320
         */

        int px = (int) (dip * density + .5f);

        return px;
    }

    /**
     * px-->dp
     *
     * @param px
     * @return
     */
    public static int px2Dip(int px) {
        //px/dp = density ①

        //px和dp倍数关系
        float density = UIUtils.getResources().getDisplayMetrics().density;
        int dp = (int) (px / density + .5f);
        return dp;
    }
}
