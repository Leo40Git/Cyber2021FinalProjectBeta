package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public final class DBLatLng {
    public double lat, lng;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(double, double)} instead.
     */
    @Deprecated
    public DBLatLng() { }

    public static @NonNull DBLatLng create(double lat, double lng) {
        DBLatLng ll = new DBLatLng();
        ll.lat = lat;
        ll.lng = lng;
        return ll;
    }
}
