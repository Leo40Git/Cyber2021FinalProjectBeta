package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;

import androidx.annotation.NonNull;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;

public class ManagerActivity extends UserPermActivity {
    public ManagerActivity() {
        super("CFPB2021:Manager");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        findToolbar();
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return perms.manager;
    }
}