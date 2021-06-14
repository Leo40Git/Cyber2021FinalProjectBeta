package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;
import edu.kfirawad.cyber2021finalprojectbeta.fragment.ChildListFragment;

public class AideActivity extends UserPermActivity implements ChildListFragment.Callback {
    public AideActivity() {
        super("CFPB2021:Aide");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aide);
        findToolbar();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layPlaceholder, ChildListFragment.newInstance(this,
                        false, () -> rideUid, (uid, data) -> true))
                .commit();
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return perms.aide;
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
                    .inflate(R.layout.list_item_button2, parent, false);
        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvDesc = convertView.findViewById(R.id.tvDesc);
        Button button = convertView.findViewById(R.id.button);
        Button button2 = convertView.findViewById(R.id.button2);
        tvTitle.setText(name);
        tvDesc.setText(parentName);
        button.setText("Got On");
        button2.setText("Arrived");
        return convertView;
    }
}