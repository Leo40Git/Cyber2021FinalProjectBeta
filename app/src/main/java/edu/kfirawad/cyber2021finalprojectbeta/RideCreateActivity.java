package edu.kfirawad.cyber2021finalprojectbeta;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
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

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUser;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;
import edu.kfirawad.cyber2021finalprojectbeta.dialog.TimePickerFragment;

public class RideCreateActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {
    private static final String TAG = "C2021FPB:RideCreate";

    private DatabaseReference dbRefRides;

    private FirebaseAuth fbAuth;
    private FirebaseUser fbUser;
    private DatabaseReference dbRefUser;
    private ValueEventListener dbRefUserL;

    private DBUser dbUser;

    private EditText etName, etDestination;
    private TextView tvStartTime;
    private int startHour, startMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_create);

        etName = findViewById(R.id.etName);
        etDestination = findViewById(R.id.etDestination);
        tvStartTime = findViewById(R.id.tvStartTime);

        final Calendar c = Calendar.getInstance();
        startHour = c.get(Calendar.HOUR_OF_DAY);
        startMinute = c.get(Calendar.MINUTE);
        updateStartTime();
    }

    @Override
    protected void onStart() {
        super.onStart();
        fbAuth = FirebaseAuth.getInstance();
        FirebaseDatabase fbDb = FirebaseDatabase.getInstance();
        dbRefRides = fbDb.getReference("rides");
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
                    }
                } else {
                    dbUser = DBUser.create(fbUser.getUid(),
                            Objects.requireNonNull(fbUser.getDisplayName()),
                            Objects.requireNonNull(fbUser.getEmail()));
                    dbRefUser.setValue(dbUser);
                }
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

    private void updateStartTime() {
        tvStartTime.setText(String.format(Locale.ROOT,
                "%02d:%02d", startHour, startMinute));
    }

    public void onClick_btnChangeTime(View view) {
        TimePickerFragment tpFrag = new TimePickerFragment();
        tpFrag.show(getSupportFragmentManager(), "startTimePicker");
    }

    public void onClick_btnCreate(View view) {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }

        String destination = etDestination.getText().toString().trim();
        if (destination.isEmpty()) {
            Toast.makeText(this, "Destination cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }

        etName.setText("");
        etDestination.setText("");

        DatabaseReference ref = dbRefRides.push();
        String uid = ref.getKey();
        DBRide ride = DBRide.create(Objects.requireNonNull(uid), name, destination, startHour, startMinute);
        ride.addUser(dbUser, DBUserPerms.create(true, false, false, false));
        ref.setValue(ride);
        dbRefUser.setValue(dbUser);

        Intent i = new Intent(this, DashboardActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(DashboardActivity.RIDE_UID, uid);
        startActivity(i);
        finish();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        startHour = hourOfDay;
        startMinute = minute;
        updateStartTime();
    }
}