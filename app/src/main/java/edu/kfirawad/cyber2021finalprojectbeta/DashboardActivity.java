package edu.kfirawad.cyber2021finalprojectbeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUser;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;

public class DashboardActivity extends AppCompatActivity {
    public static final String RIDE_UID = "edu.kfirawad.cyber2021finalprojectbeta.RideUid";

    private static final String TAG = "C2021FPB:Dashboard";

    private String rideUid;

    private DBUser dbUser;
    private DatabaseReference dbRefUser;
    private ValueEventListener dbRefUserL;
    private DBRide dbRide;
    private DatabaseReference dbRefRide;
    private ValueEventListener dbRefRideL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Intent i = getIntent();
        rideUid = i.getStringExtra(RIDE_UID);
        if (rideUid == null) {
            Toast.makeText(this, "Missing ride UID", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = fbAuth.getCurrentUser();
        if (fbUser == null) {
            Toast.makeText(this, "Log in first!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        FirebaseDatabase fbDb = FirebaseDatabase.getInstance();

        final String userId = fbUser.getUid();
        dbRefUser = fbDb.getReference("users/" + userId);
        dbRefUserL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        dbUser = dataSnapshot.getValue(DBUser.class);
                        if (dbUser == null) {
                            dbUser = DBUser.create(userId,
                                    Objects.requireNonNull(fbUser.getEmail()),
                                    Objects.requireNonNull(fbUser.getDisplayName()));
                            dbRefUser.setValue(dbUser);
                        }
                        dbUser.uid = userId;
                    } catch (Exception e) {
                        dbUser = null;
                        Log.e(TAG, "dbRefUserL:onDataChange:getValue:exception", e);
                    }
                    updateUserPermissions();
                } else {
                    Toast.makeText(DashboardActivity.this, "User does not exist!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "dbRefUserL:onCancelled", databaseError.toException());
            }
        };
        dbRefUser.addValueEventListener(dbRefUserL);

        dbRefRide = fbDb.getReference("rides/" + rideUid);
        dbRefRideL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        dbRide = dataSnapshot.getValue(DBRide.class);
                        if (dbRide == null)
                            throw new NullPointerException("Ride \"" + rideUid + "\" does not exist?!");
                        dbRide.uid = rideUid;
                    } catch (Exception e) {
                        dbRide = null;
                        Log.e(TAG, "dbRefRideL:onDataChange:getValue:exception", e);
                    }
                    updateUserPermissions();
                } else {
                    Toast.makeText(DashboardActivity.this, "Ride does not exist!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "dbRefRideL:onCancelled", databaseError.toException());
            }
        };
        dbRefRide.addValueEventListener(dbRefRideL);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (dbRefUser != null && dbRefRideL != null) {
            dbRefUser.removeEventListener(dbRefUserL);
            dbRefUserL = null;
            dbRefUser = null;
        }

        if (dbRefRide != null && dbRefRideL != null) {
            dbRefRide.removeEventListener(dbRefRideL);
            dbRefRideL = null;
            dbRefRide = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        DBUserPerms userPerms = null;
        if (dbUser != null && dbRide != null)
            userPerms = dbRide.getUserPerms(dbUser);
        if (userPerms == null)
            userPerms = DBUserPerms.create();
        setMenuItemEnabled(menu, R.id.menuManager, userPerms.manager);
        setMenuItemEnabled(menu, R.id.menuDriver, userPerms.driver);
        setMenuItemEnabled(menu, R.id.menuAide, userPerms.aide);
        setMenuItemEnabled(menu, R.id.menuParent, userPerms.parent);
        return menu.hasVisibleItems();
    }

    private DBUserPerms oldPerms;

    private void updateUserPermissions() {
        if (dbUser == null || dbRide == null)
            return;
        DBUserPerms newPerms = dbRide.getUserPerms(dbUser);
        if (!Objects.equals(oldPerms, newPerms)) {
            oldPerms = newPerms;
            supportInvalidateOptionsMenu();
        }
    }

    private void setMenuItemEnabled(Menu menu, @IdRes int menuItemId, boolean enabled) {
        MenuItem menuItem = menu.findItem(menuItemId);
        if (menuItem == null)
            return;
        menuItem.setVisible(enabled);
        menuItem.setEnabled(enabled);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuManager) {
            // TODO manager activity
            return true;
        } else if (id == R.id.menuDriver) {
            // TODO driver activity
            return true;
        } else if (id == R.id.menuAide) {
            // TODO aide activity
            return true;
        } else if (id == R.id.menuParent) {
            // TODO parent activity
            return true;
        } else if (id == R.id.menuSettings) {
            // TODO settings
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}