package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;
import android.view.View;

public class DashboardActivity extends BaseDashboardActivity {
    private static final String TAG = "C2021FPB:Dashboard";

    public DashboardActivity() {
        super(TAG, R.id.menuDashboard);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
    }

    public void onClick_btnLogOut(View view) {
        fbAuth.signOut();
        toLogin();
    }
}