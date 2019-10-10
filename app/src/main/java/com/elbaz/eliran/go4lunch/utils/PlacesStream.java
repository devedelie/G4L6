package com.elbaz.eliran.go4lunch.utils;

import android.util.Log;

import com.elbaz.eliran.go4lunch.BuildConfig;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.PlacesResults;
import com.elbaz.eliran.go4lunch.models.restaurantDetails.RestaurantDetails;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.models.Constants.SEARCH_FIELDS;

/**
 * Created by Eliran Elbaz on 28-Sep-19.
 */
public class PlacesStream {

    public static Observable<PlacesResults> streamFetchNearbyLocations(String location, int radius, String type){
        Log.d(TAG, "streamFetchNearbyLocations: " + location + " " + radius + " " + type + " " + BuildConfig.GOOGLE_BROWSER_API_KEY);
        GoogleMapApiService googleMapApiService = GoogleMapApiService.retrofit.create(GoogleMapApiService.class);
        return googleMapApiService.getNearbyRestaurants(location, radius, type, BuildConfig.GOOGLE_BROWSER_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }

//    public static Observable<PlacesResults> streamFetchNextPageToken(String token){
//        GoogleMapApiService googleMapApiService = GoogleMapApiService.retrofit.create(GoogleMapApiService.class);
//        return googleMapApiService.getNextPageToken(token, BuildConfig.GOOGLE_BROWSER_API_KEY)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .timeout(10, TimeUnit.SECONDS);
//    }

    public static Observable<RestaurantDetails> streamFetchRestaurantDetailsByID(String restaurantID){
        GoogleMapApiService googleMapApiService = GoogleMapApiService.retrofit.create(GoogleMapApiService.class);
        Log.d(TAG, "streamFetchRestaurantDetailsByID: "+restaurantID +" "+ SEARCH_FIELDS+ " "+ BuildConfig.GOOGLE_BROWSER_API_KEY);
        return googleMapApiService.getRestaurantDetailsByID(restaurantID, SEARCH_FIELDS, BuildConfig.GOOGLE_BROWSER_API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(10, TimeUnit.SECONDS);
    }
}
