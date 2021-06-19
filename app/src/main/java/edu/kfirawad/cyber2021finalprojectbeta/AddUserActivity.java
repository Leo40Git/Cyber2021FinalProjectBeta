package edu.kfirawad.cyber2021finalprojectbeta;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Objects;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUser;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;

public class AddUserActivity extends AppCompatActivity {
    private static final String TAG = "C2021FPB:AddUser";

    private final HashMap<String, DBUser> emailsToUsers = new HashMap<>();

    private EditText etEmail;
    private CheckBox cbDriver, cbParent, cbAide;

    private String rideUid;

    private FirebaseDatabase fbDb;
    private DBRide dbRide;
    private DatabaseReference dbRefRide;
    private ValueEventListener dbRefRideL;
    private DatabaseReference dbRefUsers;
    private ValueEventListener dbRefUsersL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        Intent i = getIntent();
        rideUid = i.getStringExtra(UserPermActivity.RIDE_UID);
        if (rideUid == null) {
            Toast.makeText(this, "Missing ride UID", Toast.LENGTH_LONG).show();
            finish();
        }

        etEmail = findViewById(R.id.etEmail);
        cbDriver = findViewById(R.id.cbDriver);
        cbParent = findViewById(R.id.cbParent);
        cbAide = findViewById(R.id.cbAide);
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
                    } catch (Exception e) {
                        dbRide = null;
                        Log.e(TAG, "dbRefRideL:onDataChange:getValue:exception", e);
                    }
                } else {
                    Toast.makeText(AddUserActivity.this, "Ride does not exist!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "dbRefRideL:onCancelled", databaseError.toException());
            }
        });
        
        dbRefUsers = fbDb.getReference("users");
        dbRefUsersL = dbRefUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                emailsToUsers.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    DBUser user = child.getValue(DBUser.class);
                    if (user != null) {
                        user.uid = Objects.requireNonNull(child.getKey());
                        emailsToUsers.put(user.email, user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "dbRefUsersL:onCancelled", error.toException());
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

        if (dbRefUsers != null && dbRefUsersL != null) {
            dbRefUsers.removeEventListener(dbRefUsersL);
            dbRefUsers = null;
            dbRefUsersL = null;
        }
    }

    public void onClick_btnAdd(View view) {
        String email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "E-mail cannot be empty!", Toast.LENGTH_LONG).show();
            return;
        }

        DBUser user = emailsToUsers.get(email);
        if (user == null) {
            Toast.makeText(this, "No user with that e-mail!", Toast.LENGTH_LONG).show();
            return;
        }

        etEmail.setText("");
        user.invites.add(DBUser.Invite.create(dbRide.uid, dbRide.name,
                DBUserPerms.create(false,
                        cbDriver.isChecked(), cbAide.isChecked(), cbParent.isChecked())));
        DatabaseReference dbRefUser = fbDb.getReference("users/" + user.uid);
        dbRefUser.setValue(user);
        Toast.makeText(this, "Invited " + user.name + " to your ride!", Toast.LENGTH_SHORT).show();
    }
}