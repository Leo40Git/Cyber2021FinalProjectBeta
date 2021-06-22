package edu.kfirawad.cyber2021finalprojectbeta.service;

import android.location.Location;
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
import java.util.concurrent.atomic.AtomicBoolean;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBLatLng;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUser;

public class UserServiceHandler extends Handler {
    private static final String TAG = "C2021FPB:servhnd:User";

    public interface SimpleLocationListener {
        void onLocationChanged(Location location);
    }

    public interface Hooks {
        void killService();
        void pushNotification(String title, String desc);
        void addLocationListener(SimpleLocationListener listener);
        void removeLocationListener(SimpleLocationListener listener);
    }

    public UserServiceHandler(Looper looper) {
        super(looper);
    }

    private FirebaseAuth fbAuth;
    private FirebaseUser fbUser;
    private FirebaseAuth.AuthStateListener authStateListener;

    private FirebaseDatabase fbDb;

    private SimpleLocationListener locationListener;

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
        
        public DatabaseListener() {
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
                        hooks.killService();
                        return;
                    }
                    if (dbUser == null)
                        return;
                    onDBUserUpdated(rideUids);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "dbRefUser:onCancelled", error.toException());
                }
            });
        }

        private void onDBUserUpdated(@NonNull Set<String> rideUids) {
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
                                onDBRideUpdated(newRef, dbRide);
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

        private final AtomicBoolean sentPickUpNotif = new AtomicBoolean(false);

        private void onDBRideUpdated(@NonNull DatabaseReference dbRefRide, @NonNull DBRide dbRide) {
            if (!removedRides.isEmpty()) {
                for (String removedUid : removedRides) {
                    RefLisPair pair = dbRefRides.remove(removedUid);
                    if (pair != null)
                        pair.cleanup();
                }
                removedRides.clear();
            }

            DBRide.UserData userData = dbRide.users.get(dbUser.uid);
            if (userData == null) {
                removedRides.add(dbRide.uid);
                return;
            }

            boolean modified = false;

            if (userData.pickUpNotifyState == DBRide.NOTIFY_STATE_YES) {
                if (sentPickUpNotif.compareAndSet(false, true)) {
                    if (!userData.perms.driver) {
                        hooks.pushNotification(
                                dbRide.name + ": Ride started!",
                                "The driver has started the ride!");
                    }
                }
                userData.pickUpNotifyState = DBRide.NOTIFY_STATE_YES_AND_PUSHED;
                modified = true;
            }

            if (!DBRide.STATE_ACTIVE_PICKUP.equals(dbRide.state))
                sentPickUpNotif.lazySet(false);

            if (userData.dropOffNotifyState == DBRide.NOTIFY_STATE_YES) {
                if (!userData.perms.driver) {
                    hooks.pushNotification(
                            dbRide.name + ": At the destination!",
                            "The driver has reached the destination!");
                }
                userData.dropOffNotifyState = DBRide.NOTIFY_STATE_YES_AND_PUSHED;
                modified = true;
            }

            if (userData.perms.parent) {
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
                    if (data.lateNotifyState == DBRide.NOTIFY_STATE_YES) {
                        hooks.pushNotification(
                                dbRide.name + ": I'll be late!",
                                "The driver is running late. Still, be prepared!");
                        data.lateNotifyState = DBRide.NOTIFY_STATE_YES_AND_PUSHED;
                        modified = true;
                    }
                    if (data.readyNotifyState == DBRide.NOTIFY_STATE_YES) {
                        hooks.pushNotification(
                                dbRide.name + ": Be ready!",
                                "Make sure " + data.name + " is ready to be picked up!");
                        data.readyNotifyState = DBRide.NOTIFY_STATE_YES_AND_PUSHED;
                        modified = true;
                    }
                }
            }

            if (modified)
                dbRefRide.setValue(dbRide);

            if (userData.perms.driver && DBRide.STATE_ACTIVE_PICKUP.equals(dbRide.state)) {
                if (locationListener == null) {
                    final DatabaseReference dbRefLoc = fbDb.getReference(
                            "rides/" + dbRide.uid + "/driverLocation");
                    hooks.addLocationListener(locationListener = l ->
                            dbRefLoc.setValue(DBLatLng.create(l.getLatitude(), l.getLongitude())));
                }
            } else if (locationListener != null) {
                hooks.removeLocationListener(locationListener);
                locationListener = null;
            }
        }

        public void cleanup() {
            dbRefUser.cleanup();
            for (RefLisPair pair : dbRefRides.values())
                pair.cleanup();
            dbRefRides.clear();
            if (locationListener != null) {
                hooks.removeLocationListener(locationListener);
                locationListener = null;
            }
        }
    }

    private Hooks hooks;
    private DatabaseListener dbLis;

    public static final int ARG1_START = 1000;
    public static final int ARG1_STOP = 2000;

    @Override
    public void handleMessage(@NonNull Message msg) {
        if (msg.arg1 == ARG1_START) {
            if (hooks != null)
                // already running!
                return;

            if (msg.obj instanceof Hooks)
                hooks = (Hooks) msg.obj;
            else
                throw new IllegalArgumentException("Message object is not hooks interface");

            fbAuth = FirebaseAuth.getInstance();
            fbUser = fbAuth.getCurrentUser();
            if (fbUser == null) {
                // not authenticated, kill service
                hooks.killService();
                return;
            }

            authStateListener = auth -> {
                if (auth.getCurrentUser() == null)
                    // (presumably) logged out, kill service
                    hooks.killService();
            };
            fbAuth.addAuthStateListener(authStateListener);

            fbDb = FirebaseDatabase.getInstance();
            dbLis = new DatabaseListener();
        } else if (msg.arg1 == ARG1_STOP)
            cleanup();
    }

    private void cleanup() {
        if (fbAuth != null && authStateListener != null) {
            fbAuth.removeAuthStateListener(authStateListener);
            authStateListener = null;
        }

        if (dbLis != null) {
            dbLis.cleanup();
            dbLis = null;
        }
    }
}
