package com.badjoras.safetynettest;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by baama on 14/02/2017.
 */

public class SNApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Timber.i("Timber in debug mode");
        }
    }
}
