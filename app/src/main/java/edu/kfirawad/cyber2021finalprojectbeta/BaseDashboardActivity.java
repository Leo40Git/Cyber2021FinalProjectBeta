package edu.kfirawad.cyber2021finalprojectbeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.kfirawad.cyber2021finalprojectbeta.db.UserPermissions;

public abstract class BaseDashboardActivity extends AppCompatActivity {
    private final String logTag;
    private final int menuItemToHide;

    protected BaseDashboardActivity(String logTag, int menuItemToHide) {
        this.logTag = logTag;
        this.menuItemToHide = menuItemToHide;
    }

    protected FirebaseAuth fbAuth;
    protected FirebaseDatabase fbDb;

    protected UserPermissions userPerms;
    protected DatabaseReference userPermsRef;
    private ValueEventListener userPermsRefListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fbAuth = FirebaseAuth.getInstance();
        fbDb = FirebaseDatabase.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = fbAuth.getCurrentUser();
        if (user == null) {
            toLogin();
            return;
        }

        userPermsRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserPermissions userPerms = null;
                try {
                    userPerms = dataSnapshot.getValue(UserPermissions.class);
                } catch (Exception e) {
                    Log.e(logTag, "userPerms:getValue:exception", e);
                }
                if (userPerms == null) {
                    userPerms = new UserPermissions();
                    userPermsRef.setValue(userPerms);
                }
                BaseDashboardActivity.this.userPerms = userPerms;
                invalidateOptionsMenu();
                onUserPermissionsUpdated();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(logTag, "userPerms:onCancelled", databaseError.toException());
            }
        };
        userPermsRef = UserPermissions.getReference(fbDb, user);
        userPermsRef.addValueEventListener(userPermsRefListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (userPermsRef != null && userPermsRefListener != null) {
            userPermsRef.removeEventListener(userPermsRefListener);
            userPermsRefListener = null;
            userPermsRef = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        setMenuItemEnabled(menu, menuItemToHide, false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (userPerms != null) {
            setMenuItemEnabled(menu, R.id.menuManager, userPerms.manager);
            setMenuItemEnabled(menu, R.id.menuDriver, userPerms.driver);
            setMenuItemEnabled(menu, R.id.menuAide, userPerms.aide);
            setMenuItemEnabled(menu, R.id.menuParent, userPerms.parent);
        }
        return menu.hasVisibleItems();
    }

    private void setMenuItemEnabled(Menu menu, int menuItemId, boolean enabled) {
        MenuItem menuItem = menu.findItem(menuItemId);
        if (menuItem == null)
            return;
        if (menuItemId == menuItemToHide)
            enabled = false;
        menuItem.setEnabled(enabled);
        menuItem.setVisible(enabled);
    }

    protected void toLogin() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void onUserPermissionsUpdated() { }
}
