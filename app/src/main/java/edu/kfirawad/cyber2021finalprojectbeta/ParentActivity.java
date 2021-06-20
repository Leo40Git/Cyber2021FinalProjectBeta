package edu.kfirawad.cyber2021finalprojectbeta;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;
import edu.kfirawad.cyber2021finalprojectbeta.fragment.ChildListFragment;

public class ParentActivity extends UserPermActivity implements ChildListFragment.Callback {
    public ParentActivity() {
        super("CFPB2021:Parent");
    }

    private ChildListFragment childListFrag;
    private NotificationManagerCompat notifyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        findToolbar();

        notifyManager = NotificationManagerCompat.from(getApplicationContext());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layPlaceholder, childListFrag = ChildListFragment.newInstance(this,
                        rideUid, false, (uid, data) -> {
                    if (dbUser == null)
                        return false;
                    return data.parentUid.equals(dbUser.uid);
                }))
                .commit();
    }

    @Override
    protected void onDBUserUpdated() {
        if (childListFrag != null)
            childListFrag.forceUpdate();
    }

    public static final String CHANNEL_ID = "edu.kfirawad.cyber2021finalprojectbeta.notifications.parent";

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Parent";
            String description = "Notifications for parents";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int nId = 0;
    private void pushNotification(String title, String desc) {
        createNotificationChannel();
        Notification n = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(desc)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();
        notifyManager.notify(tag, nId++, n);
    }

    @Override
    protected void onDBRideUpdated() {
        if (dbUser == null)
            return;
        boolean modified = false;
        for (DBRide.ChildData data : dbRide.children.values()) {
            if (!dbUser.uid.equals(data.parentUid))
                continue;
            if (data.pickedUp && !data.notifiedPickedUp) {
                pushNotification(
                        dbRide.name + ": Your child has been picked up!",
                        data.name + " has been picked up by the driver.");
                data.notifiedPickedUp = true;
                modified = true;
            }
            if (data.droppedOff && !data.notifiedDroppedOff) {
                pushNotification(
                        dbRide.name + ": Your child has been dropped off!",
                        data.name + " has been dropped off by the driver.");
                data.notifiedDroppedOff = true;
                modified = true;
            }
            if (data.lateNotifyState == DBRide.ChildData.NOTIFY_STATE_YES) {
                pushNotification(
                        dbRide.name + ": I'll be late!",
                        "The driver is running late. Still, be prepared!");
                data.lateNotifyState = DBRide.ChildData.NOTIFY_STATE_YES_AND_PUSHED;
                modified = true;
            }
            if (data.readyNotifyState == DBRide.ChildData.NOTIFY_STATE_YES) {
                pushNotification(
                        dbRide.name + ": Be ready!",
                        "Make sure " + data.name + " is ready to be picked up!");
                data.readyNotifyState = DBRide.ChildData.NOTIFY_STATE_YES_AND_PUSHED;
                modified = true;
            }
        }
        if (modified)
            dbRefRide.setValue(dbRide);
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return perms.parent;
    }

    @Override
    public void onChildSelected(@NonNull String uid, @NonNull String name,
                                @NonNull String parentUid, @NonNull String parentName,
                                @NonNull String pickupSpot) { }

    @NonNull
    @Override
    public View getChildItemView(@NonNull ViewGroup parent, @Nullable View convertView,
                                 @NonNull String uid, @NonNull String name,
                                 @NonNull String parentUid, @NonNull String parentName,
                                 @NonNull String pickupSpot) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.parent_list_item, parent, false);
        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        tvTitle.setText(name);
        checkBox.setText("Coming Today");
        checkBox.setChecked(dbRide.isChildComingToday(uid));
        boolean canSetChildComingToday = false;
        if (dbRide != null) {
            DBRide.ChildData data = dbRide.children.get(uid);
            if (data != null)
                canSetChildComingToday = !data.pickedUp;
        }
        if (canSetChildComingToday)
            checkBox.setEnabled(false);
        else {
            checkBox.setOnCheckedChangeListener((v, isChecked) -> {
                dbRide.setChildComingToday(uid, isChecked);
                dbRefRide.setValue(dbRide);
            });
        }
        return convertView;
    }
}