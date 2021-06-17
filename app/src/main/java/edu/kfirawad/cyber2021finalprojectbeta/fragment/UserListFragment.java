package edu.kfirawad.cyber2021finalprojectbeta.fragment;

import android.annotation.SuppressLint;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

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
import edu.kfirawad.cyber2021finalprojectbeta.util.UidSupplier;

public class UserListFragment extends Fragment implements AdapterView.OnItemClickListener {
    @FunctionalInterface
    public interface Callback {
        void onUserSelected(@NonNull String uid, @NonNull String name, @NonNull DBUserPerms perms);

        default void onAddUserButtonClicked() { }

        @SuppressLint("SetTextI18n")
        default @NonNull View getUserItemView(@NonNull ViewGroup parent, @Nullable View convertView,
                                              @NonNull String uid, @NonNull String name,
                                              @NonNull DBUserPerms perms,
                                              @Nullable String myUserUid) {
            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item, parent, false);
            TextView tvTitle = convertView.findViewById(R.id.tvTitle);
            TextView tvDesc = convertView.findViewById(R.id.tvDesc);
            if (uid.equals(myUserUid))
                tvTitle.setText(name + " (you)");
            else
                tvTitle.setText(name);
            tvDesc.setText(perms.toString());
            return convertView;
        }
    }

    static final class Adapter extends BaseAdapter {
        static final class Entry {
            public final @NonNull String uid, name;
            public final @NonNull DBUserPerms perms;

            public Entry(@NonNull String uid, @NonNull String name, @NonNull DBUserPerms perms) {
                this.uid = uid;
                this.name = name;
                this.perms = perms;
            }
        }

        private final @NonNull Callback callback;
        private final @NonNull UidSupplier myUserUidSupplier;
        public Adapter(@NonNull Callback callback, @NonNull UidSupplier myUserUidSupplier) {
            this.callback = callback;
            this.myUserUidSupplier = myUserUidSupplier;
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

        @SuppressLint("SetTextI18n")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Entry entry = getItem(position);
            return callback.getUserItemView(parent, convertView,
                    entry.uid, entry.name, entry.perms, myUserUidSupplier.getUid());
        }
    }

    private static final String TAG = "C2021FPB:frag:UserList";

    private static final String ARG_RIDE_UID = "rideUid";
    private static final String ARG_SHOW_ADD_BUTTON = "showAddButton";

    private final @NonNull Callback callback;
    private final @NonNull UidSupplier myUserUidSupplier;

    private String rideUid;
    private boolean showAddButton;
    private Adapter adapter;

    private DBRide dbRide;
    private DatabaseReference dbRefRide;
    private ValueEventListener dbRefRideL;

    public UserListFragment(@NonNull Callback callback, @NonNull UidSupplier myUserUidSupplier) {
        this.callback = callback;
        this.myUserUidSupplier = myUserUidSupplier;
    }

    public static UserListFragment newInstance(@NonNull Callback callback,
                                               @NonNull String rideUid,
                                               boolean showAddButton,
                                               @NonNull UidSupplier myUserUidSupplier) {
        UserListFragment fragment = new UserListFragment(callback, myUserUidSupplier);
        Bundle args = new Bundle();
        args.putString(ARG_RIDE_UID, rideUid);
        args.putBoolean(ARG_SHOW_ADD_BUTTON, showAddButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity();

        Bundle args = getArguments();
        if (args != null) {
            rideUid = args.getString(ARG_RIDE_UID);
            showAddButton = args.getBoolean(ARG_SHOW_ADD_BUTTON);
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
        ListView lvUsers = view.findViewById(R.id.lvUsers);
        adapter = new Adapter(callback, myUserUidSupplier);
        lvUsers.setAdapter(adapter);
        lvUsers.setChoiceMode(ListView.CHOICE_MODE_NONE);
        lvUsers.setOnItemClickListener(this);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setVisibility(showAddButton ? View.VISIBLE : View.GONE);
        fabAdd.setOnClickListener(v -> callback.onAddUserButtonClicked());
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Adapter.Entry entry = adapter.getItem(position);
        callback.onUserSelected(entry.uid, entry.name, entry.perms);
    }
}