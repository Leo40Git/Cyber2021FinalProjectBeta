package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a database object with a UID (in a list) that is associated with one or more {@linkplain DBRide rides}.
 */
public abstract class DBRideObject extends DBObject {
    public @NotNull String name = "";
    public @NotNull Map<String, String> rides = new HashMap<>();

    public DBRideObject() {
        super();
    }

    protected void addRide(@NonNull DBRide ride) {
        if (rides == null)
            rides = new HashMap<>();
        rides.put(ride.uid, ride.name);
    }

    protected void removeRide(@NonNull DBRide ride) {
        if (rides != null)
            rides.remove(ride.uid);
    }
}
