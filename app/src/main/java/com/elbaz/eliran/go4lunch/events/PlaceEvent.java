package com.elbaz.eliran.go4lunch.events;

import com.google.android.libraries.places.api.model.Place;

/**
 * Created by Eliran Elbaz on 24-Sep-19.
 */
public class PlaceEvent {

    private Place mPlace;

    public PlaceEvent(Place place) {
        mPlace = place;
    }

    public Place getPlace() {
        return mPlace;
    }

    public void setPlace(Place place) {
        mPlace = place;
    }
}
