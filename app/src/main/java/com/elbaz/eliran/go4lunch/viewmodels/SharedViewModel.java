package com.elbaz.eliran.go4lunch.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created by Eliran Elbaz on 25-Sep-19.
 */
public class SharedViewModel extends ViewModel {
    private static final String TAG = "MapViewFragmentViewMode";

    /**
     * Get/Set current pager (visible fragment)
     */
    // Create an instance of Mutable live data
    private MutableLiveData<Integer> mPagerCurrentItem = new MutableLiveData<>();

    public void setPagerCurrentItem(Integer searchRestaurantOnMap){
        mPagerCurrentItem.setValue(searchRestaurantOnMap);
        Log.d(TAG, "LiveDataTest setLocationOnMap: "+ mPagerCurrentItem);
    }

    public LiveData<Integer> getPagerCurrentItem(){
        Log.d(TAG, "LiveDataTest getLocationFromMap was called: "+ mPagerCurrentItem);
        return mPagerCurrentItem;
    }


//    /**
//     * Get/Set Location on MapFragment
//     */
//    // Create an instance of Mutable live data
//    private MutableLiveData<Place> mPlace = new MutableLiveData<>();
//
//    public void setLocationOnMap(Place place){
//        mPlace.setValue(place);
//        Log.d(TAG, "LiveDataTest setLocationOnMap: "+ mPlace);
//    }
//
//    public LiveData<Place> getLocationFromMap(){
//        Log.d(TAG, "LiveDataTest getLocationFromMap was called: "+ mPagerCurrentItem);
//        return mPlace;
//    }

}
