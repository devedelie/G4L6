package com.elbaz.eliran.go4lunch.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.elbaz.eliran.go4lunch.models.restaurantDetails.RestaurantDetails;

import java.util.List;

/**
 * Created by Eliran Elbaz on 25-Sep-19.
 */
public class SharedViewModel extends ViewModel {
    private static final String TAG = "MapViewFragmentViewMode";

    /**
     * Get/Set fetched results List (nearbyPlaces)
     */
    private MutableLiveData<List<Result>> mResultsList = new MutableLiveData<>();

    public void setResultsList(List<Result> results){
        mResultsList.setValue(results);
        Log.d(TAG, "LiveDataTest setResult: "+ results);
    }

    public LiveData<List<Result>> getResultsList(){
        Log.d(TAG, "LiveDataTest getResult: "+ mResultsList);
        return mResultsList;
    }


    /**
     * Get/Set BottomSheet fetched data (to avoid unnecessary Http Requests in cases when we load the same restaurant)
     */
    private MutableLiveData<RestaurantDetails> mBottomSheetRestaurantDetails = new MutableLiveData<>();

    public void setRestaurantDetails (RestaurantDetails restaurantDetails){
        mBottomSheetRestaurantDetails.setValue(restaurantDetails);
        Log.d(TAG, "setSearchObject: " + restaurantDetails);
    }

    public LiveData<RestaurantDetails> getRestaurantDetails(){
        Log.d(TAG, "getSearchObject: " + mBottomSheetRestaurantDetails);
        return mBottomSheetRestaurantDetails;
    }


}
