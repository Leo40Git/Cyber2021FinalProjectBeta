package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.annotations.NotNull;

@IgnoreExtraProperties
public final class DBChild extends DBRideObject {
    public @NotNull String parentUid, parentName, pickupSpot;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(String, String, DBUser, String)} instead.
     */
    @Deprecated
    public DBChild() {
        super();
        parentUid = "";
        parentName = "";
        pickupSpot = "";
    }

    public static @NonNull DBChild create(@NonNull String uid, @NonNull String name,
                                          @NonNull DBUser parent, @NonNull String pickupSpot) {
        DBChild child = new DBChild();
        child.uid = uid;
        child.name = name;
        child.parentUid = parent.uid;
        child.parentName = parent.name;
        child.pickupSpot = pickupSpot;
        return child;
    }
}
