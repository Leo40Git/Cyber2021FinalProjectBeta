package edu.kfirawad.cyber2021finalprojectbeta;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
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

import java.util.Objects;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUser;

public class InvitesActivity extends AppCompatActivity {
    private static final String TAG = "C2021FPB:Invites";

    final class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            if (dbUser == null)
                return 0;
            return dbUser.invites.size();
        }

        @Override
        public DBUser.Invite getItem(int position) {
            if (dbUser == null)
                return null;
            return dbUser.invites.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_button2, parent, false);
            final DBUser.Invite invite = getItem(position);

            TextView tvTitle = convertView.findViewById(R.id.tvTitle);
            TextView tvDesc = convertView.findViewById(R.id.tvDesc);
            Button button = convertView.findViewById(R.id.button);
            Button button2 = convertView.findViewById(R.id.button2);
            if (invite == null) {
                tvTitle.setText("TO: <unknown>");
                tvDesc.setText("<unknown>");
                button.setText("???");
                button.setOnClickListener(v -> { });
                button2.setText("???");
                button2.setOnClickListener(v -> { });
            } else {
                tvTitle.setText("TO: " + invite.rideName);
                tvDesc.setText(invite.perms.toString());
                button.setText("Accept");
                button.setOnClickListener(v -> {
                    DatabaseReference dbRideRef = fbDb.getReference("rides/" + invite.rideUid);
                    dbRideRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                DBRide dbRide;
                                try {
                                    dbRide = snapshot.getValue(DBRide.class);
                                    if (dbRide == null)
                                        throw new NullPointerException("Ride \"" + invite.rideUid + "\" does not exist?!");
                                    dbRide.uid = invite.rideUid;
                                } catch (Exception e) {
                                    Log.e(TAG, "dbRefRideL:onDataChange:getValue:exception", e);
                                    Toast.makeText(InvitesActivity.this, "Failed to accept invite!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                dbRide.addUser(dbUser, invite.perms);
                                dbRideRef.setValue(dbRide);
                                dbUser.invites.remove(position);
                                dbRefUser.setValue(dbUser);
                            } else {
                                Toast.makeText(InvitesActivity.this, "Ride does not exist!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "dbRefRideL:onCancelled", error.toException());
                            Toast.makeText(InvitesActivity.this, "Failed to accept invite!", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
                button2.setText("Deny");
                button2.setOnClickListener(v -> {
                    dbUser.invites.remove(position);
                    dbRefUser.setValue(dbUser);
                });
            }
            return convertView;
        }
    }

    private ListView lvInvites;
    private Adapter adapter;

    private FirebaseDatabase fbDb;
    private FirebaseUser fbUser;
    private DatabaseReference dbRefUser;
    private ValueEventListener dbRefUserL;
    private DBUser dbUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invites);

        lvInvites = findViewById(R.id.lvInvites);
        adapter = new Adapter();
        lvInvites.setAdapter(adapter);
        lvInvites.setChoiceMode(ListView.CHOICE_MODE_NONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth fbAuth = FirebaseAuth.getInstance();
        fbDb = FirebaseDatabase.getInstance();
        fbUser = fbAuth.getCurrentUser();
        if (fbUser == null) {
            Toast.makeText(this, "Log in first!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, AuthActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
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
                adapter.notifyDataSetInvalidated();
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
}