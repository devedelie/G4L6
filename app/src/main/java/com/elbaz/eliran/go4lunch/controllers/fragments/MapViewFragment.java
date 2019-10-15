package com.elbaz.eliran.go4lunch.controllers.fragments;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.base.BaseFragment;
import com.elbaz.eliran.go4lunch.models.RestaurantDetailsFetch;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.PlacesResults;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.elbaz.eliran.go4lunch.utils.PlacesStream;
import com.elbaz.eliran.go4lunch.viewmodels.SharedViewModel;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.models.Constants.NEARBY_RADIUS;
import static com.elbaz.eliran.go4lunch.models.Constants.NEARBY_TYPE;

public class MapViewFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    @BindView(R.id.mapView_loading_animation) ProgressBar mapProgressBarAnimation;
    @BindView(R.id.mapView_loading_text) TextView mapLoadingText;
    private static final float DEFAULT_ZOOM = 15f ;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SharedViewModel mSharedViewModel;
    private int AUTO_COMPLETE_INDEX_CODE = 100;
    private Disposable mDisposable;
    private String deviceLocationVariable;
    public static Location deviceLocation; // Used for distance calculation on other fragments.
    // Nearby Places
    private RestaurantDetailsFetch mRestaurantDetailsFetch;
    private List<Result> mResults;
    private boolean isRestaurantValueExist = false;
    private List<String> mListOfBookedRestaurants = new ArrayList<>();  // list of restaurants for applying green markers

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);
        ButterKnife.bind(this, view); //Configure Butterknife
        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());
        this.getRestaurantCollectionForMarkers();
        // Initialise map
        this.initMap();
        return view;
    }

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_map_view; }

    // This method will be called onDestroy to avoid any risk of memory leaks.
    private void disposeWhenDestroy(){
        if (this.mDisposable != null && !this.mDisposable.isDisposed()) this.mDisposable.dispose();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Set ViewModel Elements under onActivityCreated() to scope it to the lifeCycle of the Fragment
        mSharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
        // Retrieve back fetched Results from ViewModel in case of system changes
        mSharedViewModel.getResultsList().observe(getViewLifecycleOwner(), new Observer<List<Result>>() {
            @Override
            public void onChanged(List<Result> results) {
                if(mResults.isEmpty()){
                    mResults = results;
                }
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
        mMap.setMinZoomPreference(14.5f);
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
        getDeviceLocation();
    }

    // Get Device location
    private void getDeviceLocation(){
        try{
            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                deviceLocation = location; // Set device location variable for distance calculation
                                mapLoadingText.setVisibility(View.GONE);
                                mapProgressBarAnimation.setVisibility(View.GONE);
                                // create a location string for retrofit (LatLng toString())
                                deviceLocationVariable = new LatLng(location.getLatitude(), location.getLongitude()).toString(); // set a global location variable for other use
                                deviceLocationVariable = deviceLocationVariable.replaceAll("[()]", "");
                                deviceLocationVariable = deviceLocationVariable.replaceAll("[lat/lng:]", "");

                                // move camera to location
                                moveCamera(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM);
                                // If NULL, execute Http request, ELSE, update the UI with the data from ViewModel
                                if(mResults == null){
                                    Log.d(TAG, "onSuccess: mResults is null");
                                    executeHttpRequestForNearbyPlaces();
                                }else {
                                    Log.d(TAG, "onSuccess: mResults isn't null");
                                    updateUI(mResults);
                                }
                            }else{
                                Log.d(TAG, "onComplete: current location is null");
                            }
                        }
                    });
        }catch (SecurityException e){
            Log.e(TAG, "Failed to get device location: ", e);
            Toast.makeText(getActivity(), R.string.no_location_found, Toast.LENGTH_LONG).show();
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
                        // Set data in ViewModel
                        setResultsInViewModel(placesResults);
                        // Update UI with results
                        updateUI(placesResults.getResults());
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onErrorHTTP: "+ e );
                    }
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: "); }
                });
    }

    // Send data to ViewModel - LiveData
    private void setResultsInViewModel(PlacesResults placesResults){
        mSharedViewModel.setResultsList(placesResults.getResults());
    }

    private void updateUI(List<Result> results){
        mResults = new ArrayList<>();
        mResults.clear();
        mResults.addAll(results);
        setNearbyRestaurantsWithMarkers(mResults);
    }

    private void setNearbyRestaurantsWithMarkers(List<Result> results){
        for (int i=results.size()-1; i>=0 ; i-- ){
            LatLng latLng = new LatLng(results.get(i).getGeometry().getLocation().getLat(), results.get(i).getGeometry().getLocation().getLng());
            Log.d(TAG, "updateUI: " + i + "----:" + latLng);
            // Create a detail object to fetch data
            mRestaurantDetailsFetch = new RestaurantDetailsFetch(results.get(i).getPlaceId(), results.get(i).getName(), i);
            setCustomMarker(latLng, i, mRestaurantDetailsFetch);
        }
    }

    // set custom marker
    private void setCustomMarker(LatLng latLng,int i, RestaurantDetailsFetch detailObject){
        mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(getActivity(), R.drawable.amu_bubble_mask, i , detailObject.getRestaurantId()))))
                .setTag(detailObject);
    }

    // CustomMarker Bitmap
    public Bitmap createCustomMarker(Context context, @DrawableRes int resource, int index, String restaurantID) {
        View marker;
        // Verify if the restaurantID exist in the List and set the correct marker color
        if (mListOfBookedRestaurants.contains(restaurantID)){
            marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_green_layout, null);
        }else{
            marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        }

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
        // get the current marker id from 'RestaurantDetailsFetch' Object and pass to BottomSheet fragment
        RestaurantDetailsFetch tagObject = (RestaurantDetailsFetch) marker.getTag();
        // Check if the restaurant ID equales to one of the fetched details on the List<>
        for(int i = mResults.size()-1; i>=0 ; i--){
            if(mResults.get(i).getPlaceId().equals(tagObject.getRestaurantId())){
                isRestaurantValueExist = true;
                Log.d(TAG, "onMarkerClick: " + i + " " + isRestaurantValueExist);
            }
        }
        // If data exist: avoid another Http Request and use ViewModel. Otherwise, execute httpRequest
        if(isRestaurantValueExist){
            Log.d(TAG, "onMarkerClick: value exists");
            RestaurantDetailsFragment_FromViewModel.newInstance(tagObject.getRestaurantId(), tagObject.getRestaurantName(), tagObject.getIndex()).show(getActivity().getSupportFragmentManager(), getTag());
            isRestaurantValueExist =false;
        }else{
            Log.d(TAG, "onMarkerClick: value doesn't exists");
            RestaurantDetailsFragment_FromRetrofit.newInstance(tagObject.getRestaurantId(), tagObject.getRestaurantName()).show(getActivity().getSupportFragmentManager(), getTag());
            isRestaurantValueExist =false;
        }
        return true;
    }

    public void getRestaurantCollectionForMarkers(){
        // Get the users where's the value isGoing=true on their document, and collect the restaurantID
        UserHelper.getUsersCollection().whereEqualTo("isGoing", true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Put Documents in a DocumentSnapshot List
                            List<DocumentSnapshot> mListOfDocuments = task.getResult().getDocuments();
                            // String Regex - to extract restaurantID's
                            for (int i = 0 ; i<mListOfDocuments.size() ; i++ ){
                                String data = mListOfDocuments.get(i).getData().toString();
                                data = data.substring(data.indexOf("restaurantID=")+13);
                                data = data.contains(",") ? data.split(",")[0] : data;
                                data = data.substring(0, 1).toUpperCase() + data.substring(1);
                                mListOfBookedRestaurants.add(i, data);
                            }
                            Log.d(TAG, "onComplete: Regex -"+ mListOfBookedRestaurants);
                        }
                    }
                });
    }

    // Update the UI with the result from Search-Autocomplete bar
    public void searchAction(Place place){
        String restaurantID = place.getId();
        if(!restaurantID.isEmpty() && restaurantID != null){
            moveCamera(place.getLatLng(), DEFAULT_ZOOM);
            mRestaurantDetailsFetch = new RestaurantDetailsFetch(place.getId(), place.getName(), AUTO_COMPLETE_INDEX_CODE);
            setCustomMarker(place.getLatLng(), AUTO_COMPLETE_INDEX_CODE, mRestaurantDetailsFetch);
            AUTO_COMPLETE_INDEX_CODE++;
        }
    }

}
