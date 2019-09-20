package com.elbaz.eliran.go4lunch;

import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class RestaurantsActivity extends BaseActivity implements OnMapReadyCallback{
    private GoogleMap mMap;

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
