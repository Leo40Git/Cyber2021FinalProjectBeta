package edu.kfirawad.cyber2021finalprojectbeta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.experimental.UseExperimental;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBUser;

public class RideSelectActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "C2021FPB:RideSelect";

    private static final class RideAdapter extends BaseAdapter {
        private static final class Entry {
            public final String id, name;

            public Entry(String id, String name) {
                this.id = id;
                this.name = name;
            }
        }

        private final LayoutInflater inflater;
        private final ArrayList<Entry> entries;

        public RideAdapter(Context ctx) {
            inflater = LayoutInflater.from(ctx);
            entries = new ArrayList<>();
        }

        public void setRides(@Nullable Map<String, String> rides) {
            if (rides == null) {
                entries.clear();
                return;
            }
            entries.ensureCapacity(rides.size());
            for (Map.Entry<String, String> entry : rides.entrySet())
                entries.add(new Entry(entry.getKey(), entry.getValue()));
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return entries.size();
        }

        @Override
        public String getItem(int position) {
            return entries.get(position).id;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inflater.inflate(R.layout.ride_select_list_item, parent, false);
            TextView tvTitle = convertView.findViewById(R.id.tvTitle);
            tvTitle.setText(entries.get(position).name);
            return convertView;
        }
    }

    private FirebaseAuth fbAuth;
    private FirebaseUser fbUser;
    private DatabaseReference dbRefUser;
    private ValueEventListener dbRefUserL;

    private Toolbar toolbar;
    private RideAdapter rideAdapter;
    private BadgeDrawable badgeDrawable;

    private DBUser dbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_select);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView lvRides = findViewById(R.id.lvRides);
        lvRides.setOnItemClickListener(this);
        lvRides.setChoiceMode(ListView.CHOICE_MODE_NONE);
        rideAdapter = new RideAdapter(getApplicationContext());
        lvRides.setAdapter(rideAdapter);

        badgeDrawable = BadgeDrawable.create(this);
        badgeDrawable.setNumber(5);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fbAuth = FirebaseAuth.getInstance();
        FirebaseDatabase fbDb = FirebaseDatabase.getInstance();
        fbUser = fbAuth.getCurrentUser();
        if (fbUser == null) {
            Toast.makeText(this, "Log in first!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        dbRefUser = fbDb.getReference("users/" + fbUser.getUid());
        dbRefUserL = dbRefUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        dbUser = dataSnapshot.getValue(DBUser.class);
                        if (dbUser == null)
                            throw new Exception("dataSnapshot claims it exists, but retrieved value was null!");
                        dbUser.uid = Objects.requireNonNull(dbRefUser.getKey());
                    } catch (Exception e) {
                        Log.e(TAG, "dbRefUserL:onDataChange:getValue:exception", e);
                        return;
                    }
                } else {
                    dbUser = DBUser.create(fbUser.getUid(),
                            Objects.requireNonNull(fbUser.getDisplayName()),
                            Objects.requireNonNull(fbUser.getEmail()));
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

    @UseExperimental(markerClass = ExperimentalBadgeUtils.class)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_ride, menu);
        BadgeUtils.attachBadgeDrawable(badgeDrawable, toolbar, R.id.menuInvites);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuInvites) {
            // TODO invites activity
            return true;
        } else if (id == R.id.menuSettings) {
            // TODO settings
            return true;
        } else if (id == R.id.menuLogOut) {
            fbAuth.signOut();
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick_fabCreate(View view) {
        Intent intent = new Intent(this, RideCreateActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String rideUid = rideAdapter.getItem(position);
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.putExtra(DashboardActivity.RIDE_UID, rideUid);
        startActivity(intent);
    }
}