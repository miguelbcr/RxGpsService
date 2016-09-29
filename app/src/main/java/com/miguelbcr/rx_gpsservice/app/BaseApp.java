package com.miguelbcr.rx_gpsservice.app;

import android.app.Application;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import io.reactivecache.ReactiveCache;
import io.victoralbertos.jolyglot.GsonSpeaker;

/**
 * Created by miguel on 15/07/2016.
 */

//@ReportsCrashes(
//        mailTo = "youremail@mail.com"
//)
public class BaseApp extends Application {
    private ReactiveCache reactiveCache;

    @Override
    public void onCreate() {
        super.onCreate();

//        ACRA.init(this);
        initReactiveCache();
    }

    private void initReactiveCache() {
        reactiveCache = new ReactiveCache.Builder()
                .using(getFilesDir(), new GsonSpeaker());
    }

    public ReactiveCache getReactiveCache() {
        return reactiveCache;
    }
}
