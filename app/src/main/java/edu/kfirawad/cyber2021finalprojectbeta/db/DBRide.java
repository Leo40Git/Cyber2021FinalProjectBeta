package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public final class DBRide {
    @Exclude
    public @NonNull String uid;
    public @NotNull String name;
    public @NotNull Map<String, UserData> users;
    public @NotNull Map<String, String> children;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(String, String)} instead.
     */
    @Deprecated
    public DBRide() {
        uid = "";
    }

    public static @NonNull DBRide create(@NonNull String uid, @NonNull String name) {
        DBRide ride = new DBRide();
        ride.uid = uid;
        ride.name = name;
        ride.users = new HashMap<>();
        ride.children = new HashMap<>();
        return ride;
    }

    public boolean hasUser(@NonNull DBUser user) {
        if (users == null)
            return false;
        return users.containsKey(user.uid);
    }

    public void addUser(@NonNull DBUser user, @NonNull DBUser.Permissions initialPerms) {
        if (users == null)
            users = new HashMap<>();
        users.put(user.uid, UserData.create(user.name, initialPerms));
        user.addRide(this);
    }

    public void addUser(@NonNull DBUser user) {
        addUser(user, DBUser.Permissions.create());
    }

    public void removeUser(@NonNull DBUser user) {
        if (users != null)
            users.remove(user.uid);
        user.removeRide(this);
    }

    public @Nullable DBUser.Permissions getUserPerms(@NonNull DBUser user) {
        if (users == null)
            return null;
        UserData data = users.get(user.uid);
        if (data == null)
            return null;
        return data.permissions;
    }

    public @NonNull DBUser.Permissions getOrCreateUserPerms(@NonNull DBUser user) {
        DBUser.Permissions perms = getUserPerms(user);
        if (perms == null) {
            perms = DBUser.Permissions.create();
            setUserPerms(user, perms);
        }
        return perms;
    }

    public void setUserPerms(@NonNull DBUser user, @NonNull DBUser.Permissions perms) {
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
        children.put(child.uid, child.name);
        child.addRide(this);
    }

    public void removeChild(@NonNull DBChild child) {
        if (children != null)
            children.remove(child.uid);
        child.removeRide(this);
    }

    @IgnoreExtraProperties
    public static final class UserData {
        public String name;
        public DBUser.Permissions permissions;

        /**
         * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
         *     Use {@link #create(String, String)} instead.
         */
        @Deprecated
        public UserData() { }

        public static @NonNull UserData create(@NonNull String name, @NonNull DBUser.Permissions permissions) {
            UserData data = new UserData();
            data.name = name;
            data.permissions = permissions;
            return data;
        }
    }
}
