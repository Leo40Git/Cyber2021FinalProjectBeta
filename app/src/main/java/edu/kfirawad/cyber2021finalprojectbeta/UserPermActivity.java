package edu.kfirawad.cyber2021finalprojectbeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

public abstract class UserPermActivity extends AppCompatActivity {
    public static final String RIDE_UID = "edu.kfirawad.cyber2021finalprojectbeta.RideUid";

    protected String rideUid;
    protected Toolbar toolbar;

    protected FirebaseDatabase fbDb;
    protected DBUser dbUser;
    protected DatabaseReference dbRefUser;
    protected ValueEventListener dbRefUserL;
    protected DBRide dbRide;
    protected DatabaseReference dbRefRide;
    protected ValueEventListener dbRefRideL;
    protected DBUserPerms oldPerms;

    protected final @NonNull String tag;

    protected UserPermActivity(@NonNull String tag) {
        super();
        this.tag = "C2021FPB:" + tag;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        rideUid = i.getStringExtra(RIDE_UID);
        if (rideUid == null) {
            Toast.makeText(this, "Missing ride UID", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Call this after setting the content view.
     */
    protected void findToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    protected void onDBUserUpdated() { }

    protected void onDBRideUpdated() { }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = fbAuth.getCurrentUser();
        if (fbUser == null) {
            Toast.makeText(this, "Log in first!", Toast.LENGTH_SHORT).show();
            Intent authIntent = new Intent(this, AuthActivity.class);
            authIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(authIntent);
            finish();
            return;
        }

        fbDb = FirebaseDatabase.getInstance();
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
                        toolbar.setTitle(dbUser.name);
                    } catch (Exception e) {
                        dbUser = null;
                        Log.e(tag, "dbRefUserL:onDataChange:getValue:exception", e);
                    }
                    onDBUserUpdated();
                    updateUserPermissions();
                } else {
                    Toast.makeText(UserPermActivity.this, "User does not exist!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(tag, "dbRefUserL:onCancelled", databaseError.toException());
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
                        Log.e(tag, "dbRefRideL:onDataChange:getValue:exception", e);
                    }
                    onDBRideUpdated();
                    updateUserPermissions();
                } else {
                    Toast.makeText(UserPermActivity.this, "Ride does not exist!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(tag, "dbRefRideL:onCancelled", databaseError.toException());
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

    protected @MenuRes int getOptionsMenu() {
        return R.menu.dashboard;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getOptionsMenu(), menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        DBUserPerms userPerms = null;
        if (dbUser != null && dbRide != null)
            userPerms = dbRide.getUserPerms(dbUser);
        if (userPerms == null)
            userPerms = DBUserPerms.create();
        setMenuItemEnabled(menu, R.id.menuManager, userPerms.manager
                && getClass() != ManagerActivity.class);
        setMenuItemEnabled(menu, R.id.menuDriver, userPerms.driver
                && getClass() != DriverActivity.class);
        setMenuItemEnabled(menu, R.id.menuTeacher, userPerms.teacher
                && getClass() != TeacherActivity.class);
        setMenuItemEnabled(menu, R.id.menuParent, userPerms.parent
                && getClass() != ParentActivity.class);
        return menu.hasVisibleItems();
    }

    protected abstract boolean hasRequiredPermission(@NonNull DBUserPerms perms);

    private void updateUserPermissions() {
        if (dbUser == null || dbRide == null)
            return;
        DBUserPerms newPerms = dbRide.getUserPerms(dbUser);
        if (!Objects.equals(oldPerms, newPerms)) {
            oldPerms = newPerms;
            if (hasRequiredPermission(oldPerms))
                supportInvalidateOptionsMenu();
            else {
                Toast.makeText(this, "Lost required permission for this activity!", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    protected final void setMenuItemEnabled(Menu menu, @IdRes int menuItemId, boolean enabled) {
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
            if (getClass() != ManagerActivity.class) {
                Intent i = new Intent(this, ManagerActivity.class);
                startActivity(i);
            }
            return true;
        } else if (id == R.id.menuDriver) {
            if (getClass() != DriverActivity.class) {
                Intent i = new Intent(this, DriverActivity.class);
                startActivity(i);
            }
            return true;
        } else if (id == R.id.menuTeacher) {
            if (getClass() != TeacherActivity.class) {
                Intent i = new Intent(this, TeacherActivity.class);
                startActivity(i);
            }
            return true;
        } else if (id == R.id.menuParent) {
            if (getClass() != ParentActivity.class) {
                Intent i = new Intent(this, ParentActivity.class);
                startActivity(i);
            }
            return true;
        } else if (id == R.id.menuCredits) {
            Intent i = new Intent(this, CreditsActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.menuExit) {
            Intent i = new Intent(this, RideSelectActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
