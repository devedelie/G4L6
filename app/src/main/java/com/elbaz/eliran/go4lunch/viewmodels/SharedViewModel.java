package com.elbaz.eliran.go4lunch.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.libraries.places.api.model.Place;

/**
 * Created by Eliran Elbaz on 25-Sep-19.
 */
public class SharedViewModel extends ViewModel {
    private static final String TAG = "MapViewFragmentViewMode";

    // Create an instance of Mutable live data
    private MutableLiveData<Place> mPlace = new MutableLiveData<>();


    public void setPlace(Place place){
        mPlace.setValue(place);
        Log.d(TAG, "LiveDataTest setPlace: "+ mPlace);
    }

    public LiveData<Place> getPlace(){
        Log.d(TAG, "LiveDataTest getPlace was called: "+ mPlace);
        return mPlace;
    }

}
