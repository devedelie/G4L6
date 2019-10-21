package com.elbaz.eliran.go4lunch.utils;

import android.location.Location;
import android.util.Log;

import com.elbaz.eliran.go4lunch.api.GoingUserHelper;
import com.elbaz.eliran.go4lunch.controllers.activities.SplashScreen;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.QuerySnapshot;

import static android.content.ContentValues.TAG;
import static com.elbaz.eliran.go4lunch.controllers.activities.SplashScreen.deviceLocation;

/**
 * Created by Eliran Elbaz on 16-Oct-19.
 */
public class UtilsHelper {
    //Calculate the number of stars
    public static Integer rating (double rating) {
        rating = (rating / 5) * 3; // Generate new equivalent value
        if (rating < 0.75)
            return 0;
        if (rating >= 0.75 && rating < 1.5)
            return 1;
        if (rating >= 1.5 && rating < 2.25)
            return 2;
        else return 3;
    }


    public static int calculateDistance(Result result){
        float[] distance = new float[1];
        try{
            if(result.getGeometry().getLocation().getLat()!=null || result.getGeometry().getLocation().getLng()!=null){
                Location.distanceBetween(deviceLocation.getLatitude(), deviceLocation.getLongitude(), result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng(), distance);            }
        }catch (Exception e){
            Log.d(TAG, "calculateDistance Error: " +e);
        }
        return Math.round(distance[0]);
    }

    public static void retrieveGoingPersons(Result result, int index){
        GoingUserHelper.getAllRestaurantsWithGoingUsers(result.getName()).addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG, "#of persons: "+ queryDocumentSnapshots.size());
                if(queryDocumentSnapshots.size() > 0){
                    SplashScreen.mResults.get(index).setWorkmates(queryDocumentSnapshots.size());
                    Log.d(TAG, "onSuccess GoingPersons: "+ SplashScreen.mResults.get(index).getWorkmates());
                }else{
                    SplashScreen.mResults.get(index).setWorkmates(0);
                }
            }
        });
    }

}
