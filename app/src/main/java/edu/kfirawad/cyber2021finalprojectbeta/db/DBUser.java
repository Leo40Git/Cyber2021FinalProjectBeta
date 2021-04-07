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
    public @NotNull String email;
    public @NotNull Map<String, String> rides;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(String, String, String)} instead.
     */
    @Deprecated
    public DBUser() {
        uid = "";
    }

    public static @NonNull DBUser create(@NonNull String uid, @NonNull String name, @NonNull String email) {
        DBUser user = new DBUser();
        user.uid = uid;
        user.name = name;
        user.email = email;
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

}
