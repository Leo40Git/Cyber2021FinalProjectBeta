package edu.kfirawad.cyber2021finalprojectbeta.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import edu.kfirawad.cyber2021finalprojectbeta.R;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;

public class UserListFragment extends Fragment {
    @FunctionalInterface
    public interface Callback {
        void onUserSelected(@NonNull String uid, @NonNull String name, @NonNull DBUserPerms perms);
    }

    private static final String TAG = "C2021FPB:frag:UserList";

    static final class Adapter extends RecyclerView.Adapter<ListViewHolder> {
        public static final class Entry {
            public final @NonNull String uid, name;
            public final @NonNull DBUserPerms perms;

            public Entry(@NonNull String uid, @NonNull String name, @NonNull DBUserPerms perms) {
                this.uid = uid;
                this.name = name;
                this.perms = perms;
            }
        }

        public final ArrayList<Entry> entries = new ArrayList<>();

        private final @NonNull Callback callback;
        private final @Nullable String myUserUid;

        Adapter(@NonNull Callback callback, @Nullable String myUserUid) {
            this.callback = callback;
            this.myUserUid = myUserUid;
        }

        @NonNull
        @Override
        public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            return new ListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
            Entry entry = entries.get(position);
            if (entry.uid.equals(myUserUid))
                holder.setTitle(entry.name + " (you)");
            else
                holder.setTitle(entry.name);
            holder.setDescription(entry.perms.toString());
            holder.setOnClickListener(() -> callback.onUserSelected(entry.uid, entry.name, entry.perms));
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }
    }

    private static final String ARG_RIDE_UID = "rideUid";
    private static final String ARG_SHOW_ADD_BUTTON = "showAddButton";
    private static final String ARG_MY_USER_UID = "myUserUid";

    private final @NonNull Callback callback;

    private String rideUid;
    private boolean showAddButton;
    private String myUserUid;
    private Adapter adapter;

    private DBRide dbRide;
    private DatabaseReference dbRefRide;
    private ValueEventListener dbRefRideL;

    public UserListFragment(@NonNull Callback callback) {
        this.callback = callback;
    }

    public static UserListFragment newInstance(@NonNull Callback callback,
                                               @NonNull String rideUid, boolean showAddButton,
                                               @Nullable String myUserUid) {
        UserListFragment fragment = new UserListFragment(callback);
        Bundle args = new Bundle();
        args.putString(ARG_RIDE_UID, rideUid);
        args.putBoolean(ARG_SHOW_ADD_BUTTON, showAddButton);
        args.putString(ARG_MY_USER_UID, myUserUid);
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
            showAddButton = args.getBoolean(ARG_SHOW_ADD_BUTTON);
            myUserUid = args.getString(ARG_MY_USER_UID);
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
                    FragmentActivity activity = UserListFragment.this.requireActivity();
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
        adapter.entries.clear();
        if (dbRide != null) {
            for (Map.Entry<String, DBRide.UserData> user : dbRide.users.entrySet())
                adapter.entries.add(new Adapter.Entry(user.getKey(),
                        user.getValue().name, user.getValue().perms));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        RecyclerView rvUsers = view.findViewById(R.id.rvUsers);
        adapter = new Adapter(callback, myUserUid);
        rvUsers.setAdapter(adapter);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setVisibility(showAddButton ? View.VISIBLE : View.GONE);
        return view;
    }
}