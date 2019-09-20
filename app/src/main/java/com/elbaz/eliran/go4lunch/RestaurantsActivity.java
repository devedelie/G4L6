package com.elbaz.eliran.go4lunch;

import android.Manifest;
import android.widget.Toast;

import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import pub.devrel.easypermissions.EasyPermissions;

public class RestaurantsActivity extends BaseActivity implements OnMapReadyCallback{
    protected GoogleMap mMap;
    // Permission Data
    public static final String PERMS_COARSE = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final String PERMS_FINE = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final int RC_PERMISSION_CODE = 100;

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_restaurants;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Initialise map
        this.initMap();
        // Setup the toolbar
        this.configureToolbarWithDrawer();
        // setup the drawer
        this.configureDrawerLayoutAndNavigationView();
        // Ask for permissions
        this.askPermission();
    }

    /**
     * Method to ask the user for location authorization (with EasyPermissions support)
     */
    private void askPermission() {
        if (!EasyPermissions.hasPermissions(this, PERMS_COARSE, PERMS_FINE)) {
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_files_access), RC_PERMISSION_CODE, PERMS_COARSE, PERMS_FINE);
            return;
        }
        Toast.makeText(this, "Location access authorized!", Toast.LENGTH_SHORT).show();
    }
    // 
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 2 - Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    // Initialise Google Map
    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMinZoomPreference(15.0f);
        mMap.setMaxZoomPreference(18.0f);

        // Add a marker in Sydney and move the camera
        LatLng paris = new LatLng(48.864716, 2.349014);
        mMap.addMarker(new MarkerOptions().position(paris).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(paris));
    }
}
