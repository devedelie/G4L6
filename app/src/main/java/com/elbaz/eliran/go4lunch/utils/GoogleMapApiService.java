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

// https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=CpQCAgEAAFxg8o-eU7_uKn7Yqjana-HQIx1hr5BrT4zBaEko29ANsXtp9mrqN0yrKWhf-y2PUpHRLQb1GT-mtxNcXou8TwkXhi1Jbk-ReY7oulyuvKSQrw1lgJElggGlo0d6indiH1U-tDwquw4tU_UXoQ_sj8OBo8XBUuWjuuFShqmLMP-0W59Vr6CaXdLrF8M3wFR4dUUhSf5UC4QCLaOMVP92lyh0OdtF_m_9Dt7lz-Wniod9zDrHeDsz_by570K3jL1VuDKTl_U1cJ0mzz_zDHGfOUf7VU1kVIs1WnM9SGvnm8YZURLTtMLMWx8-doGUE56Af_VfKjGDYW361OOIj9GmkyCFtaoCmTMIr5kgyeUSnB-IEhDlzujVrV6O9Mt7N4DagR6RGhT3g1viYLS4kO5YindU6dm3GIof1Q&key=YOUR_API_KEY
    @GET("maps/api/place/nearbysearch/json")
    Observable<PlacesResults> getNextPageToken (@Query("pagetoken") String pageToken,
                                                @Query("key") String key);


    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(GOOGLE_MAPS_API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build();

}


