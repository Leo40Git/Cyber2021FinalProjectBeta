package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public final class UserPermissions {
    public boolean manager;
    public boolean driver;
    public boolean aide;
    public boolean parent;

    public UserPermissions() { }

    public static @NonNull DatabaseReference getReference(@NonNull FirebaseDatabase db, @NonNull FirebaseUser user) {
        return DBUtils.getUserReference(db, user).child("permissions");
    }
}
