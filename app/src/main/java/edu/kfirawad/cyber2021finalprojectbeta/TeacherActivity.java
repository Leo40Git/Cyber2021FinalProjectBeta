package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;
import edu.kfirawad.cyber2021finalprojectbeta.fragment.ChildListFragment;

public class TeacherActivity extends UserPermActivity implements ChildListFragment.Callback {
    public TeacherActivity() {
        super("CFPB2021:Teacher");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);
        findToolbar();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layPlaceholder, ChildListFragment.newInstance(this,
                        rideUid, false))
                .commit();
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return perms.teacher;
    }

    @Override
    protected void onDBRideUpdated() {
        switch (dbRide.state) {
        case DBRide.STATE_INACTIVE:
            Toast.makeText(this, "Ride hasn't started yet!", Toast.LENGTH_SHORT).show();
            finish();
            break;
        default:
            Toast.makeText(this, "Unknown state?!", Toast.LENGTH_SHORT).show();
            finish();
        case DBRide.STATE_ACTIVE_PICKUP:
        case DBRide.STATE_ACTIVE_DROPOFF:
            break;
        }
    }

    @Override
    protected int getOptionsMenu() {
        return R.menu.teacher;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean canEndRide = false;
        if (dbRide != null)
            canEndRide = DBRide.STATE_ACTIVE_DROPOFF.equals(dbRide.state);
        setMenuItemEnabled(menu, R.id.menuEndRide, canEndRide);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuEndRide) {
            if (dbRide != null) {
                dbRide.finishDropOff();
                dbRefRide.setValue(dbRide);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                    .inflate(R.layout.teacher_list_item, parent, false);
        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvDesc = convertView.findViewById(R.id.tvDesc);
        TextView tvNotToday = convertView.findViewById(R.id.tvNotToday);
        LinearLayout layBtns = convertView.findViewById(R.id.layBtns);
        CheckBox cbPickedUp = convertView.findViewById(R.id.cbPickedUp);
        CheckBox cbDroppedOff = convertView.findViewById(R.id.cbDroppedOff);
        tvTitle.setText(name);
        tvDesc.setText(parentName);
        if (dbRide.isChildComingToday(uid)) {
            tvNotToday.setVisibility(View.GONE);
            layBtns.setVisibility(View.VISIBLE);
            DBRide.ChildData data = dbRide.children.get(uid);
            if (data == null) {
                // huh
                cbPickedUp.setEnabled(false);
                cbDroppedOff.setEnabled(false);
            } else {
                cbPickedUp.setChecked(data.pickedUp);
                if (DBRide.STATE_ACTIVE_PICKUP.equals(dbRide.state)) {
                    cbPickedUp.setEnabled(true);
                    cbPickedUp.setOnCheckedChangeListener((v, checked) -> {
                        data.pickedUp = checked;
                        dbRefRide.setValue(dbRide);
                    });
                } else
                    cbPickedUp.setEnabled(false);
                cbDroppedOff.setChecked(data.droppedOff);
                if (DBRide.STATE_ACTIVE_DROPOFF.equals(dbRide.state)) {
                    cbDroppedOff.setEnabled(true);
                    cbDroppedOff.setOnCheckedChangeListener((v, checked) -> {
                        data.droppedOff = checked;
                        dbRefRide.setValue(dbRide);
                    });
                } else
                    cbDroppedOff.setEnabled(false);
            }
        } else {
            tvNotToday.setVisibility(View.VISIBLE);
            layBtns.setVisibility(View.GONE);
            cbPickedUp.setEnabled(false);
            cbDroppedOff.setEnabled(false);
        }
        return convertView;
    }
}