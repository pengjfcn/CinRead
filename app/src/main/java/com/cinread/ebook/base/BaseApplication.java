package com.cinread.ebook.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/**
 * @Project: mupdfdemo
 * @Package: com.cinread.cindle
 * @Author: Think
 * @Time: 2016/2/26
 * @desc: TODO
 */
public class BaseApplication extends Application {

    private static Context mContext;
    private static Handler mHandler;
    private static long    mMainThreadId;

    /**
     * 得到上下文
     */
    public static Context getContext() {
        return mContext;
    }


    /**
     * 得到主线程的handler
     */
    public static Handler getHandler() {
        return mHandler;
    }


    /**
     * 得到主线程的id
     */
    public static long getMainThreadId() {
        return mMainThreadId;
    }

    @Override
    public void onCreate() {//程序的入口方法
        //1.上下文
        mContext = getApplicationContext();

        //2.得到主线程的handler
        mHandler = new Handler();

        //3.得到主线程的id
        mMainThreadId = android.os.Process.myTid();
        /**
         Tid: Thread
         Uid:User
         Pid:Process
         */

        super.onCreate();
    }
}
