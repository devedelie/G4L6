package com.elbaz.eliran.go4lunch.utils;

import android.util.Log;

import com.elbaz.eliran.go4lunch.BuildConfig;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.PlacesResults;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import static android.content.ContentValues.TAG;

/**
 * Created by Eliran Elbaz on 28-Sep-19.
 */
public class PlacesStream {

    public static Observable<PlacesResults> streamFetchNearbyLocations(String location, int radius, String type){
        Log.d(TAG, "streamFetchNearbyLocations: " + location + " " + radius + " " + type + " " + BuildConfig.GOOGLE_BROWSER_API_KEY);
        GoogleMapApiService googleMapApiService = GoogleMapApiService.retrofit.create(GoogleMapApiService.class);
        return googleMapApiService.getRestaurants(location, radius, type, BuildConfig.GOOGLE_BROWSER_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
}
