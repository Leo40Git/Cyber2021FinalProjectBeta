package edu.kfirawad.cyber2021finalprojectbeta.fragment;

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

public class ChildListFragment extends Fragment implements AdapterView.OnItemClickListener {
    @FunctionalInterface
    public interface Callback {
        void onChildSelected(@NonNull String uid, @NonNull String name,
                             @NonNull String parentUid, @NonNull String parentName,
                             @NonNull String pickupSpot);
        
        default void onCreateChildButtonPressed() { }

        default @NonNull View getChildItemView(@NonNull ViewGroup parent, @Nullable View convertView,
                                               @NonNull String uid, @NonNull String name,
                                               @NonNull String parentUid, @NonNull String parentName,
                                               @NonNull String pickupSpot) {
            if (convertView == null)
                convertView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item, parent, false);
            TextView tvTitle = convertView.findViewById(R.id.tvTitle);
            TextView tvDesc = convertView.findViewById(R.id.tvDesc);
            tvTitle.setText(name);
            tvDesc.setText(parentName);
            return convertView;
        }
    }

    static final class Adapter extends BaseAdapter {
        static final class Entry {
            public final @NonNull String uid, name, parentUid, parentName, pickupSpot;

            public Entry(@NonNull String uid, @NonNull String name,
                         @NonNull String parentUid, @NonNull String parentName,
                         @NonNull String pickupSpot) {
                this.uid = uid;
                this.name = name;
                this.parentUid = parentUid;
                this.parentName = parentName;
                this.pickupSpot = pickupSpot;
            }
        }

        private final @NonNull Callback callback;
        public Adapter(@NonNull Callback callback) {
            this.callback = callback;
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
            Entry entry = getItem(position);
            return callback.getChildItemView(parent, convertView,
                    entry.uid, entry.name, entry.parentUid, entry.parentName, entry.pickupSpot);
        }
    }

    @FunctionalInterface
    public interface Filter {
        boolean shouldDisplayChild(@NonNull String uid, @NonNull DBRide.ChildData data);
    }

    private static final String TAG = "C2021FPB:frag:ChildList";

    private static final String ARG_RIDE_UID = "rideUid";
    private static final String ARG_SHOW_CREATE_BUTTON = "showCreateButton";

    private final @NonNull Callback callback;
    private final @NonNull Filter filter;

    private String rideUid;
    private boolean showCreateButton;
    private Adapter adapter;

    private DBRide dbRide;
    private DatabaseReference dbRefRide;
    private ValueEventListener dbRefRideL;

    public ChildListFragment(@NonNull Callback callback,
                             @NonNull Filter filter) {
        this.callback = callback;
        this.filter = filter;
    }

    public static ChildListFragment newInstance(@NonNull Callback callback,
                                                @NonNull String rideUid,
                                                boolean showCreateButton,
                                                @NonNull Filter filter) {
        ChildListFragment fragment = new ChildListFragment(callback, filter);
        Bundle args = new Bundle();
        args.putString(ARG_RIDE_UID, rideUid);
        args.putBoolean(ARG_SHOW_CREATE_BUTTON, showCreateButton);
        fragment.setArguments(args);
        return fragment;
    }

    public static ChildListFragment newInstance(@NonNull Callback callback,
                                                @NonNull String rideUid,
                                                boolean showCreateButton) {
        return newInstance(callback, rideUid, showCreateButton, (uid, data) -> true);
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
            for (Map.Entry<String, DBRide.ChildData> child : dbRide.children.entrySet()) {
                String uid = child.getKey();
                DBRide.ChildData data = child.getValue();
                if (filter.shouldDisplayChild(uid, data))
                    adapter.entries.add(new Adapter.Entry(uid,
                            data.name, data.parentUid, data.parentName, data.pickupSpot));
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child_list, container, false);
        ListView lvChildren = view.findViewById(R.id.lvChildren);
        adapter = new Adapter(callback);
        lvChildren.setAdapter(adapter);
        lvChildren.setChoiceMode(ListView.CHOICE_MODE_NONE);
        lvChildren.setOnItemClickListener(this);
        FloatingActionButton fabCreate = view.findViewById(R.id.fabCreate);
        fabCreate.setVisibility(showCreateButton ? View.VISIBLE : View.GONE);
        fabCreate.setOnClickListener(v -> callback.onCreateChildButtonPressed());
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Adapter.Entry entry = adapter.getItem(position);
        callback.onChildSelected(entry.uid, entry.name, entry.parentUid, entry.parentName, entry.pickupSpot);
    }
}