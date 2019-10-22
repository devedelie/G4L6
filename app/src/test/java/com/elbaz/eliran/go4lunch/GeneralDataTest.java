package com.elbaz.eliran.go4lunch;

import com.elbaz.eliran.go4lunch.models.Constants;
import com.elbaz.eliran.go4lunch.utils.UtilsHelper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Eliran Elbaz on 22-Oct-19.
 */
public class GeneralDataTest {
        @Test
    public void MainRestaurantActivity_verifyBaseURL_returnCorrectBaseURL() {
        assertEquals(Constants.GOOGLE_MAPS_API_BASE_URL, "https://maps.googleapis.com/");
    }
    @Test
    public void MainRestaurantActivity_verifySearchFieldsString_returnCorrectSearchFields() {
        assertEquals(Constants.SEARCH_FIELDS, "place_id,photo,name,vicinity,rating,formatted_phone_number,website,opening_hours,review");
    }

    @Test
    public void MainRestaurantActivity_verifyStarRatingCalculator_returnCorrectStarRating() {
        Integer expectedStars = 3;
        assertEquals(UtilsHelper.rating(4.1), expectedStars );
        expectedStars--;
        assertEquals(UtilsHelper.rating(3.5), expectedStars );
        expectedStars--;
        assertEquals(UtilsHelper.rating(2.2), expectedStars );
        expectedStars--;
        assertEquals(UtilsHelper.rating(1), expectedStars );
    }
}
