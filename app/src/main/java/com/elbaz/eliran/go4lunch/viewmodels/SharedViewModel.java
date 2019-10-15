package com.elbaz.eliran.go4lunch.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.elbaz.eliran.go4lunch.models.restaurantDetails.RestaurantDetails;
import com.google.android.libraries.places.api.model.Place;

import java.util.List;

/**
 * Created by Eliran Elbaz on 25-Sep-19.
 */
public class SharedViewModel extends ViewModel {
    private static final String TAG = "MapViewFragmentViewMode";


//    /**
//     * Get/Set current pager (of the visible fragment)
//     */
//    // Create an instance of Mutable live data
//    private MutableLiveData<Integer> mPagerCurrentItem = new MutableLiveData<>();
//
//    public void setPagerCurrentItem(Integer searchRestaurantOnMap){
//        mPagerCurrentItem.setValue(searchRestaurantOnMap);
//        Log.d(TAG, "LiveDataTest setLocationOnMap: "+ mPagerCurrentItem);
//    }
//
//    public LiveData<Integer> getPagerCurrentItem(){
//        Log.d(TAG, "LiveDataTest getLocationFromMap was called: "+ mPagerCurrentItem);
//        return mPagerCurrentItem;
//    }

    /**
     * Get/Set fetched results List (nearby markers)
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


//    /**
//     * Get/Set fetched result Object
//     */
//    private MutableLiveData<Result> mResult = new MutableLiveData<>();
//
//    public void setResult(Result result){
//        mResult.setValue(result);
//        Log.d(TAG, "LiveDataTest setResult: "+ result);
//    }
//
//    public LiveData<Result> getResult(){
//        Log.d(TAG, "LiveDataTest getResult: "+ mResult);
//        return mResult;
//    }


    /**
     * Get/Set Search_Auto-complete objects
     */
    private MutableLiveData<Place> mAutoCompleteSearchObject = new MutableLiveData<>();

    public void setSearchObject(Place searchObject){
        mAutoCompleteSearchObject.setValue(searchObject);
        Log.d(TAG, "setSearchObject: " + searchObject);
    }

    public LiveData<Place> getSearchObject(){
        Log.d(TAG, "getSearchObject: " + mAutoCompleteSearchObject);
        return mAutoCompleteSearchObject;
    }

    /**
     * Get/Set BottomSheet fetched data (to avoid unnecessary Http Requests in cases when the we load the same restaurant)
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
