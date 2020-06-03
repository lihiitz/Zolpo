package com.tutsplus.code.zolpo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tutsplus.code.zolpo.Adapters.PlaceAutocompleteAdapter;
import com.tutsplus.code.zolpo.Models.PlaceInfo;
import com.tutsplus.code.zolpo.Models.RequestToServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GoogleMapActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "GoogleMapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(-40, -168), new LatLng(71,136));//those are the bounds of the entire world

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private boolean mLocationIsSetSuccessfully = false;
    private GoogleMap mMap;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;

    //widgets
    private AutoCompleteTextView mSearchText;
    private ImageView mGps, mDone;

    /************** Implements GoogleApiClient.OnConnectionFailedListener method******************/

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    /************************* Implements OnMapReadyCallback method ******************************/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

            if (mLocationPermissionsGranted) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                init();

            }
    }
    /*********************************************************************************************/

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        Bundle extras = getIntent().getExtras();
        String LocationWasNotFoundMsg = extras.getString("LocationWasNotFoundMsg");
        if(LocationWasNotFoundMsg!=null)
        {
            Toast.makeText(this, LocationWasNotFoundMsg, Toast.LENGTH_SHORT).show();
        }
        mSearchText = findViewById(R.id.searchEditText);
        mGps = findViewById(R.id.gpsLocationImageView);
        mDone = findViewById(R.id.doneSearchingImageView);
        getLocationPermission();
    }

    private void init()
    {
        Log.d(TAG,"init : initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mSearchText.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,LAT_LNG_BOUNDS, null);
        mSearchText.setAdapter(mPlaceAutocompleteAdapter);
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    //execute our method for searching
                    geoLocate();
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG," onClick: clicked gps icon");
                //getDeviceLocation();
            }
        });

        mDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG," onClick: clicked done icon");
                if(mLocationIsSetSuccessfully)
                {
                    Log.d(TAG," onClick: clicked done icon - location was set ->going to next activity");
                    RequestToServer requestForServer = new RequestToServer();
                    requestForServer.setLatitude(mPlace.getLatlng().latitude);
                    requestForServer.setLongitude(mPlace.getLatlng().longitude);
                    //moving to the next activity
                    Intent intent= new Intent(GoogleMapActivity.this, ScanBarcodeActivity.class);
                    intent.putExtra("RequestToServer", requestForServer);
                    startActivity(intent);
                }
                else
                {
                    Log.d(TAG," onClick: clicked done icon - location was not set yet");
                    Toast.makeText(getApplicationContext(), "אנא אתר את מיקומך על המפה", Toast.LENGTH_LONG).show();
                }
            }
        });
        hideSoftKeyboard();
    }

    private void geoLocate()
    {
        Log.d(TAG, "geoLocate: geoLocating");
        String searchString = mSearchText.getText().toString();
        Geocoder geocoder = new Geocoder(GoogleMapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOExceprion: "+ e.getMessage());
        }

        if(list.size()>0)
        {//we have some addresses' results
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: found a location" + address.toString());
            mLocationIsSetSuccessfully = true;
            setPlaceInfoAndMoveCamera(address.getAddressLine(0), address.getAddressLine(0), new LatLng(address.getLatitude(), address.getLongitude()));
        }
    }


    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if(!title.equals("My Location"))
        {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(markerOptions);
        }
        hideSoftKeyboard();
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(GoogleMapActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard()
    {//to hide the keyboard
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    /********************************** Autocomplete places ***************************************/

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()){
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release(); // must release! To prevent memory leaks
                return;
            }
            // we have a place
            final Place place = places.get(0);
            setPlaceInfoAndMoveCamera(place.getName().toString(), place.getAddress().toString() , place.getLatLng());

            places.release();
        }
    };


    private void setPlaceInfoAndMoveCamera(String iPlaceName, String iAddress, LatLng iLatLng) {
        Log.d(TAG, "setPlaceInfoAndMoveCamera: setting PlaceInfo and MoveCamera");
        try {
            mPlace = new PlaceInfo();
            mPlace.setName(iPlaceName);
            Log.d(TAG, "setPlaceInfoAndMoveCamera: name: " + iPlaceName);
            mPlace.setAddress(iAddress);
            Log.d(TAG, "setPlaceInfoAndMoveCamera: address: " + iAddress);
            mPlace.setLatlng(iLatLng);
            Log.d(TAG, "setPlaceInfoAndMoveCamera: latlng: " + iLatLng);
            mLocationIsSetSuccessfully = true;
            Log.d(TAG, "setPlaceInfoAndMoveCamera: place: " + mPlace.toString());

        } catch (NullPointerException e) { // in case one of the parameters above is null
            Log.e(TAG, "setPlaceInfoAndMoveCamera: NullPointerException: " + e.getMessage());
        }

        moveCamera(iLatLng, DEFAULT_ZOOM, mPlace.getName());

    }
}