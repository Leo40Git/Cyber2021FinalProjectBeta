package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public final class DBUserPerms {
    public boolean manager;
    public boolean driver;
    public boolean aide;
    public boolean parent;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(boolean, boolean, boolean, boolean)} or {@link #create()} instead.
     */
    @Deprecated
    public DBUserPerms() { }

    public static @NonNull
    DBUserPerms create(boolean manager, boolean driver, boolean aide, boolean parent) {
        DBUserPerms perms = new DBUserPerms();
        perms.manager = manager;
        perms.driver = driver;
        perms.aide = aide;
        perms.parent = parent;
        return perms;
    }

    public static @NonNull DBUserPerms create() {
        return create(false, false, false, false);
    }
}
