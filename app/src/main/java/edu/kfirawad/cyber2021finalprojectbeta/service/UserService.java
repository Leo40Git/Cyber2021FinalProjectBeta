package edu.kfirawad.cyber2021finalprojectbeta.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import edu.kfirawad.cyber2021finalprojectbeta.LocPermPromptActivity;
import edu.kfirawad.cyber2021finalprojectbeta.R;

public class UserService extends Service {
    private static final String TAG = "C2021FPB:serv:User";
    private static boolean running = false;

    public UserService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final ArrayList<UserServiceHandler.SimpleLocationListener> locationListenerDelegates = new ArrayList<>();
    private NotificationManagerCompat notificationManager;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Looper looper;
    private UserServiceHandler handler;

    @Override
    public void onCreate() {
        HandlerThread handlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        looper = handlerThread.getLooper();
        handler = new UserServiceHandler(looper);
        notificationManager = NotificationManagerCompat.from(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    private void createLocationListener() {
        if (locationListener != null)
            return;
        if (haveLocationPermissions()) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    for (UserServiceHandler.SimpleLocationListener listener : locationListenerDelegates)
                        listener.onLocationChanged(location);
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {
                    if (LocationManager.GPS_PROVIDER.equals(provider))
                        notificationManager.cancel(LOC_TAG, LOC_ID_ENABLE);
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pi = PendingIntent.getActivity(UserService.this, 0, i, 0);

                    notificationManager.notify(LOC_TAG, LOC_ID_ENABLE,
                            new NotificationCompat.Builder(UserService.this, CHANNEL_ID)
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setContentTitle("Please enable location!")
                                    .setContentText("Touch this notification to go to location settings.")
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setContentIntent(pi).setOngoing(true)
                                    .build());
                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000, 10F, locationListener, looper);
        }
    }

    private void destroyLocationListener() {
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }

    private static final String LOC_TAG = TAG + ":LOCATION";
    private static final int LOC_ID_PERMS = 0;
    private static final int LOC_ID_ENABLE = 1;

    private boolean haveLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(UserService.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(UserService.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.cancel(LOC_TAG, LOC_ID_PERMS);
            return true;
        } else {
            Intent i = new Intent(UserService.this, LocPermPromptActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pi = PendingIntent.getActivity(UserService.this, 0, i, 0);

            notificationManager.notify(LOC_TAG, LOC_ID_PERMS,
                    new NotificationCompat.Builder(UserService.this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Location permissions required!")
                        .setContentText("Touch this notification to grant the app the required permissions.")
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setContentIntent(pi).setOngoing(true)
                        .build());
            return false;
        }
    }

    public static final String CHANNEL_ID = "edu.kfirawad.cyber2021finalprojectbeta.notifs";

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "General";
            String description = "All notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private final AtomicInteger nId = new AtomicInteger();
    private void pushNotification(@NonNull Notification n) {
        notificationManager.notify(TAG, nId.getAndIncrement(), n);
    }

    private void pushNotification(String title, String desc) {
        createNotificationChannel();
        pushNotification(new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (running) {
            // we're already running!
            stopSelf(startId);
            return START_STICKY;
        } else
            running = true;

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

            @Override
            public void addLocationListener(UserServiceHandler.SimpleLocationListener listener) {
                if (!locationListenerDelegates.contains(listener))
                    locationListenerDelegates.add(listener);
                createLocationListener();
            }

            @Override
            public void removeLocationListener(UserServiceHandler.SimpleLocationListener listener) {
                locationListenerDelegates.remove(listener);
                if (locationListenerDelegates.isEmpty())
                    destroyLocationListener();
            }
        };
        handler.sendMessage(msg);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        looper.quitSafely();
        if (handler != null) {
            handler.cleanup();
            handler = null;
        }
        destroyLocationListener();
        running = false;
    }
}