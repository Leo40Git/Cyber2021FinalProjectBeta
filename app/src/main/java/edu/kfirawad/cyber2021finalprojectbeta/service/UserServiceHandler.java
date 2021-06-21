package edu.kfirawad.cyber2021finalprojectbeta.service;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUser;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;

public class UserServiceHandler extends Handler {
    private static final String TAG = "C2021FPB:servhnd:User";

    public interface Hooks {
        void killService();
        void pushNotification(String title, String desc);
    }

    public UserServiceHandler(Looper looper) {
        super(looper);
    }

    private FirebaseAuth fbAuth;
    private FirebaseUser fbUser;
    private FirebaseAuth.AuthStateListener authStateListener;

    private FirebaseDatabase fbDb;

    public static final class RefLisPair {
        public final DatabaseReference ref;
        private ValueEventListener vel;

        public RefLisPair(DatabaseReference ref, ValueEventListener vel) {
            this.ref = ref;
            this.vel = ref.addValueEventListener(vel);
        }

        public void cleanup() {
            if (vel != null) {
                ref.removeEventListener(vel);
                vel = null;
            }
        }
    }
    
    private final class DatabaseListener {
        private final RefLisPair dbRefUser;
        private DBUser dbUser;

        private final HashMap<String, RefLisPair> dbRefRides = new HashMap<>();
        private final HashSet<String> removedRides = new HashSet<>();
        
        public DatabaseListener(@NonNull Hooks hooks) {
            final String userId = fbUser.getUid();
            dbRefUser = new RefLisPair(fbDb.getReference("users/" + userId), new ValueEventListener() {
                private final HashSet<String> rideUids = new HashSet<>();

                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try {
                            dbUser = snapshot.getValue(DBUser.class);
                            if (dbUser == null) {
                                dbUser = DBUser.create(userId,
                                        Objects.requireNonNull(fbUser.getEmail()),
                                        Objects.requireNonNull(fbUser.getDisplayName()));
                                dbRefUser.ref.setValue(dbUser);
                            }
                            dbUser.uid = userId;
                        } catch (Exception e) {
                            dbUser = null;
                            Log.e(TAG, "dbRefUser:onDataChange:getValue:exception", e);
                        }
                    } else {
                        // user does not exist, kill service
                        killService(hooks);
                        return;
                    }
                    if (dbUser == null)
                        return;
                    onDBUserUpdated(hooks, rideUids);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "dbRefUser:onCancelled", error.toException());
                }
            });
        }

        private void onDBUserUpdated(@NonNull Hooks hooks, @NonNull Set<String> rideUids) {
            rideUids.clear();
            rideUids.addAll(dbUser.rides.keySet());
            Iterator<Map.Entry<String, RefLisPair>> it = dbRefRides.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, RefLisPair> entry = it.next();
                if (!rideUids.remove(entry.getKey())) {
                    entry.getValue().cleanup();
                    it.remove();
                }
            }
            for (String newUid : rideUids) {
                DatabaseReference newRef = fbDb.getReference("rides/" + newUid);
                RefLisPair newPair = new RefLisPair(newRef, new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        DBRide dbRide;
                        if (snapshot.exists()) {
                            try {
                                dbRide = snapshot.getValue(DBRide.class);
                                if (dbRide == null)
                                    throw new NullPointerException("Ride \"" + newUid + "\" does not exist?!");
                                dbRide.uid = newUid;
                            } catch (Exception e) {
                                dbRide = null;
                                Log.e(TAG, "dbRefRide:onDataChange:getValue:exception", e);
                            }
                            if (dbRide != null)
                                onDBRideUpdated(hooks, newRef, dbRide);
                        } else
                            removedRides.add(newUid);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "dbRefRide:onCancelled", error.toException());
                    }
                });
                dbRefRides.put(newUid, newPair);
            }
        }

        private void onDBRideUpdated(@NonNull Hooks hooks,
                                     @NonNull DatabaseReference dbRefRide, @NonNull DBRide dbRide) {
            if (!removedRides.isEmpty()) {
                for (String removedUid : removedRides) {
                    RefLisPair pair = dbRefRides.remove(removedUid);
                    if (pair != null)
                        pair.cleanup();
                }
                removedRides.clear();
            }

            DBUserPerms perms = dbRide.getUserPerms(dbUser);
            if (perms == null) {
                removedRides.add(dbRide.uid);
                return;
            }

            boolean modified = false;

            if (perms.parent) {
                for (DBRide.ChildData data : dbRide.children.values()) {
                    if (!dbUser.uid.equals(data.parentUid))
                        continue;
                    if (data.pickedUp && !data.notifiedPickedUp) {
                        hooks.pushNotification(
                                dbRide.name + ": Your child has been picked up!",
                                data.name + " has been picked up by the driver.");
                        data.notifiedPickedUp = true;
                        modified = true;
                    }
                    if (data.droppedOff && !data.notifiedDroppedOff) {
                        hooks.pushNotification(
                                dbRide.name + ": Your child has been dropped off!",
                                data.name + " has been dropped off by the driver.");
                        data.notifiedDroppedOff = true;
                        modified = true;
                    }
                    if (data.lateNotifyState == DBRide.ChildData.NOTIFY_STATE_YES) {
                        hooks.pushNotification(
                                dbRide.name + ": I'll be late!",
                                "The driver is running late. Still, be prepared!");
                        data.lateNotifyState = DBRide.ChildData.NOTIFY_STATE_YES_AND_PUSHED;
                        modified = true;
                    }
                    if (data.readyNotifyState == DBRide.ChildData.NOTIFY_STATE_YES) {
                        hooks.pushNotification(
                                dbRide.name + ": Be ready!",
                                "Make sure " + data.name + " is ready to be picked up!");
                        data.readyNotifyState = DBRide.ChildData.NOTIFY_STATE_YES_AND_PUSHED;
                        modified = true;
                    }
                }
            }

            if (modified)
                dbRefRide.setValue(dbRide);
        }

        public void cleanup() {
            dbRefUser.cleanup();
            for (RefLisPair pair : dbRefRides.values())
                pair.cleanup();
            dbRefRides.clear();
        }
    }

    private DatabaseListener dbLis;

    @Override
    public void handleMessage(@NonNull Message msg) {
        Hooks hooks;
        if (msg.obj instanceof Hooks)
            hooks = (Hooks) msg.obj;
        else
            throw new IllegalArgumentException("Message object is not hooks interface");

        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();
        if (fbUser == null) {
            // not authenticated, kill service
            killService(hooks);
            return;
        }

        authStateListener = auth -> {
            if (auth.getCurrentUser() == null)
                // (presumably) logged out, kill service
                killService(hooks);
        };
        fbAuth.addAuthStateListener(authStateListener);

        fbDb = FirebaseDatabase.getInstance();
        dbLis = new DatabaseListener(hooks);
    }

    private void killService(@NonNull Hooks hooks) {
        if (fbAuth != null && authStateListener != null) {
            fbAuth.removeAuthStateListener(authStateListener);
            authStateListener = null;
        }

        if (dbLis != null) {
            dbLis.cleanup();
            dbLis = null;
        }

        hooks.killService();
    }
}
