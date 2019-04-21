package com.chamelon.sabadmin.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;

import com.chamelon.sabadmin.services.JobServiceUpload;

public class Util {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void scheduleJob(Context context) {

        ComponentName serviceComponent = new ComponentName(context, JobServiceUpload.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(10 * 1000); // wait at least
        builder.setOverrideDeadline(15 * 1000); // maximum delay
        JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
        jobScheduler.schedule(builder.build());

    }
}