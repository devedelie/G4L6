package com.elbaz.eliran.go4lunch.controllers.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.api.UserHelper;
import com.elbaz.eliran.go4lunch.base.BaseFragment;
import com.elbaz.eliran.go4lunch.models.RestaurantDetailsFetch;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
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
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.controllers.activities.SplashScreen.deviceLocation;
import static com.elbaz.eliran.go4lunch.controllers.activities.SplashScreen.mSharedViewModel;
import static com.elbaz.eliran.go4lunch.models.Constants.MAXIMUM_ZOOM_PREFERENCE;
import static com.elbaz.eliran.go4lunch.models.Constants.MINIMUM_ZOOM_PREFERENCE;

public class MapViewFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final float DEFAULT_ZOOM = 15f ;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private int AUTO_COMPLETE_INDEX_CODE = 100;
    private Disposable mDisposable;
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
        // Retrieve back fetched Results from ViewModel in case of system changes
        mSharedViewModel.getResultsList().observe(getViewLifecycleOwner(), new Observer<List<Result>>() {
            @Override
            public void onChanged(List<Result> results) {
                mResults = new ArrayList<>();
                mResults.clear();
                mResults.addAll(results);
                updateUI();
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
        mMap.setMinZoomPreference(MINIMUM_ZOOM_PREFERENCE);
        mMap.setMaxZoomPreference(MAXIMUM_ZOOM_PREFERENCE);
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
        moveCamera(new LatLng(deviceLocation.getLatitude(), deviceLocation.getLongitude()), DEFAULT_ZOOM);
    }

    // A method to move the camera(map) to specific location by passing LatLng and Zoom
    private void moveCamera(LatLng latLng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void updateUI(){
        getRestaurantCollectionForMarkers();
    }

    private void setNearbyRestaurantsWithMarkers(){
        for (int i= 0 ; i<mResults.size(); i++ ){
            LatLng latLng = new LatLng(mResults.get(i).getGeometry().getLocation().getLat(), mResults.get(i).getGeometry().getLocation().getLng());
            // Create a detail object to fetch data
            mRestaurantDetailsFetch = new RestaurantDetailsFetch(mResults.get(i).getPlaceId(), mResults.get(i).getName(), i);
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
        for(int i = 0; i< mResults.size();  i++){
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

    private void getRestaurantCollectionForMarkers(){
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
                            Log.d(TAG, "onComplete: Regex -1s"+ mListOfBookedRestaurants);
                        }
                        setNearbyRestaurantsWithMarkers();
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
