package com.elbaz.eliran.go4lunch.utils;

import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.PlacesResults;

import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import static com.elbaz.eliran.go4lunch.models.Constants.GOOGLE_MAPS_API_BASE_URL;

/**
 * Created by Eliran Elbaz on 26-Sep-19.
 */
// URL to fetch nearby restaurants within 1500m around the location (return JSON)
// https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=-33.8670522,151.1957362&radius=1500&type=restaurant&key=API_KEY

public interface GoogleMapApiService {
    @GET("maps/api/place/nearbysearch/json")
    Observable<PlacesResults> getRestaurants (@Query("location") String location,
                                              @Query("radius") int radius,
                                              @Query("type") String type,
                                              @Query("key") String key);
    

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(GOOGLE_MAPS_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();

}


