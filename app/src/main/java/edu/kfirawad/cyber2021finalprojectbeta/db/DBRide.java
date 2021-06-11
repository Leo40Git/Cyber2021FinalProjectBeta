package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public final class DBRide extends DBObject {
    public @NotNull String name, destination;
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
        ride.users = new HashMap<>();
        ride.children = new HashMap<>();
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
        users.put(user.uid, UserData.create(user.name, initialPerms));
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
                ChildData.create(child.name, child.parentUid, child.parentName, child.pickupSpot));
        child.addRide(this);
    }

    public void removeChild(@NonNull DBChild child) {
        if (children != null)
            children.remove(child.uid);
        child.removeRide(this);
    }

    @IgnoreExtraProperties
    public static final class UserData {
        public @NotNull String name;
        public @NotNull DBUserPerms perms;

        /**
         * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
         *     Use {@link #create(String, DBUserPerms)} instead.
         */
        @Deprecated
        public UserData() { }

        public static @NonNull UserData create(@NonNull String name, @NonNull DBUserPerms perms) {
            UserData data = new UserData();
            data.name = name;
            data.perms = perms;
            return data;
        }
    }

    @IgnoreExtraProperties
    public static final class ChildData {
        public @NotNull String name, parentUid, parentName, pickupSpot;

        /**
         * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
         *     Use {@link #create(String, String, String, String)} instead.
         */
        @Deprecated
        public ChildData() { }

        public static @NonNull ChildData create(@NonNull String name,
                                                @NonNull String parentUid, @NonNull String parentName,
                                                @NonNull String pickupSpot) {
            ChildData data = new ChildData();
            data.name = name;
            data.parentUid = parentUid;
            data.parentName = parentName;
            data.pickupSpot = pickupSpot;
            return data;
        }
    }
}
