package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;

import androidx.annotation.NonNull;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;

public class ParentActivity extends UserPermActivity {
    public ParentActivity() {
        super("CFPB2021:Parent");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);
        findToolbar();
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return perms.parent;
    }
}