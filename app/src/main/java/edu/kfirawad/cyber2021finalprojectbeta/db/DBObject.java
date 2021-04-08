package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.Exclude;

/**
 * Represents a database object that has a UID (is a member of a list).<p>
 * Database objects that do <em>not</em> have UIDs should not extend this class.
 */
public abstract class DBObject {
    @Exclude
    public @NonNull String uid;

    protected DBObject() {
        uid = "";
    }
}
