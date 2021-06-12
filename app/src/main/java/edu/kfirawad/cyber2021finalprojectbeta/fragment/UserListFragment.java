package edu.kfirawad.cyber2021finalprojectbeta.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import edu.kfirawad.cyber2021finalprojectbeta.R;

public class UserListFragment extends Fragment {
    private static final String ARG_SHOW_ADD_BUTTON = "showAddButton";

    private boolean showAddButton;

    public UserListFragment() { }

    public static UserListFragment newInstance(boolean showAddButton) {
        UserListFragment fragment = new UserListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_ADD_BUTTON, showAddButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            showAddButton = getArguments().getBoolean(ARG_SHOW_ADD_BUTTON);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAdd);
        fabAdd.setVisibility(showAddButton ? View.VISIBLE : View.GONE);
        return view;
    }
}