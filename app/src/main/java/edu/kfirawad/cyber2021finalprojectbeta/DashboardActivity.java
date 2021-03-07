package edu.kfirawad.cyber2021finalprojectbeta;

import android.os.Bundle;
import android.view.View;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DashboardActivity extends BaseDashboardActivity {
    private static final String TAG = "C2021FPB:Dashboard";

    private FirebaseStorage fbStorage;

    public DashboardActivity() {
        super(TAG, R.id.menuDashboard);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        fbStorage = FirebaseStorage.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void onClick_btnLogOut(View view) {
        fbAuth.signOut();
        toLogin();
    }
}