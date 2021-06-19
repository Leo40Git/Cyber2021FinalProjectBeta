package edu.kfirawad.cyber2021finalprojectbeta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;

public class DashboardActivity extends UserPermActivity {
    private Button btnManager, btnDriver, btnParent, btnAide;

    public DashboardActivity() {
        super("C2021FPB:Dashboard");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);
        btnManager = findViewById(R.id.btnManager);
        btnDriver = findViewById(R.id.btnDriver);
        btnParent = findViewById(R.id.btnParent);
        btnAide = findViewById(R.id.btnTeacher);

        findToolbar();
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        btnManager.setEnabled(perms.manager);
        btnDriver.setEnabled(perms.driver);
        btnParent.setEnabled(perms.parent);
        btnAide.setEnabled(perms.teacher);
        return true;
    }

    public void onClick_btnManager(View view) {
        Intent i = new Intent(this, ManagerActivity.class);
        i.putExtra(RIDE_UID, rideUid);
        startActivity(i);
    }

    public void onClick_btnDriver(View view) {
        Intent i = new Intent(this, DriverActivity.class);
        i.putExtra(RIDE_UID, rideUid);
        startActivity(i);
    }

    public void onClick_btnParent(View view) {
        Intent i = new Intent(this, ParentActivity.class);
        i.putExtra(RIDE_UID, rideUid);
        startActivity(i);
    }

    public void onClick_btnTeacher(View view) {
        Intent i = new Intent(this, TeacherActivity.class);
        i.putExtra(RIDE_UID, rideUid);
        startActivity(i);
    }
}