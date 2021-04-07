package edu.kfirawad.cyber2021finalprojectbeta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUser;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;
import edu.kfirawad.cyber2021finalprojectbeta.dialog.StringInputDialogFragment;

public class RideSelectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        StringInputDialogFragment.Listener {
    private static final String TAG = "C2021FPB:RideSelect";

    private static final class RideAdapter extends BaseAdapter {
        private final LayoutInflater inflater;
        private int rideCount;
        private String[] rideNames, rideIds;

        public RideAdapter(Context ctx) {
            inflater = LayoutInflater.from(ctx);
        }

        private void removeAllRides() {
            rideCount = 0;
            rideNames = new String[0];
            rideIds = new String[0];
        }

        public void setRides(@Nullable Map<String, String> rides) {
            if (rides == null) {
                removeAllRides();
                return;
            }
            rideCount = rides.size();
            rideNames = new String[rideCount];
            rideIds = new String[rideCount];
            int i = 0;
            for (Map.Entry<String, String> entry : rides.entrySet()) {
                rideNames[i] = entry.getValue();
                rideIds[i] = entry.getKey();
                i++;
            }
            notifyDataSetChanged();
        }

        public String getRideName(int position) {
            return rideNames[position];
        }

        @Override
        public int getCount() {
            return rideCount;
        }

        @Override
        public String getItem(int position) {
            return rideIds[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(R.layout.support_simple_spinner_dropdown_item, parent, false);
            TextView tv = convertView.findViewById(android.R.id.text1);
            tv.setText(rideNames[position]);
            return convertView;
        }
    }

    private FirebaseAuth fbAuth;
    private FirebaseDatabase fbDb;
    private FirebaseUser fbUser;
    private DatabaseReference dbRefUser;
    private ValueEventListener dbRefUserL;

    private ListView lvRides;
    private RideAdapter rideAdapter;

    private DBUser dbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_select);

        lvRides = findViewById(R.id.lvRides);
        lvRides.setOnItemClickListener(this);
        lvRides.setChoiceMode(ListView.CHOICE_MODE_NONE);
        rideAdapter = new RideAdapter(getApplicationContext());
        lvRides.setAdapter(rideAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fbAuth = FirebaseAuth.getInstance();
        fbDb = FirebaseDatabase.getInstance();
        fbUser = fbAuth.getCurrentUser();
        dbRefUser = fbDb.getReference("users/" + fbUser.getUid());
        dbRefUserL = dbRefUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        dbUser = dataSnapshot.getValue(DBUser.class);
                        dbUser.uid = dbRefUser.getKey();
                    } catch (Exception e) {
                        Log.e(TAG, "dbRefUserL:onDataChange:getValue:exception", e);
                        return;
                    }
                } else {
                    dbUser = DBUser.create(fbUser.getUid(), fbUser.getDisplayName(), fbUser.getEmail());
                    dbRefUser.setValue(dbUser);
                }
                rideAdapter.setRides(dbUser.rides);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "dbRefUserL:onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dbRefUser != null && dbRefUserL != null) {
            dbRefUser.removeEventListener(dbRefUserL);
            dbRefUserL = null;
            dbRefUser = null;
        }
    }

    public void onClick_btnNew(View view) {
        DialogFragment fragment = new StringInputDialogFragment("Ride Name");
        fragment.show(getSupportFragmentManager(), "ride_name_input");
    }

    public void btnSignOut_onClick(View view) {
        fbAuth.signOut();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void toDashboard(String uid) {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra(DashboardActivity.RIDE_UID, uid);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String rideUid = rideAdapter.getItem(position);
        toDashboard(rideUid);
    }

    @Override
    public void onStringInputComplete(DialogFragment dialog, String input) {
        input = input.trim();
        if (input.isEmpty()) {
            Toast.makeText(this, "Ride name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        dialog.dismiss();
        DatabaseReference dbRefRide = fbDb.getReference("rides");
        dbRefRide = dbRefRide.push();
        DBRide ride = DBRide.create(dbRefRide.getKey(), input);
        ride.setUserPerms(dbUser, DBUserPerms.create(true, false, false, false));
        dbRefRide.setValue(ride);
        dbRefUser.setValue(dbUser);
        toDashboard(ride.uid);
    }

    @Override
    public void onStringInputCancelled(DialogFragment dialog) {
        dialog.dismiss();
    }
}