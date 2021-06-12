package edu.kfirawad.cyber2021finalprojectbeta.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import edu.kfirawad.cyber2021finalprojectbeta.R;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;

public class ChildListFragment extends Fragment {
    @FunctionalInterface
    public interface Callback {
        void onChildSelected(@NonNull String uid, @NonNull String name,
                             @NonNull String parentUid, @NonNull String parentName,
                             @NonNull String pickupSpot);
    }

    private static final String TAG = "C2021FPB:frag:ChildList";

    private static final String ARG_RIDE_UID = "rideUid";
    private static final String ARG_SHOW_CREATE_BUTTON = "showCreateButton";

    private final @NonNull Callback callback;

    private String rideUid;
    private boolean showCreateButton;

    private DBRide dbRide;
    private DatabaseReference dbRefRide;
    private ValueEventListener dbRefRideL;

    public ChildListFragment(@NonNull Callback callback) {
        this.callback = callback;
    }

    public static ChildListFragment newInstance(@NonNull Callback callback,
                                                @NonNull String rideUid, boolean showCreateButton) {
        ChildListFragment fragment = new ChildListFragment(callback);
        Bundle args = new Bundle();
        args.putString(ARG_RIDE_UID, rideUid);
        args.putBoolean(ARG_SHOW_CREATE_BUTTON, showCreateButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity();

        Bundle args = getArguments();
        if (args != null) {
            rideUid = args.getString(rideUid);
            showCreateButton = args.getBoolean(ARG_SHOW_CREATE_BUTTON);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseDatabase fbDb = FirebaseDatabase.getInstance();

        dbRefRide = fbDb.getReference("rides/" + rideUid);
        dbRefRideL = new ValueEventListener() {
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
                    updateAdapter();
                } else {
                    FragmentActivity activity = ChildListFragment.this.requireActivity();
                    Toast.makeText(activity,
                            "Ride does not exist!", Toast.LENGTH_SHORT).show();
                    // roughly equivalent to Activity.finish();
                    activity.getSupportFragmentManager().popBackStackImmediate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "dbRefRideL:onCancelled", databaseError.toException());
            }
        };
        dbRefRide.addValueEventListener(dbRefRideL);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (dbRefRide != null && dbRefRideL != null) {
            dbRefRide.removeEventListener(dbRefRideL);
            dbRefRideL = null;
            dbRefRide = null;
        }
    }

    private void updateAdapter() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child_list, container, false);
        // TODO set create button visibilty
        return view;
    }
}