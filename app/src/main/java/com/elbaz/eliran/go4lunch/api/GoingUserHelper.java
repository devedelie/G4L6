package com.elbaz.eliran.go4lunch.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.elbaz.eliran.go4lunch.models.Restaurant;
import com.elbaz.eliran.go4lunch.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QuerySnapshot;

import static android.content.ContentValues.TAG;

/**
 * Created by Eliran Elbaz on 06-Oct-19.
 */
public class GoingUserHelper {
    private static final String COLLECTION_NAME = "goingUsers";

    // --- GET ---

    public static Task<QuerySnapshot> getAllRestaurantsWithGoingUsers(String restaurantName) {
        // Query of the current 'going-users' in the entire collection
        return RestaurantHelper.getRestaurantCollection().document(restaurantName).collection(COLLECTION_NAME).get();
    }


//    public static Query getAllUsersForWorkmates(String restaurants){
//        return RestaurantHelper.getRestaurantCollection()
//                .document()
//                .collection(COLLECTION_NAME)
//                .orderBy("dateCreated")
//                .limit(50);
//    }

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

    public static Task<Void> deleteUserFromGoingListAfterAccountDelete(String restaurantNameId, String userId){
        // Delete user from isGoing Collection
        return RestaurantHelper.getRestaurantCollection()
                .document(restaurantNameId)
                .collection(COLLECTION_NAME)
                .document(userId)
                .delete();
    }

    public static Task<QuerySnapshot> deleteUserFromPreviousGoingList(User userGoing, String selctedRestaurantOnLoad){
        // Query of the current user in the entire collection (call 'delete' OnComplete)
        return RestaurantHelper.getRestaurantCollection()
                .whereEqualTo(userGoing.getUid(), true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "deleteUserFromGoingList onComplete: ");
                            deleteUserFromGoingList(userGoing.getSelectedRestaurantName(), userGoing);
                        }
                    }
                });
    }
}
