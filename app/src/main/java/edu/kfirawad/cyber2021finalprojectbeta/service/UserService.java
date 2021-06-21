package edu.kfirawad.cyber2021finalprojectbeta.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.atomic.AtomicInteger;

import edu.kfirawad.cyber2021finalprojectbeta.R;

public class UserService extends Service {
    private static final String TAG = "C2021FPB:serv:User";

    public UserService() { }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private NotificationManagerCompat notifyManager;
    private Looper looper;
    private UserServiceHandler handler;

    @Override
    public void onCreate() {
        HandlerThread handlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        looper = handlerThread.getLooper();
        handler = new UserServiceHandler(looper);
        notifyManager = NotificationManagerCompat.from(this);
    }

    public static final String CHANNEL_ID = "edu.kfirawad.cyber2021finalprojectbeta.notifs";

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Parent";
            String description = "Notifications for parents";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notifyManager.createNotificationChannel(channel);
        }
    }

    private final AtomicInteger nId = new AtomicInteger();
    private void pushNotification(String title, String desc) {
        createNotificationChannel();
        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();
        notifyManager.notify(TAG, nId.getAndIncrement(), n);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = handler.obtainMessage();
        msg.obj = new UserServiceHandler.Hooks() {
            @Override
            public void killService() {
                stopSelf(startId);
            }

            @Override
            public void pushNotification(String title, String desc) {
                UserService.this.pushNotification(title, desc);
            }
        };
        handler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}