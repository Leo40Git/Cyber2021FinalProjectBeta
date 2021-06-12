package edu.kfirawad.cyber2021finalprojectbeta;

import androidx.annotation.NonNull;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;

public class DashboardActivity extends UserPermActivity {
    public DashboardActivity() {
        super("C2021FPB:Dashboard");
        setContentView(R.layout.activity_dashboard);
        findToolbar();
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return true;
    }
}