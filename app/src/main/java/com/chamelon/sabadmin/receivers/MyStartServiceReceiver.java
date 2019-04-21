package com.chamelon.sabadmin.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.chamelon.sabadmin.utils.Util;

public class MyStartServiceReceiver extends BroadcastReceiver {

    public MyStartServiceReceiver() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Util.scheduleJob(context);
        }
    }
}