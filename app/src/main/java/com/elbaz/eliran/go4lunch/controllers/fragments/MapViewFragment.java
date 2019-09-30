package com.elbaz.eliran.go4lunch.controllers.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.base.BaseFragment;
import com.elbaz.eliran.go4lunch.controllers.activities.MainRestaurantActivity;
import com.elbaz.eliran.go4lunch.models.Constants;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.PlacesResults;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.elbaz.eliran.go4lunch.utils.PlacesStream;
import com.elbaz.eliran.go4lunch.viewmodels.SharedViewModel;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.models.Constants.NEARBY_RADIUS;
import static com.elbaz.eliran.go4lunch.models.Constants.NEARBY_TYPE;

public class MapViewFragment extends BaseFragment implements OnMapReadyCallback {
    @BindView(R.id.mapView_loading_animation) ProgressBar mapProgressBarAnimation;
    @BindView(R.id.mapView_loading_text) TextView mapLoadingText;
    private static final float DEFAULT_ZOOM = 15f ;
    protected Marker mMarker;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    protected Context mContext;
    private SharedViewModel mSharedViewModel;
    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    private Disposable mDisposable;
    private String deviceLocationVariable;
    private List<Result> mResults;
    private Marker poiMarker;
    public static String bottomSheetMainImageURL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);
        ButterKnife.bind(this, view); //Configure Butterknife
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());
        // Initialise map
        this.initMap();
        return view;
    }


    @Override
    protected int getFragmentLayout() { return R.layout.fragment_map_view; }

    @Override
    protected void updateData() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disposeWhenDestroy();
    }

    // This method will be called onDestroy to avoid any risk of memory leaks.
    private void disposeWhenDestroy(){
        if (this.mDisposable != null && !this.mDisposable.isDisposed()) this.mDisposable.dispose();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set ViewModel Elements under onActivityCreated() to scope it to the lifeCycle of the Fragment
        mSharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        // Get currentPagerItem for Auto-Complete-SearchBar
        mSharedViewModel.getPagerCurrentItem().observe(getViewLifecycleOwner(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (integer == Constants.MAP_VIEW_FRAGMENT){
                    autoCompleteSearchBar();
                }
            }
        });
    }

    // Initialise Google Map
    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Map-Zoom limitations
        mMap.setMinZoomPreference(15.0f);
        mMap.setMaxZoomPreference(18.0f);
        // Map configurations
        mMap.setBuildingsEnabled(true);
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Hiding Map Features (settings inside style_strings.xml)
        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources()
                .getString(R.string.style_json)));
        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }
        // Show current location
        if(MainRestaurantActivity.mLocationPermissionGranted){
            getDeviceLocation();
        }

        mMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {

                // https://maps.googleapis.com/maps/api/place/photo?maxwidth=100&photoreference=CmRaAAAAcDV4HqKigr5g-sbx2TKqua1W_n4Z_z6J4EREdifKwY9N3zu-GgjwGV-oT3fjoO3Hv5sRt3AcKShCAbHHyT5You9UHsVvV8wsW8ZnEX4WvQrWZjeg3tMpn7GtyjYw_4RvEhDrzRlft23jUMx3_OgXWyXQGhRchfjmPyCh1dU3XHyht2t5qJTJdg&key=YOUR_API_KEY
                bottomSheetMainImageURL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=CmRaAAAAcDV4HqKigr5g-sbx2TKqua1W_n4Z_z6J4EREdifKwY9N3zu-GgjwGV-oT3fjoO3Hv5sRt3AcKShCAbHHyT5You9UHsVvV8wsW8ZnEX4WvQrWZjeg3tMpn7GtyjYw_4RvEhDrzRlft23jUMx3_OgXWyXQGhRchfjmPyCh1dU3XHyht2t5qJTJdg&key=AIzaSyAFi9SMndxVfBk4sG3QAz-g_QOh4AjQQ74";

                RestaurantBottomSheetFragment.newInstance(1).show(getActivity().getSupportFragmentManager(), "MODAL");



//                // add marker
//                poiMarker = mMap.addMarker(new MarkerOptions()
//                        .position(pointOfInterest.latLng)
//                        .title(pointOfInterest.name));
//                poiMarker.showInfoWindow();
//                poiMarker.setTag("POI Tag");
//
//                // Show detailed Toast
//                Toast.makeText(getActivity().getApplicationContext(), "Clicked: " +
//                                pointOfInterest.name + "\nPlace ID:" + pointOfInterest.placeId +
//                                "\nLatitude:" + pointOfInterest.latLng.latitude +
//                                " Longitude:" + pointOfInterest.latLng.longitude,
//                        Toast.LENGTH_LONG).show();
            }
        });

    }

    // Get Device location
    private void getDeviceLocation(){
        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d(TAG, "onComplete: found location:" + location.getLatitude() + " & " + location.getLongitude());
                            mapLoadingText.setVisibility(View.GONE);
                            mapProgressBarAnimation.setVisibility(View.GONE);
                            // create a location string for retrofit (LatLng toString())
                            deviceLocationVariable = new LatLng(location.getLatitude(), location.getLongitude()).toString(); // set a global location variable for other use
                            deviceLocationVariable = deviceLocationVariable.replaceAll("[()]", "");
                            deviceLocationVariable = deviceLocationVariable.replaceAll("[lat/lng:]", "");
                            Log.d(TAG, "myReplace: " + "-"+deviceLocationVariable+"-");
                            // move camera to location
                            moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM);
                            executeHttpRequestWithRetrofit();
                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            // Call the method until location is synchronised
//                            getDeviceLocation();
                        }
                    }
                });
    }

    private void autoCompleteSearchBar(){
        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.PHONE_NUMBER,
                Place.Field.OPENING_HOURS,
                Place.Field.WEBSITE_URI,
                Place.Field.PHOTO_METADATAS,
                Place.Field.PRICE_LEVEL,
                Place.Field.RATING,
                Place.Field.LAT_LNG);

        // Bias results to Paris region (use 'bounds' variable in below filter)
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(48.832304, 2.239726),
                new LatLng(48.900962, 2.42124));

        // Start the autocomplete intent. (OVERLAY + ESTABLISHMENT + FR)
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, fields)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setCountry("FR")
                .setLocationBias(bounds)
                .build(getActivity());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
        Log.d(TAG, "onOptionsItemSelected: check");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            Log.d(TAG, "onActivityResult: code is " + requestCode +" "+ resultCode);
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i(TAG, "onActivityResult Place: " + place.getLatLng().latitude +" " + " " + place.getLatLng().longitude + place.getName() + ", " + place.getId() +" "+ place.getAddress()+ " " + place.getPhoneNumber()+ " " + place.getWebsiteUri() + " " + place.getPriceLevel()+ " " + place.getRating());
                // Share data - ViewModel + LiveData
                moveCamera(place.getLatLng(), DEFAULT_ZOOM);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, "onActivityResult Error: " + status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    // A method to move the camera(map) to specific location by passing LatLng and Zoom
    public void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to lat: " + latLng.latitude + " lng: " + latLng.longitude + " " + zoom + " " + latLng );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.addMarker(new MarkerOptions().position(latLng).title("Your Location"));
    }

    //-----------------
    // HTTP (RxJAVA)
    //-----------------

    // Execute the stream to fetch nearby locations
    private void executeHttpRequestWithRetrofit(){
        Log.d(TAG, "executeHttpRequestWithRetrofit: " + deviceLocationVariable+ " " +NEARBY_RADIUS+ " "+ NEARBY_TYPE);
        // Execute the stream subscribing to Observable defined inside PlacesResults
        this.mDisposable = PlacesStream.streamFetchNearbyLocations(deviceLocationVariable, NEARBY_RADIUS, NEARBY_TYPE)
                .subscribeWith(new DisposableObserver<PlacesResults>(){

                    @Override
                    public void onNext(PlacesResults placesResults) {
                        Log.d(TAG, "onNext: HTTP");
                        // Update UI with results
                        updateUI(placesResults.getResults());
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onErrorHTTP: "+ e );
                    }
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onCompleteHTTP");
                    }
                });
    }


    private void updateUI(List<Result> results){
        mResults = new ArrayList<>();
        mResults.clear();
        mResults.addAll(results);
        setNearbyRestaurantsWithMarkers();
    }

    private void setNearbyRestaurantsWithMarkers(){
        Log.d(TAG, "setNearbyRestaurantsWithMarkers: " + mResults.get(1).getGeometry().getLocation().getLat().toString() + "  " + mResults.get(1).getGeometry().getLocation().getLng().toString() + " size= "+ mResults.size());

        for (int i=mResults.size()-1; i>=0 ; i-- ){
            Log.d(TAG, "updateUI: " + i);
            LatLng latLng = new LatLng(mResults.get(i).getGeometry().getLocation().getLat(), mResults.get(i).getGeometry().getLocation().getLng());
            // set custom marker
            mMap.addMarker(new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(getActivity(), R.drawable.amu_bubble_mask,"Title"))))
                    .setTitle(mResults.get(i).getName());
        }
    }

    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String _name) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }


}
