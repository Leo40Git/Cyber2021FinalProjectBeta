package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

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
                return UserListFragment.newInstance(ManagerActivity.this, rideUid,
                        true, dbUser.uid);
            case 1: // Children
                return ChildListFragment.newInstance(ManagerActivity.this, rideUid,
                        true);
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
                               @NonNull DBUserPerms perms) {
        // TODO open details dialog
    }

    @Override
    public void onChildSelected(@NonNull String uid, @NonNull String name,
                                @NonNull String parentUid, @NonNull String parentName,
                                @NonNull String pickupSpot) {
        // TODO open details dialog
    }
}