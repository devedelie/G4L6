package com.elbaz.eliran.go4lunch.api.google;

import com.elbaz.eliran.go4lunch.models.Restaurant;
import com.elbaz.eliran.go4lunch.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;

/**
 * Created by Eliran Elbaz on 06-Oct-19.
 */
public class GoingUserHelper {
    private static final String COLLECTION_NAME = "goingUsers";

    // --- GET ---

    public static Query getAllUsersForWorkmates(String restaurant){
        return RestaurantHelper.getRestaurantCollection()
                .document(restaurant)
                .collection(COLLECTION_NAME)
                .orderBy("dateCreated")
                .limit(50);
    }

    public static Task<Void> createUserForGoingList(String restaurantID, String restaurantNameId, User userGoing){
        // Create userGoing object
        Restaurant restaurant = new Restaurant(restaurantID, restaurantNameId, userGoing);

        // Store user to Firestore isGoing Collection
        return RestaurantHelper.getRestaurantCollection()
                .document(restaurantNameId)
                .collection(COLLECTION_NAME)
                .document(userGoing.getUid())
                .set(restaurant);
    }

    public static Task<Void> deleteUserFromGoingList(String restaurantNameId, User userGoing){
        // Delete user from isGoing Collection
        return RestaurantHelper.getRestaurantCollection()
                .document(restaurantNameId)
                .collection(COLLECTION_NAME)
                .document(userGoing.getUid())
                .delete();
    }
}
