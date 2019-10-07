package com.elbaz.eliran.go4lunch.api;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Created by Eliran Elbaz on 06-Oct-19.
 */
public class RestaurantHelper {
    private static final String COLLECTION_NAME = "restaurants";

    // --- COLLECTION REFERENCE ---

    public static CollectionReference getRestaurantCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }
}
