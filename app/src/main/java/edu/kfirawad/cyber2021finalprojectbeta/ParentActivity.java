package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;
import edu.kfirawad.cyber2021finalprojectbeta.fragment.ChildListFragment;

public class ParentActivity extends UserPermActivity implements ChildListFragment.Callback {
    public ParentActivity() {
        super("CFPB2021:Parent");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        findToolbar();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layPlaceholder, ChildListFragment.newInstance(this,
                        (uid, data) -> data.parentUid.equals(dbUser.uid), rideUid, false))
                .commit();
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
                    .inflate(R.layout.list_item_checkbox, parent, false);
        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvDesc = convertView.findViewById(R.id.tvDesc);
        CheckBox checkBox = convertView.findViewById(R.id.checkBox);
        tvTitle.setText(name);
        tvDesc.setText(parentName);
        checkBox.setText("Won't be Coming Today");
        return convertView;
    }
}