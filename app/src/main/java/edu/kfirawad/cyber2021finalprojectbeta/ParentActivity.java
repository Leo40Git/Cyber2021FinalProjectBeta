package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;
import edu.kfirawad.cyber2021finalprojectbeta.fragment.ChildListFragment;

public class ParentActivity extends UserPermActivity implements ChildListFragment.Callback {
    public ParentActivity() {
        super("CFPB2021:Parent");
    }

    private ChildListFragment childListFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        findToolbar();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layPlaceholder, childListFrag = ChildListFragment.newInstance(this,
                        rideUid, false, (uid, data) -> {
                    if (dbUser == null)
                        return false;
                    return data.parentUid.equals(dbUser.uid);
                }))
                .commit();
    }

    @Override
    protected void onDBUserUpdated() {
        if (childListFrag != null)
            childListFrag.forceUpdate();
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return perms.parent;
    }

    @Override
    public void onChildSelected(@NonNull String uid, @NonNull String name,
                                @NonNull String parentUid, @NonNull String parentName,
                                @NonNull String pickupSpot) { }

    @NonNull
    @Override
    public View getChildItemView(@NonNull ViewGroup parent, @Nullable View convertView,
                                 @NonNull String uid, @NonNull String name,
                                 @NonNull String parentUid, @NonNull String parentName,
                                 @NonNull String pickupSpot) {
        if (convertView == null)
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.parent_list_item, parent, false);
        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        tvTitle.setText(name);
        checkBox.setText("Coming Today");
        checkBox.setChecked(dbRide.isChildComingToday(uid));
        boolean canSetChildComingToday = false;
        if (dbRide != null) {
            DBRide.ChildData data = dbRide.children.get(uid);
            if (data != null)
                canSetChildComingToday = !data.pickedUp;
        }
        if (canSetChildComingToday) {
            checkBox.setEnabled(true);
            checkBox.setOnCheckedChangeListener((v, isChecked) -> {
                dbRide.setChildComingToday(uid, isChecked);
                dbRefRide.setValue(dbRide);
            });
        } else
            checkBox.setEnabled(false);
        return convertView;
    }
}