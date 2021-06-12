package edu.kfirawad.cyber2021finalprojectbeta.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.kfirawad.cyber2021finalprojectbeta.R;

public class ChildListFragment extends Fragment {
    private static final String ARG_SHOW_CREATE_BUTTON = "showCreateButton";

    private boolean showCreateButton;

    public ChildListFragment() {}

    public static ChildListFragment newInstance(boolean showCreateButton) {
        ChildListFragment fragment = new ChildListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SHOW_CREATE_BUTTON, showCreateButton);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            showCreateButton = getArguments().getBoolean(ARG_SHOW_CREATE_BUTTON);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_child_list, container, false);
        // TODO set create button visibilty
        return view;
    }
}