package com.fanbo.taokehelper.Application;

import android.app.Application;

import com.scienjus.smartqq.model.UserInfo;


/**
 * Created by Administrator on 2018/1/4 0004.
 */

public class MyApplication extends Application {
    private static MyApplication myApplication ;
    public UserInfo userInfo ;
    public static MyApplication getInstance(){
        return myApplication ;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
//        CookieJarImpl cookieJar = new CookieJarImpl(new PersistentCookieStore(getApplicationContext()));
//        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .cookieJar(cookieJar)
//                //其他配置
//                .build();
//        OkHttpUtils.initClient(okHttpClient);
    }
}
