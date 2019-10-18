package com.elbaz.eliran.go4lunch.api;

import com.elbaz.eliran.go4lunch.models.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Created by Eliran Elbaz on 19-Sep-19.
 */
public class UserHelper {

    private static final String COLLECTION_NAME = "users";
    private static final String LIKE_COLLECTION_NAME = "likes";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    public static Task<Void> createUser(String uid, String username, String urlPicture) {
        // 1 - Create User object
        User userToCreate = new User(uid, username, urlPicture);
        // 2 - Add a new User Document to Firestore
        return UserHelper.getUsersCollection()
                .document(uid) // Setting uID for Document
                .set(userToCreate); // Setting object for Document
    }

    // --- GET (Read) ---

    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateUsername(String username, String uid) {
        return UserHelper.getUsersCollection().document(uid).update("username", username);
    }

    public static Task<Void> updateTodaysRestaurant(String uid, String todaysRestaurant) {
        return UserHelper.getUsersCollection().document(uid).update("selectedRestaurantName", todaysRestaurant);
    }

    public static Task<Void> updateTodaysRestaurantAddress(String uid, String todaysAddress) {
        return UserHelper.getUsersCollection().document(uid).update("selectedRestaurantAddress", todaysAddress);
    }

    public static Task<Void> updateIsGoing (String uid, boolean isGoing){
        return UserHelper.getUsersCollection().document(uid).update("isGoing", isGoing);
    }

    public static Task<Void> updateRestaurantID(String uid, String restaurantID){
        return UserHelper.getUsersCollection().document(uid).update("restaurantID", restaurantID);
    }

    public static Task<Void> updateLikedRestaurants(String uid, List<String> restaurantID){
        return UserHelper.getUsersCollection().document(uid).update("likes", restaurantID);
    }

    // --- DELETE ---

    public static Task<Void> deleteUser(String uid) {
        return UserHelper.getUsersCollection().document(uid).delete();
    }
}
