package com.tutsplus.code.zolpo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.tutsplus.code.zolpo.Models.RequestToServer;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_LOCATION = 1;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private final Activity THIS_ACTIVITY = MainActivity.this;


    //Members
    private LocationManager mLocationManager;
    private Context mContext;
    private Button mStartSearchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        ActivityCompat.requestPermissions(this, new String[]{FINE_LOCATION}, REQUEST_LOCATION);
        mStartSearchBtn = findViewById(R.id.searchForProduct_bt);
        mStartSearchBtn.setOnClickListener(v -> manageLocationAccess());
    }

    private void manageLocationAccess() {
        boolean locationAccessStatus;
        Log.d(TAG, "manageLocationAccess");

        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationAccessStatus = checkLocationAccess();
        if (!locationAccessStatus)
        {
            buildAlertMessageNoGps();
        }
        else {
            getLocation();
        }
    }

    private boolean checkLocationAccess() {
        Log.d(TAG, "checkLocationAccess: checking location services' status");
        return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void getLocation() {
        Log.d(TAG, "getLocation: trying to get current location");
        double latitude, longitude;

        if (ActivityCompat.checkSelfPermission(MainActivity.this, FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (MainActivity.this, COURSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "getLocation: permissions were not granted");
            ActivityCompat.requestPermissions(THIS_ACTIVITY, new String[]{FINE_LOCATION}, 1);
        }
        else
        {
            Log.d(TAG, "getLocation: permissions granted");
            Location networkLastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location gpsLastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (networkLastKnownLocation != null) {
                latitude = networkLastKnownLocation.getLatitude();
                longitude = networkLastKnownLocation.getLongitude();
                configureLocation(latitude, longitude);

            } else if (gpsLastKnownLocation != null) {
                latitude = gpsLastKnownLocation.getLatitude();
                longitude = gpsLastKnownLocation.getLongitude();
                configureLocation(latitude, longitude);
            }
            else
            {
                Log.d(TAG, "getLocation: permissions granted but was unable to find current location");
                Intent intent= new Intent(THIS_ACTIVITY, GoogleMapActivity.class);
                intent.putExtra("LocationWasNotFoundMsg","זולפה לא הצליח לאתר את מיקומך, \n אנא הזן אותו ידנית ");
                startActivity(intent);
            }
        }
    }

    private void configureLocation(double latitude, double longitude) {
        Log.d(TAG, "configureLocation: getting latitude and longitude from current location");
        //creating the request and inserting the needed parameters for now.
        RequestToServer requestForServer = new RequestToServer();
        requestForServer.setLatitude(latitude);
        requestForServer.setLongitude(longitude);
        //moving to the next activity
        Intent intent= new Intent(MainActivity.this, ScanBarcodeActivity.class);
        intent.putExtra("RequestToServer", requestForServer);
        startActivity(intent);
    }

    protected void buildAlertMessageNoGps() {
        Log.d(TAG, "buildAlertMessageNoGps: build Alert Message No Gps");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("יש להפעיל שירותי מיקום")
                .setCancelable(false)
                .setPositiveButton("כן", (dialog, id) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("לא", (dialog, id) -> {
                    dialog.cancel();
                    Toast.makeText(getApplicationContext(),"יש להפעיל שירותי מיקום",Toast.LENGTH_LONG).show();
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

