package com.myapplication.utils;

import android.os.Environment;

public class Url {
    public static String filckrapi="https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=062a6c0c49e4de1d78497d13a7dbb360&tags=%s&extras=date_taken,owner_name,description,url_o&format=json&nojsoncallback=1&page=%s";
    public static String appfolder= Environment.getExternalStorageDirectory().getAbsolutePath()+"/ImageSearch";
}
