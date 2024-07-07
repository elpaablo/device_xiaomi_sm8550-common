/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.xiaomi.settings.edgesuppression;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;

public class EdgeSuppressionService extends Service {

    private static final String TAG = "XiaomiPartsEdgeSuppressionService";
    //private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private static final boolean DEBUG = true;

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(TAG, "Creating service");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.d(TAG, "onStartCommand");
        EdgeSuppressionManager.getInstance(getApplicationContext()).handleEdgeModeFeatureDirectionModeChange();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (DEBUG) Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (DEBUG) Log.d(TAG, "onConfigurationChanged");
        EdgeSuppressionManager.getInstance(getApplicationContext()).handleEdgeModeFeatureDirectionModeChange();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
