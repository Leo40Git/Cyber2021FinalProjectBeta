package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;

import androidx.annotation.NonNull;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;

public class DriverActivity extends UserPermActivity {
    public DriverActivity() {
        super("CFPB2021:Driver");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);
        findToolbar();
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return perms.driver;
    }
}