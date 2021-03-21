package com.jbsw.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class StartTrackingReceiver extends BroadcastReceiver {
    private static final String TAG = "TAGSTrackingReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent MyIntent = new Intent(context, GpsTracker.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Android version >= 26");
            context.startForegroundService(MyIntent);
        }
        else {
            Log.d(TAG, "Android version < 26");
            context.startService(MyIntent);
        }
    }
}
