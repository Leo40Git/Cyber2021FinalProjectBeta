package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;

public class ManagerActivity extends BaseDashboardActivity {
    private static final String TAG = "C2021FPB:Manager";

    public ManagerActivity() {
        super(TAG, R.id.menuManager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
    }

    @Override
    public void onUserPermissionsUpdated() {
        if (!userPerms.manager)
            finish();
    }
}