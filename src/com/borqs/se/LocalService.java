package com.borqs.se;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.borqs.se.home3d.SEHomeActivity;
import com.borqs.se.home3d.SEHomeUtils;

public class LocalService extends Service {

    @Override
    public void onCreate() {
        if(SEHomeUtils.DEBUG) {
            Log.d(SEHomeUtils.TAG, "###########################start localservice");
        }
        Notification notification = new Notification(0, SEHomeUtils.TAG, System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, SEHomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, SEHomeUtils.TAG, "Welcome to 3DHome !", pendingIntent);
       
        startForeground(12314, notification);
    }

    @Override
    public void onDestroy() {
        if(SEHomeUtils.DEBUG) {
            Log.d(SEHomeUtils.TAG, "###########################stop localservice");
        }
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

}
