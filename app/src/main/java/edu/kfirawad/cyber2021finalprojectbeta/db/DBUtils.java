package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public final class DBUtils {
    private DBUtils() { }

    public static @NonNull DatabaseReference getUserReference(@NonNull FirebaseDatabase db, @NonNull FirebaseUser user) {
        return db.getReference().child("users").child(user.getUid());
    }
}
