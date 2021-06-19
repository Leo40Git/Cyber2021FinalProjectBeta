package edu.kfirawad.cyber2021finalprojectbeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import edu.kfirawad.cyber2021finalprojectbeta.db.DBChild;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUser;

public class CreateChildActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "C2021FPB:CreateChild";

    private static final class Adapter extends BaseAdapter {
        public static final class Entry {
            public final String uid, name, email;

            public Entry(String uid, String name, String email) {
                this.uid = uid;
                this.name = name;
                this.email = email;
            }
        }

        public final ArrayList<Entry> entries = new ArrayList<>();

        @Override
        public int getCount() {
            return entries.size();
        }

        @Override
        public Entry getItem(int position) {
            return entries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item, parent, false);
            Entry entry = getItem(position);
            TextView tvTitle = convertView.findViewById(R.id.tvTitle);
            TextView tvDesc = convertView.findViewById(R.id.tvDesc);
            tvTitle.setText(entry.name);
            tvDesc.setText(entry.email);
            return convertView;
        }
    }

    private EditText etName, etPickupSpot;
    private Spinner spnParent;
    private Adapter adapter;

    private String rideUid;
    private String parentUid;

    private FirebaseDatabase fbDb;
    private DBRide dbRide;
    private DatabaseReference dbRefRide;
    private ValueEventListener dbRefRideL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_child);

        Intent i = getIntent();
        rideUid = i.getStringExtra(UserPermActivity.RIDE_UID);
        if (rideUid == null) {
            Toast.makeText(this, "Missing ride UID", Toast.LENGTH_LONG).show();
            finish();
        }

        etName = findViewById(R.id.etName);
        etPickupSpot = findViewById(R.id.etPickupSpot);
        spnParent = findViewById(R.id.spnParent);
        adapter = new Adapter();
        spnParent.setAdapter(adapter);
        spnParent.setOnItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = fbAuth.getCurrentUser();
        if (fbUser == null) {
            Toast.makeText(this, "Log in first!", Toast.LENGTH_SHORT).show();
            Intent authIntent = new Intent(this, AuthActivity.class);
            authIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(authIntent);
            finish();
            return;
        }

        fbDb = FirebaseDatabase.getInstance();

        dbRefRide = fbDb.getReference("rides/" + rideUid);
        dbRefRideL = dbRefRide.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        dbRide = dataSnapshot.getValue(DBRide.class);
                        if (dbRide == null)
                            throw new NullPointerException("Ride \"" + rideUid + "\" does not exist?!");
                        dbRide.uid = rideUid;
                        adapter.entries.clear();
                        for (Map.Entry<String, DBRide.UserData> user : dbRide.users.entrySet()) {
                            if (!user.getValue().perms.parent)
                                continue;
                            adapter.entries.add(new Adapter.Entry(user.getKey(), user.getValue().name, user.getValue().email));
                        }
                        adapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        dbRide = null;
                        Log.e(TAG, "dbRefRideL:onDataChange:getValue:exception", e);
                    }
                } else {
                    Toast.makeText(CreateChildActivity.this, "Ride does not exist!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "dbRefRideL:onCancelled", databaseError.toException());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (dbRefRide != null && dbRefRideL != null) {
            dbRefRide.removeEventListener(dbRefRideL);
            dbRefRideL = null;
            dbRefRide = null;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Adapter.Entry entry = adapter.getItem(position);
        parentUid = entry.uid;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        parentUid = null;
    }

    public void onClick_btnCreate(View view) {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }

        String pickupSpot = etPickupSpot.getText().toString().trim();
        if (pickupSpot.isEmpty()) {
            Toast.makeText(this, "Pickup spot cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }

        if (parentUid == null) {
            Toast.makeText(this, "Please select a parent!", Toast.LENGTH_LONG).show();
            return;
        }

        DatabaseReference dbRefUser = fbDb.getReference("users/" + parentUid);
        dbRefUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        DBUser user = snapshot.getValue(DBUser.class);
                        if (user == null)
                            throw new NullPointerException("wot (" + parentUid + ")");
                        user.uid = parentUid;
                        String childUid = Objects.requireNonNull(fbDb
                                .getReference("rides/" + rideUid + "/children").push().getKey());
                        DBChild child = DBChild.create(childUid, name, user, pickupSpot);
                        dbRide.addChild(child);
                        dbRefRide.setValue(dbRide);
                        etName.setText("");
                    } catch (Exception e) {
                        Log.e(TAG, "onClick_btnCreate:singleUserL:onDataChange:getValue", e);
                    }

                } else
                    Toast.makeText(CreateChildActivity.this, "User does not exist?!", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onClick_btnCreate:singleUserL:onCancelled", error.toException());
                Toast.makeText(CreateChildActivity.this, "Failed to create child!", Toast.LENGTH_LONG).show();
            }
        });
    }
}