package com.myapplication;

import android.app.Application;

import com.myapplication.utils.Url;

import java.io.File;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        File file = new File(Url.appfolder);
        file.mkdirs();
    }


}
