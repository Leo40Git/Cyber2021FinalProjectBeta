package edu.kfirawad.cyber2021finalprojectbeta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;

import static edu.kfirawad.cyber2021finalprojectbeta.DashboardActivity.RIDE_UID;

public class ManagerActivity extends AppCompatActivity {
    private static final String TAG = "C2021FPB:Manager";

    private String rideUid;

    private FirebaseDatabase fbDb;
    private DBRide dbRide;
    private DatabaseReference dbRefRide;
    private ValueEventListener dbRefRideL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        Intent i = getIntent();
        rideUid = i.getStringExtra(RIDE_UID);
        if (rideUid == null) {
            Toast.makeText(this, "Missing ride ID", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        fbDb = FirebaseDatabase.getInstance();

        dbRefRide = fbDb.getReference("rides/" + rideUid);
        dbRefRideL = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    try {
                        dbRide = dataSnapshot.getValue(DBRide.class);
                        if (dbRide == null)
                            throw new Exception("dataSnapshot claims it exists, but retrieved value was null!");
                        dbRide.uid = Objects.requireNonNull(dbRefRide.getKey());
                    } catch (Exception e) {
                        Log.e(TAG, "dbRefRideL:onDataChange:getValue:exception", e);
                        return;
                    }
                } else {
                    Toast.makeText(ManagerActivity.this, "Ride does not exist!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                // TODO
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "dbRefRideL:onCancelled", databaseError.toException());
            }
        };
        dbRefRide.addValueEventListener(dbRefRideL);
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
}