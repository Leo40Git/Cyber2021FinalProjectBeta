package edu.kfirawad.cyber2021finalprojectbeta;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBUser;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;
import edu.kfirawad.cyber2021finalprojectbeta.fragment.ChildListFragment;
import edu.kfirawad.cyber2021finalprojectbeta.fragment.UserListFragment;

public class ManagerActivity extends UserPermActivity implements UserListFragment.Callback, ChildListFragment.Callback {
    final class PageAdater extends FragmentStatePagerAdapter {
        public PageAdater(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
            case 0: // Users
                return UserListFragment.newInstance(ManagerActivity.this, rideUid, true,
                        () -> {
                    if (dbUser == null)
                        return null;
                    return dbUser.uid;
                });
            case 1: // Children
                return ChildListFragment.newInstance(ManagerActivity.this, rideUid, true);
            default:
                throw new IndexOutOfBoundsException(position + "");
            }
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case 0: // Users
                return "Users";
            case 1: // Children
                return "Children";
            default:
                return null;
            }
        }
    }

    public ManagerActivity() {
        super("CFPB2021:Manager");
    }

    private ViewPager pager;
    private TabLayout tabLayout;
    private PageAdater pageAdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        findToolbar();

        pageAdater = new PageAdater(getSupportFragmentManager());
        pager = findViewById(R.id.pager);
        pager.setAdapter(pageAdater);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return perms.manager;
    }

    @Override
    public void onUserSelected(@NonNull String uid, @NonNull String name,
                               @NonNull String email, @NonNull DBUserPerms perms) {
        new AlertDialog.Builder(this)
                .setTitle("User Details")
                .setMessage(Html.fromHtml(
                        "<b>Name:</b> " + name + "<br>"
                                + "<b>E-mail:</b> " + email + "<br>"
                ))
                .setNegativeButton("Remove", (dialog, which) -> new AlertDialog.Builder(ManagerActivity.this)
                        .setTitle("Confirm Removal")
                        .setMessage(Html.fromHtml(
                                "Are you <b>sure</b> you want to remove " + name + "?"
                        ))
                        .setNeutralButton("No", (dialog1, which1) -> dialog1.dismiss())
                        .setNegativeButton("Yes", (dialog1, which1) -> {
                            dialog1.dismiss();
                            dialog.dismiss();
                            DatabaseReference dbRefUser = fbDb.getReference("users/" + uid);
                            dbRefUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        try {
                                            DBUser user = snapshot.getValue(DBUser.class);
                                            if (user == null)
                                                throw new NullPointerException("wot (" + uid + ")");
                                            user.uid = uid;
                                            dbRide.removeUser(user);
                                            dbRefRide.setValue(dbRide);
                                            dbRefUser.setValue(user);
                                            Toast.makeText(ManagerActivity.this, "Removed user" + name, Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Log.e(tag, "onUserSelected:singleUserL:onDataChange:getValue", e);
                                        }
                                    } else
                                        Toast.makeText(ManagerActivity.this, "User does not exist?!", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(tag, "onUserSelected:singleUserL:onCancelled", error.toException());
                                    Toast.makeText(ManagerActivity.this, "Failed to remove user!", Toast.LENGTH_LONG).show();
                                }
                            });
                        })
                        .show())
                .show();
    }

    @Override
    public void onAddUserButtonClicked() {
        Intent i = new Intent(this, AddUserActivity.class);
        i.putExtra(RIDE_UID, rideUid);
        startActivity(i);
    }

    @Override
    public void onChildSelected(@NonNull String uid, @NonNull String name,
                                @NonNull String parentUid, @NonNull String parentName,
                                @NonNull String pickupSpot) {
        new AlertDialog.Builder(this)
                .setTitle("Child Details")
                .setMessage(Html.fromHtml(
                        "<b>Name:</b> " + name + "<br>"
                                + "<b>Parent:</b> " + parentName + "<br>"
                                + "<b>Pickup Spot:</b> " + pickupSpot
                ))
                .setNegativeButton("Delete", (dialog, which) -> {
                    new AlertDialog.Builder(ManagerActivity.this)
                            .setTitle("Confirm Deletion")
                            .setMessage(Html.fromHtml(
                                    "Are you <b>sure</b> you want to delete " + name + "?"
                            ))
                            .setNeutralButton("No", (dialog1, which1) -> dialog1.dismiss())
                            .setNegativeButton("Yes", (dialog1, which1) -> {
                                dialog1.dismiss();
                                dialog.dismiss();
                                dbRide.children.remove(uid);
                                dbRefRide.setValue(dbRide);
                            })
                            .show();
                })
                .show();
    }

    @Override
    public void onCreateChildButtonPressed() {
        Intent i = new Intent(this, CreateChildActivity.class);
        i.putExtra(RIDE_UID, rideUid);
        startActivity(i);
    }
}