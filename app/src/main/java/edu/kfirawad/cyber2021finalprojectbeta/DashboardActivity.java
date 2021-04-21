package edu.kfirawad.cyber2021finalprojectbeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
    public static final String RIDE_UID = "RIDE_UID";

    private static final String TAG = "C2021FPB:Dashboard";

    private String rideUid;

    private FirebaseAuth fbAuth;
    private FirebaseDatabase fbDb;
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

        fbAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = fbAuth.getCurrentUser();
        if (fbUser == null) {
            Toast.makeText(this, "Log in first!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        fbDb = FirebaseDatabase.getInstance();

        dbRefUser = fbDb.getReference("users/" + fbUser.getUid());
        dbRefUserL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        dbUser = dataSnapshot.getValue(DBUser.class);
                        dbUser.uid = dbRefUser.getKey();
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
                        dbRide.uid = dbRefRide.getKey();
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
        getMenuInflater().inflate(R.menu.main, menu);
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
        Intent i = null;
        int id = item.getItemId();
        if (id == R.id.menuManager)
            i = new Intent(this, ManagerActivity.class);
        if (i != null) {
            i.putExtra(RIDE_UID, rideUid);
            startActivity(i);
        }
        return true;
    }

    public void onClick_button(View view) {
        Intent i = new Intent(this, MapsActivity.class);
        startActivity(i);
    }
}