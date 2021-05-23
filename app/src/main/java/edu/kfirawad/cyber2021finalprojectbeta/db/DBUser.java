package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public final class DBUser extends DBRideObject {
    public @NotNull String email;
    public @NotNull Map<String, String> children;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(String, String, String)} instead.
     */
    @Deprecated
    public DBUser() {
        super();
    }

    public static @NonNull DBUser create(@NonNull String uid, @NonNull String name, @NonNull String email) {
        DBUser user = new DBUser();
        user.uid = uid;
        user.name = name;
        user.email = email;
        user.rides = new HashMap<>();
        user.children = new HashMap<>();
        return user;
    }

    protected void addChild(@NonNull DBChild child) {
        if (children == null)
            children = new HashMap<>();
        children.put(child.uid, child.name);
    }

    public void removeChild(@NonNull DBChild child) {
        if (children == null)
            return;
        children.remove(child.uid);
    }
}
