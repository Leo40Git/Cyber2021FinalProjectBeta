package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public final class DBChild {
    @Exclude
    public @NonNull String uid;
    public @NotNull String name;
    public @NotNull Map<String, String> rides;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(String, String)} instead.
     */
    @Deprecated
    public DBChild() {
        uid = "";
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

    public static @NonNull DBChild create(@NonNull String uid, @NonNull String name) {
        DBChild child = new DBChild();
        child.uid = uid;
        child.name = name;
        child.rides = new HashMap<>();
        return child;
    }
}
