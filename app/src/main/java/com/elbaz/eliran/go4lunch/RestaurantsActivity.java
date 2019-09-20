package com.elbaz.eliran.go4lunch;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.elbaz.eliran.go4lunch.utils.SnackbarAndVibrations;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import pub.devrel.easypermissions.EasyPermissions;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.models.Constants.PERMISSIONS_REQUEST_ENABLE_GPS;

public class RestaurantsActivity extends BaseActivity implements OnMapReadyCallback{

    protected GoogleMap mMap;
    protected FusedLocationProviderClient mFusedLocationProviderClient;
    private Boolean mLocationPermissionGranted = false;
    private static final float DEFAULT_ZOOM = 15f ;
    View rootView;
    // Permission Data
    public static final String PERMS_COARSE = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String PERMS_FINE = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final int RC_PERMISSION_CODE = 100;

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_restaurants;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ask for permissions
        this.isGpsEnabled();
        // Location service
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Get RootView for snackBarMessage
        rootView = getWindow().getDecorView().getRootView();
        // Initialise map
        this.initMap();
        // Setup the toolbar
        this.configureToolbarWithDrawer();
        // setup the drawer
        this.configureDrawerLayoutAndNavigationView();
    }

    //--------------------
    // Permissions
    //--------------------

    public void isGpsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }else {
            askPermission();
        }

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                        askPermission();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    /**
     * Method to ask the user for location authorization (with EasyPermissions support)
     */
    private void askPermission() {
        if (!EasyPermissions.hasPermissions(this, PERMS_FINE )) {
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_files_access), RC_PERMISSION_CODE, PERMS_FINE);
            return;
        }
        mLocationPermissionGranted = true;
        Toast.makeText(this, "Location access authorized!", Toast.LENGTH_SHORT).show();
    }
    //
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 2 - Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    //-----------------End-------------------------

    // Initialise Google Map
    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        mMap.setMinZoomPreference(15.0f);
//        mMap.setMaxZoomPreference(18.0f);

        if(mLocationPermissionGranted){
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            getDeviceLocation();
        }

//        // Add a marker in Sydney and move the camera
//        LatLng paris = new LatLng(48.864716, 2.349014);
//        mMap.addMarker(new MarkerOptions().position(paris).title("Your Location"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(paris));
    }

    // Get Device location
    private void getDeviceLocation(){

        try{
                final Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            SnackbarAndVibrations.showSnakbarMessage(rootView.findViewById(R.id.drawer_restaurant_activity),getString(R.string.location_is_null));
                       }
                    }
                });

        }catch(SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException" + e.getMessage() );
        }
    }

    // A method to move the camera(map) to specific location by passing LatLng and Zoom
    protected void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + "lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location"));
    }

}
