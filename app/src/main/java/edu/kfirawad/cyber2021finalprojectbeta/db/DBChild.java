package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;

@IgnoreExtraProperties
public final class DBChild extends DBRideObject {
    public @NotNull String parentUid;
    public @NotNull String parentName;
    public @NotNull DBLatLng pickupSpot;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(String, String, DBUser, DBLatLng)} instead.
     */
    @Deprecated
    public DBChild() {
        super();
    }

    public static @NonNull DBChild create(@NonNull String uid, @NonNull String name,
                                          @NonNull DBUser parent, @NonNull DBLatLng pickupSpot) {
        DBChild child = new DBChild();
        child.uid = uid;
        child.name = name;
        child.rides = new HashMap<>();
        child.parentUid = parent.uid;
        child.parentName = parent.name;
        parent.addChild(child);
        child.pickupSpot = pickupSpot;
        return child;
    }
}
