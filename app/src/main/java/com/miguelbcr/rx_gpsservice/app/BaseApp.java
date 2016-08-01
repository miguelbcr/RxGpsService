package com.miguelbcr.rx_gpsservice.app;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ConfigurationBuilder;

/**
 * Created by miguel on 15/07/2016.
 */

@ReportsCrashes(
        mailTo = "youremail@mail.com"
)
public class BaseApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

//        final ACRAConfiguration config = new ConfigurationBuilder(this)
//                .setMailTo("youremail@mail.com")
//                .build();
//        ACRA.init(this, config);
        ACRA.init(this);
    }
}
