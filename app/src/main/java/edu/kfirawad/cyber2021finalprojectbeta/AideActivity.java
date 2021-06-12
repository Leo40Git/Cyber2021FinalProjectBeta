package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;

import androidx.annotation.NonNull;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;

public class AideActivity extends UserPermActivity {
    public AideActivity() {
        super("CFPB2021:Aide");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aide);
        findToolbar();
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return perms.aide;
    }
}