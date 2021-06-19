package edu.kfirawad.cyber2021finalprojectbeta.db;

import androidx.annotation.NonNull;

import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public final class DBUser extends DBRideObject {
    public @NotNull String email;
    public @NotNull List<Invite> invites;

    /**
     * @deprecated This constructor is only for Firebase Realtime Database serialization.<br>
     *     Use {@link #create(String, String, String)} instead.
     */
    @Deprecated
    public DBUser() {
        super();
        email = "";
        invites = new ArrayList<>();
    }

    public static @NonNull DBUser create(@NonNull String uid, @NonNull String name, @NonNull String email) {
        DBUser user = new DBUser();
        user.uid = uid;
        user.name = name;
        user.email = email;
        return user;
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
