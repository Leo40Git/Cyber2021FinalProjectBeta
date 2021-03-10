package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public final class DBUser {
    @Exclude
    public @NonNull String uid;
    public @NotNull String name;
    public @NotNull Map<String, String> rides;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(String, String)} instead.
     */
    @Deprecated
    public DBUser() {
        uid = "";
    }

    public static @NonNull DBUser create(@NonNull String uid, @NonNull String name) {
        DBUser user = new DBUser();
        user.uid = uid;
        user.name = name;
        user.rides = new HashMap<>();
        return user;
    }

    public void addRide(@NonNull DBRide ride) {
        if (rides == null)
            rides = new HashMap<>();
        rides.put(ride.uid, ride.name);
    }

    public void removeRide(@NonNull DBRide ride) {
        if (rides != null)
            rides.remove(ride.uid);
    }

    @IgnoreExtraProperties
    public static final class Permissions {
        public boolean manager;
        public boolean driver;
        public boolean aide;
        public boolean parent;

        /**
         * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
         *     Use {@link #create(boolean, boolean, boolean, boolean)} or {@link #create()} instead.
         */
        @Deprecated
        public Permissions() { }

        public static @NonNull Permissions create(boolean manager, boolean driver, boolean aide, boolean parent) {
            Permissions perms = new Permissions();
            perms.manager = manager;
            perms.driver = driver;
            perms.aide = aide;
            perms.parent = parent;
            return perms;
        }

        public static @NonNull Permissions create() {
            return create(false, false, false, false);
        }
    }
}
