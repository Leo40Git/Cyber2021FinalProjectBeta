package edu.kfirawad.cyber2021finalprojectbeta;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import edu.kfirawad.cyber2021finalprojectbeta.service.UserService;

public class LocPermPromptActivity extends AppCompatActivity {
    private static final String TAG = "C2021FPB:LocPermPrompt";

    private static final int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loc_perm_prompt);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            finish();
            return;
        }
        ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            boolean gotPerms = false;
            for (int grantResult : grantResults)
                gotPerms |= grantResult == PackageManager.PERMISSION_GRANTED;
            Intent i = new Intent(this, UserService.class);
            i.putExtra(UserService.EXTRA_GOT_LOCATION_PERMS, gotPerms);
            startService(i);
            finish();
        }
    }
}