package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public final class DBUserPerms {
    public boolean manager;
    public boolean driver;
    public boolean teacher;
    public boolean parent;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(boolean, boolean, boolean, boolean)} or {@link #create()} instead.
     */
    @Deprecated
    public DBUserPerms() { }

    public static @NonNull
    DBUserPerms create(boolean manager, boolean driver, boolean teacher, boolean parent) {
        DBUserPerms perms = new DBUserPerms();
        perms.manager = manager;
        perms.driver = driver;
        perms.teacher = teacher;
        perms.parent = parent;
        return perms;
    }

    public static @NonNull DBUserPerms create() {
        return create(false, false, false, false);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (manager)
            sb.append("Manager, ");
        if (driver)
            sb.append("Driver, ");
        if (teacher)
            sb.append("Teacher, ");
        if (parent)
            sb.append("Parent, ");
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
            return sb.toString();
        }
        return "(none)";
    }
}
