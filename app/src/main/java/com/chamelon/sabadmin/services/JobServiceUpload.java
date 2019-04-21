package com.chamelon.sabadmin.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import android.util.Log;

import com.chamelon.sabadmin.asynctasks.FirebaseSendMessage;
import com.chamelon.sabadmin.info.Info;
import com.chamelon.sabadmin.pojo.Content;
import com.chamelon.sabadmin.utils.Util;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class JobServiceUpload extends JobService implements Info {

    private DatabaseReference mRootRef;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onStartJob(JobParameters params) {

        Log.v("MyService", "Service Running");

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.child(KEY_PENDING_CONTENT).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists())
                    return;

                List<Content> pending = new ArrayList<>();

                Iterable<DataSnapshot> dataSnapshots = dataSnapshot.getChildren();
                for (DataSnapshot d : dataSnapshots) {
                    Content c = d.getValue(Content.class);
                    if (c.getToBePublishedOn() <= System.currentTimeMillis()) {

                        c.setPublishedOn(System.currentTimeMillis());
                        pending.add(c);
                        d.getRef().removeValue();

                    }
                }

                if (pending.size() == 0)
                    return;

                for (final Content c : pending) {
                    mRootRef.child(KEY_CONTENT)
                            .push()
                            .setValue(c)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    try {

                                        Log.v(TAG, "New Content Published.");
                                        new FirebaseSendMessage(c.getContentId()).execute();
                                    } catch (Exception e) {
                                        Log.d("Exception", e.toString());
                                    }

                                }
                            });
                }
                mRootRef.child(KEY_PENDING_CONTENT).removeEventListener(this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendSundayNotification();
        Util.scheduleJob(getApplicationContext());
        return true;
    }


    private void sendSundayNotification() {

        SharedPreferences lastNotification = getSharedPreferences(KEY_LAST_NOTIFICATION, MODE_PRIVATE);
        long last = lastNotification.getLong(KEY_LAST_TIME_STAMP, 0);

        long difference = System.currentTimeMillis() - last;

        Calendar cal = Calendar.getInstance();
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && difference > MILLISEC_ONE_DAY) {

            new FirebaseSendMessage(0).execute();
            lastNotification.edit().putLong(KEY_LAST_TIME_STAMP, System.currentTimeMillis()).apply();

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
