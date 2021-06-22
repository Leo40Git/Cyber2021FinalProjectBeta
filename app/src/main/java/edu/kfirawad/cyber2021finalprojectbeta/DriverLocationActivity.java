package edu.kfirawad.cyber2021finalprojectbeta;

import androidx.annotation.NonNull;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import edu.kfirawad.cyber2021finalprojectbeta.db.DBLatLng;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBRide;
import edu.kfirawad.cyber2021finalprojectbeta.db.DBUserPerms;

public class DriverLocationActivity extends UserPermActivity implements OnMapReadyCallback {
    public DriverLocationActivity() {
        super("DriverLocation");
    }

    private GoogleMap map;
    private DBLatLng lastLocation;
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment == null) {
            Log.e(tag, "mapFragment == null!!!!");
            Toast.makeText(this, "Could not find map fragment! This is a bug, report this!!!",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mapFragment.getMapAsync(this);
    }

    @Override
    protected boolean hasRequiredPermission(@NonNull DBUserPerms perms) {
        return true;
    }

    @Override
    protected void onDBRideUpdated() {
        switch (dbRide.state) {
        case DBRide.STATE_INACTIVE:
            Toast.makeText(this, "Ride hasn't started yet!", Toast.LENGTH_SHORT).show();
            finish();
            break;
        case DBRide.STATE_ACTIVE_DROPOFF:
            Toast.makeText(this, "Driver is at the destination!", Toast.LENGTH_SHORT).show();
            finish();
            break;
        default:
            Toast.makeText(this, "Unknown state?!", Toast.LENGTH_SHORT).show();
            finish();
        case DBRide.STATE_ACTIVE_PICKUP:
            break;
        }
        DBLatLng newLocation = dbRide.driverLocation;
        if (lastLocation == null)
            lastLocation = newLocation;
        else if (newLocation.lat == lastLocation.lat && newLocation.lng == lastLocation.lng)
            return;
        if (map == null)
            return;
        setDriverLocation(newLocation);
    }

    private void setDriverLocation(@NonNull DBLatLng location) {
        LatLng gLoc = new LatLng(location.lat, location.lng);
        if (marker == null) {
            marker = map.addMarker(new MarkerOptions()
                    .position(gLoc)
                    .title("Driver"));
        } else
            marker.setPosition(gLoc);
        map.moveCamera(CameraUpdateFactory.newLatLng(gLoc));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        if (lastLocation != null)
            setDriverLocation(lastLocation);
    }
}