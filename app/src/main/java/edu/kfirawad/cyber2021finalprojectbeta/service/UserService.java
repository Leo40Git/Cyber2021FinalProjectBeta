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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import edu.kfirawad.cyber2021finalprojectbeta.LocPermPromptActivity;
import edu.kfirawad.cyber2021finalprojectbeta.R;

public class UserService extends Service {
    public static final String EXTRA_GOT_LOCATION_PERMS = "gotLocationPerms";

    private static final String TAG = "C2021FPB:serv:User";
    private static boolean running = false;

    public UserService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final ArrayList<UserServiceHandler.SimpleLocationListener> locationListenerDelegates
            = new ArrayList<>();
    private NotificationManagerCompat notificationManager;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Looper looper;
    private UserServiceHandler handler;

    @Override
    public void onCreate() {
        Log.d(TAG, "Initializing service!");

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
            Log.d(TAG, "Creating location listener!");
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    for (UserServiceHandler.SimpleLocationListener listener : locationListenerDelegates)
                        listener.onLocationChanged(location);
                }

                @Override
                public void onProviderEnabled(@NonNull String provider) {
                    if (LocationManager.GPS_PROVIDER.equals(provider))
                        notificationManager.cancel(NOTIF_TAG_LOC, NOTIF_LOC_ID_ENABLE);
                }

                @Override
                public void onProviderDisabled(@NonNull String provider) {
                    Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pi = PendingIntent.getActivity(UserService.this, 0, i, 0);

                    pushNotification(NOTIF_TAG_LOC, NOTIF_LOC_ID_ENABLE,
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
            Log.d(TAG, "Destroying location listener!");
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }

    private static final String NOTIF_TAG = "NOTIF";
    private static final String NOTIF_TAG_LOC = NOTIF_TAG + ":LOCATION";
    private static final int NOTIF_LOC_ID_PERMS = 0;
    private static final int NOTIF_LOC_ID_ENABLE = 1;

    private boolean haveLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(UserService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(UserService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "No permissions, pushing notification");
            Intent i = new Intent(UserService.this, LocPermPromptActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pi = PendingIntent.getActivity(UserService.this, 0, i, 0);

            pushNotification(NOTIF_TAG_LOC, NOTIF_LOC_ID_PERMS,
                    new NotificationCompat.Builder(UserService.this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setContentTitle("Location permissions required!")
                            .setContentText("Touch this notification to grant the app the required permissions.")
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setContentIntent(pi).setOngoing(true)
                            .build());
            return false;
        } else {
            Log.d(TAG, "Cancelling perm notification");
            notificationManager.cancel(NOTIF_TAG_LOC, NOTIF_LOC_ID_PERMS);
            return true;
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

    private void pushNotification(String tag, int id, @NonNull Notification n) {
        createNotificationChannel();
        notificationManager.notify(tag, id, n);
    }

    private final AtomicInteger nId = new AtomicInteger();
    private void pushNotification(@NonNull Notification n) {
        pushNotification(TAG, nId.getAndIncrement(), n);
    }

    private void pushNotification(String title, String desc) {
        pushNotification(new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra(EXTRA_GOT_LOCATION_PERMS)) {
            if (intent.getBooleanExtra(EXTRA_GOT_LOCATION_PERMS, false)) {
                Log.d(TAG, "Creating location listener after we got perms!");
                createLocationListener();
            }
            return START_STICKY;
        }

        if (running) {
            // we're already running!
            Log.d(TAG, "Tried to start service again (startId = " + startId + ")");
            return START_STICKY;
        } else
            running = true;
        Log.d(TAG, "Starting service!!! (startId = " + startId + ")");

        Message msg = handler.obtainMessage();
        msg.arg1 = UserServiceHandler.ARG1_START;
        msg.obj = new UserServiceHandler.Hooks() {
            @Override
            public void killService() {
                stopSelf();
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
        Log.d(TAG, "Service destroyed");
        if (handler != null) {
            Message msg = handler.obtainMessage();
            msg.arg1 = UserServiceHandler.ARG1_STOP;
            handler.sendMessage(msg);
            handler = null;
        }
        destroyLocationListener();
        looper.quitSafely();

        looper = null;
        running = false;
    }
}