package com.elbaz.eliran.go4lunch.controllers.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import com.elbaz.eliran.go4lunch.models.SearchAuto;
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

public class MapViewFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    @BindView(R.id.mapView_loading_animation) ProgressBar mapProgressBarAnimation;
    @BindView(R.id.mapView_loading_text) TextView mapLoadingText;
    private static final float DEFAULT_ZOOM = 15f ;
    protected Marker mMarker;
    public View mViewMarker;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    protected Context mContext;
    private SharedViewModel mSharedViewModel;
    private int AUTOCOMPLETE_REQUEST_CODE = 1;
    private int AUTO_COMPLETE_INDEX_CODE = 100;
    private Disposable mDisposable;
    private String deviceLocationVariable;
    private PlacesResults mPlacesResults;
    private PlacesResults mPlacesResultsTokenOne;
    private List<Result> mResults;
    private List<Result> mResults_nextPageTokenOne;
    private List<Result> mResults_nextPageTokenTwo;
    public SearchAuto searchAuto;
    public List<SearchAuto> sSearchAutoList = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Create new Array for search objects
        sSearchAutoList= new ArrayList<>();
    }

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

        // get fetched Results
        mSharedViewModel.getResults().observe(getViewLifecycleOwner(), new Observer<List<Result>>() {
            @Override
            public void onChanged(List<Result> results) {
                mResults = results;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.disposeWhenDestroy();
    }

    // Initialise Google Map
    private void initMap(){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Marker click listener
        mMap.setOnMarkerClickListener((GoogleMap.OnMarkerClickListener) this);
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

        // Show current device's location
        if(MainRestaurantActivity.mLocationPermissionGranted){
            getDeviceLocation();
        }
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
                            executeHttpRequestForNearbyPlaces();
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

    // onActivityResult for Search Auto-Complete
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
                setCustomMarker(place.getLatLng(), AUTO_COMPLETE_INDEX_CODE);
                // Create an object
                searchAuto = null;
                searchAuto = new SearchAuto(AUTO_COMPLETE_INDEX_CODE,place.getId(),place.getName(),place.getAddress(),place.getPhoneNumber(),place.getOpeningHours().getWeekdayText(),place.getWebsiteUri(),place.getPhotoMetadatas(),place.getRating().toString(),place.getLatLng());
                // add the object into Array and set in ViewModel
                sSearchAutoList.add(searchAuto);
                Log.d(TAG, "Array onActivityResult: " + sSearchAutoList.get(0));
                mSharedViewModel.setSearchArray(sSearchAutoList);
                AUTO_COMPLETE_INDEX_CODE++;


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
    }

    //-----------------
    // HTTP (RxJAVA)
    //-----------------

    // Execute the stream to fetch nearby locations
    private void executeHttpRequestForNearbyPlaces(){
        Log.d(TAG, "executeHttpRequestForNearbyPlaces: " + deviceLocationVariable+ " " +NEARBY_RADIUS+ " "+ NEARBY_TYPE);
        // Execute the stream subscribing to Observable defined inside PlacesResults
        this.mDisposable = PlacesStream.streamFetchNearbyLocations(deviceLocationVariable, NEARBY_RADIUS, NEARBY_TYPE)
                .subscribeWith(new DisposableObserver<PlacesResults>(){

                    @Override
                    public void onNext(PlacesResults placesResults) {
                        Log.d(TAG, "onNext: HTTP");
                        mPlacesResults = placesResults;
                        // Update UI with results
                        updateUI(placesResults.getResults());
                        setResultsInViewModel(placesResults.getResults());
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onErrorHTTP: "+ e );
                    }
                    @Override
                    public void onComplete() {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                // Actions after the delay
//                                executeHttpRequestForNextToken();
                            }}, 1000);
                    }
                });
    }

    private void executeHttpRequestForNextToken() {
        Log.d(TAG, "executeHttpRequestForNextToken: " + mPlacesResults.getNextPageToken());
        // Execute the stream subscribing to Observable defined inside PlacesResults
        this.mDisposable = PlacesStream.streamFetchNextPageToken(mPlacesResults.getNextPageToken()).subscribeWith(new DisposableObserver<PlacesResults>() {
            @Override
            public void onNext(PlacesResults placesResults) {

                // Update UI with results
                updateUIWithNextToken(placesResults.getResults());
            }
            @Override
            public void onError(Throwable e) { }
            @Override
            public void onComplete() { }
        });
    }


    private void updateUI(List<Result> results){
        mResults = new ArrayList<>();
        mResults.clear();
        mResults.addAll(results);
        setNearbyRestaurantsWithMarkers(mResults);
    }
    private void updateUIWithNextToken(List<Result> results){
        mResults_nextPageTokenOne = new ArrayList<>();
        mResults_nextPageTokenOne.clear();
        mResults_nextPageTokenOne.addAll(results);
        setNearbyRestaurantsWithMarkers(mResults_nextPageTokenOne);
    }

    // Send data to ViewModel - LiveData
    private void setResultsInViewModel(List<Result> results){
        mSharedViewModel.setResults(results);
    }

    private void setNearbyRestaurantsWithMarkers(List<Result> results){
//        Log.d(TAG, "setNearbyRestaurantsWithMarkers: " + results.get(1).getGeometry().getLocation().getLat().toString() + "  " + results.get(1).getGeometry().getLocation().getLng().toString() + " size= "+ results.size());
        for (int i=results.size()-1; i>=0 ; i-- ){
            LatLng latLng = new LatLng(results.get(i).getGeometry().getLocation().getLat(), results.get(i).getGeometry().getLocation().getLng());
            Log.d(TAG, "updateUI: " + i + "----:" + latLng);
            setCustomMarker(latLng, i);
        }
    }

    // set custom marker
    private void setCustomMarker(LatLng latLng, int index){
        mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(getActivity(), R.drawable.amu_bubble_mask, index ))))
                .setTag(index);
    }

    // CustomMarker Bitmap
    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, int index) {
        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        Log.d(TAG, "Marker Index tag: "+ index);

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

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // set the current marker index to pass on BottomSheet fragment
        int i = (int) marker.getTag();
        Log.d(TAG, "onMarkerClick, index is: " + i);
        // Instanciate BottomSheet
        RestaurantBottomSheetFragment.newInstance(i).show(getActivity().getSupportFragmentManager(), getTag());
        return true;
    }

}
