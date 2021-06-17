package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@IgnoreExtraProperties
public final class DBUser extends DBRideObject {
    public @NotNull String email;
    public @NotNull Map<String, String> children;
    public @NotNull List<Invite> invites;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(String, String, String)} instead.
     */
    @Deprecated
    public DBUser() {
        super();
        email = "";
        children = new HashMap<>();
        invites = new ArrayList<>();
    }

    public static @NonNull DBUser create(@NonNull String uid, @NonNull String name, @NonNull String email) {
        DBUser user = new DBUser();
        user.uid = uid;
        user.name = name;
        user.email = email;
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

    @IgnoreExtraProperties
    public static final class Invite {
        public @NotNull String rideUid, rideName;
        public @NotNull DBUserPerms perms;

        /**
         * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
         *     Use {@link #create(String, String, DBUserPerms)} instead.
         */
        @Deprecated
        public Invite() {
            perms = DBUserPerms.create();
        }

        public static @NonNull Invite create(@NonNull String rideUid, @NonNull String rideName,
                                             @NonNull DBUserPerms perms) {
            Invite invite = new Invite();
            invite.rideUid = rideUid;
            invite.rideName = rideName;
            invite.perms = perms;
            return invite;
        }
    }
}
