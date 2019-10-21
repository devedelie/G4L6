package com.elbaz.eliran.go4lunch.controllers.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;

import com.elbaz.eliran.go4lunch.R;
import com.elbaz.eliran.go4lunch.base.BaseActivity;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.PlacesResults;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.elbaz.eliran.go4lunch.utils.PlacesStream;
import com.elbaz.eliran.go4lunch.utils.UtilsHelper;
import com.elbaz.eliran.go4lunch.viewmodels.SharedViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.models.Constants.NEARBY_RADIUS;
import static com.elbaz.eliran.go4lunch.models.Constants.NEARBY_TYPE;

public class SplashScreen extends BaseActivity {
    @BindView(R.id.mapView_loading_animation) ProgressBar mapProgressBarAnimation;
    @BindView(R.id.mapView_loading_text) TextView mapLoadingText;
    private Disposable mDisposable;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    public static Location deviceLocation; // Used for distance calculation on other fragments.
    private String deviceLocationVariable;
    public static SharedViewModel mSharedViewModel;
    public static List<Result> mResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedViewModel = ViewModelProviders.of(this).get(SharedViewModel.class);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getDeviceLocation();
    }

    @Override
    public int getFragmentLayout() {
        return R.layout.activity_splash_screen;
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

    // Get Device location
    private void getDeviceLocation(){
        try{
            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
//                                mapLoadingText.setVisibility(View.GONE);
//                                mapProgressBarAnimation.setVisibility(View.GONE);
                                deviceLocation = location; // Set device location variable for distance calculation
                                // create a location string for retrofit (LatLng toString())
                                deviceLocationVariable = new LatLng(location.getLatitude(), location.getLongitude()).toString(); // set a global location variable for other use
                                deviceLocationVariable = deviceLocationVariable.replaceAll("[()]", "");
                                deviceLocationVariable = deviceLocationVariable.replaceAll("[lat/lng:]", "");
                                Log.d(TAG, "SplashScreen onSuccess: " + deviceLocation + " " + deviceLocationVariable);
                                executeHttpRequestForNearbyPlaces();
                            }else{
                                Log.d(TAG, "onComplete: current location is null");
                            }
                        }
                    });
        }catch (SecurityException e){
            Log.e(TAG, "Failed to get device location: ", e);
            Toast.makeText(this, R.string.no_location_found, Toast.LENGTH_LONG).show();
        }
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
                        updateData(placesResults.getResults());
                    }
                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onErrorHTTP: "+ e );
                    }
                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete: ");

                    }
                });
    }

    private void updateData(List<Result> results){
        mResults = new ArrayList<>();
        mResults.clear();
        mResults.addAll(results);
        Log.d(TAG, "onNext: " + mResults.get(0).getName());
        calculateAdditionalValues();
    }


    private void calculateAdditionalValues(){
        // Set Distance and Going workmates into List<Result>
        for (int i = 0 ; i<mResults.size(); i++){
            mResults.get(i).setDistance(UtilsHelper.calculateDistance(mResults.get(i)));
            UtilsHelper.retrieveGoingPersons(mResults.get(i), i);
            Log.d(TAG, "XX1-calculateAdditionalValues: " + mResults.get(i).getDistance() + " " + mResults.get(i).getWorkmates());
        }
        // Pass data to ViewModel
        Log.d(TAG, "XX2-calculateAdditionalValues: ");
        mSharedViewModel.setResultsList(mResults);
        intentActivity();
    }

    private void intentActivity(){
        Intent intent = new Intent(this, MainRestaurantActivity.class);
        startActivity(intent);
    }
}
