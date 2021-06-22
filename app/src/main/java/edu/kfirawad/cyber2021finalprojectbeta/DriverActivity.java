package edu.kfirawad.cyber2021finalprojectbeta;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;
import edu.kfirawad.cyber2021finalprojectbeta.fragment.ChildListFragment;

public class DriverActivity extends UserPermActivity implements ChildListFragment.Callback {
    public DriverActivity() {
        super("CFPB2021:Driver");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        findToolbar();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layPlaceholder, ChildListFragment.newInstance(this,
                        rideUid, false))
                .commit();
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return perms.driver;
    }

    @Override
    protected void onDBRideUpdated() {
        switch (dbRide.state) {
        case DBRide.STATE_ACTIVE_DROPOFF:
            Toast.makeText(this, "You're already at the destination!", Toast.LENGTH_SHORT).show();
            finish();
            break;
        default:
            Toast.makeText(this, "Unknown state?!", Toast.LENGTH_SHORT).show();
            finish();
        case DBRide.STATE_INACTIVE:
        case DBRide.STATE_ACTIVE_PICKUP:
            break;
        }
    }

    @Override
    protected int getOptionsMenu() {
        return R.menu.driver;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menuToggleDrive);
        if (item != null) {
            boolean isInPickupState = false;
            if (dbRide != null)
                isInPickupState = DBRide.STATE_ACTIVE_PICKUP.equals(dbRide.state);
            if (isInPickupState) {
                item.setIcon(R.drawable.ic_stop);
                item.setTitle("End Drive");
            } else {
                item.setIcon(R.drawable.ic_start);
                item.setTitle("Start Drive");
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menuToggleDrive) {
            if (DBRide.STATE_INACTIVE.equals(dbRide.state)) {
                dbRide.startPickUp();
                dbRefRide.setValue(dbRide);
                supportInvalidateOptionsMenu();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm End Drive")
                        .setMessage("Are you at the destination?")
                        .setNeutralButton("No", (dialog, which) -> dialog.dismiss())
                        .setNegativeButton("Yes", (dialog, which) -> {
                            dbRide.finishPickUpAndStartDropOff();
                            dbRefRide.setValue(dbRide);
                        })
                        .show();
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
                    .inflate(R.layout.driver_list_item, parent, false);
        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvDesc = convertView.findViewById(R.id.tvDesc);
        TextView tvNotToday = convertView.findViewById(R.id.tvNotToday);
        LinearLayout layBtns = convertView.findViewById(R.id.layBtns);
        Button btnLate = convertView.findViewById(R.id.btnLate);
        Button btnReady = convertView.findViewById(R.id.btnReady);
        tvTitle.setText(name);
        tvDesc.setText(pickupSpot);
        if (dbRide.isChildComingToday(uid)) {
            tvNotToday.setVisibility(View.GONE);
            layBtns.setVisibility(View.VISIBLE);
            DBRide.ChildData data = dbRide.children.get(uid);
            if (data == null) {
                // huh
                btnLate.setEnabled(false);
                btnReady.setEnabled(false);
            } else {
                if (data.lateNotifyState > DBRide.ChildData.NOTIFY_STATE_NO)
                    btnLate.setEnabled(false);
                else {
                    btnLate.setEnabled(true);
                    btnLate.setOnClickListener(v -> {
                        btnLate.setEnabled(false);
                        data.lateNotifyState = DBRide.ChildData.NOTIFY_STATE_YES;
                        dbRefRide.setValue(dbRide);
                    });
                }
                if (data.readyNotifyState > DBRide.ChildData.NOTIFY_STATE_NO)
                    btnReady.setEnabled(false);
                else {
                    btnReady.setEnabled(true);
                    btnReady.setOnClickListener(v -> {
                        btnReady.setEnabled(false);
                        data.readyNotifyState = DBRide.ChildData.NOTIFY_STATE_YES;
                        dbRefRide.setValue(dbRide);
                    });
                }
            }
        } else {
            tvNotToday.setVisibility(View.VISIBLE);
            layBtns.setVisibility(View.GONE);
            btnLate.setEnabled(false);
            btnReady.setEnabled(false);
        }
        return convertView;
    }
}