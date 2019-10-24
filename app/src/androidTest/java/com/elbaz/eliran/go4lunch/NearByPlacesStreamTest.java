package com.elbaz.eliran.go4lunch;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.PlacesResults;
import com.elbaz.eliran.go4lunch.models.nearbyPlacesModel.Result;
import com.elbaz.eliran.go4lunch.models.restaurantDetails.RestaurantDetails;
import com.elbaz.eliran.go4lunch.utils.PlacesStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static com.elbaz.eliran.go4lunch.models.Constants.NEARBY_RADIUS;
import static com.elbaz.eliran.go4lunch.models.Constants.NEARBY_TYPE;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertEquals;


@RunWith(JUnit4.class)
public class NearByPlacesStreamTest {
    private final String deviceLocationTest= "48.831212,2.329754";
    private final String placeId = "ChIJvz3Ntrlx5kcRCylrI3QARlw";

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.elbaz.eliran.go4lunch", appContext.getPackageName());
    }

    //----------------------
    // Nearby-places stream
    //----------------------

    @Test
    public void isStream_fetchNearbyPlacesStream_returnCorrectData() {
        List<String> placeType = new ArrayList<>(); // List for place type[]

        Observable<PlacesResults> observableNearbyPlaces = PlacesStream.streamFetchNearbyLocations(deviceLocationTest, NEARBY_RADIUS, NEARBY_TYPE);
        TestObserver<PlacesResults> nearbyPlacesTestObserver = new TestObserver<>();

        observableNearbyPlaces.subscribeWith(nearbyPlacesTestObserver)
                .assertNoErrors()
                .assertNoTimeout()
                .awaitTerminalEvent();

        PlacesResults placesResults = nearbyPlacesTestObserver.values().get(0);
        Assert.assertTrue(placesResults.getResults().size() > 0);
        for(Result result: placesResults.getResults()){
            // Verify that critical data is not null/empty
            Assert.assertTrue(result.getName() != null && !result.getName().isEmpty());
            Assert.assertTrue(result.getPlaceId() != null && !result.getPlaceId().isEmpty());
            // Add specific place types into a list and check if it has "restaurant" type in it
            placeType.addAll(result.getTypes());
            Assert.assertThat(placeType, hasItem(NEARBY_TYPE));
        }
    }

    //--------------------------
    // Place details stream
    //--------------------------

    @Test
    public void isStream_fetchRestaurantDetailsByID_returnCorrectRestaurantData() {
        Observable<RestaurantDetails> observableRestaurantDetails = PlacesStream.streamFetchRestaurantDetailsByID(placeId);
        TestObserver<RestaurantDetails> restaurantDetailsTestObserver = new TestObserver<>();

        observableRestaurantDetails.subscribeWith(restaurantDetailsTestObserver)
                .assertNoErrors()
                .assertNoTimeout()
                .awaitTerminalEvent();

        RestaurantDetails restaurantDetails = restaurantDetailsTestObserver.values().get(0);
        // Verify that critical data is not null/empty
        Assert.assertTrue(restaurantDetails.getResult().getPlaceId() != null && !restaurantDetails.getResult().getPlaceId().isEmpty());
        Assert.assertTrue(restaurantDetails.getResult().getName() != null && !restaurantDetails.getResult().getName().isEmpty());
        // Verify that the correct data is returned to the user
        Assert.assertEquals("ChIJvz3Ntrlx5kcRCylrI3QARlw", restaurantDetails.getResult().getPlaceId());
        Assert.assertEquals("Café Oz Denfert", restaurantDetails.getResult().getName());
    }

}
