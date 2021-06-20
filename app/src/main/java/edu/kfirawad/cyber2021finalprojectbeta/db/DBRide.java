package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public final class DBRide extends DBObject {
    public static final String STATE_INACTIVE = "inactive";
    public static final String STATE_ACTIVE_PICKUP = "active_pickUp";
    public static final String STATE_ACTIVE_DROPOFF = "active_dropOff";

    public @NotNull String name, destination, state;
    public int startHour, startMinute;
    public @NotNull Map<String, UserData> users;
    public @NotNull Map<String, ChildData> children;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(String, String, String, int, int)} instead.
     */
    @Deprecated
    public DBRide() {
        super();
        name = "";
        destination = "";
        state = STATE_INACTIVE;
        users = new HashMap<>();
        children = new HashMap<>();
    }

    public static @NonNull DBRide create(@NonNull String uid,
                                         @NonNull String name, @NonNull String destination,
                                         int startHour, int startMinute) {
        DBRide ride = new DBRide();
        ride.uid = uid;
        ride.name = name;
        ride.destination = destination;
        ride.startHour = startHour;
        ride.startMinute = startMinute;
        return ride;
    }

    public boolean hasUser(@NonNull DBUser user) {
        if (users == null)
            return false;
        return users.containsKey(user.uid);
    }

    public void addUser(@NonNull DBUser user, @NonNull DBUserPerms initialPerms) {
        if (users == null)
            users = new HashMap<>();
        users.put(user.uid, UserData.create(user.name, user.email, initialPerms));
        user.addRide(this);
    }

    public void addUser(@NonNull DBUser user) {
        addUser(user, DBUserPerms.create());
    }

    public void removeUser(@NonNull DBUser user) {
        if (users != null)
            users.remove(user.uid);
        user.removeRide(this);
    }

    public @Nullable DBUserPerms getUserPerms(@NonNull DBUser user) {
        if (users == null)
            return null;
        UserData data = users.get(user.uid);
        if (data == null)
            return null;
        return data.perms;
    }

    public @NonNull DBUserPerms getOrCreateUserPerms(@NonNull DBUser user) {
        DBUserPerms perms = getUserPerms(user);
        if (perms == null) {
            perms = DBUserPerms.create();
            setUserPerms(user, perms);
        }
        return perms;
    }

    public void setUserPerms(@NonNull DBUser user, @NonNull DBUserPerms perms) {
        addUser(user, perms);
    }

    public boolean hasChild(@NonNull DBChild child) {
        if (children == null)
            return false;
        return children.containsKey(child.uid);
    }

    public void addChild(@NonNull DBChild child) {
        if (children == null)
            children = new HashMap<>();
        children.put(child.uid,
                ChildData.create(child.name, child.parentUid, child.parentName, child.pickupSpot, true));
    }

    public void removeChild(@NonNull DBChild child) {
        if (children != null)
            children.remove(child.uid);
    }

    public boolean isChildComingToday(@NotNull String uid) {
        ChildData data = children.get(uid);
        if (data == null)
            return false;
        return data.comingToday;
    }

    public void setChildComingToday(@NotNull String uid, boolean comingToday) {
        ChildData data = children.get(uid);
        if (data == null)
            return;
        data.comingToday = comingToday;
    }

    public boolean isChildComingToday(@NotNull DBChild child) {
        return isChildComingToday(child.uid);
    }

    public void setChildComingToday(@NotNull DBChild child, boolean comingToday) {
        setChildComingToday(child.uid, comingToday);
    }

    public void startPickUp() {
        if (!STATE_INACTIVE.equals(state))
            return;
        state = STATE_ACTIVE_PICKUP;
        // TODO notify driver
        // TODO notify parents and aides
    }

    public void finishPickUpAndStartDropOff() {
        if (!STATE_ACTIVE_PICKUP.equals(state))
            return;
        state = STATE_ACTIVE_DROPOFF;
        // TODO notify parents and aides
    }

    public void finishDropOff() {
        if (!STATE_ACTIVE_DROPOFF.equals(state))
            return;
        state = STATE_INACTIVE;
        for (ChildData data : children.values())
            data.reset();
    }

    @IgnoreExtraProperties
    public static final class UserData {
        public @NotNull String name, email;
        public @NotNull DBUserPerms perms;

        /**
         * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
         *     Use {@link #create(String, String, DBUserPerms)} instead.
         */
        @Deprecated
        public UserData() {
            name = "";
            email = "";
            perms = DBUserPerms.create();
        }

        public static @NonNull UserData create(@NonNull String name, @NonNull String email, @NonNull DBUserPerms perms) {
            UserData data = new UserData();
            data.name = name;
            data.email = email;
            data.perms = perms;
            return data;
        }
    }

    @IgnoreExtraProperties
    public static final class ChildData {
        public static final int NOT_NOTIFIED = 0;
        public static final int NOTIFIED = 1;
        public static final int NOTIFIED_AND_PUSHED = 2;

        public @NotNull String name, parentUid, parentName, pickupSpot;
        public boolean comingToday, pickedUp, droppedOff;
        public int notifiedLate, notifiedReady;

        /**
         * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
         *     Use {@link #create(String, String, String, String, boolean)} instead.
         */
        @Deprecated
        public ChildData() {
            name = "";
            parentUid = "";
            parentName = "";
            pickupSpot = "";
            reset();
        }

        public static @NonNull ChildData create(@NonNull String name,
                                                @NonNull String parentUid, @NonNull String parentName,
                                                @NonNull String pickupSpot,
                                                boolean comingToday) {
            ChildData data = new ChildData();
            data.name = name;
            data.parentUid = parentUid;
            data.parentName = parentName;
            data.pickupSpot = pickupSpot;
            data.comingToday = comingToday;
            return data;
        }

        public void reset() {
            comingToday = true;
            pickedUp = false;
            droppedOff = false;
            notifiedLate = NOT_NOTIFIED;
            notifiedReady = NOT_NOTIFIED;
        }
    }
}
